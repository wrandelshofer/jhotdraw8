/*
 * @(#)ImmutableSequencedChampSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
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
import java.util.Spliterators;
import java.util.function.BiFunction;


/**
 * Implements an immutable set using a Compressed Hash-Array Mapped Prefix-tree
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
 *     <li>add: O(1) amortized, due to renumbering</li>
 *     <li>remove: O(1) amortized, due to renumbering</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(N)</li>
 *     <li>iterator.next: O(1) with bucket sort, O(log N) with heap sort</li>
 *     <li>getFirst(), getLast(): O(N)</li>
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
 * The renumbering is why the {@code add} is O(1) only in an amortized sense.
 * <p>
 * The iterator of the set is a priority queue, that orders the entries by
 * their stored insertion counter value. This is why {@code iterator.next()}
 * is O(log n).
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
public class ChampImmutableSequencedSet<E>
        extends BitmapIndexedNode<SequencedElement<E>>
        implements Serializable, ImmutableSequencedSet<E> {
    private final static long serialVersionUID = 0L;
    private static final @NonNull ChampImmutableSequencedSet<?> EMPTY = new ChampImmutableSequencedSet<>(BitmapIndexedNode.emptyNode(), 0, -1, 0);

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

    ChampImmutableSequencedSet(@NonNull BitmapIndexedNode<SequencedElement<E>> root, int size, int first, int last) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        assert (long) last - first >= size : "size=" + size + " first=" + first + " last=" + last;
        this.size = size;
        this.first = first;
        this.last = last;
    }

    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param iterable an iterable
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull ChampImmutableSequencedSet<E> copyOf(@NonNull Iterable<? extends E> iterable) {
        if (iterable instanceof ChampImmutableSequencedSet) {
            return (ChampImmutableSequencedSet<E>) iterable;
        } else if (iterable instanceof ChampSequencedSet) {
            return ((ChampSequencedSet<E>) iterable).toImmutable();
        }
        ChampSequencedSet<E> tr = new ChampSequencedSet<>(of());
        tr.addAll(iterable);
        return tr.toImmutable();
    }

    /**
     * Returns true if the sequenced elements must be renumbered because
     * {@code first} or {@code last} are at risk of overflowing, or the
     * extent from {@code first - last} is not densely filled enough for an
     * efficient bucket sort.
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
    public static boolean mustRenumber(int size, int first, int last) {
        long extent = (long) last - first;
        return size == 0 && (first != -1 || last != 0)
                || last > Integer.MAX_VALUE - 2
                || first < Integer.MIN_VALUE + 2
                || extent > 16 && extent > size * 4L;
    }

    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull ChampImmutableSequencedSet<E> of() {
        return ((ChampImmutableSequencedSet<E>) ChampImmutableSequencedSet.EMPTY);
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
    public static <E> @NonNull ChampImmutableSequencedSet<E> of(E @NonNull ... elements) {
        if (elements.length == 0) {
            return (ChampImmutableSequencedSet<E>) ChampImmutableSequencedSet.EMPTY;
        } else {
            return ((ChampImmutableSequencedSet<E>) ChampImmutableSequencedSet.EMPTY).addAll(Arrays.asList(elements));
        }
    }

    @Override
    public @NonNull ChampImmutableSequencedSet<E> add(@Nullable E key) {
        return copyAddLast(key, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ChampImmutableSequencedSet<E> addAll(@NonNull Iterable<? extends E> set) {
        if (set == this || isEmpty() && (set instanceof ChampImmutableSequencedSet<?>)) {
            return (ChampImmutableSequencedSet<E>) set;
        }
        if (isEmpty() && (set instanceof ChampSequencedSet)) {
            return ((ChampSequencedSet<E>) set).toImmutable();
        }
        ChampSequencedSet<E> t = this.toMutable();
        boolean modified = false;
        for (E key : set) {
            modified |= t.add(key);
        }
        return modified ? t.toImmutable() : this;
    }

    public @NonNull ChampImmutableSequencedSet<E> addFirst(@Nullable E key) {
        return copyAddFirst(key, true);
    }

    public @NonNull ChampImmutableSequencedSet<E> addLast(@Nullable E key) {
        return copyAddLast(key, true);
    }

    @Override
    public @NonNull ChampImmutableSequencedSet<E> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return find(new SequencedElement<>(key), Objects.hashCode(key), 0, Objects::equals) != Node.NO_DATA;
    }

    private @NonNull ChampImmutableSequencedSet<E> copyAddFirst(@Nullable E key,
                                                                boolean moveToFirst) {
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedElement<E>> root = update(null,
                new SequencedElement<>(key, first - 1), Objects.hashCode(key), 0, details,
                moveToFirst ? getUpdateAndMoveToFirstFunction() : getUpdateFunction(),
                Objects::equals, Objects::hashCode);
        if (details.isReplaced()) {
            return moveToFirst
                    ? renumber(root, size,
                    details.getData().getSequenceNumber() == first ? first : first - 1,
                    details.getData().getSequenceNumber() == last ? last - 1 : last)
                    : new ChampImmutableSequencedSet<>(root, size, first, last);
        }
        return details.isModified() ? renumber(root, size + 1, first - 1, last) : this;
    }

    private @NonNull ChampImmutableSequencedSet<E> copyAddLast(@Nullable E key,
                                                               boolean moveToLast) {
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedElement<E>> root = update(null,
                new SequencedElement<>(key, last), Objects.hashCode(key), 0, details,
                moveToLast ? getUpdateAndMoveToLastFunction() : getUpdateFunction(),
                Objects::equals, Objects::hashCode);
        if (details.isReplaced()) {
            return moveToLast
                    ? renumber(root, size,
                    details.getData().getSequenceNumber() == first ? first + 1 : first,
                    details.getData().getSequenceNumber() == last ? last : last + 1)
                    : new ChampImmutableSequencedSet<>(root, size, first, last);
        }
        return details.isModified() ? renumber(root, size + 1, first, last + 1) : this;
    }

    private @NonNull ChampImmutableSequencedSet<E> copyRemove(@Nullable E key, int newFirst, int newLast) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedElement<E>> newRootNode = remove(null,
                new SequencedElement<>(key),
                keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            int seq = details.getData().getSequenceNumber();
            if (seq == newFirst) {
                newFirst++;
            }

            if (seq == newLast - 1) {
                newLast--;
            }
            return renumber(newRootNode, size - 1, newFirst, newLast);
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

        if (other instanceof ChampImmutableSequencedSet) {
            ChampImmutableSequencedSet<?> that = (ChampImmutableSequencedSet<?>) other;
            return size == that.size && equivalent(that);
        } else {
            return ReadOnlySet.setEquals(this, other);
        }
    }

    @Override
    public E getFirst() {
        return HeapSequencedIterator.getFirst(this, first, last).getElement();
    }

    @Override
    public E getLast() {
        return HeapSequencedIterator.getLast(this, first, last).getElement();
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

    public @NonNull Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(false), size, Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.DISTINCT);
    }

    /**
     * Returns an iterator over the elements of this set, that optionally
     * iterates in reversed direction.
     *
     * @param reversed whether to iterate in reverse direction
     * @return an iterator
     */
    public @NonNull Iterator<E> iterator(boolean reversed) {
        return BucketSequencedIterator.isSupported(size, first, last)
                ? new BucketSequencedIterator<>(size, first, last, this, reversed,
                null, SequencedElement::getElement)
                : new HeapSequencedIterator<>(size, this, reversed,
                null, SequencedElement::getElement);
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
    public @NonNull ChampImmutableSequencedSet<E> remove(@Nullable E key) {
        return copyRemove(key, first, last);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampImmutableSequencedSet<E> removeAll(@NonNull Iterable<?> set) {
        if (this.isEmpty()
                || (set instanceof Collection) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }
        if (set == this) {
            return of();
        }
        ChampSequencedSet<E> t = this.toMutable();
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
    public ChampImmutableSequencedSet<E> removeFirst() {
        SequencedElement<E> k = HeapSequencedIterator.getFirst(this, first, last);
        return copyRemove(k.getElement(), k.getSequenceNumber() + 1, last);
    }

    @Override
    public ChampImmutableSequencedSet<E> removeLast() {
        SequencedElement<E> k = HeapSequencedIterator.getLast(this, first, last);
        return copyRemove(k.getElement(), first, k.getSequenceNumber());
    }

    /**
     * Renumbers the sequenced elements in the trie if necessary.
     *
     * @param root  the root of the trie
     * @param size  the size of the trie
     * @param first the estimated first sequence number
     * @param last  the estimated last sequence number
     * @return a new {@link ChampImmutableSequencedSet} instance
     */
    @NonNull
    private ChampImmutableSequencedSet<E> renumber(BitmapIndexedNode<SequencedElement<E>> root, int size, int first, int last) {
        if (mustRenumber(size, first, last)) {
            return new ChampImmutableSequencedSet<>(
                    SequencedElement.renumber(size, root, new IdentityObject(), Objects::hashCode, Objects::equals),
                    size, -1, size);
        }
        return new ChampImmutableSequencedSet<>(root, size, first, last);
    }

    @Override
    public @NonNull ChampImmutableSequencedSet<E> retainAll(@NonNull Collection<?> set) {
        if (this.isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }

        ChampSequencedSet<E> t = this.toMutable();
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
    public @NonNull ChampSequencedSet<E> toMutable() {
        return new ChampSequencedSet<>(this);
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
            return ChampImmutableSequencedSet.copyOf(deserialized);
        }
    }
}