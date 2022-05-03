/*
 * @(#)PersistentTrieSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champtrie.BitmapIndexedNode;
import org.jhotdraw8.collection.champtrie.ChampTrie;
import org.jhotdraw8.collection.champtrie.ChampTrieGraphviz;
import org.jhotdraw8.collection.champtrie.ChangeEvent;
import org.jhotdraw8.collection.champtrie.Node;
import org.jhotdraw8.collection.champtrie.SequencedTrieIterator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;


/**
 * Implements a persistent set using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
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
 *     <li>toMutable: O(log n) distributed across subsequent updates</li>
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
public class PersistentSequencedTrieSet<E> extends BitmapIndexedNode<E, Void> implements PersistentSet<E>, ImmutableSet<E>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int TUPLE_LENGTH = 2;
    @SuppressWarnings("unchecked")
    private static final PersistentSequencedTrieSet<?> EMPTY_SET = new PersistentSequencedTrieSet<>(BitmapIndexedNode.emptyNode(), 0, 0);

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
    private int lastSequenceNumber;

    PersistentSequencedTrieSet(BitmapIndexedNode<E, Void> root, int size, int lastSequenceNumber) {
        super(root.nodeMap(), root.dataMap(), root.nodes, TUPLE_LENGTH);
        this.size = size;
        this.lastSequenceNumber = lastSequenceNumber;
    }

    @SuppressWarnings("unchecked")
    public static <K> @NonNull PersistentSequencedTrieSet<K> copyOf(@NonNull Iterable<? extends K> set) {
        if (set instanceof PersistentSequencedTrieSet) {
            return (PersistentSequencedTrieSet<K>) set;
        } else if (set instanceof SequencedTrieSet) {
            return ((SequencedTrieSet<K>) set).toPersistent();
        }
        SequencedTrieSet<K> tr = new SequencedTrieSet<>(of());
        tr.addAll(set);
        return tr.toPersistent();
    }


    @SafeVarargs
    public static <K> @NonNull PersistentSequencedTrieSet<K> of(@NonNull K... keys) {
        return PersistentSequencedTrieSet.<K>of().copyAddAll(Arrays.asList(keys));
    }

    @SuppressWarnings("unchecked")
    public static <K> @NonNull PersistentSequencedTrieSet<K> of() {
        return (PersistentSequencedTrieSet<K>) PersistentSequencedTrieSet.EMPTY_SET;
    }

    @NonNull
    private PersistentSequencedTrieSet<E> renumber(BitmapIndexedNode<E, Void> newRootNode) {
        newRootNode = ChampTrie.renumber(size, newRootNode, new UniqueIdentity(), TUPLE_LENGTH);
        return new PersistentSequencedTrieSet<E>(newRootNode, size + 1, size);
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return findByKey(key, Objects.hashCode(key), 0, TUPLE_LENGTH) != Node.NO_VALUE;
    }

    public @NonNull PersistentSequencedTrieSet<E> copyAdd(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<Void> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<E, Void> newRootNode = update(null, key, null, keyHash, 0, changeEvent, TUPLE_LENGTH, lastSequenceNumber);
        if (changeEvent.isModified) {
            if (lastSequenceNumber + 1 == lastSequenceNumber) {
                return new PersistentSequencedTrieSet<>(renumber(newRootNode), size + 1, size + 1);

            } else {
                return new PersistentSequencedTrieSet<>(newRootNode, size + 1, lastSequenceNumber + 1);
            }
        }

        return this;
    }

    public @NonNull PersistentSequencedTrieSet<E> copyAddAll(final @NonNull Iterable<? extends E> set) {
        if (set == this
                || (set instanceof Collection) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }

        final SequencedTrieSet<E> t = this.toMutable();
        boolean modified = false;
        for (final E key : set) {
            modified |= t.add(key);
        }
        return modified ? t.toPersistent() : this;
    }

    @Override
    public @NonNull PersistentSet<E> copyClear(@NonNull E element) {
        return isEmpty() ? this : of();
    }

    public @NonNull PersistentSequencedTrieSet<E> copyRemove(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<Void> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<E, Void> newRootNode = remove(null, key,
                keyHash, 0, changeEvent, TUPLE_LENGTH);
        if (changeEvent.isModified) {
            return new PersistentSequencedTrieSet<>(newRootNode, size - 1, lastSequenceNumber);
        }

        return this;
    }

    public @NonNull PersistentSequencedTrieSet<E> copyRemoveAll(final @NonNull Iterable<? extends E> set) {
        if (this.isEmpty()
                || (set instanceof Collection) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }
        if (set == this) {
            return of();
        }
        final SequencedTrieSet<E> t = this.toMutable();
        boolean modified = false;
        for (final E key : set) {
            if (t.remove(key)) {
                modified = true;
                if (t.isEmpty()) {
                    break;
                }
            }

        }
        return modified ? t.toPersistent() : this;
    }

    public @NonNull PersistentSequencedTrieSet<E> copyRetainAll(final @NonNull Collection<? extends E> set) {
        if (this.isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }

        final SequencedTrieSet<E> t = this.toMutable();
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
        return modified ? t.toPersistent() : this;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (other instanceof PersistentSequencedTrieSet) {
            PersistentSequencedTrieSet<?> that = (PersistentSequencedTrieSet<?>) other;
            if (this.size != that.size) {
                return false;
            }
            return this.equivalent(that, TUPLE_LENGTH, true);
        } else if (other instanceof ReadOnlySet) {
            @SuppressWarnings("unchecked")
            ReadOnlySet<E> that = (ReadOnlySet<E>) other;
            if (this.size() != that.size()) {
                return false;
            }
            return containsAll(that);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public Iterator<E> iterator() {
        return new ElementIterator<E>(size, this, TUPLE_LENGTH);
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Returns a copy of this set that is mutable.
     * <p>
     * This operation is performed in O(1) because the mutable set shares
     * the underlying trie nodes with this set.
     * <p>
     * Initially, the returned mutable set hasn't exclusive ownership of any
     * trie node. Therefore, the first few updates that it performs, are
     * copy-on-write operations, until it exclusively owns some trie nodes that
     * it can update.
     *
     * @return a mutable trie set
     */
    private @NonNull SequencedTrieSet<E> toMutable() {
        return new SequencedTrieSet<>(this);
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
        return new ChampTrieGraphviz<E, Void>().dumpTrie(this, TUPLE_LENGTH, false, true);
    }

    static class ElementIterator<E> extends SequencedTrieIterator<E, Void>
            implements Iterator<E> {

        public ElementIterator(int size, Node<E, Void> rootNode, int entryLength) {
            super(size, rootNode, entryLength);
        }

        @Override
        public E next() {
            return nextEntry().getKey();
        }
    }
}