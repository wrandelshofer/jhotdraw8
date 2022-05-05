/*
 * @(#)WrappedImmutableMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collections;
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

    public ImmutableLinkedHashMap(Map<? extends K, ? extends V> map) {
        super(new LinkedHashMap<>(map));
    }

    @SuppressWarnings("unchecked")
    public static @NonNull <K, V> ImmutableMap<K, V> copyOf(ReadOnlyMap<? extends K, ? extends V> map) {
        return map instanceof ImmutableMap<?, ?> ? (ImmutableMap<K, V>) map : new ImmutableLinkedHashMap<>(map.asMap());
    }

    public static @NonNull <K, V> ImmutableMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return new ImmutableLinkedHashMap<>(map);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableMap<K, V> of() {
        return (ImmutableMap<K, V>) EMPTY;
    }

    public static <K, V> @NonNull ImmutableMap<K, V> of(K k, V v, Object... kv) {
        return new ImmutableLinkedHashMap<K, V>(Maps.putAll(new LinkedHashMap<K, V>(), k, v, kv));
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ImmutableMap<K, V> ofEntries(Map.Entry<? extends K, ? extends V>... entries) {
        return new ImmutableLinkedHashMap<K, V>(Maps.putAll(new LinkedHashMap<>(), entries));
    }

}
