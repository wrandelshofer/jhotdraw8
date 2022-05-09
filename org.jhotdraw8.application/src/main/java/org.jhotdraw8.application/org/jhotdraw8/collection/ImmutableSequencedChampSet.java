/*
 * @(#)ImmutableSeqChampSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.ChampTrie;
import org.jhotdraw8.collection.champ.ChampTrieGraphviz;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.Node;
import org.jhotdraw8.collection.champ.SequencedEntryIterator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


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
 *     <li>iterator.next(): O(log n)</li>
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
 * This set can create a mutable copy of itself in O(1) time and O(0) space
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
public class ImmutableSequencedChampSet<E> extends BitmapIndexedNode<E, Void> implements ImmutableSet<E>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int ENTRY_LENGTH = 2;
    @SuppressWarnings("unchecked")
    private static final ImmutableSequencedChampSet<?> EMPTY_SET = new ImmutableSequencedChampSet<>(BitmapIndexedNode.emptyNode(), 0, 0, 0);

    final int size;

    /**
     * Counter for the sequence number of the last element. The counter is
     * incremented when a new entry is added to the end of the sequence.
     * <p>
     * The counter is in the range from {@code 0} to
     * {@link Integer#MAX_VALUE} - 1.
     * When the counter reaches {@link Integer#MAX_VALUE}, all
     * sequence numbers are renumbered, and the counter is reset to
     * {@code size}.
     */
    private final int last;


    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement before a new entry is added to the start of the sequence.
     * <p>
     * The counter is in the range from {@code 0} to
     * {@link Integer#MIN_VALUE}.
     * When the counter is about to wrap over to {@link Integer#MAX_VALUE}, all
     * sequence numbers are renumbered, and the counter is reset to
     * {@code 0}.
     */
    private int first = 0;

    ImmutableSequencedChampSet(BitmapIndexedNode<E, Void> root, int size, int first, int last) {
        super(root.nodeMap(), root.dataMap(), root.mixed, ENTRY_LENGTH);
        this.size = size;
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
        return ((ImmutableSequencedChampSet<E>) ImmutableSequencedChampSet.EMPTY_SET);
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
    public static <E> @NonNull ImmutableSequencedChampSet<E> of(E... elements) {
        if (elements.length == 0) {
            return (ImmutableSequencedChampSet<E>) ImmutableSequencedChampSet.EMPTY_SET;
        } else {
            return ((ImmutableSequencedChampSet<E>) ImmutableSequencedChampSet.EMPTY_SET).copyAddAll(Arrays.asList(elements));
        }
    }

    @NonNull
    private ImmutableSequencedChampSet<E> renumber(BitmapIndexedNode<E, Void> newRootNode) {
        newRootNode = ChampTrie.renumber(size, newRootNode, new UniqueId(), ENTRY_LENGTH);
        return new ImmutableSequencedChampSet<E>(newRootNode, size + 1, first, size);
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return findByKey(key, Objects.hashCode(key), 0, ENTRY_LENGTH, ENTRY_LENGTH) != Node.NO_VALUE;
    }

    @Override
    public @NonNull ImmutableSequencedChampSet<E> copyAdd(final @NonNull E key) {
        return copyAddLastIfAbsent(key);
    }

    public @NonNull ImmutableSequencedChampSet<E> copyAddFirst(final @NonNull E key) {
        return copyRemove(key).copyAddFirstIfAbsent(key);
    }

    public @NonNull ImmutableSequencedChampSet<E> copyAddLast(final @NonNull E key) {
        return copyRemove(key).copyAddLastIfAbsent(key);
    }

    private @NonNull ImmutableSequencedChampSet<E> copyAddLastIfAbsent(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<Void> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<E, Void> newRootNode = update(null, key, null, keyHash, 0, changeEvent,
                ENTRY_LENGTH, last, ENTRY_LENGTH - 1);
        if (changeEvent.isModified) {
            if (last + 1 == Node.NO_SEQUENCE_NUMBER) {
                return new ImmutableSequencedChampSet<>(renumber(newRootNode), size + 1, 0, size + 1);
            } else {
                return new ImmutableSequencedChampSet<>(newRootNode, size + 1, first, last + 1);
            }
        }

        return this;
    }

    private @NonNull ImmutableSequencedChampSet<E> copyAddFirstIfAbsent(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<Void> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<E, Void> newRootNode = update(null, key, null, keyHash, 0, changeEvent,
                ENTRY_LENGTH, last, ENTRY_LENGTH - 1);
        if (changeEvent.isModified) {
            if (last + 1 == Node.NO_SEQUENCE_NUMBER) {
                return new ImmutableSequencedChampSet<>(renumber(newRootNode), size + 1, 0, size + 1);
            } else {
                return new ImmutableSequencedChampSet<>(newRootNode, size + 1, first - 1, last);
            }
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ImmutableSequencedChampSet<E> copyAddAll(final @NonNull Iterable<? extends E> set) {
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

    @Override
    public @NonNull ImmutableSet<E> copyClear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull ImmutableSequencedChampSet<E> copyRemove(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<Void> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<E, Void> newRootNode = remove(null, key,
                keyHash, 0, changeEvent, ENTRY_LENGTH, ENTRY_LENGTH);
        if (changeEvent.isModified) {
            return new ImmutableSequencedChampSet<>(newRootNode, size - 1, first, last);
        }

        return this;
    }

    @Override
    public @NonNull ImmutableSequencedChampSet<E> copyRemoveAll(final @NonNull Iterable<? extends E> set) {
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
        for (final E key : set) {
            if (t.remove(key)) {
                modified = true;
                if (t.isEmpty()) {
                    break;
                }
            }

        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ImmutableSequencedChampSet<E> copyRetainAll(final @NonNull Collection<? extends E> set) {
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
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (other instanceof ImmutableSequencedChampSet) {
            ImmutableSequencedChampSet<?> that = (ImmutableSequencedChampSet<?>) other;
            if (this.size != that.size) {
                return false;
            }
            return this.equivalent(that, ENTRY_LENGTH, ENTRY_LENGTH - 1);
        } else {
            return ReadOnlySet.setEquals(this, other);
        }
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public Iterator<E> iterator() {
        return new MappedIterator<>(new SequencedEntryIterator<E, Void>(size, this, ENTRY_LENGTH, ENTRY_LENGTH - 1, false, null, null), Map.Entry::getKey);
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

    /**
     * Dumps the internal structure of this set in the Graphviz DOT Language.
     *
     * @return a dump of the internal structure
     */
    public String dump() {
        return new ChampTrieGraphviz<E, Void>().dumpTrie(this, ENTRY_LENGTH, false, true);
    }

}