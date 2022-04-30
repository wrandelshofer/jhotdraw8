/*
 * @(#)PersistentTrieSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ChampTrieHelper.BitmapIndexedNode;
import org.jhotdraw8.collection.ChampTrieHelper.BulkChangeEvent;
import org.jhotdraw8.collection.ChampTrieHelper.ChangeEvent;
import org.jhotdraw8.collection.ChampTrieHelper.KeyIterator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.ChampTrieHelper.EMPTY_NODE;

/**
 * Implements a persistent set using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>copyAdd: O(1)</li>
 *     <li>copyRemove: O(1)</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(log n) distributed across subsequent updates</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
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
public class PersistentTrieSet<E> extends BitmapIndexedNode<E, Void> implements PersistentSet<E>, ImmutableSet<E>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int TUPLE_LENGTH = 1;
    @SuppressWarnings("unchecked")
    private static final PersistentTrieSet<?> EMPTY_SET = new PersistentTrieSet<>((BitmapIndexedNode<Object, Void>) EMPTY_NODE, 0);

    final int size;
    private final ToIntFunction<E> hashFunction = Objects::hashCode;

    PersistentTrieSet(BitmapIndexedNode<E, Void> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.nodes, TUPLE_LENGTH);
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public static <K> @NonNull PersistentTrieSet<K> copyOf(@NonNull Iterable<? extends K> set) {
        if (set instanceof PersistentTrieSet) {
            return (PersistentTrieSet<K>) set;
        } else if (set instanceof TrieSet) {
            return ((TrieSet<K>) set).toPersistent();
        }
        TrieSet<K> tr = new TrieSet<>(of());
        tr.addAll(set);
        return tr.toPersistent();
    }


    @SafeVarargs
    public static <K> @NonNull PersistentTrieSet<K> of(@NonNull K... keys) {
        return PersistentTrieSet.<K>of().copyAddAll(Arrays.asList(keys));
    }

    @SuppressWarnings("unchecked")
    public static <K> @NonNull PersistentTrieSet<K> of() {
        return (PersistentTrieSet<K>) PersistentTrieSet.EMPTY_SET;
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return containsKey(key, hashFunction.applyAsInt(key), 0, TUPLE_LENGTH);
    }

    public @NonNull PersistentTrieSet<E> copyAdd(final @NonNull E key) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<Void> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<E, Void> newRootNode = updated(null, key, null, keyHash, 0, changeEvent, TUPLE_LENGTH, hashFunction, ChampTrieHelper.TUPLE_VALUE);
        if (changeEvent.isModified) {
            return new PersistentTrieSet<>(newRootNode, size + 1);
        }

        return this;
    }

    public @NonNull PersistentTrieSet<E> copyAddAll(final @NonNull Iterable<? extends E> set) {
        if (set == this
                || (set instanceof Collection) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }

        /*
        if (set instanceof PersistentTrieSet) {
            return copyAddAllFromTrieSet((PersistentTrieSet<E>) set);
        } else if (set instanceof TrieSet) {
            return copyAddAllFromTrieSet(((TrieSet<E>) set).toPersistent());
        }*/

        final TrieSet<E> t = this.toMutable();
        boolean modified = false;
        for (final E key : set) {
            modified |= t.add(key);
        }
        return modified ? t.toPersistent() : this;
    }

    // FIXME fix counting of merged values
    private @NonNull PersistentTrieSet<E> copyAddAllFromTrieSet(final @NonNull PersistentTrieSet<E> that) {
        if (that.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return that;
        }
        BulkChangeEvent bulkChange = new BulkChangeEvent();
        BitmapIndexedNode<E, Void> newNode = copyAddAll(that, 0, bulkChange, new UniqueIdentity(), TUPLE_LENGTH, hashFunction);
        if (newNode != this) {
            return new PersistentTrieSet<>(newNode,
                    this.size + that.size - bulkChange.numInBothCollections
            );
        }
        return this;
    }

    @Override
    public @NonNull PersistentSet<E> copyClear(@NonNull E element) {
        return isEmpty() ? this : of();
    }

    public @NonNull PersistentTrieSet<E> copyRemove(final @NonNull E key) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<Void> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<E, Void> newRootNode = (BitmapIndexedNode<E, Void>) removed(null, key,
                keyHash, 0, changeEvent, TUPLE_LENGTH, hashFunction);
        if (changeEvent.isModified) {
            return new PersistentTrieSet<>(newRootNode, size - 1);
        }

        return this;
    }

    public @NonNull PersistentTrieSet<E> copyRemoveAll(final @NonNull Iterable<? extends E> set) {
        if (this.isEmpty()
                || (set instanceof Collection) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }
        if (set == this) {
            return of();
        }
        final TrieSet<E> t = this.toMutable();
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

    public @NonNull PersistentTrieSet<E> copyRetainAll(final @NonNull Collection<? extends E> set) {
        if (this.isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }

        final TrieSet<E> t = this.toMutable();
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

        if (other instanceof PersistentTrieSet) {
            PersistentTrieSet<?> that = (PersistentTrieSet<?>) other;
            if (this.size != that.size) {
                return false;
            }
            return this.equivalent(that, TUPLE_LENGTH);
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
        return new KeyIterator<>(this, TUPLE_LENGTH, hashFunction);
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
    private @NonNull TrieSet<E> toMutable() {
        return new TrieSet<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }
}