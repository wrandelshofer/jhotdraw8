package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Map;

public abstract class AbstractImmutableSequencedMapTest extends AbstractImmutableMapTest {
    @Override
    protected abstract @NonNull <K, V> ImmutableSequencedMap<K, V> newInstance();

    @Override
    protected abstract @NonNull <K, V> ImmutableSequencedMap<K, V> newInstance(@NonNull Map<K, V> m);

    @Override
    protected abstract @NonNull <K, V> ImmutableSequencedMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> m);

    @Override
    protected abstract @NonNull <K, V> ImmutableSequencedMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m);

    @Override
    protected abstract @NonNull <K, V> ImmutableSequencedMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m);
}
