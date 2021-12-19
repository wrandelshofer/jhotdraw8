/*
 * @(#)ImmutableHashMap.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ImmutableHashMap<K, V> extends AbstractReadOnlyMap<K, V> implements ImmutableMap<K, V> {
    private final @NonNull Map<K, V> backingMap;
    static final ImmutableMap<?, ?> EMPTY_MAP = new ImmutableHashMap<>();


    public ImmutableHashMap(@NonNull Map<? extends K, ? extends V> backingMap) {
        this(backingMap, LinkedHashMap::new);
    }

    public ImmutableHashMap(@NonNull Map<? extends K, ? extends V> backingMap, @NonNull Function<Map<? extends K, ? extends V>, Map<K, V>> backingMapSupplier) {
        if (backingMap.isEmpty()) {
            this.backingMap = Collections.emptyMap();
        } else {
            this.backingMap = backingMapSupplier.apply(backingMap);
        }
    }

    public ImmutableHashMap(@NonNull ReadOnlyMap<? extends K, ? extends V> backingMap) {
        if (backingMap.isEmpty()) {
            this.backingMap = Collections.emptyMap();
        } else {
            LinkedHashMap<K, V> backingMap1 = new LinkedHashMap<>(backingMap.size());
            this.backingMap = backingMap1;
            for (Map.Entry<? extends K, ? extends V> entry : backingMap.readOnlyEntrySet()) {
                backingMap1.put(entry.getKey(), entry.getValue());
            }

        }
    }

    public ImmutableHashMap() {
        this.backingMap = Collections.emptyMap();
    }

    public ImmutableHashMap(K k1, V v1) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(1);
        backingMap1.put(k1, v1);
        this.backingMap = backingMap1;
    }

    public ImmutableHashMap(K k1, V v1, K k2, V v2) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(2);
        backingMap1.put(k1, v1);
        backingMap1.put(k2, v2);
        this.backingMap = backingMap1;
    }

    public ImmutableHashMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(2);
        backingMap1.put(k1, v1);
        backingMap1.put(k2, v2);
        backingMap1.put(k3, v3);
        this.backingMap = backingMap1;
    }

    public ImmutableHashMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(2);
        backingMap1.put(k1, v1);
        backingMap1.put(k2, v2);
        backingMap1.put(k3, v3);
        backingMap1.put(k4, v4);
        this.backingMap = backingMap1;
    }

    public ImmutableHashMap(@NonNull Collection<? extends Map.Entry<? extends K, ? extends V>> entries) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(entries.size() * 2);
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            backingMap1.put(entry.getKey(), entry.getValue());
        }
        this.backingMap = backingMap1;
    }

    public ImmutableHashMap(@NonNull ReadOnlyCollection<Map.Entry<? extends K, ? extends V>> entries) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(entries.size() * 2);
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            backingMap1.put(entry.getKey(), entry.getValue());
        }
        this.backingMap = backingMap1;
    }

    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public V get(@NonNull Object key) {
        return backingMap.get(key);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> entries() {
        return backingMap.entrySet().iterator();
    }

    @Override
    public @NonNull Iterator<K> keys() {
        return backingMap.keySet().iterator();
    }

    @Override
    public boolean containsKey(@NonNull Object key) {
        return backingMap.containsKey(key);
    }

}
