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
import org.jhotdraw8.icollection.impl.champ.TombSkippingVectorIterator;
import org.jhotdraw8.icollection.impl.champ.TombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeAPI;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeSpliterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jhotdraw8.icollection.sequenced.ReversedSequencedSetView;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Collection;
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
/// See description at [PersistentVectorSet].
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
public class MutableVectorSet<E> extends AbstractMutableChampSet<E, SequencedElement<E>> implements ReadableSequencedSet<E>,
        SequencedSet<E> {
    @Serial
    private static final long serialVersionUID = 0L;

    /// Sequence number of the first element.
    /// `vector index = sequence number - offset`
    private int offset = 0;
    /// In this vector we store the elements in the order in which they were inserted.
    private PersistentVectorList<Object> vector;

    /// Constructs a new empty set.
    public MutableVectorSet() {
        hashSet = BitmapIndexedNode.emptyNode();
        vector = FingerTreeAPI.of();
    }

    /// Constructs a set containing the elements in the specified
    /// [Iterable].
    ///
    /// @param c an iterable
    @SuppressWarnings({"this-escape"})
    public MutableVectorSet(Iterable<? extends E> c) {
        this();
        addAll(c);
    }

    @Override
    public boolean add(@Nullable E e) {
        return addLast(e, false);
    }

    @Override
    public boolean addAll(Iterable<? extends E> c) {
        if (c == this) return false;
        if (c instanceof MutableVectorSet<?> m) {
            c = (Iterable<? extends E>) m.toPersistent();
        }
        if (isEmpty() && (c instanceof PersistentVectorSet<?> cc)) {
            hashSet = (BitmapIndexedNode<SequencedElement<E>>) (BitmapIndexedNode<?>) cc.hashSet;
            vector = cc.vector;
            offset = cc.offset;
            size = cc.size;
            return true;
        }
        if (c instanceof Collection<? extends E> cc && this.size + cc.size() + offset < Integer.MAX_VALUE - 2
                || c instanceof ReadableCollection<? extends E> rc && this.size + rc.size() + offset < Integer.MAX_VALUE - 2) {
            var b = new PersistentVectorListBuilder<>();
            b.addAll(vector);
            int initialSeqNumber = offset + vector.size();
            int seqNumber = initialSeqNumber;
            for (E e : c) {
                if (addAllAddNext(e, b, seqNumber)) seqNumber++;
            }
            if (seqNumber != initialSeqNumber) {
                vector = b.build();
                return true;
            }
            return false;
        }
        return super.addAll(c);
    }

    private boolean addAllAddNext(@Nullable E e, PersistentVectorListBuilder<Object> b, int seqNumber) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, seqNumber);
        hashSet = hashSet.put(makeOwner(),
                newElem, SequencedElement.keyHash(e), 0,
                details,
                SequencedElement::putIfAbsent,
                Objects::equals, SequencedElement::elementKeyHash);
        boolean modified = details.isModified();
        if (modified) {
            modCount++;
            size++;
            b.add(newElem);
        }
        return modified;
    }

    @Override
    public void addFirst(@Nullable E e) {
        addFirst(e, true);
    }

    private boolean addFirst(@Nullable E e, boolean moveToFirst) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, offset - 1);
        hashSet = hashSet.put(makeOwner(), newElem,
                SequencedElement.keyHash(e), 0, details,
                moveToFirst ? SequencedElement::putAndMoveToFirst : SequencedElement::putIfAbsent,
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
            offset--;
            modCount++;
            vector = FingerTreeAPI.addFirst(vector, newElem);
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
                moveToLast ? SequencedElement::putAndMoveToLast : SequencedElement::putIfAbsent,
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
        offset = 0;
    }

    /// Returns a shallow copy of this set.
    @Override
    public MutableVectorSet<E> clone() {
        var that = (MutableVectorSet<E>) super.clone();
        that.owner = null;
        return that;
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
    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    public Iterator<E> iterator() {
        Iterator<E> inner;
        if (vector.size() == size()) {
            inner = new MappedIterator<>(FingerTreeAPI.iterator(vector), o -> ((SequencedElement<E>) o).getElement());
        } else {
            inner = new TombSkippingVectorIterator<>(vector, o -> ((SequencedElement<E>) o).getElement());
        }
        return new FailFastIterator<>(inner,
                this::iteratorRemove, () -> modCount);
    }

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
            var b = new PersistentVectorSetBuilder<E>(makeOwner(), size / -2);
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

    /// Returns a persistent copy of this set.
    ///
    /// @return a persistent copy
    public PersistentVectorSet<E> toPersistent() {
        owner = null;
        return size == 0
                ? PersistentVectorSet.of()
                : new PersistentVectorSet<>(hashSet, vector, size, offset);
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
            return new MutableVectorSet<>(deserializedElements);
        }
    }
}