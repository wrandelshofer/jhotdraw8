/*
 * @(#)SimpleStyleableMapProxy.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.styleable;

import javafx.css.StyleOrigin;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractMap;
import java.util.Set;

class SimpleStyleableMapProxy<K, V> extends AbstractMap<K, V> {
    private final @NonNull SimpleStyleableMap<K, V> target;
    private final @Nullable StyleOrigin origin;
    private final int originOrdinal;

    public SimpleStyleableMapProxy(@NonNull SimpleStyleableMap<K, V> target, @Nullable StyleOrigin origin) {
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
    public @NonNull Set<Entry<K, V>> entrySet() {
        return target.entrySet(origin);
    }

    @Override
    public V get(Object key) {
        return target.getOrDefault(originOrdinal, key, null);
    }

    @Override
    public V put(K key, V value) {
        return target.put(originOrdinal, key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
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
