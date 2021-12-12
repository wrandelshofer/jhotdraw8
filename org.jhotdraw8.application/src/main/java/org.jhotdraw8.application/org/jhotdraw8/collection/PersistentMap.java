package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Provides an API for a persistent map.
 * <p>
 * A persistent map provides methods for creating a new persistent map with
 * put or removed entries, without changing the original persistent map.
 * <p>
 * Implementations are expected to only require time and space that is
 * proportional to the differences (also known as 'delta')
 * between the newly created persistent map to the original persistent amp.
 */
public interface PersistentMap<K, V> extends ReadOnlyMap<K, V> {
    /**
     * Returns a copy of this map that contains all entries
     * of this map with the specified entry added or updated.
     *
     * @param key   the key of the entry
     * @param value the value of the entry
     * @return this map if it already contains the same entry, or
     * a different map with the entry added or updated
     */
    @NonNull PersistentMap<K, V> copyPut(@NonNull K key, @Nullable V value);

    /**
     * Returns a copy of this map that contains all entries
     * of this map with entries from the specified map added or updated.
     *
     * @param m another map
     * @return this map if it already contains the same entries, or
     * a different map with the entries added or updated
     */
    @NonNull PersistentMap<K, V> copyPutAll(@NonNull Map<? extends K, ? extends V> m);

    /**
     * Returns a copy of this map that contains all entries
     * of this map with the specified entry removed.
     *
     * @param key the key of the entry
     * @return this map if it already does not contain the entry, or
     * a different map with the entry removed
     */
    @NonNull PersistentMap<K, V> copyRemove(@NonNull K key);

    /**
     * Returns a copy of this map that contains all entries
     * of this map except the entries of the specified
     * collection.
     *
     * @param c a collection with keys of entries to be removed from this map
     * @return this map if it already does not contain the entries, or
     * a different map with the entries removed
     */
    @NonNull PersistentMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c);

    /**
     * Returns a copy of this map that contains only entries
     * that are in this map and in the specified collection.
     *
     * @param c a collection with keys of entries to be retained in this map
     * @return this map if it has not changed, or
     * a different map with entries removed
     */
    @NonNull PersistentMap<K, V> copyRetainAll(@NonNull Collection<? extends K> c);

}
