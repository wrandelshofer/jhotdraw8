package org.jhotdraw8.collection;

import java.util.Iterator;
import java.util.Map;

public class ReadOnlyMapWrapper<K, V> implements ReadOnlyMap<K, V> {
    private final Map<K, V> target;

    public ReadOnlyMapWrapper(Map<K, V> target) {
        this.target = target;

    }

    @Override
    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public V get(Object key) {
        return target.get(key);
    }

    @Override
    public Iterator<Map.Entry<K, V>> entries() {
        return target.entrySet().iterator();
    }

    @Override
    public Iterator<K> keys() {
        return target.keySet().iterator();
    }

    @Override
    public boolean containsKey(Object key) {
        return target.containsKey(key);
    }
}
