package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Objects;

/**
 * Abstract base class for CHAMP maps.
 *
 * @param <K> the key type of the map
 * @param <V> the value typeof the map
 */
abstract class AbstractChampMap<K, V, X> extends AbstractMap<K, V> implements Serializable, Cloneable,
        ReadOnlyMap<K, V> {
    private final static long serialVersionUID = 0L;
    protected @Nullable UniqueId mutator;
    protected BitmapIndexedNode<X> root;
    protected int size;
    protected int modCount;

    @Override
    public boolean containsEntry(final @Nullable Object o) {
        if (o instanceof Entry) {
            @SuppressWarnings("unchecked") Entry<K, V> entry = (Entry<K, V>) o;
            K key = entry.getKey();
            return containsKey(key)
                    && Objects.equals(entry.getValue(), get(key));
        }
        return false;
    }

    protected @NonNull UniqueId getOrCreateMutator() {
        if (mutator == null) {
            mutator = new UniqueId();
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
            return root.equivalent(((AbstractChampMap<?, ?, ?>) o).root);
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
}
