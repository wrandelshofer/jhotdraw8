/*
 * @(#)ImmutableSequencedChampSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.BucketSequencedIterator;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.HeapSequencedIterator;
import org.jhotdraw8.collection.champ.Node;
import org.jhotdraw8.collection.champ.Sequenced;
import org.jhotdraw8.collection.champ.SequencedElement;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;


/**
 * Implements an immutable set using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP), with predictable iteration order.
 * <p>
 * Features:
 * <ul>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>iterates in the order, in which elements were inserted</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>copyAdd: O(1) amortized</li>
 *     <li>copyRemove: O(1)</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(1) + a cost distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(N)</li>
 *     <li>iterator.next: O(1) with bucket sort or O(log N) with a heap</li>
 *     <li>getFirst(), getLast(): O(N)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This set performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP tree contains nodes that may be shared with other sets.
 * <p>
 * If a write operation is performed on a node, then this set creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP tree has a fixed maximal height, the cost is O(1).
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
public class ImmutableSequencedChampSet<E>
        extends BitmapIndexedNode<SequencedElement<E>>
        implements Serializable, ImmutableSequencedSet<E> {
    private final static long serialVersionUID = 0L;
    private static final ImmutableSequencedChampSet<?> EMPTY = new ImmutableSequencedChampSet<>(BitmapIndexedNode.emptyNode(), 0, -1, 0);

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

    ImmutableSequencedChampSet(@NonNull BitmapIndexedNode<SequencedElement<E>> root, int size, int first, int last) {
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
    public static <E> @NonNull ImmutableSequencedChampSet<E> copyOf(@NonNull Iterable<? extends E> iterable) {
        if (iterable instanceof ImmutableSequencedChampSet) {
            return (ImmutableSequencedChampSet<E>) iterable;
        } else if (iterable instanceof SequencedChampSet) {
            return ((SequencedChampSet<E>) iterable).toImmutable();
        }
        SequencedChampSet<E> tr = new SequencedChampSet<>(of());
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
    public static <E> @NonNull ImmutableSequencedChampSet<E> of() {
        return ((ImmutableSequencedChampSet<E>) ImmutableSequencedChampSet.EMPTY);
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
    public static <E> @NonNull ImmutableSequencedChampSet<E> of(E @NonNull ... elements) {
        if (elements.length == 0) {
            return (ImmutableSequencedChampSet<E>) ImmutableSequencedChampSet.EMPTY;
        } else {
            return ((ImmutableSequencedChampSet<E>) ImmutableSequencedChampSet.EMPTY).copyAddAll(Arrays.asList(elements));
        }
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return findByKey(new SequencedElement<>(key), Objects.hashCode(key), 0, Objects::equals) != Node.NO_VALUE;
    }

    @Override
    public @NonNull ImmutableSequencedChampSet<E> copyAdd(final @Nullable E key) {
        return copyAddLast(key, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ImmutableSequencedChampSet<E> copyAddAll(@NonNull Iterable<? extends E> set) {
        if (set == this || isEmpty() && (set instanceof ImmutableSequencedChampSet<?>)) {
            return (ImmutableSequencedChampSet<E>) set;
        }
        if (isEmpty() && (set instanceof SequencedChampSet)) {
            return ((SequencedChampSet<E>) set).toImmutable();
        }
        final SequencedChampSet<E> t = this.toMutable();
        boolean modified = false;
        for (final E key : set) {
            modified |= t.add(key);
        }
        return modified ? t.toImmutable() : this;
    }

    public @NonNull ImmutableSequencedChampSet<E> copyAddFirst(final @Nullable E key) {
        return copyAddFirst(key, true);
    }

    private @NonNull ImmutableSequencedChampSet<E> copyAddFirst(@Nullable E key,
                                                                boolean moveToFirst) {
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedElement<E>> root = update(null,
                new SequencedElement<>(key, first - 1), Objects.hashCode(key), 0, details,
                moveToFirst ? getUpdateAndMoveToFirstFunction() : getUpdateFunction(),
                Objects::equals, Objects::hashCode);
        if (details.updated) {
            return moveToFirst
                    ? renumber(root, size,
                    details.getOldValue().getSequenceNumber() == first ? first : first - 1,
                    details.getOldValue().getSequenceNumber() == last ? last - 1 : last)
                    : new ImmutableSequencedChampSet<>(root, size, first, last);
        }
        return details.modified ? renumber(root, size + 1, first - 1, last) : this;
    }

    public @NonNull ImmutableSequencedChampSet<E> copyAddLast(final @Nullable E key) {
        return copyAddLast(key, true);
    }

    private @NonNull ImmutableSequencedChampSet<E> copyAddLast(final @Nullable E key,
                                                               boolean moveToLast) {
        ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedElement<E>> root = update(null,
                new SequencedElement<>(key, last), Objects.hashCode(key), 0, details,
                moveToLast ? getUpdateAndMoveToLastFunction() : getUpdateFunction(),
                Objects::equals, Objects::hashCode);
        if (details.updated) {
            return moveToLast
                    ? renumber(root, size,
                    details.getOldValue().getSequenceNumber() == first ? first + 1 : first,
                    details.getOldValue().getSequenceNumber() == last ? last : last + 1)
                    : new ImmutableSequencedChampSet<>(root, size, first, last);
        }
        return details.modified ? renumber(root, size + 1, first, last + 1) : this;
    }

    @Override
    public @NonNull ImmutableSequencedChampSet<E> copyClear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull ImmutableSequencedChampSet<E> copyRemove(final @Nullable E key) {
        return copyRemove(key, first, last);
    }

    private @NonNull ImmutableSequencedChampSet<E> copyRemove(final @Nullable E key, int newFirst, int newLast) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<SequencedElement<E>> details = new ChangeEvent<>();
        final BitmapIndexedNode<SequencedElement<E>> newRootNode = remove(null,
                new SequencedElement<>(key),
                keyHash, 0, details, Objects::equals);
        if (details.modified) {
            int seq = details.getOldValue().getSequenceNumber();
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

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ImmutableSequencedChampSet<E> copyRemoveAll(final @NonNull Iterable<?> set) {
        if (this.isEmpty()
                || (set instanceof Collection) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }
        if (set == this) {
            return of();
        }
        final SequencedChampSet<E> t = this.toMutable();
        boolean modified = false;
        for (final Object key : set) {
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
    public ImmutableSequencedChampSet<E> copyRemoveFirst() {
        SequencedElement<E> k = HeapSequencedIterator.getFirst(this, first, last);
        return copyRemove(k.getElement(), k.getSequenceNumber() + 1, last);
    }

    @Override
    public ImmutableSequencedChampSet<E> copyRemoveLast() {
        SequencedElement<E> k = HeapSequencedIterator.getLast(this, first, last);
        return copyRemove(k.getElement(), first, k.getSequenceNumber());
    }

    @Override
    public @NonNull ImmutableSequencedChampSet<E> copyRetainAll(final @NonNull Collection<?> set) {
        if (this.isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }

        final SequencedChampSet<E> t = this.toMutable();
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

    @Override
    public boolean equals(final @Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (other instanceof ImmutableSequencedChampSet) {
            ImmutableSequencedChampSet<?> that = (ImmutableSequencedChampSet<?>) other;
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
        return new WrappedReadOnlySequencedSet<>(
                this::reversedIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst
        );
    }

    /**
     * Renumbers the sequenced elements in the trie if necessary.
     *
     * @param root  the root of the trie
     * @param size  the size of the trie
     * @param first the estimated first sequence number
     * @param last  the estimated last sequence number
     * @return a new {@link ImmutableSequencedChampSet} instance
     */
    @NonNull
    private ImmutableSequencedChampSet<E> renumber(BitmapIndexedNode<SequencedElement<E>> root, int size, int first, int last) {
        if (size == 0) {
            return of();
        }
        if (Sequenced.mustRenumber(size, first, last)) {
            return new ImmutableSequencedChampSet<>(
                    SequencedElement.renumber(size, root, new UniqueId(), Objects::hashCode, Objects::equals),
                    size, -1, size);
        }
        return new ImmutableSequencedChampSet<>(root, size, first, last);
    }

    public @NonNull Iterator<E> reversedIterator() {
        return iterator(true);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public @NonNull SequencedChampSet<E> toMutable() {
        return new SequencedChampSet<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<E>(this.toMutable());
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(Set<E> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return ImmutableSequencedChampSet.copyOf(deserialized);
        }
    }
}