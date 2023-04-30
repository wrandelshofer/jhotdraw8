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
import org.jhotdraw8.collection.impl.champ.*;
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
 *     <li>iterator.next: O(1)</li>
 *     <li>getFirst, getLast: O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * See description at {@link SequencedChampSet}.
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <E> the element type
 */
@SuppressWarnings("exports")
public class MutableSequencedChampSet<E> extends ChampAbstractMutableChampSet<E, ChampSequencedElement<E>> implements ReadOnlySequencedSet<E>,
        SequencedSet<E> {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * Counter for the sequence number of the last element. The counter is
     * incremented after a new entry is added to the end of the sequence.
     */
    private int last = 0;
    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement before a new entry is added to the start of the sequence.
     */
    private int first = -1;
    /**
     * The root of the CHAMP trie for the sequence numbers.
     */
    private @NonNull ChampBitmapIndexedNode<ChampSequencedElement<E>> sequenceRoot;

    /**
     * Constructs a new empty set.
     */
    public MutableSequencedChampSet() {
        root = ChampBitmapIndexedNode.emptyNode();
        sequenceRoot = ChampBitmapIndexedNode.emptyNode();
    }

    /**
     * Constructs a set containing the elements in the specified
     * {@link Iterable}.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public MutableSequencedChampSet(Iterable<? extends E> c) {
        if (c instanceof MutableSequencedChampSet<?>) {
            c = ((MutableSequencedChampSet<? extends E>) c).toImmutable();
        }
        if (c instanceof SequencedChampSet<?>) {
            SequencedChampSet<E> that = (SequencedChampSet<E>) c;
            this.root = that;
            this.size = that.size;
            this.first = that.first;
            this.last = that.last;
            this.sequenceRoot = that.sequenceRoot;
        } else {
            this.root = ChampBitmapIndexedNode.emptyNode();
            this.sequenceRoot = ChampBitmapIndexedNode.emptyNode();
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
        var details = new ChampChangeEvent<ChampSequencedElement<E>>();
        var newElem = new ChampSequencedElement<>(e, first);
        IdentityObject mutator = getOrCreateIdentity();
        root = root.update(mutator, newElem,
                Objects.hashCode(e), 0, details,
                moveToFirst ? ChampSequencedElement::updateAndMoveToFirst : ChampSequencedElement::update,
                Objects::equals, Objects::hashCode);
        boolean modified = details.isModified();
        if (modified) {
            var oldElem = details.getOldData();
            if (details.isReplaced()) {
                if (moveToFirst) {
                    sequenceRoot = ChampSequencedData.seqRemove(sequenceRoot, mutator, oldElem, details);
                    first = details.getOldData().getSequenceNumber() == first ? first : first - 1;
                    last = oldElem.getSequenceNumber() == last - 1 ? last - 1 : last;
                    first--;
                    modCount++;
                }
            } else {
                first--;
                size++;
                modCount++;
            }
            sequenceRoot = ChampSequencedData.seqUpdate(sequenceRoot, mutator, newElem, details, ChampSequencedElement::update);
            renumber();
        }
        return modified;
    }

    @Override
    public void addLast(@Nullable E e) {
        addLast(e, true);
    }

    private boolean addLast(@Nullable E e, boolean moveToLast) {
        var details = new ChampChangeEvent<ChampSequencedElement<E>>();
        var newElem = new ChampSequencedElement<>(e, last);
        var mutator = getOrCreateIdentity();
        root = root.update(
                mutator, newElem, Objects.hashCode(e), 0,
                details,
                moveToLast ? ChampSequencedElement::updateAndMoveToLast : ChampSequencedElement::update,
                Objects::equals, Objects::hashCode);
        boolean modified = details.isModified();
        if (modified) {
            var oldElem = details.getOldData();
            if (details.isReplaced()) {
                if (moveToLast) {
                    sequenceRoot = ChampSequencedData.seqRemove(sequenceRoot, mutator, oldElem, details);
                    first = oldElem.getSequenceNumber() == first - 1 ? first - 1 : first;
                    last++;
                    modCount++;
                }
            } else {
                size++;
                last++;
                modCount++;
            }
            sequenceRoot = ChampSequencedData.seqUpdate(sequenceRoot, mutator, newElem, details, ChampSequencedElement::update);
            renumber();
        }
        return modified;
    }

    /**
     * Removes all elements from this set.
     */
    @Override
    public void clear() {
        root = ChampBitmapIndexedNode.emptyNode();
        sequenceRoot = ChampBitmapIndexedNode.emptyNode();
        size = 0;
        modCount++;
        first = -1;
        last = 0;
    }

    /**
     * Returns a shallow copy of this set.
     */
    @Override
    public @NonNull MutableSequencedChampSet<E> clone() {
        return (MutableSequencedChampSet<E>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable final Object o) {
        return ChampNode.NO_DATA != root.find(new ChampSequencedElement<>((E) o),
                Objects.hashCode(o), 0, Objects::equals);
    }

    @Override
    public E getFirst() {
        return ChampNode.getFirst(sequenceRoot).getElement();
    }

    @Override
    public E getLast() {
        return ChampNode.getLast(sequenceRoot).getElement();
    }


    @Override
    public @NonNull Iterator<E> iterator() {
        return new FailFastIterator<>(new IteratorFacade<>(spliterator(),
                this::iteratorRemove), () -> modCount);
    }

    private @NonNull Iterator<E> reverseIterator() {
        return new FailFastIterator<>(new IteratorFacade<>(reverseSpliterator(),
                this::iteratorRemove), () -> modCount);
    }

    private @NonNull EnumeratorSpliterator<E> reverseSpliterator() {
        return new ChampReversedChampSpliterator<>(sequenceRoot,
                ChampSequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
    }

    @Override
    public @NonNull EnumeratorSpliterator<E> spliterator() {
        return new ChampSpliterator<>(sequenceRoot,
                ChampSequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
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
        var details = new ChampChangeEvent<ChampSequencedElement<E>>();
        var mutator = getOrCreateIdentity();
        root = root.remove(
                mutator, new ChampSequencedElement<>((E) o),
                Objects.hashCode(o), 0, details, Objects::equals);
        if (details.isModified()) {
            size--;
            modCount++;
            var elem = details.getOldData();
            int seq = elem.getSequenceNumber();
            sequenceRoot = ChampSequencedData.seqRemove(sequenceRoot, mutator, elem, details);
            if (seq == last - 1) {
                last--;
            }
            if (seq == first) {
                first++;
            }
            renumber();
        }
        return details.isModified();
    }

    @Override
    public E removeFirst() {
        var e = ChampNode.getFirst(sequenceRoot).getElement();
        remove(e);
        return e;
    }

    @Override
    public E removeLast() {
        var e = ChampNode.getLast(sequenceRoot).getElement();
        remove(e);
        return e;
    }

    /**
     * Renumbers the sequence numbers if they have overflown.
     */
    private void renumber() {
        if (ChampSequencedData.mustRenumber(size, first, last)) {
            IdentityObject mutator = getOrCreateIdentity();
            root = ChampSequencedData.renumber(size, root, sequenceRoot, mutator,
                    Objects::hashCode, Objects::equals,
                    (e, seq) -> new ChampSequencedElement<>(e.getElement(), seq));
            sequenceRoot = ChampSequencedData.buildSequencedTrie(sequenceRoot, mutator);
            last = size;
            first = -1;
        }
    }

    @Override
    public @NonNull SequencedSet<E> reversed() {
        return new SequencedSetFacade<>(
                this::reverseIterator,
                this::reverseSpliterator,
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
    public @NonNull SequencedChampSet<E> toImmutable() {
        mutator = null;
        return size == 0 ? SequencedChampSet.of() :
                new SequencedChampSet<>(root, sequenceRoot, size, first, last);
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
            return new MutableSequencedChampSet<>(deserialized);
        }
    }
}