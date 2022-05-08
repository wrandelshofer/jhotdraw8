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
 * convention in JavaFX for interfaces that provide read methods but no write methods.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReadOnlyMap<K, V> extends Iterable<Map.Entry<K, V>> {
    boolean isEmpty();

    int size();

    @Nullable V get(@NonNull Object key);

    default V getOrDefault(@NonNull Object key, @Nullable V defaultValue) {
        V v;
        return (((v = get(key)) != null) || containsKey(key))
                ? v
                : defaultValue;
    }

    boolean containsKey(@Nullable Object key);

    default boolean containsValue(@Nullable Object value) {
        for (Map.Entry<K, V> entry : this) {
            if (Objects.equals(value, entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this map contains the specified entry.
     *
     * @param o an entry
     * @return true if this map contains the entry
     */
    default boolean containsEntry(Object o) {
        if (o instanceof Map.Entry) {
            @SuppressWarnings("unchecked") Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            return containsKey(entry.getKey())
                    && Objects.equals(entry.getValue(), get(entry.getKey()));
        }
        return false;
    }

    default @NonNull ReadOnlySet<Map.Entry<K, V>> readOnlyEntrySet() {
        return new WrappedReadOnlySet<>(
                this::iterator,
                this::size,
                this::containsEntry
        );
    }

    default @NonNull ReadOnlySet<K> readOnlyKeySet() {
        return new WrappedReadOnlySet<>(
                () -> new MappedIterator<>(ReadOnlyMap.this.iterator(), Map.Entry::getKey),
                this::size,
                this::containsKey
        );
    }

    default @NonNull ReadOnlyCollection<V> readOnlyValues() {
        return new WrappedReadOnlyCollection<>(
                () -> new MappedIterator<>(ReadOnlyMap.this.iterator(), Map.Entry::getValue),
                this::size,
                this::containsValue
        );
    }

    /**
     * Wraps this map in the Map interface - without copying.
     *
     * @return the wrapped map
     */
    default @NonNull Map<K, V> asMap() {
        return new WrappedMap<>(this);
    }

    static <K, V> @NonNull String mapToString(final ReadOnlyMap<K, V> map) {
        Iterator<Map.Entry<K, V>> i = map.iterator();
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

    /**
     * Compares a read-only map with an object for equality.  Returns
     * {@code true} if the given object is also a read-only map and the two maps
     * represent the same mappings.
     *
     * @param map a map
     * @param o   an object
     * @return {@code true} if the object is equal to the map
     */
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
            for (Map.Entry<K, V> e : map) {
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
     * Returns the hash code of the provided iterable. The hash code
     * is the sum of the hash code of the entries.
     *
     * @param entries an iterable that is an entry set
     * @return the sum of the hash codes of the elements in the set
     * @see Map#hashCode()
     */
    static <K, V> int iterableToHashCode(@NonNull Iterator<Map.Entry<K, V>> entries) {
        return ReadOnlySet.iteratorToHashCode(entries);
    }

    /**
     * Compares the specified object with this map for equality.
     * <p>
     * Returns {@code true} if the given object is also a read-only map and the
     * two maps represent the same mappings, ignorig the sequence of the
     * map entries.
     *
     * @param o an object
     * @return {@code true} if the object is equal to this map
     */
    boolean equals(Object o);

    /**
     * Returns the hash code value for this map. The hash code
     * is the sum of the hash code of its entries.
     *
     * @return the hash code value for this map
     * @see Map#hashCode()
     */
    int hashCode();
}
