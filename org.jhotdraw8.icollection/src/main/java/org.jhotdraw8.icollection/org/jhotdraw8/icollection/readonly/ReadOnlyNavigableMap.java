package org.jhotdraw8.icollection.readonly;

/**
 * A read-only interface for a navigable map. A navigable map is a SortedMap
 * extended with navigation methods returning the closest matches for given search
 * targets.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReadOnlyNavigableMap<K, V> extends ReadOnlySortedMap<K, V> {
}
