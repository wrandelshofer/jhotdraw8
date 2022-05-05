/*
 * @(#)PersistentMapWrapper.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Wrap a {@link Map} in the {@link PersistentMap} API.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class WrappedPersistentMap<K, V> extends AbstractReadOnlyMap<K, V> implements PersistentMap<K, V> {
    public static final WrappedPersistentMap<Object, Object> EMPTY = new WrappedPersistentMap<>(new LinkedHashMap<>(), s -> (Map<Object, Object>) ((LinkedHashMap<Object, Object>) s).clone());
    private final @NonNull Map<K, V> map;
    private final @NonNull Function<Map<K, V>, Map<K, V>> cloneFunction;

    public WrappedPersistentMap(@NonNull Map<K, V> map, final @NonNull Function<Map<K, V>, Map<K, V>> cloneFunction) {
        this.map = map;
        this.cloneFunction = cloneFunction;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull WrappedPersistentMap<K, V> of() {
        return (WrappedPersistentMap<K, V>) EMPTY;
    }

    @SuppressWarnings("unchecked")

    @SafeVarargs
    public static <K, V> @NonNull WrappedPersistentMap<K, V> of(Map.@NonNull Entry<K, V>... entries) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }

        return new WrappedPersistentMap<K, V>(map, s -> (Map<K, V>) ((LinkedHashMap<K, V>) s).clone());
    }

    @SuppressWarnings("unchecked")

    public static <K, V> @NonNull WrappedPersistentMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        LinkedHashMap<K, V> mm = new LinkedHashMap<>(map);
        return new WrappedPersistentMap<K, V>(mm, s -> (Map<K, V>) ((LinkedHashMap<K, V>) s).clone());
    }

    @SuppressWarnings({"unchecked"})

    public static <K, V> @NonNull WrappedPersistentMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        if (map instanceof WrappedPersistentMap) {
            return (WrappedPersistentMap<K, V>) map;
        }
        LinkedHashMap<K, V> mm = new LinkedHashMap<>();
        for (@NonNull Iterator<? extends Map.Entry<? extends K, ? extends V>> it = map.entries(); it.hasNext(); ) {
            Map.Entry<? extends K, ? extends V> entry = it.next();
            mm.put(entry.getKey(), entry.getValue());
        }
        return new WrappedPersistentMap<K, V>(mm, s -> (Map<K, V>) ((LinkedHashMap<K, V>) s).clone());
    }

    @SuppressWarnings("unchecked")

    public static <K, V> @NonNull WrappedPersistentMap<K, V> ofEntries(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        if (entries instanceof WrappedPersistentMap) {
            return (WrappedPersistentMap<K, V>) entries;
        }
        LinkedHashMap<K, V> mm = new LinkedHashMap<>();
        for (@NonNull Iterator<? extends Map.Entry<? extends K, ? extends V>> it = entries.iterator(); it.hasNext(); ) {
            Map.Entry<? extends K, ? extends V> entry = it.next();
            mm.put(entry.getKey(), entry.getValue());
        }
        return new WrappedPersistentMap<K, V>(mm, s -> (Map<K, V>) ((LinkedHashMap<K, V>) s).clone());
    }

    @SafeVarargs
    public static @NonNull <K, V> ImmutableMap<K, V> ofEntries(Map.Entry<K, V>... entries) {
        return ofEntries(Arrays.asList(entries));
    }

    @Override
    public @NonNull WrappedPersistentMap<K, V> copyClear() {
        if (map.isEmpty()) {
            return this;
        }
        Map<K, V> c = cloneFunction.apply(map);
        return new WrappedPersistentMap<>(c, cloneFunction);
    }

    @Override
    public @NonNull WrappedPersistentMap<K, V> copyPut(@NonNull K key, @Nullable V value) {
        if (!map.containsKey(key) || !Objects.equals(map.get(key), value)) {
            Map<K, V> c = cloneFunction.apply(map);
            c.put(key, value);
            return new WrappedPersistentMap<>(c, cloneFunction);
        }
        return this;
    }


    @Override
    public @NonNull WrappedPersistentMap<K, V> copyPutAll(@NonNull Map<? extends K, ? extends V> m) {
        Map<K, V> c = cloneFunction.apply(map);
        boolean changed = false;
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (!c.containsKey(key) || !Objects.equals(c.get(key), value)) {
                c.put(key, value);
                changed = true;
            }
        }

        return changed ? new WrappedPersistentMap<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull WrappedPersistentMap<K, V> copyRemove(@NonNull K key) {
        if (map.containsKey(key)) {
            Map<K, V> c = cloneFunction.apply(map);
            c.remove(key);
            return new WrappedPersistentMap<>(c, cloneFunction);
        }
        return this;
    }

    @Override
    public @NonNull WrappedPersistentMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> m) {
        Map<K, V> c = cloneFunction.apply(map);
        boolean changed = false;
        for (K key : m) {
            if (c.containsKey(key)) {
                c.remove(key);
                changed = true;
            }
        }
        return changed ? new WrappedPersistentMap<>(c, cloneFunction) : this;
    }

    @Override
    public @NonNull WrappedPersistentMap<K, V> copyRetainAll(@NonNull Collection<? extends K> m) {
        Map<K, V> c = cloneFunction.apply(map);
        boolean changed = false;
        for (Iterator<K> i = c.keySet().iterator(); i.hasNext(); ) {
            K k = i.next();
            if (!m.contains(k)) {
                i.remove();
                changed = true;
            }
        }

        return changed ? new WrappedPersistentMap<>(c, cloneFunction) : this;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public @Nullable V get(@NonNull Object key) {
        return map.get(key);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> entries() {
        return Collections.unmodifiableMap(map).entrySet().iterator();
    }

    @Override
    public @NonNull Iterator<K> keys() {
        return Collections.unmodifiableMap(map).keySet().iterator();
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return map.containsKey(key);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
