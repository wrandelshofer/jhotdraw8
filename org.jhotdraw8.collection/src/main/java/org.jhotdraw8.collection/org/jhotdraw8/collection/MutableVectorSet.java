/*
 * @(#)MutableSequencedChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.collection.facade.SequencedSetFacade;
import org.jhotdraw8.collection.impl.champ.AbstractMutableChampSet;
import org.jhotdraw8.collection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.impl.champ.ChangeEvent;
import org.jhotdraw8.collection.impl.champ.Node;
import org.jhotdraw8.collection.impl.champ.ReverseSeqVectorSpliterator;
import org.jhotdraw8.collection.impl.champ.SeqVectorSpliterator;
import org.jhotdraw8.collection.impl.champ.SequencedData;
import org.jhotdraw8.collection.impl.champ.SequencedElement;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.collection.sequenced.SequencedSet;
import org.jhotdraw8.collection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

/**
 * Implements a mutable set using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP), with predictable iteration order.
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
 *     <li>toImmutable: O(1) + O(log N) distributed across subsequent updates in
 *     this set</li>
 *     <li>clone: O(1) + O(log N) distributed across subsequent updates in this
 *     set and in the clone</li>
 *     <li>iterator creation: O(1)</li>
 *     <li>iterator.next: O(1) with bucket sort, O(log N) with heap sort</li>
 *     <li>getFirst, getLast: O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * See description at {@link VectorSet}.
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. BSD-2-Clause License</dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <E> the element type
 */
@SuppressWarnings("exports")
public class MutableVectorSet<E> extends AbstractMutableChampSet<E, SequencedElement<E>> implements ReadOnlySequencedSet<E>,
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
    private @NonNull VectorList<Object> vector;

    /**
     * Constructs a new empty set.
     */
    public MutableVectorSet() {
        root = BitmapIndexedNode.emptyNode();
        vector = VectorList.of();
    }

    /**
     * Constructs a set containing the elements in the specified
     * {@link Iterable}.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public MutableVectorSet(Iterable<? extends E> c) {
        if (c instanceof MutableVectorSet<?>) {
            c = ((MutableVectorSet<? extends E>) c).toImmutable();
        }
        if (c instanceof VectorSet<?>) {
            VectorSet<E> that = (VectorSet<E>) c;
            this.root = that;
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
        IdentityObject mutator = getOrCreateIdentity();
        root = root.update(mutator, newElem,
                Objects.hashCode(e), 0, details,
                moveToFirst ? SequencedElement::updateAndMoveToFirst : SequencedElement::update,
                Objects::equals, Objects::hashCode);
        if (details.isModified()) {
            if (details.isReplaced()) {
                if (moveToFirst) {
                    vector = SequencedData.vecRemove(vector, mutator, details.getOldDataNonNull(), details, offset);
                }
            } else {
                size++;
            }
            offset++;
            modCount++;
            vector = vector.addFirst(newElem);
            renumber();
        }
        return details.isModified();
    }

    @Override
    public void addLast(@Nullable E e) {
        addLast(e, true);
    }

    private boolean addLast(@Nullable E e, boolean moveToLast) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, offset + vector.size());
        var mutator = getOrCreateIdentity();
        root = root.update(
                mutator, newElem, Objects.hashCode(e), 0,
                details,
                moveToLast ? SequencedElement::updateAndMoveToLast : SequencedElement::update,
                Objects::equals, Objects::hashCode);
        if (details.isModified()) {
            var oldElem = details.getOldData();
            if (details.isReplaced()) {
                vector = SequencedData.vecRemove(vector, mutator, oldElem, details, offset);
            } else {
                modCount++;
                size++;
            }
            vector = vector.add(newElem);
            renumber();
        }
        return details.isModified();
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
    public @NonNull MutableVectorSet<E> clone() {
        return (MutableVectorSet<E>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable final Object o) {
        return Node.NO_DATA != root.find(new SequencedElement<>((E) o),
                Objects.hashCode(o), 0, Objects::equals);
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
    public @NonNull Iterator<E> iterator() {
        return new FailFastIterator<>(new IteratorFacade<>(spliterator(),
                this::iteratorRemove), () -> modCount);
    }

    private @NonNull Iterator<E> reversedIterator() {
        return new FailFastIterator<>(new IteratorFacade<>(reversedSpliterator(),
                this::iteratorRemove), () -> modCount);
    }

    @SuppressWarnings("unchecked")
    private @NonNull EnumeratorSpliterator<E> reversedSpliterator() {
        return new ReverseSeqVectorSpliterator<>(vector,
                (Object o) -> ((SequencedElement<E>) o).getElement(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull EnumeratorSpliterator<E> spliterator() {
        return new SeqVectorSpliterator<>(vector,
                (Object o) -> ((SequencedElement<E>) o).getElement(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
    }

    private void iteratorRemove(E element) {
        mutator = null;
        remove(element);
    }

    @Override
    public @NonNull ReadOnlySequencedSet<E> readOnlyReversed() {
        return new ReadOnlySequencedSetFacade<>(reversed());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var mutator = getOrCreateIdentity();
        root = root.remove(
                mutator, new SequencedElement<>((E) o),
                Objects.hashCode(o), 0, details, Objects::equals);
        boolean modified = details.isModified();
        if (modified) {
            size--;
            modCount++;
            var elem = details.getOldData();
            int seq = elem.getSequenceNumber();
            vector = SequencedData.vecRemove(vector, mutator, elem, details, offset);
            if (seq == -offset) {
                offset--;
            }
            renumber();
        }
        return modified;
    }

    @Override
    public E removeFirst() {
        var e = getFirst();
        remove(e);
        return e;
    }

    @Override
    public E removeLast() {
        var e = getLast();
        remove(e);
        return e;
    }

    /**
     * Renumbers the sequence numbers if they have overflown.
     */
    private void renumber() {
        if (SequencedData.vecMustRenumber(size, offset, vector.size())) {
            IdentityObject mutator = getOrCreateIdentity();
            root = SequencedData.vecRenumber(size, root, vector, mutator,
                    Objects::hashCode, Objects::equals,
                    (e, seq) -> new SequencedElement<>(e.getElement(), seq));
            vector = SequencedData.vecBuildSequencedTrie(root, mutator, size);
            offset = 0;
        }
    }

    @Override
    public @NonNull SequencedSet<E> reversed() {
        return new SequencedSetFacade<>(
                this::reversedIterator,
                this::reversedSpliterator,
                this::iterator,
                this::spliterator,
                this::size,
                this::contains,
                this::clear,
                this::remove,
                this::getLast, this::getFirst,
                e -> addFirst(e, false), this::add,
                this::addLast, this::addFirst
        );
    }

    /**
     * Returns an immutable copy of this set.
     *
     * @return an immutable copy
     */
    public @NonNull VectorSet<E> toImmutable() {
        mutator = null;
        return size == 0 ? VectorSet.of() :
                new VectorSet<>(root, vector, size, offset);
    }

    @Serial
    private @NonNull Object writeReplace() {
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
        protected @NonNull Object readResolve() {
            return new MutableVectorSet<>(deserialized);
        }
    }
}