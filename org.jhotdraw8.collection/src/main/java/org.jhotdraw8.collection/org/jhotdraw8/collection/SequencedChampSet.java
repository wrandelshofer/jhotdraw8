/*
 * @(#)SequencedChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.collection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.collection.impl.champ.ChampBitmapIndexedNode;
import org.jhotdraw8.collection.impl.champ.ChampChangeEvent;
import org.jhotdraw8.collection.impl.champ.ChampNode;
import org.jhotdraw8.collection.impl.champ.ChampReversedChampSpliterator;
import org.jhotdraw8.collection.impl.champ.ChampSequencedData;
import org.jhotdraw8.collection.impl.champ.ChampSequencedElement;
import org.jhotdraw8.collection.impl.champ.ChampSpliterator;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.collection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

import static org.jhotdraw8.collection.impl.champ.ChampSequencedData.mustRenumber;


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
 *     <li>add: O(1) in an amortized sense, because we sometimes have to renumber the elements.</li>
 *     <li>remove: O(1) in an amortized sense, because we sometimes have to renumber the elements.</li>
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
        extends ChampBitmapIndexedNode<ChampSequencedElement<E>>
        implements Serializable, ImmutableSequencedSet<E> {
    @Serial
    private static final long serialVersionUID = 0L;

    private static final @NonNull SequencedChampSet<?> EMPTY = new SequencedChampSet<>(
            ChampBitmapIndexedNode.emptyNode(), ChampBitmapIndexedNode.emptyNode(), 0, -1, 0);

    /**
     * The root of the CHAMP trie for the sequence numbers.
     */
    final @NonNull ChampBitmapIndexedNode<ChampSequencedElement<E>> sequenceRoot;

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
            @NonNull ChampBitmapIndexedNode<ChampSequencedElement<E>> root,
            @NonNull ChampBitmapIndexedNode<ChampSequencedElement<E>> sequenceRoot,
            int size, int first, int last) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        assert (long) last - first >= size : "size=" + size + " first=" + first + " last=" + last;
        this.size = size;
        this.first = first;
        this.last = last;
        this.sequenceRoot = Objects.requireNonNull(sequenceRoot);
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
    @SuppressWarnings({"unchecked"})
    public @NonNull SequencedChampSet<E> addAll(@NonNull Iterable<? extends E> set) {
        if (set == this || isEmpty() && (set instanceof SequencedChampSet<?>)) {
            return (SequencedChampSet<E>) set;
        }
        if (isEmpty() && (set instanceof MutableSequencedChampSet<?> t)) {
            return (SequencedChampSet<E>) t.toImmutable();
        }
        // XXX if the other set is a ChampSet, we should merge the trees
        // See kotlinx collections:
        // https://github.com/Kotlin/kotlinx.collections.immutable/blob/d7b83a13fed459c032dab1b4665eda20a04c740f/core/commonMain/src/implementations/immutableSet/TrieNode.kt#L338
        var t = toMutable();
        return t.addAll(set) ? t.toImmutable() : this;
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
        return find(new ChampSequencedElement<>(key), Objects.hashCode(key), 0, Objects::equals) != ChampNode.NO_DATA;
    }

    private @NonNull SequencedChampSet<E> addFirst(@Nullable E e,
                                                   boolean moveToFirst) {
        var details = new ChampChangeEvent<ChampSequencedElement<E>>();
        var newElem = new ChampSequencedElement<>(e, first);
        var newRoot = update(null, newElem,
                Objects.hashCode(e), 0, details,
                moveToFirst ? ChampSequencedElement::updateAndMoveToFirst : ChampSequencedElement::update,
                Objects::equals, Objects::hashCode);
        if (details.isModified()) {
            var newSeqRoot = sequenceRoot;
            int newSize = size;
            int newFirst = first;
            int newLast = last;
            IdentityObject mutator = new IdentityObject();
            if (details.isReplaced()) {
                if (moveToFirst) {
                    var oldElem = details.getOldDataNonNull();
                    newSeqRoot = ChampSequencedData.seqRemove(newSeqRoot, mutator, oldElem, details);
                    newLast = oldElem.getSequenceNumber() == newLast - 1 ? newLast - 1 : newLast;
                    newFirst--;
                }
            } else {
                newFirst--;
                newSize++;
            }
            newSeqRoot = ChampSequencedData.seqUpdate(newSeqRoot, mutator, details.getNewDataNonNull(), details, ChampSequencedElement::update);
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    private @NonNull SequencedChampSet<E> addLast(@Nullable E e,
                                                  boolean moveToLast) {
        var details = new ChampChangeEvent<ChampSequencedElement<E>>();
        var newElem = new ChampSequencedElement<E>(e, last);
        var newRoot = update(
                null, newElem, Objects.hashCode(e), 0,
                details,
                moveToLast ? ChampSequencedElement::updateAndMoveToLast : ChampSequencedElement::update,
                Objects::equals, Objects::hashCode);
        if (details.isModified()) {
            var newSeqRoot = sequenceRoot;
            int newFirst = first;
            int newLast = last;
            int newSize = size;
            var mutator = new IdentityObject();
            if (details.isReplaced()) {
                if (moveToLast) {
                    var oldElem = details.getOldDataNonNull();
                    newSeqRoot = ChampSequencedData.seqRemove(newSeqRoot, mutator, oldElem, details);
                    newFirst = oldElem.getSequenceNumber() == newFirst + 1 ? newFirst + 1 : newFirst;
                    newLast++;
                }
            } else {
                newSize++;
                newLast++;
            }
            newSeqRoot = ChampSequencedData.seqUpdate(newSeqRoot, mutator, details.getNewDataNonNull(), details, ChampSequencedElement::update);
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    private @NonNull SequencedChampSet<E> remove(@Nullable E key, int newFirst, int newLast) {
        int keyHash = Objects.hashCode(key);
        var details = new ChampChangeEvent<ChampSequencedElement<E>>();
        ChampBitmapIndexedNode<ChampSequencedElement<E>> newRoot = remove(null,
                new ChampSequencedElement<>(key),
                keyHash, 0, details, Objects::equals);
        ChampBitmapIndexedNode<ChampSequencedElement<E>> newSeqRoot = sequenceRoot;
        if (details.isModified()) {
            var oldElem = details.getOldData();
            newSeqRoot = ChampSequencedData.seqRemove(newSeqRoot, null, oldElem, details);
            int seq = oldElem.getSequenceNumber();
            return renumber(newRoot, newSeqRoot, size - 1,
                    (seq == newFirst) ? newFirst + 1 : newFirst,
                    (seq == newLast - 1) ? newLast - 1 : newLast);
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
        return ChampNode.getFirst(sequenceRoot).getElement();
    }

    @Override
    public E getLast() {
        return ChampNode.getLast(sequenceRoot).getElement();
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
        return new ChampReversedChampSpliterator<>(sequenceRoot, ChampSequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
    }

    @Override
    public EnumeratorSpliterator<E> spliterator() {
        return new ChampSpliterator<>(sequenceRoot, ChampSequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
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

    @Override
    public @NonNull SequencedChampSet<E> removeAll(@NonNull Iterable<?> set) {
        if (set == this) {
            return of();
        }
        if (isEmpty()
                || (set instanceof Collection<?> c) && c.isEmpty()
                || (set instanceof ReadOnlyCollection<?> rc) && rc.isEmpty()) {
            return this;
        }
        var t = toMutable();
        return t.removeAll(set) ? t.toImmutable() : this;
    }

    @Override
    public SequencedChampSet<E> removeFirst() {
        ChampSequencedElement<E> k = ChampNode.getFirst(sequenceRoot);
        return remove(k.getElement(), k.getSequenceNumber() + 1, last);
    }

    @Override
    public SequencedChampSet<E> removeLast() {
        ChampSequencedElement<E> k = ChampNode.getLast(sequenceRoot);
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
            ChampBitmapIndexedNode<ChampSequencedElement<E>> root,
            ChampBitmapIndexedNode<ChampSequencedElement<E>> seqRoot,
            int size, int first, int last) {
        if (mustRenumber(size, first, last)) {
            var mutator = new IdentityObject();
            var renumberedRoot = ChampSequencedData.renumber(
                    size, root, seqRoot, mutator, Objects::hashCode, Objects::equals,
                    (e, seq) -> new ChampSequencedElement<>(e.getElement(), seq));
            var renumberedSeqRoot = ChampSequencedData.buildSequencedTrie(renumberedRoot, mutator);
            return new SequencedChampSet<>(
                    renumberedRoot, renumberedSeqRoot,
                    size, -1, size);
        }
        return new SequencedChampSet<>(root, seqRoot, size, first, last);
    }

    @Override
    public @NonNull SequencedChampSet<E> retainAll(@NonNull Collection<?> set) {
        if (isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }
        var t = toMutable();
        return t.retainAll(set) ? t.toImmutable() : this;
    }

    public @NonNull Iterator<E> reversedIterator() {
        return new IteratorFacade<>(reversedSpliterator(), null);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public @NonNull MutableSequencedChampSet<E> toMutable() {
        return new MutableSequencedChampSet<>(this);
    }


    @Override
    public @NonNull String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(toMutable());
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
            return SequencedChampSet.copyOf(deserialized);
        }
    }
}