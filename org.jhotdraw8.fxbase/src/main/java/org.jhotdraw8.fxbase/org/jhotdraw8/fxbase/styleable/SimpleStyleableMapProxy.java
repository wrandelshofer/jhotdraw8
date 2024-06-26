/*
 * @(#)SimpleStyleableMapProxy.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.styleable;

import javafx.css.StyleOrigin;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Set;

class SimpleStyleableMapProxy<K, V> extends AbstractMap<K, V> {
    private final SimpleStyleableMap<K, V> target;
    private final @Nullable StyleOrigin origin;
    private final int originOrdinal;

    public SimpleStyleableMapProxy(SimpleStyleableMap<K, V> target, @Nullable StyleOrigin origin) {
        this.target = target;
        this.originOrdinal = origin == null ? SimpleStyleableMap.AUTO_ORIGIN : origin.ordinal();
        this.origin = origin;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        return target.containsKey(origin, (K) key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return target.getOrDefault(originOrdinal, key, defaultValue);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return target.entrySet(origin);
    }

    @Override
    public V get(Object key) {
        return target.getOrDefault(originOrdinal, key, null);
    }

    @Override
    public @Nullable V put(K key, V value) {
        return target.put(originOrdinal, key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V remove(Object key) {
        if (origin == null) {
            return null;
        } else {
            return target.removeKey(origin, (K) key);
        }
    }

    @Override
    public int size() {
        return target.size(origin);
    }
}
