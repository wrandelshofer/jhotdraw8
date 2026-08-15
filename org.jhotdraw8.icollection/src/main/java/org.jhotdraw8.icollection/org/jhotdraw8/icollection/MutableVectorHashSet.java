/*
 * @(#)MutableChampVectorSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSequencedSetFacade;
import org.jhotdraw8.icollection.impl.champ.AbstractMutableChampSet;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.Node;
import org.jhotdraw8.icollection.impl.champ.ReverseTombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.impl.champ.SequencedData;
import org.jhotdraw8.icollection.impl.champ.SequencedElement;
import org.jhotdraw8.icollection.impl.champ.TombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.impl.fingertree.FingerTree;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeAPI;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeSpliterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jhotdraw8.icollection.sequenced.ReversedSequencedSetView;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Iterator;
import java.util.Objects;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

import static org.jhotdraw8.icollection.impl.champ.SequencedData.vecRemove;

/// Implements the [SequencedSet] interface using a Compressed
/// Hash-Array Mapped Prefix-tree (CHAMP) and a bit-mapped trie (Vector).
///
/// Features:
///
///   - supports up to 2<sup>30</sup> elements
///   - allows null elements
///   - is mutable
///   - is not thread-safe
///   - iterates in the order, in which elements were inserted
///
///
/// Performance characteristics:
///
///   - add: O(1) amortized
///   - remove: O(1)
///   - contains: O(1)
///   - toPersistent: O(1) + O(log N) distributed across subsequent updates in
///     this set
///   - clone: O(1) + O(log N) distributed across subsequent updates in this
///     set and in the clone
///   - iterator creation: O(1)
///   - iterator.next: O(1)
///   - getFirst, getLast: O(1)
///
///
/// Implementation details:
///
/// See description at [PersistentVectorHashSet].
///
/// References:
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
@SuppressWarnings("exports")
public class MutableVectorHashSet<E> extends AbstractMutableChampSet<E, SequencedElement<E>> implements ReadableSequencedSet<E>,
        SequencedSet<E> {
    @Serial
    private static final long serialVersionUID = 0L;

    /// Offset of sequence numbers to vector indices.
    /// <pre>vector offset = sequence number + offset</pre>
    private int offset = 0;
    /// In this vector we store the elements in the order in which they were inserted.
    private FingerTree<Object> vector;

    /// Constructs a new empty set.
    public MutableVectorHashSet() {
        hashSet = BitmapIndexedNode.emptyNode();
        vector = FingerTreeAPI.of();
    }

    /// Constructs a set containing the elements in the specified
    /// [Iterable].
    ///
    /// @param c an iterable
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableVectorHashSet(Iterable<? extends E> c) {
        var b = new PersistentVectorHashSetBuilder<E>();
        b.addAll(c);
        var cc = b.build();
        hashSet = cc.hashSet;
        vector = cc.vector;
        offset = cc.offset;
        size = cc.size;
    }

    @Override
    public boolean add(@Nullable E e) {
        return addLast(e, false);
    }

    @Override
    public void addFirst(@Nullable E e) {
        addFirst(e, true);
    }

    private boolean addFirst(@Nullable E e, boolean moveToFirst) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, -offset - 1);
        hashSet = hashSet.put(makeOwner(), newElem,
                SequencedElement.keyHash(e), 0, details,
                moveToFirst ? SequencedElement::putAndMoveToFirst : SequencedElement::put,
                Objects::equals, SequencedElement::elementKeyHash);
        boolean modified = details.isModified();
        if (modified) {
            if (details.isReplaced()) {
                if (moveToFirst) {
                    var result = vecRemove(vector, details.getOldDataNonNull(), offset);
                    vector = result.tree();
                }
            } else {
                size++;
            }
            offset++;
            modCount++;
            vector = FingerTreeAPI.addFirst(newElem, vector);
            renumber();
        }
        return modified;
    }

    @Override
    public void addLast(@Nullable E e) {
        addLast(e, true);
    }

    private boolean addLast(@Nullable E e, boolean moveToLast) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, offset + vector.size());
        hashSet = hashSet.put(makeOwner(),
                newElem, SequencedElement.keyHash(e), 0,
                details,
                moveToLast ? SequencedElement::putAndMoveToLast : SequencedElement::put,
                Objects::equals, SequencedElement::elementKeyHash);
        boolean modified = details.isModified();
        if (modified) {
            var oldElem = details.getOldData();
            if (details.isReplaced()) {
                var result = vecRemove(vector, oldElem, offset);
                vector = result.tree();
                offset = result.offset();
            } else {
                modCount++;
                size++;
            }
            vector = FingerTreeAPI.addLast(vector, newElem);
            renumber();
        }
        return modified;
    }

    /// Removes all elements from this set.
    @Override
    public void clear() {
        hashSet = BitmapIndexedNode.emptyNode();
        vector = FingerTreeAPI.of();
        size = 0;
        modCount++;
        offset = -1;
    }

    /// Returns a shallow copy of this set.
    @Override
    public MutableVectorHashSet<E> clone() {
        return (MutableVectorHashSet<E>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return Node.NO_DATA != hashSet.find(new SequencedElement<>((E) o),
                SequencedElement.keyHash(o), 0, Objects::equals);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getFirst() {
        return ((SequencedElement<E>) vector.getFirst()).getElement();
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getLast() {
        return ((SequencedElement<E>) vector.getLast()).getElement();
    }


    @Override
    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
        // FIXME - must implement iterator directly to obtain a good performance
        return new FailFastIterator<>(Spliterators.iterator(spliterator()),
                this::iteratorRemove, () -> modCount);
    }
/*
    public boolean removeAll(Iterable<?> c) {
        if (isEmpty()
                || (c instanceof Collection<?> cc && cc.isEmpty())
                || (c instanceof ReadableCollection<?> rc) && rc.isEmpty()) {
            return false;
        }
        if (c == this) {
            clear();
            return true;
        }
        Predicate<E> predicate;
        if (c instanceof Collection<?> that) {
            predicate = that::contains;
        } else if (c instanceof ReadableCollection<?> that) {
            predicate = that::contains;
        } else {
            HashSet<Object> that = new HashSet<>();
            c.forEach(that::add);
            predicate = that::contains;
        }
        return filterAll(predicate.negate());
    }
/*
    public boolean retainAll(Iterable<?> c) {
        if(c==this||isEmpty()) {
            return false;
        }
        if ((c instanceof Collection<?> cc && cc.isEmpty())
                || (c instanceof ReadableCollection<?> rc) && rc.isEmpty()) {
            clear();
            return true;
        }
        Predicate<E> predicate;
        if (c instanceof Collection<?> that) {
            predicate = that::contains;
        } else if (c instanceof ReadableCollection<?> that) {
            predicate = that::contains;
        } else {
            HashSet<Object> that = new HashSet<>();
            c.forEach(that::add);
            predicate = that::contains;
        }
        return filterAll(predicate);
    }
    boolean filterAll(Predicate<E> predicate) {
        class VectorPredicate implements Predicate<SequencedElement<E>> {
            SimplePersistentList<Object> newVector = vector;
            int newOffset = offset;

            @Override
            public boolean test(SequencedElement<E> e) {
                if (!predicate.test(e.getElement())) {
                    OrderedPair<SimplePersistentList<Object>, Integer> result = vecRemove(newVector, e, newOffset);
                    newVector = result.tree();
                    newOffset = result.offset();
                    return false;
                }
                return true;
            }
        }
        VectorPredicate vp = new VectorPredicate();
        BulkChangeEvent bulkChange = new BulkChangeEvent();
        BitmapIndexedNode<SequencedElement<E>> newRootNode = root.filterAll(makeOwner(), vp, 0, bulkChange);
        if (bulkChange.removed == 0) {
            return false;
        }
        root = newRootNode;
        vector = vp.newVector;
        offset = vp.newOffset;
        size -= bulkChange.removed;
        modCount++;
        return true;
    }
*/

    private void iteratorRemove(E element) {
        owner = null;
        remove(element);
    }

    @Override
    public ReadableSequencedSet<E> readableReversed() {
        return new ReadableSequencedSetFacade<>(this.reversed());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        var details = new ChangeEvent<SequencedElement<E>>();
        hashSet = hashSet.remove(makeOwner(),
                new SequencedElement<>((E) o),
                SequencedElement.keyHash(o), 0, details, Objects::equals);
        boolean modified = details.isModified();
        if (modified) {
            var result = vecRemove(vector, details.getOldDataNonNull(), offset);
            size--;
            modCount++;
            vector = result.tree();
            offset = result.offset();
            renumber();
        }
        return modified;
    }

    @Override
    public E removeFirst() {
        var e = this.getFirst();
        remove(e);
        return e;
    }

    @Override
    public E removeLast() {
        var e = this.getLast();
        remove(e);
        return e;
    }

    /// Renumbers the sequence numbers if they have overflown.
    private void renumber() {
        if (SequencedData.vecMustRenumber(size, offset, vector.size())) {
            var b = new PersistentVectorHashSetBuilder<E>(size / -2);
            b.addAll(this);
            var tmp = b.build();
            hashSet = tmp.hashSet;
            vector = tmp.vector;
            offset = tmp.offset;
        }
    }

    @SuppressWarnings("unchecked")
    private Iterator<E> reverseIterator() {
        return new FailFastIterator<>(Spliterators.iterator(new ReverseTombSkippingVectorSpliterator<>(vector,
                (Object o) -> ((SequencedElement<E>) o).getElement(), size(),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED)),
                this::iteratorRemove, () -> modCount);
    }

    @SuppressWarnings("unchecked")
    private Spliterator<E> reverseSpliterator() {
        return new FailFastSpliterator<>(new ReverseTombSkippingVectorSpliterator<>(vector,
                (Object o) -> ((SequencedElement<E>) o).getElement(), size(),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED), () -> modCount, null);
    }

    @Override
    public SequencedSet<E> reversed() {
        return new ReversedSequencedSetView<>(this, this::reverseIterator,
                this::reverseSpliterator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<E> spliterator() {
        //FIXME Only neighbors if we have tombstones
        return new FailFastSpliterator<>(new TombSkippingVectorSpliterator<>(
                new FingerTreeSpliterator<>(vector),
                e -> ((SequencedElement<E>) e).getElement(),
                0, size(), vector.size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED), () -> modCount, null);
    }

    /// Returns an persistent copy of this set.
    ///
    /// @return an persistent copy
    public PersistentVectorHashSet<E> toPersistent() {
        owner = null;
        return size == 0
                ? PersistentVectorHashSet.of()
                : new PersistentVectorHashSet<>(hashSet, vector, size, offset);
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
            return new MutableVectorHashSet<>(deserializedElements);
        }
    }
}