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

/**
 * Implements the {@link SequencedSet} interface using a Compressed
 * Hash-Array Mapped Prefix-tree (CHAMP) and a bit-mapped trie (Vector).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>30</sup> elements</li>
 *     <li>allows null elements</li>
 *     <li>is mutable</li>
 *     <li>is not thread-safe</li>
 *     <li>iterates in the order, in which elements were inserted</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>add: O(1) amortized</li>
 *     <li>remove: O(1)</li>
 *     <li>contains: O(1)</li>
 *     <li>toPersistent: O(1) + O(log N) distributed across subsequent updates in
 *     this set</li>
 *     <li>clone: O(1) + O(log N) distributed across subsequent updates in this
 *     set and in the clone</li>
 *     <li>iterator creation: O(1)</li>
 *     <li>iterator.next: O(1)</li>
 *     <li>getFirst, getLast: O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * See description at {@link ChampVectorSet}.
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Persistent Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-persistent-collections">michael.steindorfer.name</a>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <E> the element type
 */
@SuppressWarnings("exports")
public class MutableChampVectorSet<E> extends AbstractMutableChampSet<E, SequencedElement<E>> implements ReadableSequencedSet<E>,
        SequencedSet<E> {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * Offset of sequence numbers to vector indices.
     *
     * <pre>vector index = sequence number + offset</pre>
     */
    private int offset = 0;
    /**
     * In this vector we store the elements in the order in which they were inserted.
     */
    private VectorList<Object> vector;

    /**
     * Constructs a new empty set.
     */
    public MutableChampVectorSet() {
        root = BitmapIndexedNode.emptyNode();
        vector = VectorList.of();
    }

    /**
     * Constructs a set containing the elements in the specified
     * {@link Iterable}.
     *
     * @param c an iterable
     */
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableChampVectorSet(Iterable<? extends E> c) {
        if (c instanceof MutableChampVectorSet<?>) {
            c = ((MutableChampVectorSet<? extends E>) c).toPersistent();
        }
        if (c instanceof ChampVectorSet<?>) {
            ChampVectorSet<E> that = (ChampVectorSet<E>) c;
            this.root = that.root;
            this.size = that.size;
            this.offset = that.offset;
            this.vector = that.vector;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            this.vector = VectorList.of();
            addAll(c);
        }
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
        root = root.put(makeOwner(), newElem,
                SequencedElement.keyHash(e), 0, details,
                moveToFirst ? SequencedElement::putAndMoveToFirst : SequencedElement::put,
                Objects::equals, SequencedElement::elementKeyHash);
        boolean modified = details.isModified();
        if (modified) {
            if (details.isReplaced()) {
                if (moveToFirst) {
                    var result = vecRemove(vector, details.getOldDataNonNull(), offset);
                    vector = result.first();
                }
            } else {
                size++;
            }
            offset++;
            modCount++;
            vector = vector.addFirst(newElem);
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
        root = root.put(makeOwner(),
                newElem, SequencedElement.keyHash(e), 0,
                details,
                moveToLast ? SequencedElement::putAndMoveToLast : SequencedElement::put,
                Objects::equals, SequencedElement::elementKeyHash);
        boolean modified = details.isModified();
        if (modified) {
            var oldElem = details.getOldData();
            if (details.isReplaced()) {
                var result = vecRemove(vector, oldElem, offset);
                vector = result.first();
                offset = result.second();
            } else {
                modCount++;
                size++;
            }
            vector = vector.add(newElem);
            renumber();
        }
        return modified;
    }

    /**
     * Removes all elements from this set.
     */
    @Override
    public void clear() {
        root = BitmapIndexedNode.emptyNode();
        vector = VectorList.of();
        size = 0;
        modCount++;
        offset = -1;
    }

    /**
     * Returns a shallow copy of this set.
     */
    @Override
    public MutableChampVectorSet<E> clone() {
        return (MutableChampVectorSet<E>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable final Object o) {
        return Node.NO_DATA != root.find(new SequencedElement<>((E) o),
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
        return new FailFastIterator<>(Spliterators.iterator(new TombSkippingVectorSpliterator<>(vector.trie,
                (Object o) -> ((SequencedElement<E>) o).getElement(),
                0, size, vector.size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED)),
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
                    newVector = result.first();
                    newOffset = result.second();
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

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(new TombSkippingVectorSpliterator<>(vector.trie,
                (Object o) -> ((SequencedElement<E>) o).getElement(),
                0, size(), vector.size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED), () -> modCount, null);
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
        root = root.remove(makeOwner(),
                new SequencedElement<>((E) o),
                SequencedElement.keyHash(o), 0, details, Objects::equals);
        boolean modified = details.isModified();
        if (modified) {
            var result = vecRemove(vector, details.getOldDataNonNull(), offset);
            size--;
            modCount++;
            vector = result.first();
            offset = result.second();
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

    /**
     * Renumbers the sequence numbers if they have overflown.
     */
    private void renumber() {
        if (SequencedData.vecMustRenumber(size, offset, vector.size())) {
            var result = SequencedData.vecRenumber(makeOwner(), size, vector.size(), root, vector.trie,
                    SequencedElement::elementKeyHash, Objects::equals,
                    (e, seq) -> new SequencedElement<>(e.getElement(), seq));
            root = result.first();
            vector = result.second();
            offset = 0;
        }
    }

    @Override
    public SequencedSet<E> reversed() {
        return new ReversedSequencedSetView<>(this, this::reverseIterator,
                this::reverseSpliterator);
    }

    /**
     * Returns an persistent copy of this set.
     *
     * @return an persistent copy
     */
    public ChampVectorSet<E> toPersistent() {
        owner = null;
        return size == 0
                ? ChampVectorSet.of()
                : new ChampVectorSet<>(root, vector, size, offset);
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
            return new MutableChampVectorSet<>(deserializedElements);
        }
    }
}