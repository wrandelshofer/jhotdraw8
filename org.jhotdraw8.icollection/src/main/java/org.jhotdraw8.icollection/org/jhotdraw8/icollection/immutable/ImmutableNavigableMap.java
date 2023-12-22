package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.icollection.readonly.ReadOnlyNavigableMap;

/**
 * An interface to an immutable navigable map; the implementation guarantees that the state of the collection does not change.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ImmutableNavigableMap<K, V> extends ReadOnlyNavigableMap<K, V>, ImmutableSortedMap<K, V> {
}
