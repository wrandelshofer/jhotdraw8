package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.icollection.readonly.ReadOnlyNavigableMap;

public interface ImmutableNavigableMap<K, V> extends ReadOnlyNavigableMap<K, V>, ImmutableSortedMap<K, V> {
}
