/*
 * @(#)MutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.champset.DeltaCounter;
import org.jhotdraw8.icollection.impl.champset.ElementIterator;
import org.jhotdraw8.icollection.impl.champset.TrieBuilder;
import org.jhotdraw8.icollection.impl.champset.TrieNode;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

/// Implements the [Set] interface using a Compressed Hash-Array Mapped
/// Prefix-tree (CHAMP).
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 elements
///   - allows null elements
///   - is mutable
///   - is not thread-safe
///   - does not guarantee a specific iteration order
///
///
/// Performance characteristics:
///
///   - add: O(log₃₂ N)
///   - remove: O(log₃₂ N)
///   - contains: O(log₃₂ N)
///   - toPersistent: O(1) + O(log₃₂ N) distributed across subsequent updates in
///     this set
///   - clone: O(1) + O(log₃₂ N) distributed across subsequent updates in this
///     set and in the clone
///   - iterator.next: O(1)
///
///
/// Implementation details:
///
/// See description at [PersistentHashSet].
///
/// References:
///
/// Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
/// <dl>
///      <dt>Michael J. Steindorfer (2017).
///      Efficient Persistent Collections.</dt>
///      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-persistent-collections">michael.steindorfer.name</a>
///      <dt>The Capsule Hash Trie Collections Library.
///
/// Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
///      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
/// </dl>
///
/// @param <E> the element type
public class MutableHashSet<E> extends AbstractSet<E> implements Cloneable, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private TrieNode<E> node;
    private int size;
    private int modCount;
    private TrieBuilder<E> mutator = new TrieBuilder<>();

    /// Constructs a new empty set.
    public MutableHashSet() {
        node = TrieNode.empty();
    }

    MutableHashSet(TrieNode<E> node, int size) {
        this.node = node;
        this.size = size;
    }

    /// Constructs a set containing the elements in the specified iterable.
    ///
    /// @param c an iterable
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableHashSet(Iterable<? extends E> c) {
        this();
        addAll(c);
    }

    @Override
    public boolean add(@Nullable E e) {
        node = node.mutableAdd(Objects.hashCode(e), e, 0, mutator.reset());
        if (mutator.isModified()) {
            size++;
            modCount++;
        }
        return mutator.isModified();
    }

    /// Adds all specified elements that are not already in this set.
    ///
    /// @param c an iterable of elements
    /// @return `true` if this set changed
    @SuppressWarnings("unchecked")
    public boolean addAll(Iterable<? extends E> c) {
        if (c == this) return false;
        if (c instanceof MutableHashSet<?> m) {
            c = (Iterable<? extends E>) m.toPersistent();
        }
        if (c instanceof PersistentHashSet<?> cc) {
            if (isEmpty()) {
                node = (TrieNode<E>) cc.node;
                size = cc.size;
                modCount++;
                return true;
            }
            var delta = new DeltaCounter();
            node.mutableAddAll((TrieNode<E>) cc.node, 0, delta, mutator.reset());
            if (delta.isModified()) {
                size += delta.count;
                modCount++;
            }
            return delta.isModified();
        }
        mutator.reset();
        for (E e : c) {
            node = node.mutableAdd(Objects.hashCode(e), e, 0, mutator);
        }
        if (mutator.isModified()) {
            size += mutator.size;
            modCount++;
        }
        return mutator.isModified();
    }

    @SuppressWarnings("unchecked")
    public boolean removeAll(Iterable<?> c) {
        if (isEmpty()
                || (c instanceof Collection<?> cc) && cc.isEmpty()
                || (c instanceof ReadableCollection<?> rc) && rc.isEmpty()) {
            return false;
        }
        if (c == this) {
            clear();
            return true;
        }
        if (c instanceof PersistentHashSet<?> cc) {
            var delta = new DeltaCounter();
            node.mutableRemoveAll((TrieNode<E>) cc.node, 0, delta, mutator.reset());
            if (delta.isModified()) {
                size += delta.count;
                modCount++;
            }
            return delta.isModified();
        }
        mutator.reset();
        for (Object e : c) {
            node = node.mutableRemove(Objects.hashCode(e), (E) e, 0, mutator);
        }
        if (mutator.isModified()) {
            size += mutator.size;
            modCount++;
        }
        return mutator.isModified();
    }

    @SuppressWarnings("unchecked")
    public boolean retainAll(Iterable<?> c) {
        if (isEmpty()
                || (c instanceof Collection<?> cc) && cc.isEmpty()
                || (c instanceof ReadableCollection<?> rc) && rc.isEmpty()) {
            return false;
        }
        if (c == this) {
            clear();
            return true;
        }
        if (c instanceof PersistentHashSet<?> cc) {
            var delta = new DeltaCounter();
            node.mutableRetainAll((TrieNode<E>) cc.node, 0, delta, mutator.reset());
            if (delta.isModified()) {
                size += delta.count;
                modCount++;
            }
            return delta.isModified();
        }

        int count = 0;
        if (c instanceof ReadableCollection<?> rc) {
            for (E e : this) {
                if (!rc.contains(e)) {
                    remove(e);
                    count++;
                }
            }
        }
        if (!(c instanceof Collection<?>)) {
            var cc = new HashSet<E>();
            for (var it = c.iterator(); it.hasNext(); ) {
                cc.add((E) it.next());
            }
            c = cc;
        }
        var ccc = (Collection<E>) c;
        boolean modified = false;
        for (E e : this) {
            if (!ccc.contains(e)) {
                remove(e);
                count++;
            }
        }

        if (count != 0) {
            size -= count;
            modCount++;
            return true;
        }
        return false;
    }


    /// Removes all elements from this set.
    @Override
    public void clear() {
        node = TrieNode.empty();
        size = 0;
        modCount++;
    }

    /// Returns a shallow copy of this set.
    @Override
    public MutableHashSet<E> clone() {
        MutableHashSet<E> that = null;
        try {
            that = (MutableHashSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        that.mutator = new TrieBuilder<>();
        return that;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return node.contains(Objects.hashCode(o), (E) o, 0);
    }

    @Override
    public Iterator<E> iterator() {
        return new FailFastIterator<>(
                new ElementIterator<>(node),
                this::iteratorRemove, this::getModCount
        );
    }

    @Override
    public int size() {
        return size;
    }

    private int getModCount() {
        return modCount;
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<E>(
                Spliterators.<E>spliterator(new ElementIterator<E>(node), size, Spliterator.DISTINCT | Spliterator.SIZED),
                () -> this.modCount, null);
    }

    private void iteratorRemove(E e) {
        remove(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        node = node.mutableRemove(Objects.hashCode(o), (E) o, 0, mutator.reset());
        if (mutator.isModified()) {
            size--;
            modCount++;
        }
        return mutator.isModified();
    }

    /// Returns a persistent copy of this set.
    ///
    /// @return a persistent copy
    public PersistentHashSet<E> toPersistent() {
        mutator = new TrieBuilder<>();
        return size == 0
                ? PersistentHashSet.of()
                : new PersistentHashSet<>(node, size);
    }

    @Serial
    private Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Set<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            return new MutableHashSet<>(deserializedElements);
        }
    }
}