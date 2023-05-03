/*
 * @(#)AbstractChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract base class for CHAMP maps.
 *
 * @param <K> the key type of the map
 * @param <V> the value typeof the map
 */
public abstract class AbstractMutableChampMap<K, V, X> extends AbstractMap<K, V> implements Serializable, Cloneable,
        ReadOnlyMap<K, V> {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * The current owner id of this map.
     * <p>
     * All nodes that have the same non-null owner id, are exclusively owned
     * by this map, and therefore can be mutated without affecting other map.
     * <p>
     * If this owner id is null, then this map does not own any nodes.
     */
    @Nullable
    protected IdentityObject owner;

    /**
     * The root of this CHAMP trie.
     */
    protected BitmapIndexedNode<X> root;

    /**
     * The number of entries in this map.
     */
    protected int size;

    /**
     * The number of times this map has been structurally modified.
     */
    protected int modCount;

    @NonNull
    protected IdentityObject getOrCreateOwner() {
        if (owner == null) {
            owner = new IdentityObject();
        }
        return owner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull AbstractMutableChampMap<K, V, X> clone() {
        try {
            owner = null;
            return (AbstractMutableChampMap<K, V, X>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof AbstractMutableChampMap<?, ?, ?>) {
            AbstractMutableChampMap<?, ?, ?> that = (AbstractMutableChampMap<?, ?, ?>) o;
            return size == that.size && root.equivalent(that.root);
        }
        return super.equals(o);
    }

    @Override
    public V getOrDefault(@NonNull Object key, V defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    protected int getModCount() {
        return modCount;
    }

    /**
     * Adds all specified elements that are not already in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
     */
    public boolean putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        if (c == this) {
            return false;
        }
        boolean modified = false;
        for (var e : c) {
            var oldValue = put(e.getKey(), e.getValue());
            modified = modified || !Objects.equals(oldValue, e);
        }
        return modified;
    }

    @SuppressWarnings("unchecked")
    protected boolean removeEntry(@Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            remove(((Entry<K, V>) o).getKey());
            return true;
        }
        return false;
    }
}
