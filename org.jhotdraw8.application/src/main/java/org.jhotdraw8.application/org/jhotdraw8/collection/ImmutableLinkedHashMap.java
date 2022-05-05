/*
 * @(#)WrappedImmutableMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An immutable map backed by a {@link LinkedHashMap}.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Werner Randelshofer
 */
public class ImmutableLinkedHashMap<K, V> extends WrappedReadOnlyMap<K, V> implements ImmutableMap<K, V> {
    private final static ImmutableMap<Object, Object> EMPTY = new ImmutableLinkedHashMap<>(Collections.emptyMap());

    public ImmutableLinkedHashMap(Map<? extends K, ? extends V> target) {
        super(new LinkedHashMap<>(target));
    }

    @SuppressWarnings("unchecked")
    public static @NonNull <K, V> ImmutableMap<K, V> copyOf(ReadOnlyMap<? extends K, ? extends V> map) {
        return map instanceof ImmutableMap<?, ?> ? (ImmutableMap<K, V>) map : new ImmutableLinkedHashMap<>(map.asMap());
    }

    public static @NonNull <K, V> ImmutableMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return new ImmutableLinkedHashMap<>(map);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableMap<K, V> emptyMap() {
        return (ImmutableMap<K, V>) EMPTY;
    }

    public static @NonNull <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(@NonNull ReadOnlyCollection<Map.Entry<? extends K, ? extends V>> entrySet) {
        Map<K, V> backingMap1 = new LinkedHashMap<>(entrySet.size() * 2);
        for (Map.Entry<? extends K, ? extends V> entry : entrySet) {
            backingMap1.put(entry.getKey(), entry.getValue());
        }
        return new ImmutableLinkedHashMap<K, V>(backingMap1);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(@NonNull Collection<Map.Entry<K, V>> entrySet) {
        Map<K, V> backingMap1 = new LinkedHashMap<>(entrySet.size() * 2);
        for (Map.Entry<? extends K, ? extends V> entry : entrySet) {
            backingMap1.put(entry.getKey(), entry.getValue());
        }
        return new ImmutableLinkedHashMap<>(backingMap1);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of() {
        return ImmutableLinkedHashMap.<K, V>emptyMap();
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(K k1, V v1) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(2);
        backingMap1.put(k1, v1);
        return new ImmutableLinkedHashMap<>(backingMap1);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(2);
        backingMap1.put(k1, v1);
        backingMap1.put(k2, v2);
        return new ImmutableLinkedHashMap<>(backingMap1);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(2);
        backingMap1.put(k1, v1);
        backingMap1.put(k2, v2);
        backingMap1.put(k3, v3);
        return new ImmutableLinkedHashMap<>(backingMap1);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        HashMap<K, V> backingMap1 = new LinkedHashMap<>(2);
        backingMap1.put(k1, v1);
        backingMap1.put(k2, v2);
        backingMap1.put(k3, v3);
        backingMap1.put(k4, v4);
        return new ImmutableLinkedHashMap<>(backingMap1);
    }

    @SafeVarargs
    public static @NonNull <K, V> ImmutableMap<K, V> ofEntries(Map.Entry<K, V>... entries) {
        return of(Arrays.asList(entries));
    }

}
