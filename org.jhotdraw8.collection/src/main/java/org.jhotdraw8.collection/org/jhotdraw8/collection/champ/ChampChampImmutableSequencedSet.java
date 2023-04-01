/*
 * @(#)ImmutableSequencedChampSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.enumerator.Enumerator;
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
import java.util.function.BiFunction;


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
public class ChampChampImmutableSequencedSet<E>
        extends BitmapIndexedNode<SequencedElement<E>>
        implements Serializable, ImmutableSequencedSet<E> {
    private final static long serialVersionUID = 0L;

    private static final @NonNull ChampChampImmutableSequencedSet<?> EMPTY = new ChampChampImmutableSequencedSet<>(
            BitmapIndexedNode.emptyNode(), BitmapIndexedNode.emptyNode(), 0, -1, 0);

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

    ChampChampImmutableSequencedSet(
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
            seqRoot = seqRoot.update(mutator, elem, ChampChampImmutableSequencedSet.seqHash(elem.getSequenceNumber()),
                    0, details, (oldK, newK) -> oldK, Object::equals, ChampChampImmutableSequencedSet::seqHashCode);
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
    public static <E> @NonNull ChampChampImmutableSequencedSet<E> copyOf(@NonNull Iterable<? extends E> iterable) {
        if (iterable instanceof ChampChampImmutableSequencedSet) {
            return (ChampChampImmutableSequencedSet<E>) iterable;
        } else if (iterable instanceof ChampChampSequencedSet) {
            return ((ChampChampSequencedSet<E>) iterable).toImmutable();
        }
        ChampChampSequencedSet<E> tr = new ChampChampSequencedSet<>(of());
        tr.addAll(iterable);
        return tr.toImmutable();
    }

    /**
     * Returns true if the sequenced elements must be renumbered because
     * {@code first} or {@code last} are at risk of overflowing.
     * <p>
     * {@code first} and {@code last} are estimates of the first and last
     * sequence numbers in the trie. The estimated extent may be larger
     * than the actual extent, but not smaller.
     *
     * @param size  the size of the trie
     * @param first the estimated first sequence number
     * @param last  the estimated last sequence number
     * @return
     */
    static boolean mustRenumber(int size, int first, int last) {
        return size == 0 && (first != -1 || last != 0)
                || last > Integer.MAX_VALUE - 2
                || first < Integer.MIN_VALUE + 2;
    }


    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */

    @SuppressWarnings("unchecked")
    public static <E> @NonNull ChampChampImmutableSequencedSet<E> of() {
        return ((ChampChampImmutableSequencedSet<E>) ChampChampImmutableSequencedSet.EMPTY);
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
    public static <E> @NonNull ChampChampImmutableSequencedSet<E> of(E @NonNull ... elements) {
        if (elements.length == 0) {
            return (ChampChampImmutableSequencedSet<E>) ChampChampImmutableSequencedSet.EMPTY;
        } else {
            return ((ChampChampImmutableSequencedSet<E>) ChampChampImmutableSequencedSet.EMPTY).addAll(Arrays.asList(elements));
        }
    }

    @Override
    public @NonNull ChampChampImmutableSequencedSet<E> add(@Nullable E key) {
        return copyAddLast(key, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ChampChampImmutableSequencedSet<E> addAll(@NonNull Iterable<? extends E> set) {
        if (set == this || isEmpty() && (set instanceof ChampChampImmutableSequencedSet<?>)) {
            return (ChampChampImmutableSequencedSet<E>) set;
        }
        if (isEmpty() && (set instanceof ChampChampSequencedSet)) {
            return ((ChampChampSequencedSet<E>) set).toImmutable();
        }
        ChampChampSequencedSet<E> t = this.toMutable();
        boolean modified = false;
        for (E key : set) {
            modified |= t.add(key);
        }
        return modified ? t.toImmutable() : this;
    }

    public @NonNull ChampChampImmutableSequencedSet<E> addFirst(@Nullable E key) {
        return copyAddFirst(key, true);
    }

    public @NonNull ChampChampImmutableSequencedSet<E> addLast(@Nullable E key) {
        return copyAddLast(key, true);
    }

    @Override
    public @NonNull ChampChampImmutableSequencedSet<E> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return find(new SequencedElement<>(key), Objects.hashCode(key), 0, Objects::equals) != Node.NO_DATA;
    }

    private @NonNull ChampChampImmutableSequencedSet<E> copyAddFirst(@Nullable E e,
                                                                     boolean moveToFirst) {
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        SequencedElement<E> newElem = new SequencedElement<>(e, first - 1);
        var newRoot = update(null, newElem,
                Objects.hashCode(e), 0, details,
                moveToFirst ? getUpdateAndMoveToFirstFunction() : getUpdateFunction(),
                Objects::equals, Objects::hashCode);
        var newSeqRoot = sequenceRoot;
        int newSize = size;
        int newFirst = first;
        int newLast = last;
        if (details.isModified()) {
            IdentityObject mutator = new IdentityObject();
            SequencedElement<E> oldElem = details.getData();
            boolean isUpdated = details.isUpdated();
            newSeqRoot = newSeqRoot.update(mutator,
                    newElem, seqHash(first - 1), 0, details,
                    getUpdateFunction(),
                    Objects::equals, ChampChampImmutableSequencedSet::seqHashCode);
            if (isUpdated) {
                newSeqRoot = newSeqRoot.remove(mutator,
                        oldElem, seqHash(oldElem.getSequenceNumber()), 0, details,
                        Objects::equals);

                newFirst = details.getData().getSequenceNumber() == newFirst ? newFirst : newFirst - 1;
                newLast = details.getData().getSequenceNumber() == newLast ? newLast - 1 : newLast;
            } else {
                newFirst--;
                newSize++;
            }
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    private @NonNull ChampChampImmutableSequencedSet<E> copyAddLast(@Nullable E e,
                                                                    boolean moveToLast) {
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        SequencedElement<E> newElem = new SequencedElement<>(e, last);
        var newRoot = update(
                null, newElem, Objects.hashCode(e), 0,
                details,
                moveToLast ? getUpdateAndMoveToLastFunction() : getUpdateFunction(),
                Objects::equals, Objects::hashCode);
        var newSeqRoot = sequenceRoot;
        int newFirst = first;
        int newLast = last;
        int newSize = size;
        if (details.isModified()) {
            IdentityObject mutator = new IdentityObject();
            SequencedElement<E> oldElem = details.getData();
            boolean isUpdated = details.isUpdated();
            newSeqRoot = newSeqRoot.update(mutator,
                    newElem, seqHash(last), 0, details,
                    getUpdateFunction(),
                    Objects::equals, ChampChampImmutableSequencedSet::seqHashCode);
            if (isUpdated) {
                newSeqRoot = newSeqRoot.remove(mutator,
                        oldElem, seqHash(oldElem.getSequenceNumber()), 0, details,
                        Objects::equals);

                newFirst = details.getData().getSequenceNumber() == newFirst - 1 ? newFirst - 1 : newFirst;
                newLast = details.getData().getSequenceNumber() == newLast ? newLast : newLast + 1;
            } else {
                newSize++;
                newLast++;
            }
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    private @NonNull ChampChampImmutableSequencedSet<E> copyRemove(@Nullable E key, int newFirst, int newLast) {
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
                    seqHash(seq), 0, details, Objects::equals);
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

        if (other instanceof ChampChampImmutableSequencedSet) {
            ChampChampImmutableSequencedSet<?> that = (ChampChampImmutableSequencedSet<?>) other;
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
    private BiFunction<SequencedElement<E>, SequencedElement<E>, SequencedElement<E>> getUpdateAndMoveToFirstFunction() {
        return (oldK, newK) -> oldK.getSequenceNumber() == newK.getSequenceNumber() + 1 ? oldK : newK;
    }

    @NonNull
    private BiFunction<SequencedElement<E>, SequencedElement<E>, SequencedElement<E>> getUpdateAndMoveToLastFunction() {
        return (oldK, newK) -> oldK.getSequenceNumber() == newK.getSequenceNumber() - 1 ? oldK : newK;
    }

    @NonNull
    private BiFunction<SequencedElement<E>, SequencedElement<E>, SequencedElement<E>> getUpdateFunction() {
        return (oldK, newK) -> oldK;
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return iterator(false);
    }

    private @NonNull Iterator<E> iterator(boolean reversed) {
        Enumerator<E> i;
        if (reversed) {
            i = new ReversedKeyEnumeratorSpliterator<>(sequenceRoot, SequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
        } else {
            i = new KeyEnumeratorSpliterator<>(sequenceRoot, SequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
        }
        return new IteratorFacade<>(i, null);
    }

    private @NonNull Spliterator<E> spliterator(boolean reversed) {
        Spliterator<E> i;
        if (reversed) {
            i = new ReversedKeyEnumeratorSpliterator<>(sequenceRoot, SequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
        } else {
            i = new KeyEnumeratorSpliterator<>(sequenceRoot, SequencedElement::getElement, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
        }
        return i;
    }

    @Override
    public Spliterator<E> spliterator() {
        return spliterator(false);
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
    public @NonNull ChampChampImmutableSequencedSet<E> remove(@Nullable E key) {
        return copyRemove(key, first, last);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampChampImmutableSequencedSet<E> removeAll(@NonNull Iterable<?> set) {
        if (this.isEmpty()
                || (set instanceof Collection) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }
        if (set == this) {
            return of();
        }
        ChampChampSequencedSet<E> t = this.toMutable();
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
    public ChampChampImmutableSequencedSet<E> removeFirst() {
        SequencedElement<E> k = HeapSequencedIterator.getFirst(this, first, last);
        return copyRemove(k.getElement(), k.getSequenceNumber() + 1, last);
    }

    @Override
    public ChampChampImmutableSequencedSet<E> removeLast() {
        SequencedElement<E> k = HeapSequencedIterator.getLast(this, first, last);
        return copyRemove(k.getElement(), first, k.getSequenceNumber());
    }


    /**
     * Renumbers the sequenced elements in the trie if necessary.
     *
     * @param root    the root of the trie
     * @param seqRoot
     * @param size    the size of the trie
     * @param first   the estimated first sequence number
     * @param last    the estimated last sequence number
     * @return a new {@link ChampChampImmutableSequencedSet} instance
     */

    @NonNull
    private ChampChampImmutableSequencedSet<E> renumber(
            BitmapIndexedNode<SequencedElement<E>> root,
            BitmapIndexedNode<SequencedElement<E>> seqRoot,
            int size, int first, int last) {
        if (mustRenumber(size, first, last)) {
            IdentityObject mutator = new IdentityObject();
            BitmapIndexedNode<SequencedElement<E>> renumberedRoot = SequencedElement.renumber(size, root, mutator, Objects::hashCode, Objects::equals);
            BitmapIndexedNode<SequencedElement<E>> renumberedSeqRoot = buildSequenceRoot(renumberedRoot, mutator);
            return new ChampChampImmutableSequencedSet<>(
                    renumberedRoot, renumberedSeqRoot,
                    size, -1, size);
        }
        return new ChampChampImmutableSequencedSet<>(root, seqRoot, size, first, last);
    }

    @Override
    public @NonNull ChampChampImmutableSequencedSet<E> retainAll(@NonNull Collection<?> set) {
        if (this.isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }

        ChampChampSequencedSet<E> t = this.toMutable();
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
        return iterator(true);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public @NonNull ChampChampSequencedSet<E> toMutable() {
        return new ChampChampSequencedSet<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<E>(toMutable());
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(Set<E> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return ChampChampImmutableSequencedSet.copyOf(deserialized);
        }
    }


    /**
     * Computes a hash code from the sequence number, so that we can
     * use it for iteration in a CHAMP trie.
     * <p>
     * Convert the sequence number to unsigned 32 by adding Integer.MIN_VALUE.
     * Then reorders its bits from 66666555554444433333222221111100 to
     * 00111112222233333444445555566666.
     *
     * @param sequenceNumber a sequence number
     * @return a hash code
     */
    static int seqHash(int sequenceNumber) {
        int u = sequenceNumber + Integer.MIN_VALUE;
        return (u >>> 27)
                | ((u & 0b00000_11111_00000_00000_00000_00000_00) >>> 17)
                | ((u & 0b00000_00000_11111_00000_00000_00000_00) >>> 7)
                | ((u & 0b00000_00000_00000_11111_00000_00000_00) << 3)
                | ((u & 0b00000_00000_00000_00000_11111_00000_00) << 13)
                | ((u & 0b00000_00000_00000_00000_00000_11111_00) << 23)
                | ((u & 0b00000_00000_00000_00000_00000_00000_11) << 30);
    }

    static <E> int seqHashCode(SequencedElement<E> e) {
        return seqHash(e.getSequenceNumber());
    }
}