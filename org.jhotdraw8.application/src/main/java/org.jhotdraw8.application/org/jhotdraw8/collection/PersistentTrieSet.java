/*
 * @(#)PersistentTrieSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.TrieSetHelper.BitmapIndexedNode;
import org.jhotdraw8.collection.TrieSetHelper.BulkChangeEvent;
import org.jhotdraw8.collection.TrieSetHelper.ChangeEvent;
import org.jhotdraw8.collection.TrieSetHelper.TrieIterator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import static org.jhotdraw8.collection.TrieSetHelper.EMPTY_NODE;


public class PersistentTrieSet<E> extends BitmapIndexedNode<E> implements PersistentSet<E>, ImmutableSet<E>, Serializable {
    private final static long serialVersionUID = 0L;

    private static final PersistentTrieSet<?> EMPTY_SET = new PersistentTrieSet<>(EMPTY_NODE, 0);

    final int size;

    PersistentTrieSet(BitmapIndexedNode<E> root, int size) {
        super(root.nodeMap, root.dataMap, root.nodes);
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
        return contains(key, Objects.hashCode(key), 0);
    }

    public @NonNull PersistentTrieSet<E> copyAdd(final @NonNull E key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent changeEvent = new ChangeEvent();
        final BitmapIndexedNode<E> newRootNode = updated(null, key, keyHash, 0, changeEvent);
        if (changeEvent.isModified) {
            return new PersistentTrieSet<>(newRootNode, size + 1);
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

    private @NonNull PersistentTrieSet<E> copyAddAllFromTrieSet(final @NonNull PersistentTrieSet<E> that) {
        if (that.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return that;
        }
        BulkChangeEvent bulkChange = new BulkChangeEvent();
        bulkChange.sizeChange = that.size;
        BitmapIndexedNode<E> newNode = copyAddAll(that, 0, bulkChange, new UniqueIdentity());
        if (newNode != this) {
            return new PersistentTrieSet<>(newNode,
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
        final ChangeEvent changeEvent = new ChangeEvent();
        final BitmapIndexedNode<E> newRootNode = (BitmapIndexedNode<E>) removed(null, key,
                keyHash, 0, changeEvent);
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
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public Iterator<E> iterator() {
        return new TrieIterator<>(this);
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