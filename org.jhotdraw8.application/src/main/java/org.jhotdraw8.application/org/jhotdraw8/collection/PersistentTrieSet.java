/*
 * @(#)PersistentTrieSet.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import static org.jhotdraw8.collection.PersistentTrieSetHelper.EMPTY_NODE;


/**
 * Implements the {@link PersistentSet} interface with a
 * Compressed Hash-Array Mapped Prefix-trie (CHAMP).
 * <p>
 * Creating a new copy with a single element added or removed
 * is performed in {@code O(1)} time and space.
 * <p>
 * References:
 * <dl>
 *     <dt>This class has been derived from "The Capsule Hash Trie Collections Library".</dt>
 *     <dd>Copyright (c) Michael Steindorfer, Centrum Wiskunde & Informatica, and Contributors.
 *         BSD 2-Clause License.
 *         <a href="https://github.com/usethesource/capsule">github.com</a>.</dd>
 * </dl>
 *
 * @param <E> the element type
 */
public class PersistentTrieSet<E> extends PersistentTrieSetHelper.BitmapIndexedNode<E> implements PersistentSet<E>, ImmutableSet<E>, Serializable {
    private final static long serialVersionUID = 0L;

    private static final PersistentTrieSet<?> EMPTY_SET = new PersistentTrieSet<>(EMPTY_NODE, 0, 0);

    final int hashCode;
    final int size;

    PersistentTrieSet(PersistentTrieSetHelper.BitmapIndexedNode<E> root, int hashCode, int size) {
        super(root.nodeMap, root.dataMap, root.nodes);
        this.hashCode = hashCode;
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public static <K> @NonNull PersistentTrieSet<K> copyOf(@NonNull Iterable<? extends K> set) {
        if (set instanceof PersistentTrieSet) {
            return (PersistentTrieSet<K>) set;
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
        return contains(key, Objects.hashCode(key), 0);
    }

    public @NonNull PersistentTrieSet<E> copyAdd(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieSetHelper.ChangeEvent changeEvent = new PersistentTrieSetHelper.ChangeEvent();
        final PersistentTrieSetHelper.BitmapIndexedNode<E> newRootNode = (PersistentTrieSetHelper.BitmapIndexedNode<E>) updated(null, key,
                keyHash, 0, changeEvent);
        if (changeEvent.isModified) {
            return new PersistentTrieSet<>(newRootNode, hashCode + keyHash, size + 1);
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    public @NonNull PersistentTrieSet<E> copyAddAll(final @NonNull Iterable<? extends E> set) {
        if (set == this
                || (set instanceof Collection) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }

        if (set instanceof PersistentTrieSet) {
            return copyAddAllFromTrieSet((PersistentTrieSet<E>) set);
        } else if (set instanceof TrieSet) {
            return copyAddAllFromTrieSet(((TrieSet<E>) set).toPersistent());
        }

        final TrieSet<E> t = this.toTransient();
        boolean modified = false;
        for (final E key : set) {
            modified |= t.add(key);
        }
        return modified ? t.toPersistent() : this;
    }

    private @NonNull PersistentTrieSet<E> copyAddAllFromTrieSet(final @NonNull PersistentTrieSet<E> that) {
        if (that.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return that;
        }
        PersistentTrieSetHelper.BulkChangeEvent bulkChange = new PersistentTrieSetHelper.BulkChangeEvent();
        bulkChange.hashChange = that.hashCode;
        bulkChange.sizeChange = that.size;
        PersistentTrieSetHelper.BitmapIndexedNode<E> newNode = this.copyAddAll(that, 0, bulkChange);
        if (newNode != this) {
            return new PersistentTrieSet<>(newNode,
                    this.hashCode + bulkChange.hashChange,
                    this.size + bulkChange.sizeChange
            );
        }
        return this;
    }

    @Override
    public @NonNull PersistentSet<E> copyClear(@NonNull E element) {
        return isEmpty() ? this : of();
    }

    public @NonNull PersistentTrieSet<E> copyRemove(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieSetHelper.ChangeEvent changeEvent = new PersistentTrieSetHelper.ChangeEvent();
        final PersistentTrieSetHelper.BitmapIndexedNode<E> newRootNode = (PersistentTrieSetHelper.BitmapIndexedNode<E>) removed(null, key,
                keyHash, 0, changeEvent);
        if (changeEvent.isModified) {
            return new PersistentTrieSet<>(newRootNode, hashCode - keyHash, size - 1);
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
        final TrieSet<E> t = this.toTransient();
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

        final TrieSet<E> t = this.toTransient();
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
            if (this.size != that.size || this.hashCode != that.hashCode) {
                return false;
            }
            return this.equivalent(that);
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
        return hashCode;
    }

    @Override
    public Iterator<E> iterator() {
        return new PersistentTrieSetHelper.TrieIterator<>(this);
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Returns a copy of this set that is transient.
     * <p>
     * This operation is performed in O(1).
     *
     * @return a transient trie set
     */
    private @NonNull TrieSet<E> toTransient() {
        return new TrieSet<>(this);
    }

    @Override
    public @NonNull String toString() {
        return AbstractReadOnlyCollection.iterableToString(this);
    }
}