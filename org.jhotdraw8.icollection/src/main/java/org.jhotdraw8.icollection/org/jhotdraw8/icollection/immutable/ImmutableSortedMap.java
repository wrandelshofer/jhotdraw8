package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.icollection.readonly.ReadOnlySortedMap;

/**
 * An interface to an immutable sorted map; the implementation guarantees that the state of the collection does not change.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ImmutableSortedMap<K, V> extends ReadOnlySortedMap<K, V>, ImmutableMap<K, V> {
}
