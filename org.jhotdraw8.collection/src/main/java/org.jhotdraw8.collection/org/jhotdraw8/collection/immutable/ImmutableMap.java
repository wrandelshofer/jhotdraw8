/*
 * @(#)ImmutableMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for an immutable map.
 * <p>
 * An immutable map provides methods for creating a new immutable map with
 * new, updated or deleted entries, without changing the original immutable map.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ImmutableMap<K, V> extends ReadOnlyMap<K, V> {

    /**
     * Returns a copy of this map that is empty.
     *
     * @return this set instance if it is already empty, or a different set
     * instance that is empty.
     */
    @NonNull ImmutableMap<K, V> clear();

    /**
     * Returns a copy of this map that contains all entries
     * of this map with the specified entry added or updated.
     *
     * @param key   the key of the entry
     * @param value the value of the entry
     * @return this map instance if it already contains the same entry, or
     * a different map instance with the entry added or updated
     */
    @NonNull ImmutableMap<K, V> put(@NonNull K key, @Nullable V value);

    /**
     * Returns a copy of this map that contains all entries
     * of this map with entries from the specified map added or updated.
     *
     * @param m another map
     * @return this map instance if it already contains the same entries, or
     * a different map instance with the entries added or updated
     */
    default @NonNull ImmutableMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return putAll(m.entrySet());
    }

    /**
     * Returns a copy of this map that contains all entries
     * of this map with entries from the specified map added or updated.
     *
     * @param m another map
     * @return this map instance if it already contains the same entries, or
     * a different map instance with the entries added or updated
     */
    @NonNull ImmutableMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> m);

    /**
     * Returns a copy of this map that contains all entries
     * of this map with entries from the specified map added or updated.
     *
     * @param kv a list of alternating keys and values
     * @return this map instance if it already contains the same entries, or
     * a different map instance with the entries added or updated
     */
    @SuppressWarnings("unchecked")
    default @NonNull ImmutableMap<K, V> putKeyValues(@NonNull Object @NonNull ... kv) {
        ImmutableMap<K, V> that = this;
        for (int i = 0; i < kv.length; i += 2) {
            that = that.put((K) kv[i], (V) kv[i + 1]);
        }
        return that;
    }


    /**
     * Returns a copy of this map that contains all entries
     * of this map with the specified entry removed.
     *
     * @param key the key of the entry
     * @return this map instance if it already does not contain the entry, or
     * a different map instance with the entry removed
     */
    @NonNull ImmutableMap<K, V> remove(@NonNull K key);

    /**
     * Returns a copy of this map that contains all entries
     * of this map except the entries of the specified
     * collection.
     *
     * @param c a collection with keys of entries to be removed from this map
     * @return this map instance if it already does not contain the entries, or
     * a different map instance with the entries removed
     */
    @NonNull ImmutableMap<K, V> removeAll(@NonNull Iterable<? extends K> c);

    /**
     * Returns a copy of this map that contains only entries
     * that are in this map and in the specified collection.
     *
     * @param c a collection with keys of entries to be retained in this map
     * @return this map instance if it has not changed, or
     * a different map instance with entries removed
     */
    @NonNull ImmutableMap<K, V> retainAll(@NonNull Collection<? extends K> c);

    /**
     * Returns a copy of this map that contains only entries
     * that are in this map and in the specified collection.
     *
     * @param c a collection with keys of entries to be retained in this map
     * @return this map instance if it has not changed, or
     * a different map instance with entries removed
     */
    default @NonNull ImmutableMap<K, V> retainAll(@NonNull ReadOnlyCollection<? extends K> c) {
        return retainAll(c.asCollection());
    }

    /**
     * Returns a mutable copy of this map.
     *
     * @return a mutable copy.
     */
    @NonNull Map<K, V> toMutable();

}
