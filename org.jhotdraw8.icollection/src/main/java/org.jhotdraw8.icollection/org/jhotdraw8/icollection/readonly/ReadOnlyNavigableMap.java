package org.jhotdraw8.icollection.readonly;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * A read-only interface to a navigable map. A navigable map is a SortedMap
 * extended with navigation methods returning the closest matches for given search
 * targets.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReadOnlyNavigableMap<K, V> extends ReadOnlySortedMap<K, V> {
    /**
     * Returns the least entry in this map with a key
     * greater than or equal to the given key,
     * or null if there is no such entry.
     *
     * @param k the given key
     * @return ceiling entry or null
     */
    Map.@Nullable Entry<K, V> ceilingEntry(K k);

    /**
     * Returns the greatest entry in this map with a key
     * less than or equal to the given key,
     * or null if there is no such entry.
     *
     * @param k the given key
     * @return floor entry or null
     */
    Map.@Nullable Entry<K, V> floorEntry(K k);

    /**
     * Returns the least entry in this map with a key
     * greater than the given key,
     * or null if there is no such entry.
     *
     * @param k the given key
     * @return higher entry or null
     */
    Map.@Nullable Entry<K, V> higherEntry(K k);

    /**
     * Returns the greatest entry in this map with a key
     * less than the given key,
     * or null if there is no such entry.
     *
     * @param k the given key
     * @return lower entry or null
     */
    Map.@Nullable Entry<K, V> lowerEntry(K k);

    /**
     * Returns the least entry in this map with a key
     * greater than or equal to the given key,
     * or null if there is no such key.
     *
     * @param k the given key
     * @return ceiling key or null
     */
    default @Nullable K ceilingKey(K k) {
        Map.Entry<K, V> entry = ceilingEntry(k);
        return entry == null ? null : entry.getKey();
    }

    /**
     * Returns the greatest key in this map
     * less than or equal to the given key,
     * or null if there is no such key.
     *
     * @param k the given key
     * @return floor key or null
     */
    default @Nullable K floorKey(K k) {
        Map.Entry<K, V> entry = floorEntry(k);
        return entry == null ? null : entry.getKey();
    }

    /**
     * Returns the least key in this map
     * greater than the given key,
     * or null if there is no such key.
     *
     * @param k the given key
     * @return higher key or null
     */
    default @Nullable K higherKey(K k) {
        Map.Entry<K, V> entry = higherEntry(k);
        return entry == null ? null : entry.getKey();
    }

    /**
     * Returns the greatest key in this map
     * less than the given key,
     * or null if there is no such key.
     *
     * @param k the given key
     * @return lower key or null
     */
    default @Nullable K lowerKey(K k) {
        Map.Entry<K, V> entry = lowerEntry(k);
        return entry == null ? null : entry.getKey();
    }

}
