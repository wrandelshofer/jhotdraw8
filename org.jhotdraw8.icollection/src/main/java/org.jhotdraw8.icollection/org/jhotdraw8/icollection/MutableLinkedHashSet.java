/*
 * @(#)MutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.champlinked.TrieBuilder;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.sequenced.ReversedSequencedSetView;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.SequencedSet;
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
/// See description at [PersistentLinkedHashSet].
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
public class MutableLinkedHashSet<E> extends AbstractSet<E> implements SequencedSet<E>, Cloneable, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    private TrieBuilder<E, Object> builder = new TrieBuilder<>();
    private PersistentLinkedHashSet<E> set;
    private int modCount;

    /// Constructs a new empty set.
    public MutableLinkedHashSet() {
        set = PersistentLinkedHashSet.of();
    }


    /// Constructs a set containing the elements in the specified iterable.
    ///
    /// @param c an iterable
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableLinkedHashSet(Iterable<? extends E> c) {
        if (c instanceof PersistentLinkedHashSet) {
            set = (PersistentLinkedHashSet<E>) c;
        } else {
            set = PersistentLinkedHashSet.of();
            addAll(c);
        }
    }

    @Override
    public boolean add(@Nullable E e) {
        var newSet = set.adding(e, builder);
        if (newSet == set) return false;
        this.set = newSet;
        modCount++;
        return true;
    }

    @Override
    public E getFirst() {
        return set.getFirst();
    }

    @Override
    public E getLast() {
        return set.getLast();
    }

    @Override
    public void addFirst(@Nullable E e) {
        var newSet = set.addingFirst(e, builder);
        if (newSet == set) return;
        this.set = newSet;
        modCount++;
    }

    @Override
    public void addLast(@Nullable E e) {
        var newSet = set.addingLast(e, builder);
        if (newSet == set) return;
        this.set = newSet;
        modCount++;
    }


    @SuppressWarnings("unchecked")
    public boolean addAll(Iterable<? extends E> c) {
        var newSet = set.addingAll(c, builder);
        if (newSet == set) return false;
        this.set = newSet;
        modCount++;
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean removeAll(Iterable<?> c) {
        var newSet = set.removingAll(c, builder);
        if (newSet == set) return false;
        this.set = newSet;
        modCount++;
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean retainAll(Iterable<?> c) {
        var newSet = set.retainingAll(c, builder);
        if (newSet == set) return false;
        this.set = newSet;
        modCount++;
        return true;
    }


    /// Removes all elements from this set.
    @Override
    public void clear() {
        set = PersistentLinkedHashSet.of();
        modCount++;
    }

    /// Returns a shallow copy of this set.
    @Override
    public MutableLinkedHashSet<E> clone() {
        MutableLinkedHashSet<E> that = null;
        try {
            that = (MutableLinkedHashSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        that.builder = new TrieBuilder<>();
        return that;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return new FailFastIterator<>(
                set.iterator(),
                this::iteratorRemove, this::getModCount
        );
    }

    Iterator<E> reverseIterator() {
        return new FailFastIterator<>(
                set.reverseIterator(),
                this::iteratorRemove, this::getModCount
        );
    }

    @Override
    public SequencedSet<E> reversed() {
        return new ReversedSequencedSetView<>(this, this::reverseIterator,
                this::reverseSpliterator);
    }

    @Override
    public int size() {
        return set.size();
    }

    private int getModCount() {
        return modCount;
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<E>(
                Spliterators.<E>spliterator(
                        set.iterator(),
                        set.size(), Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.ORDERED),
                () -> this.modCount, null);
    }

    Spliterator<E> reverseSpliterator() {
        return new FailFastSpliterator<E>(
                Spliterators.<E>spliterator(
                        set.reverseIterator(),
                        set.size(), Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.ORDERED),
                () -> this.modCount, null);
    }

    private void iteratorRemove(E e) {
        remove(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        var newSet = set.removing((E) o, builder);
        if (newSet == set) return false;
        this.set = newSet;
        modCount++;
        return true;
    }

    /// Returns a persistent copy of this set.
    ///
    /// @return a persistent copy
    public PersistentLinkedHashSet<E> toPersistent() {
        builder = new TrieBuilder<>();
        return set;
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
            return new MutableLinkedHashSet<>(deserializedElements);
        }
    }
}