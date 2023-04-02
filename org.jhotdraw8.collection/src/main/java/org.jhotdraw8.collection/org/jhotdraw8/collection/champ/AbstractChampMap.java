/*
 * @(#)AbstractChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Iterator;

/**
 * Abstract base class for CHAMP maps.
 *
 * @param <K> the key type of the map
 * @param <V> the value typeof the map
 */
abstract class AbstractChampMap<K, V, X> extends AbstractMap<K, V> implements Serializable, Cloneable,
        ReadOnlyMap<K, V> {
    private static final long serialVersionUID = 0L;

    /**
     * The current mutator id of this map.
     * <p>
     * All nodes that have the same non-null mutator id, are exclusively owned
     * by this map, and therefore can be mutated without affecting other map.
     * <p>
     * If this mutator id is null, then this map does not own any nodes.
     */
    @Nullable IdentityObject mutator;

    /**
     * The root of this CHAMP trie.
     */
    BitmapIndexedNode<X> root;

    /**
     * The number of entries in this map.
     */
    int size;

    /**
     * The number of times this map has been structurally modified.
     */
    int modCount;

    @NonNull IdentityObject getOrCreateIdentity() {
        if (mutator == null) {
            mutator = new IdentityObject();
        }
        return mutator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull AbstractChampMap<K, V, X> clone() {
        try {
            mutator = null;
            return (AbstractChampMap<K, V, X>) super.clone();
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
        if (o instanceof AbstractChampMap<?, ?, ?>) {
            AbstractChampMap<?, ?, ?> that = (AbstractChampMap<?, ?, ?>) o;
            return size == that.size && root.equivalent(that.root);
        }
        return super.equals(o);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return entrySet().iterator();
    }

    @SuppressWarnings("unchecked")
    boolean removeEntry(@Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            remove(((Entry<K, V>) o).getKey());
            return true;
        }
        return false;
    }
}
