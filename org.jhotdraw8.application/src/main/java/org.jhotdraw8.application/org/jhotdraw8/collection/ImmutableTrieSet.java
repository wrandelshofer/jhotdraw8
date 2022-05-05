/*
 * @(#)PersistentTrieSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.ChampTrieGraphviz;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.KeyIterator;
import org.jhotdraw8.collection.champ.Node;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;


/**
 * Implements an immutable set using a Compressed Hash-Array Mapped Prefix-tree
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
public class ImmutableTrieSet<E> extends BitmapIndexedNode<E, Void> implements ImmutableSet<E>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int ENTRY_LENGTH = 1;
    @SuppressWarnings("unchecked")
    private static final ImmutableTrieSet<?> EMPTY = new ImmutableTrieSet<>(BitmapIndexedNode.emptyNode(), 0);

    final int size;

    ImmutableTrieSet(BitmapIndexedNode<E, Void> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.mixed, ENTRY_LENGTH);
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public static <E> @NonNull ImmutableTrieSet<E> of() {
        return ((ImmutableTrieSet<E>) ImmutableTrieSet.EMPTY);
    }

    @SuppressWarnings("unchecked")
    public static <E> @NonNull ImmutableTrieSet<E> of(E... elements) {
        return ((ImmutableTrieSet<E>) ImmutableTrieSet.EMPTY).copyAddAll(Arrays.asList(elements));
    }

    @SuppressWarnings("unchecked")
    public static <E> ImmutableTrieSet<E> copyOf(Iterable<? extends E> list) {
        return ((ImmutableTrieSet<E>) ImmutableTrieSet.EMPTY).copyAddAll(list);
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return findByKey(key, Objects.hashCode(key), 0, ENTRY_LENGTH, ENTRY_LENGTH) != Node.NO_VALUE;
    }

    public @NonNull ImmutableTrieSet<E> copyAdd(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<Void> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<E, Void> newRootNode = update(null, key, null, keyHash, 0, changeEvent, ENTRY_LENGTH, Node.NO_SEQUENCE_NUMBER, ENTRY_LENGTH);
        if (changeEvent.isModified) {
            return new ImmutableTrieSet<>(newRootNode, size + 1);
        }

        return this;
    }

    @SuppressWarnings({"unchecked"})
    public @NonNull ImmutableTrieSet<E> copyAddAll(final @NonNull Iterable<? extends E> set) {
        if (set == this || isEmpty() && (set instanceof ImmutableTrieSet<?>)) {
            return (ImmutableTrieSet<E>) set;
        }
        final TrieSet<E> t = this.toMutable();
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

    public @NonNull ImmutableTrieSet<E> copyRemove(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<Void> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<E, Void> newRootNode = remove(null, key,
                keyHash, 0, changeEvent, ENTRY_LENGTH, ENTRY_LENGTH);
        if (changeEvent.isModified) {
            return new ImmutableTrieSet<>(newRootNode, size - 1);
        }

        return this;
    }

    public @NonNull ImmutableTrieSet<E> copyRemoveAll(final @NonNull Iterable<? extends E> set) {
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
        return modified ? t.toImmutable() : this;
    }

    public @NonNull ImmutableTrieSet<E> copyRetainAll(final @NonNull Collection<? extends E> set) {
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
        if (other instanceof ImmutableTrieSet) {
            ImmutableTrieSet<?> that = (ImmutableTrieSet<?>) other;
            if (this.size != that.size) {
                return false;
            }
            return this.equivalent(that, ENTRY_LENGTH, ENTRY_LENGTH);
        }
        return ReadOnlySet.setEquals(this, other);
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public Iterator<E> iterator() {
        return new KeyIterator<>(this, ENTRY_LENGTH);
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

    /**
     * Dumps the internal structure of this set in the Graphviz DOT Language.
     *
     * @return a dump of the internal structure
     */
    public String dump() {
        return new ChampTrieGraphviz<E, Void>().dumpTrie(this, ENTRY_LENGTH, false, false);
    }
}