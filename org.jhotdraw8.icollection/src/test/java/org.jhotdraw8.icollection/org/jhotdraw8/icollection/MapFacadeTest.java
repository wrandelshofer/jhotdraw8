package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.MapFacade;

import java.util.HashMap;
import java.util.Map;

public class MapFacadeTest extends AbstractMapTest {
    @Override
    protected <K, V> Map<K, V> newInstance() {
        return new MapFacade<>(new HashMap<>());
    }

    @Override
    protected <K, V> Map<K, V> newInstance(int numElements, float loadFactor) {
        return new MapFacade<>(new HashMap<>((int) (numElements / loadFactor), loadFactor));
    }

    @Override
    protected <K, V> Map<K, V> newInstance(Map<K, V> m) {
        return new MapFacade<>(new HashMap<>(m));
    }

    @Override
    protected <K, V> Map<K, V> toClonedInstance(Map<K, V> m) {
        return new MapFacade<>(new HashMap<>(m));
    }

    @Override
    protected <K, V> Map<K, V> newInstance(Iterable<Map.Entry<K, V>> m) {
        HashMap<K, V> i = new HashMap<>();
        for (Map.Entry<K, V> e : m) {
            i.put(e.getKey(), e.getValue());
        }
        return new MapFacade<>(i);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    protected boolean supportsNullEntries() {
        return true;
    }

}
