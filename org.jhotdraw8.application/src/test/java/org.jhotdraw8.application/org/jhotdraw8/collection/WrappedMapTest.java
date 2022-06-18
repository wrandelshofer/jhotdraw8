package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class WrappedMapTest extends AbstractMapTest {
    @Override
    protected <K, V> @NonNull Map<K, V> newInstance() {
        return new WrappedMap<K, V>(new HashMap<>());
    }

    @Override
    protected <K, V> @NonNull Map<K, V> newInstance(int numElements, float loadFactor) {
        return new WrappedMap<K, V>(new HashMap<>((int) (numElements / loadFactor), loadFactor));
    }

    @Override
    protected <K, V> @NonNull Map<K, V> newInstance(Map<K, V> m) {
        return new WrappedMap<K, V>(new HashMap<>(m));
    }

    @Override
    protected <K, V> @NonNull Map<K, V> toClonedInstance(Map<K, V> m) {
        return new WrappedMap<K, V>(new HashMap<>(m));
    }

    @Override
    protected <K, V> @NonNull Map<K, V> newInstance(Iterable<Map.Entry<K, V>> m) {
        HashMap<K, V> i = new HashMap<>();
        for (Map.Entry<K, V> e : m) {
            i.put(e.getKey(), e.getValue());
        }
        return new WrappedMap<K, V>(i);
    }
}
