package org.jhotdraw8.pcollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.facade.MapFacade;

import java.util.HashMap;
import java.util.Map;

public class MapFacadeTest extends AbstractMapTest {
    @Override
    protected <K, V> @NonNull Map<K, V> newInstance() {
        return new MapFacade<>(new HashMap<>());
    }

    @Override
    protected <K, V> @NonNull Map<K, V> newInstance(int numElements, float loadFactor) {
        return new MapFacade<>(new HashMap<>((int) (numElements / loadFactor), loadFactor));
    }

    @Override
    protected <K, V> @NonNull Map<K, V> newInstance(Map<K, V> m) {
        return new MapFacade<>(new HashMap<>(m));
    }

    @Override
    protected <K, V> @NonNull Map<K, V> toClonedInstance(Map<K, V> m) {
        return new MapFacade<>(new HashMap<>(m));
    }

    @Override
    protected <K, V> @NonNull Map<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m) {
        HashMap<K, V> i = new HashMap<>();
        for (Map.Entry<K, V> e : m) {
            i.put(e.getKey(), e.getValue());
        }
        return new MapFacade<>(i);
    }
}
