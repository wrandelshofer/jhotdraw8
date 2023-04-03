/*
 * @(#)ImmutableSequencedChampSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.collection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.collection.serialization.SetSerializationProxy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

import static org.jhotdraw8.collection.champ.SequencedData.mustRenumber;
import static org.jhotdraw8.collection.champ.SequencedData.seqHash;


/**
 * Implements a mutable set using two Compressed Hash-Array Mapped Prefix-trees
 * (CHAMP), with predictable iteration order.
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>30</sup> elements</li>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>iterates in the order, in which elements were inserted</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>copyAdd: O(1) amortized due to
 *  *     renumbering</li>
 *     <li>copyRemove: O(1) amortized due to
 *  *     renumbering</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(1)</li>
 *     <li>iterator.next: O(1)</li>
 *     <li>getFirst(), getLast(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This set performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP trie contains nodes that may be shared with other sets.
 * <p>
 * If a write operation is performed on a node, then this set creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP trie has a fixed maximal height, the cost is O(1).
 * <p>
 * This set can create a mutable copy of itself in O(1) time and O(1) space
 * using method {@link #toMutable()}}. The mutable copy shares its nodes
 * with this set, until it has gradually replaced the nodes with exclusively
 * owned nodes.
 * <p>
 * Insertion Order:
 * <p>
 * This set uses a counter to keep track of the insertion order.
 * It stores the current value of the counter in the sequence number
 * field of each data entry. If the counter wraps around, it must renumber all
 * sequence numbers.
 * <p>
 * The renumbering is why the {@code add} and {@code remove} methods are O(1)
 * only in an amortized sense.
 * <p>
 * To support iteration, a second CHAMP trie is maintained. The second CHAMP
 * trie has the same contents as the first. However, we use the sequence number
 * for computing the hash code of an element.
 * <p>
 * In this implementation, a hash code has a length of
 * 32 bits, and is split up in little-endian order into 7 parts of
 * 5 bits (the last part contains the remaining bits).
 * <p>
 * We convert the sequence number to unsigned 32 by adding Integer.MIN_VALUE
 * to it. And then we reorder its bits from
 * 66666555554444433333222221111100 to 00111112222233333444445555566666.
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
public class SequencedChampSet<E>
        extends BitmapIndexedNode<SequencedElement<E>>
        implements Serializable, ImmutableSequencedSet<E> {
    private static final long serialVersionUID = 0L;

    private static final @NonNull SequencedChampSet<?> EMPTY = new SequencedChampSet<>(
            BitmapIndexedNode.emptyNode(), BitmapIndexedNode.emptyNode(), 0, -1, 0);

    /**
     * The root of the CHAMP trie for the sequence numbers.
     */
    final @NonNull BitmapIndexedNode<SequencedElement<E>> sequenceRoot;

    final int size;

    /**
     * Counter for the sequence number of the last element. The counter is
     * incremented after a new entry has been added to the end of the sequence.
     */
    final int last;


    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement after a new entry has been added to the start of the sequence.
     */
    final int first;

    SequencedChampSet(
            @NonNull BitmapIndexedNode<SequencedElement<E>> root,
            @NonNull BitmapIndexedNode<SequencedElement<E>> sequenceRoot,
            int size, int first, int last) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        assert (long) last - first >= size : "size=" + size + " first=" + first + " last=" + last;
        this.size = size;
        this.first = first;
        this.last = last;
        this.sequenceRoot = Objects.requireNonNull(sequenceRoot);
    }


    static <E> BitmapIndexedNode<SequencedElement<E>> buildSequenceRoot(@NonNull BitmapIndexedNode<SequencedElement<E>> root, @NonNull IdentityObject mutator) {
        BitmapIndexedNode<SequencedElement<E>> seqRoot = emptyNode();
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        for (KeyIterator<SequencedElement<E>> i = new KeyIterator<>(root, null); i.hasNext(); ) {
            SequencedElement<E> elem = i.next();
            seqRoot = seqRoot.update(mutator, elem, SequencedData.seqHash(elem.getSequenceNumber()),
                    0, details, (oldK, newK) -> oldK, SequencedData::seqEquals, SequencedData::seqHash);
        }
        return seqRoot;
    }

    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param iterable an iterable
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */

    @SuppressWarnings("unchecked")
    public static <E> @NonNull SequencedChampSet<E> copyOf(@NonNull Iterable<? extends E> iterable) {
        if (iterable instanceof SequencedChampSet) {
            return (SequencedChampSet<E>) iterable;
        } else if (iterable instanceof MutableSequencedChampSet) {
            return ((MutableSequencedChampSet<E>) iterable).toImmutable();
        }
        MutableSequencedChampSet<E> tr = new MutableSequencedChampSet<>(of());
        tr.addAll(iterable);
        return tr.toImmutable();
    }




    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */

    @SuppressWarnings("unchecked")
    public static <E> @NonNull SequencedChampSet<E> of() {
        return ((SequencedChampSet<E>) SequencedChampSet.EMPTY);
    }


    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */

    @SuppressWarnings({"unchecked", "varargs"})
    @SafeVarargs
    public static <E> @NonNull SequencedChampSet<E> of(E @NonNull ... elements) {
        if (elements.length == 0) {
            return (SequencedChampSet<E>) SequencedChampSet.EMPTY;
        } else {
            return ((SequencedChampSet<E>) SequencedChampSet.EMPTY).addAll(Arrays.asList(elements));
        }
    }

    @Override
    public @NonNull SequencedChampSet<E> add(@Nullable E key) {
        return addLast(key, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull SequencedChampSet<E> addAll(@NonNull Iterable<? extends E> set) {
        if (set == this || isEmpty() && (set instanceof SequencedChampSet<?>)) {
            return (SequencedChampSet<E>) set;
        }
        if (isEmpty() && (set instanceof MutableSequencedChampSet)) {
            return ((MutableSequencedChampSet<E>) set).toImmutable();
        }
        MutableSequencedChampSet<E> t = this.toMutable();
        boolean modified = false;
        for (E key : set) {
            modified |= t.add(key);
        }
        return modified ? t.toImmutable() : this;
    }

    public @NonNull SequencedChampSet<E> addFirst(@Nullable E key) {
        return addFirst(key, true);
    }

    public @NonNull SequencedChampSet<E> addLast(@Nullable E key) {
        return addLast(key, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull SequencedChampSet<E> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return find(new SequencedElement<>(key), Objects.hashCode(key), 0, Objects::equals) != Node.NO_DATA;
    }

    private @NonNull SequencedChampSet<E> addFirst(@Nullable E e,
                                                   boolean moveToFirst) {
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        SequencedElement<E> newElem = new SequencedElement<>(e, first);
        var newRoot = update(null, newElem,
                Objects.hashCode(e), 0, details,
                moveToFirst ? SequencedChampSet::updateAndMoveToFirst : SequencedChampSet::update,
                Objects::equals, Objects::hashCode);
        var newSeqRoot = sequenceRoot;
        int newSize = size;
        int newFirst = first;
        int newLast = last;
        if (details.isModified()) {
            IdentityObject mutator = new IdentityObject();
            SequencedElement<E> oldElem = details.getData();
            boolean isUpdated = details.isReplaced();
            if (isUpdated) {
                newSeqRoot = newSeqRoot.remove(mutator,
                        oldElem, seqHash(oldElem.getSequenceNumber()), 0, details,
                        SequencedData::seqEquals);

                newFirst = details.getData().getSequenceNumber() == newFirst ? newFirst : newFirst - 1;
                newLast = details.getData().getSequenceNumber() == newLast ? newLast - 1 : newLast;
            } else {
                newFirst--;
                newSize++;
            }
            newSeqRoot = newSeqRoot.update(mutator,
                    newElem, seqHash(first), 0, details,
                    SequencedChampSet::update,
                    SequencedData::seqEquals, SequencedData::seqHash);
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    private @NonNull SequencedChampSet<E> addLast(@Nullable E e,
                                                  boolean moveToLast) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<E>(e, last);
        var newRoot = update(
                null, newElem, Objects.hashCode(e), 0,
                details,
                moveToLast ? SequencedChampSet::updateAndMoveToLast : SequencedChampSet::update,
                Objects::equals, Objects::hashCode);
        if (details.isModified()) {
            var newSeqRoot = sequenceRoot;
            int newFirst = first;
            int newLast = last;
            int newSize = size;
            var mutator = new IdentityObject();
            var oldElem = details.getData();
            if (details.isReplaced()) {
                newSeqRoot = newSeqRoot.remove(mutator,
                        oldElem, seqHash(oldElem.getSequenceNumber()), 0, details,
                        SequencedData::seqEquals);
                newFirst = details.getData().getSequenceNumber() == newFirst - 1 ? newFirst - 1 : newFirst;
                newLast = details.getData().getSequenceNumber() == newLast ? newLast : newLast + 1;
            } else {
                newSize++;
                newLast++;
            }
            newSeqRoot = newSeqRoot.update(mutator,
                    newElem, seqHash(last), 0, details,
                    SequencedChampSet::update,
                    SequencedData::seqEquals, SequencedData::seqHash);
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    private @NonNull SequencedChampSet<E> remove(@Nullable E key, int newFirst, int newLast) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedElement<E>> newRoot = remove(null,
                new SequencedElement<>(key),
                keyHash, 0, details, Objects::equals);
        BitmapIndexedNode<SequencedElement<E>> newSeqRoot = sequenceRoot;
        if (details.isModified()) {
            var oldElem = details.getData();
            int seq = oldElem.getSequenceNumber();
            newSeqRoot = newSeqRoot.remove(null,
                    oldElem,
                    seqHash(seq), 0, details, SequencedData::seqEquals);
            if (seq == newFirst) {
                newFirst++;
            }
            if (seq == newLast - 1) {
                newLast--;
            }
            return renumber(newRoot, newSeqRoot, size - 1, newFirst, newLast);
        }
        return this;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (other instanceof SequencedChampSet) {
            SequencedChampSet<?> that = (SequencedChampSet<?>) other;
            return size == that.size && equivalent(that);
        } else {
            return ReadOnlySet.setEquals(this, other);
        }
    }

    @Override
    public E getFirst() {
        return Node.getFirst(sequenceRoot).getElement();
    }

    @Override
    public E getLast() {
        return Node.getLast(sequenceRoot).getElement();
    }

    @NonNull
    private static <E> SequencedElement<E> updateAndMoveToFirst(@NonNull SequencedElement<E> oldK, @NonNull SequencedElement<E> newK) {
        return oldK.getSequenceNumber() == newK.getSequenceNumber() + 1 ? oldK : newK;
    }

    @NonNull
    private static <E> SequencedElement<E> updateAndMoveToLast(@NonNull SequencedElement<E> oldK, @NonNull SequencedElement<E> newK) {
        return oldK.getSequenceNumber() == newK.getSequenceNumber() - 1 ? oldK : newK;
    }

    @NonNull
    private static <E> SequencedElement<E> update(@NonNull SequencedElement<E> oldK, @NonNull SequencedElement<E> newK) {
        return oldK;
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new IteratorFacade<>(spliterator(), null);
    }

    private @NonNull EnumeratorSpliterator<E> reversedSpliterator() {
        return new ReversedKeySpliterator<>(sequenceRoot, SequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
    }

    @Override
    public EnumeratorSpliterator<E> spliterator() {
        return new KeySpliterator<>(sequenceRoot, SequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
    }

    @Override
    public @NonNull ReadOnlySequencedSet<E> readOnlyReversed() {
        return new ReadOnlySequencedSetFacade<>(
                this::reversedIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst
        );
    }

    @Override
    public @NonNull SequencedChampSet<E> remove(@Nullable E key) {
        return remove(key, first, last);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull SequencedChampSet<E> removeAll(@NonNull Iterable<?> set) {
        if (this.isEmpty()
                || (set instanceof Collection) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }
        if (set == this) {
            return of();
        }
        MutableSequencedChampSet<E> t = this.toMutable();
        boolean modified = false;
        for (Object key : set) {
            if (t.remove((E) key)) {
                modified = true;
                if (t.isEmpty()) {
                    break;
                }
            }

        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public SequencedChampSet<E> removeFirst() {
        SequencedElement<E> k = Node.getFirst(sequenceRoot);
        return remove(k.getElement(), k.getSequenceNumber() + 1, last);
    }

    @Override
    public SequencedChampSet<E> removeLast() {
        SequencedElement<E> k = Node.getLast(sequenceRoot);
        return remove(k.getElement(), first, k.getSequenceNumber());
    }


    /**
     * Renumbers the sequenced elements in the trie if necessary.
     *
     * @param root    the root of the trie
     * @param seqRoot
     * @param size    the size of the trie
     * @param first   the estimated first sequence number
     * @param last    the estimated last sequence number
     * @return a new {@link SequencedChampSet} instance
     */
    @NonNull
    private SequencedChampSet<E> renumber(
            BitmapIndexedNode<SequencedElement<E>> root,
            BitmapIndexedNode<SequencedElement<E>> seqRoot,
            int size, int first, int last) {
        if (mustRenumber(size, first, last)) {
            IdentityObject mutator = new IdentityObject();
            BitmapIndexedNode<SequencedElement<E>> renumberedRoot = SequencedData.renumber(
                    size, root, seqRoot, mutator, Objects::hashCode, Objects::equals,
                    (e, seq) -> new SequencedElement<>(e.getElement(), seq));
            BitmapIndexedNode<SequencedElement<E>> renumberedSeqRoot = buildSequenceRoot(renumberedRoot, mutator);
            return new SequencedChampSet<>(
                    renumberedRoot, renumberedSeqRoot,
                    size, -1, size);
        }
        return new SequencedChampSet<>(root, seqRoot, size, first, last);
    }

    @Override
    public @NonNull SequencedChampSet<E> retainAll(@NonNull Collection<?> set) {
        if (this.isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }

        MutableSequencedChampSet<E> t = this.toMutable();
        boolean modified = false;
        for (E key : this) {
            if (!set.contains(key)) {
                t.remove(key);
                modified = true;
                if (t.isEmpty()) {
                    break;
                }
            }
        }
        return modified ? t.toImmutable() : this;
    }

    public @NonNull Iterator<E> reversedIterator() {
        return new IteratorFacade<>(reversedSpliterator(), null);
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Creates a mutable copy of this set.
     *
     * @return a mutable sequenced CHAMP set
     */
    @Override
    public @NonNull MutableSequencedChampSet<E> toMutable() {
        return new MutableSequencedChampSet<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(toMutable());
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Set<E> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return SequencedChampSet.copyOf(deserialized);
        }
    }
}