/*
 * @(#)MutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champset.AbstractMutableChampSet;
import org.jhotdraw8.icollection.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.impl.champset.Node;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.linked.LinkedElement;
import org.jhotdraw8.icollection.impl.linked.LinkedElementIterator;
import org.jhotdraw8.icollection.impl.linked.ReversedLinkedElementIterator;
import org.jhotdraw8.icollection.sequenced.ReversedSequencedSetView;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

/// Implements the [Set] interface using a Compressed Hash-Array Mapped
/// Prefix-tree (CHAMP) and a doubly linked list.
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 elements
///   - does not allow null elements
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
///   - iterator.next: O(log₃₂ N)
///
///
/// Implementation details:
///
/// See description at [PersistentLinkedHashElementSet].
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
public class MutableLinkedHashElementSet<E> extends AbstractMutableChampSet<E, LinkedElement<E>> implements SequencedSet<E> {
    @Serial
    private static final long serialVersionUID = 0L;
    private @Nullable LinkedElement<E> first;
    private @Nullable LinkedElement<E> last;


    /// Constructs a new empty set.
    public MutableLinkedHashElementSet() {
        hashSet = BitmapIndexedNode.emptyNode();
    }

    /// Constructs a set containing the elements in the specified iterable.
    ///
    /// @param c an iterable
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableLinkedHashElementSet(Iterable<? extends E> c) {
        this();
        addAll(c);
    }

    private @Nullable LinkedElement<E> get(@Nullable E o) {
        if (o == null) {
            return null;
        }
        Object result = hashSet.find(
                new LinkedElement<>((E) o, null, null),
                Objects.hashCode(o), 0, Objects::equals);
        return result == org.jhotdraw8.icollection.impl.champset.Node.NO_DATA ? null : (LinkedElement<E>) result;
    }

    @Override
    public boolean add(@Nullable E e) {
        return addLast(e, false);
    }

    @Override
    public void addLast(@Nullable E e) {
        addLast(e, true);
    }

    private boolean addLast(@Nullable E e, boolean moveToLast) {
        Objects.requireNonNull(e, "e must not be null");
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        var linkedElement = new LinkedElement<>(e, last == null ? null : last.key(), null, this.owner);
        hashSet = hashSet.put(this.owner,
                linkedElement, PersistentLinkedHashElementSet.keyHash(e), 0, details,
                (oldKey, newKey) -> moveToLast && oldKey.next() != null ? newKey : oldKey,
                Objects::equals, PersistentLinkedHashElementSet::keyHash);
        if (details.isModified()) {
            if (details.isReplaced()) {
                var removed = details.getOldData();
                updateNext(get(removed.prev()), removed.next());
                updatePrev(get(removed.next()), removed.prev());
                updateNext(last, e);
                if (removed == first) {
                    first = get(first.next());
                }
            } else {
                if (last == null) {
                    last = first = linkedElement;
                } else {
                    updateNext(last, e);
                }
                size++;
                modCount++;
            }
            last = linkedElement;
        }
        return details.isModified();
    }

    private LinkedElement<E> put(@Nullable LinkedElement<E> linkedElement) {
        var e = linkedElement.key();
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        hashSet = hashSet.put(this.owner,
                linkedElement, PersistentLinkedHashElementSet.keyHash(e), 0, details,
                (oldKey, newKey) -> newKey,
                Objects::equals, PersistentLinkedHashElementSet::keyHash);
        return details.getOldData();
    }

    @Override
    public void addFirst(@Nullable E e) {
        addFirst(e, true);
    }

    private boolean addFirst(@Nullable E e, boolean moveToFirst) {
        Objects.requireNonNull(e, "e must not be null");
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        var linkedElement = new LinkedElement<>(e, null, first == null ? null : first.key(), this.owner);
        hashSet = hashSet.put(this.owner,
                linkedElement, PersistentLinkedHashElementSet.keyHash(e), 0, details,
                (oldKey, newKey) -> moveToFirst && oldKey.prev() != null ? newKey : oldKey,
                Objects::equals, PersistentLinkedHashElementSet::keyHash);
        if (details.isModified()) {
            if (details.isReplaced()) {
                var removed = details.getOldData();
                assert removed != null;
                updateNext(get(removed.prev()), removed.next());
                updatePrev(get(removed.next()), removed.prev());
                updatePrev(first, e);
                if (removed == last) {
                    last = get(removed.prev());
                }
            } else {
                if (last == null) {
                    last = first = linkedElement;
                } else {
                    updatePrev(first, e);
                }
                size++;
                modCount++;
            }
            first = linkedElement;
        }
        return details.isModified();
    }

    private void updateNext(@Nullable LinkedElement<E> elem, @Nullable E e) {
        if (elem == null) {
            return;
        }
        if (elem.isOwned(this.owner)) {
            elem.setNext(e);
        } else {
            LinkedElement<E> newData = new LinkedElement<>(elem.key(), elem.prev(), e, this.owner);
            var oldData = put(newData);
            if (last == oldData) {
                last = newData;
            }
            if (first == oldData) {
                first = newData;
            }
        }
    }

    private void updatePrev(@Nullable LinkedElement<E> elem, @Nullable E e) {
        if (elem == null) {
            return;
        }
        if (elem.isOwned(this.owner)) {
            elem.setPrev(e);
        } else {
            LinkedElement<E> newData = new LinkedElement<>(elem.key(), e, elem.next(), this.owner);
            var oldData = put(newData);
            if (last == oldData) {
                last = newData;
            }
            if (first == oldData) {
                first = newData;
            }
        }
    }

    /// Removes all elements from this set.
    @Override
    public void clear() {
        hashSet = BitmapIndexedNode.emptyNode();
        size = 0;
        first = last = null;
        modCount++;
    }

    /// Returns a shallow copy of this set.
    @Override
    public MutableLinkedHashElementSet<E> clone() {
        MutableLinkedHashElementSet<E> that = (MutableLinkedHashElementSet<E>) super.clone();
        that.owner = new IdentityObject();
        return that;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return Node.NO_DATA != hashSet.find(new LinkedElement((E) o, null, null), PersistentLinkedHashElementSet.keyHash(o), 0, Objects::equals);
    }

    @Override
    public Iterator<E> iterator() {
        return new FailFastIterator<>(
                new LinkedElementIterator<>(first, hashSet, LinkedElement::key),
                this::iteratorRemove, this::getModCount
        );
    }


    private Iterator<E> reverseIterator() {
        return new FailFastIterator<>(
                new ReversedLinkedElementIterator<>(last, hashSet, LinkedElement::key),
                this::iteratorRemove, this::getModCount
        );
    }

    @Override
    public SequencedSet<E> reversed() {
        return new ReversedSequencedSetView<>(this, this::reverseIterator,
                this::reverseSpliterator);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size, Spliterator.SIZED | Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT);
    }

    private Spliterator<E> reverseSpliterator() {
        return Spliterators.spliterator(reverseIterator(), size, Spliterator.SIZED | Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT);
    }

    private void iteratorRemove(E e) {
        remove(e);
    }

    @Override
    public E getFirst() {
        if (first == null) {
            throw new NoSuchElementException();
        }
        return first.key();
    }

    @Override
    public E getLast() {
        if (last == null) {
            throw new NoSuchElementException();
        }
        return last.key();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        ChangeEvent<LinkedElement<E>> details = new ChangeEvent<>();
        hashSet = hashSet.remove(this.owner,
                new LinkedElement<>((E) o, null, null), PersistentLinkedHashElementSet.keyHash(o), 0, details,
                Objects::equals);
        if (details.isModified()) {
            var removed = details.getOldData();
            assert removed != null;
            updateNext(get(removed.prev()), removed.next());
            updatePrev(get(removed.next()), removed.prev());
            if (last == removed) last = get(last.prev());
            if (first == removed) first = get(first.next());
            size--;
            modCount++;
        }
        return details.isModified();
    }

    /// Returns a persistent copy of this set.
    ///
    /// @return a persistent copy
    public PersistentLinkedHashElementSet<E> toPersistent() {
        owner = new IdentityObject();
        return size == 0
                ? PersistentLinkedHashElementSet.of()
                : new PersistentLinkedHashElementSet<>(first, last, hashSet, size);
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
            return new MutableLinkedHashElementSet<>(deserializedElements);
        }
    }
}