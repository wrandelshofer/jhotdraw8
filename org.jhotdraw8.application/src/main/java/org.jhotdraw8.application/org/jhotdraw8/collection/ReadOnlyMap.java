/*
 * @(#)ReadOnlyMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only interface for a map.
 * <p>
 * This interface does not guarantee 'read-only', it actually guarantees
 * 'readable'. We use the prefix 'ReadOnly' because this is the naming
 * convention in JavaFX for APIs that provide read methods but no write methods.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReadOnlyMap<K, V> {
    boolean isEmpty();

    int size();

    @Nullable V get(@NonNull Object key);

    default V getOrDefault(@NonNull Object key, @Nullable V defaultValue) {
        V v;
        return (((v = get(key)) != null) || containsKey(key))
                ? v
                : defaultValue;
    }

    @NonNull Iterator<Map.Entry<K, V>> entries();

    @NonNull Iterator<K> keys();

    boolean containsKey(@Nullable Object key);

    default boolean containsValue(@Nullable Object value) {
        for (Iterator<Map.Entry<K, V>> i = entries(); i.hasNext(); ) {
            Map.Entry<K, V> entry = i.next();
            if (Objects.equals(value, entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    default @NonNull ReadOnlySet<Map.Entry<K, V>> readOnlyEntrySet() {
        return new ReadOnlySet<Map.Entry<K, V>>() {

            @Override
            public @NonNull Iterator<Map.Entry<K, V>> iterator() {
                return ReadOnlyMap.this.entries();
            }

            @Override
            public int size() {
                return ReadOnlyMap.this.size();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry<?, ?>)) {
                    return false;
                }
                @SuppressWarnings("unchecked") Map.Entry<K, V> e = (Map.Entry<K, V>) o;
                K key = e.getKey();
                V value = ReadOnlyMap.this.get(key);

                // The value returned by get(key) can be null, because the value
                // is null, or because the map does not contain the key.
                return Objects.equals(value, e.getValue())
                        && (value != null || ReadOnlyMap.this.containsKey(key));
            }
        };
    }

    default @NonNull ReadOnlySet<K> readOnlyKeySet() {
        return new ReadOnlySet<K>() {

            @Override
            public @NonNull Iterator<K> iterator() {
                return ReadOnlyMap.this.keys();
            }

            @Override
            public int size() {
                return ReadOnlyMap.this.size();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry)) {
                    return false;
                }
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                @SuppressWarnings("unchecked") K key = (K) e.getKey();
                V value = ReadOnlyMap.this.get(key);
                return Objects.equals(value, e.getValue());
            }
        };
    }

    /**
     * Wraps this map in the Map API - without copying.
     *
     * @return the wrapped map
     */
    default @NonNull Map<K, V> asMap() {
        return new WrappedMap<>(this);
    }

    static <K, V> @NonNull String mapToString(final ReadOnlyMap<K, V> map) {
        Iterator<Map.Entry<K, V>> i = map.entries();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Map.Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == map ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == map ? "(this Map)" : value);
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

    static <K, V> boolean mapEquals(ReadOnlyMap<K, V> map, Object o) {
        if (o == map) {
            return true;
        }

        if (!(o instanceof ReadOnlyMap)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        ReadOnlyMap<K, V> that = (ReadOnlyMap<K, V>) o;
        if (that.size() != map.size()) {
            return false;
        }

        try {
            for (Map.Entry<K, V> e : map.readOnlyEntrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(that.get(key) == null && that.containsKey(key))) {
                        return false;
                    }
                } else {
                    if (!value.equals(that.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }

        return true;
    }
    /**
     * Returns the hash code of the provided iterable, assuming that
     * the iterable is an entry set of a map.
     *
     * @param entries an iterable that is an entry set
     * @return the sum of the hash codes of the elements in the set
     * @see Map#hashCode()
     */
    static <K, V> int iterableToHashCode(@NonNull Iterator<Map.Entry<K, V>> entries) {
        return ReadOnlySet.iteratorToHashCode(entries);
    }
}
