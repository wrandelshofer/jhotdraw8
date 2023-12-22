package org.jhotdraw8.icollection.readonly;

import org.jhotdraw8.annotation.Nullable;

import java.util.Comparator;

/**
 * A read-only interface for a sorted map. A sorted map is a map that  provides a total ordering on its keys.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReadOnlySortedMap<K, V> extends ReadOnlySequencedMap<K, V> {
    /**
     * Returns the comparator used to order the keys in this map, or null if this set uses
     * the natural ordering of its keys.
     *
     * @return comparator or null
     */
    @Nullable
    Comparator<? super K> comparator();
}
