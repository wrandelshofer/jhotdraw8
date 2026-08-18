/*
 * @(#)MutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champset.AbstractMutableChampSet;
import org.jhotdraw8.icollection.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champset.BulkChangeEvent;
import org.jhotdraw8.icollection.impl.champset.ChampIterator;
import org.jhotdraw8.icollection.impl.champset.ChampSpliterator;
import org.jhotdraw8.icollection.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.impl.champset.Node;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;

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
/// See description at [OldPersistentHashSet].
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
public class OldMutableHashSet<E> extends AbstractMutableChampSet<E, E> {
    @Serial
    private static final long serialVersionUID = 0L;

    /// Constructs a new empty set.
    public OldMutableHashSet() {
        hashSet = BitmapIndexedNode.emptyNode();
    }

    /// Constructs a set containing the elements in the specified iterable.
    ///
    /// @param c an iterable
    @SuppressWarnings({"unchecked", "this-escape"})
    public OldMutableHashSet(Iterable<? extends E> c) {
        this();
        addAll(c);
    }

    @Override
    public boolean add(@Nullable E e) {
        ChangeEvent<E> details = new ChangeEvent<>();
        hashSet = hashSet.put(owner,
                e, OldPersistentHashSet.keyHash(e), 0, details,
                (oldKey, newKey) -> oldKey,
                Objects::equals, OldPersistentHashSet::keyHash);
        if (details.isModified()) {
            size++;
            modCount++;
        }
        return details.isModified();
    }

    /// Adds all specified elements that are not already in this set.
    ///
    /// @param c an iterable of elements
    /// @return `true` if this set changed
    @SuppressWarnings("unchecked")
    public boolean addAll(Iterable<? extends E> c) {
        if (c == this) return false;
        if (c instanceof OldMutableHashSet<?> m) {
            c = (Iterable<? extends E>) m.toPersistent();
        }
        if (isEmpty() && (c instanceof OldPersistentHashSet<?> cc)) {
            hashSet = (BitmapIndexedNode<E>) cc.root;
            size = cc.size;
            return true;
        }
        if (c instanceof OldPersistentHashSet<?> that) {
            var bulkChange = new BulkChangeEvent();
            var newRootNode = hashSet.putAll(owner, (Node<E>) that.root, 0, bulkChange, OldPersistentHashSet::keepOldElement, Objects::equals, OldPersistentHashSet::keyHash, new ChangeEvent<>());
            if (bulkChange.inBoth == that.size()) {
                return false;
            }
            hashSet = newRootNode;
            size += that.size - bulkChange.inBoth;
            modCount++;
            return true;
        }
        return super.addAll(c);
    }

    @SuppressWarnings("unchecked")
    @Override
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
        if (c instanceof OldMutableHashSet<?> m) {
            c = m.toPersistent();
        }
        if (c instanceof OldPersistentHashSet<?> that) {
            BulkChangeEvent bulkChange = new BulkChangeEvent();
            BitmapIndexedNode<E> newRootNode = hashSet.removeAll(owner, (BitmapIndexedNode<E>) that.root, 0, bulkChange, OldPersistentHashSet::keepOldElement, Objects::equals, OldPersistentHashSet::keyHash, new ChangeEvent<>());
            if (bulkChange.removed == 0) {
                return false;
            }
            hashSet = newRootNode;
            size -= bulkChange.removed;
            modCount++;
            return true;
        }
        return super.removeAll(c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(Collection<?> c) {
        if (isEmpty()) {
            return false;
        }
        if (c.isEmpty()) {
            clear();
            return true;
        }
        if (c instanceof OldMutableHashSet<?> m) {
            OldPersistentHashSet<?> that = m.toPersistent();
            BulkChangeEvent bulkChange = new BulkChangeEvent();
            BitmapIndexedNode<E> newRootNode = hashSet.retainAll(owner, (BitmapIndexedNode<E>) that.root, 0, bulkChange, OldPersistentHashSet::keepOldElement, Objects::equals, OldPersistentHashSet::keyHash, new ChangeEvent<>());
            if (bulkChange.removed == 0) {
                return false;
            }
            hashSet = newRootNode;
            size -= bulkChange.removed;
            modCount++;
            return true;
        }
        return super.retainAll(c);
    }


    @SuppressWarnings("unchecked")
    public boolean retainAll(Iterable<?> c) {
        if (c == this || isEmpty()) {
            return false;
        }
        if ((c instanceof Collection<?> cc && cc.isEmpty())
                || (c instanceof ReadableCollection<?> rc) && rc.isEmpty()) {
            clear();
            return true;
        }
        BulkChangeEvent bulkChange = new BulkChangeEvent();
        BitmapIndexedNode<E> newRootNode;
        switch (c) {
            case OldPersistentHashSet<?> that ->
                    newRootNode = hashSet.retainAll(owner, (BitmapIndexedNode<E>) that.root, 0, bulkChange, OldPersistentHashSet::keepOldElement, Objects::equals, OldPersistentHashSet::keyHash, new ChangeEvent<>());
            case Collection<?> that -> newRootNode = hashSet.filterAll(owner, that::contains, 0, bulkChange);
            case ReadableCollection<?> that -> newRootNode = hashSet.filterAll(owner, that::contains, 0, bulkChange);
            default -> {
                HashSet<Object> that = new HashSet<>();
                c.forEach(that::add);
                newRootNode = hashSet.filterAll(owner, that::contains, 0, bulkChange);
            }
        }
        if (bulkChange.removed == 0) {
            return false;
        }
        hashSet = newRootNode;
        size -= bulkChange.removed;
        modCount++;
        return true;
    }

    /// Removes all elements from this set.
    @Override
    public void clear() {
        hashSet = BitmapIndexedNode.emptyNode();
        size = 0;
        modCount++;
    }

    /// Returns a shallow copy of this set.
    @Override
    public OldMutableHashSet<E> clone() {
        OldMutableHashSet<E> that = (OldMutableHashSet<E>) super.clone();
        that.owner = new IdentityObject();
        return that;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return Node.NO_DATA != hashSet.find((E) o, OldPersistentHashSet.keyHash(o), 0, Objects::equals);
    }

    @Override
    public Iterator<E> iterator() {
        return new FailFastIterator<>(
                new ChampIterator<>(hashSet, null),
                this::iteratorRemove, this::getModCount
        );
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(new ChampSpliterator<>(hashSet, Function.identity(), size, Spliterator.DISTINCT | Spliterator.SIZED), () -> this.modCount, null);
    }

    private void iteratorRemove(E e) {
        owner = new IdentityObject();
        remove(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        ChangeEvent<E> details = new ChangeEvent<>();
        hashSet = hashSet.remove(owner,
                (E) o, OldPersistentHashSet.keyHash(o), 0, details,
                Objects::equals);
        if (details.isModified()) {
            size--;
            modCount++;
        }
        return details.isModified();
    }

    /// Returns a persistent copy of this set.
    ///
    /// @return a persistent copy
    public OldPersistentHashSet<E> toPersistent() {
        owner = new IdentityObject();
        return size == 0
                ? OldPersistentHashSet.of()
                : new OldPersistentHashSet<>(hashSet, size);
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
            return new OldMutableHashSet<>(deserializedElements);
        }
    }
}