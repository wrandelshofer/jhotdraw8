/*
 * @(#)ReadableMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.readable;

import org.jhotdraw8.icollection.facade.MapFacade;
import org.jhotdraw8.icollection.facade.ReadableCollectionFacade;
import org.jhotdraw8.icollection.facade.ReadableSetFacade;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A readable interface to a map. A map is an object that maps keys to values.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReadableMap<K, V> extends Iterable<Map.Entry<K, V>> {
    /**
     * Returns {@code true} if this map contains no entries.
     *
     * @return {@code true} if empty
     */
    boolean isEmpty();

    /**
     * Returns the number of entries contained in this map..
     *
     * @return the number of entries
     */
    int size();

    /**
     * Returns the value to which the key is mapped, or {@code null} if this map
     * contains no entry for the key.
     *
     * @param key a key
     * @return the mapped value or {@code null}
     */
    @Nullable V get(Object key);

    /**
     * Returns the value to which the key is mapped, or the specified default
     * value if this map contains no entry for the key.
     *
     * @param key          a key
     * @param defaultValue a default value
     * @return the mapped value or the specified default value
     */
    @SuppressWarnings("unchecked")
    default @Nullable V getOrDefault(Object key, @Nullable V defaultValue) {
        V v;
        return (((v = get(key)) != null) || containsKey(key))
                ? v
                : defaultValue;
    }

    /**
     * Returns {@code true} if this map contains a entry for the specified
     * key.
     *
     * @param key a key
     * @return {@code true} if this map contains a entry for the specified
     * key
     */
    boolean containsKey(@Nullable Object key);

    /**
     * Returns {@code true} if this map contains one or more keys to the
     * specified value.
     *
     * @param value a value
     * @return {@code true} if this map maps one or more keys to the
     * specified value
     */
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
     * @param o an entry (should be a {@link Map.Entry}).
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

    /**
     * Returns a {@link ReadableSet} view to the entries contained
     * in this map.
     *
     * @return a readable view
     */
    default ReadableSet<Map.Entry<K, V>> readOnlyEntrySet() {
        return new ReadableSetFacade<>(
                this::iterator,
                this::size,
                this::containsEntry,
                Spliterator.NONNULL);
    }

    /**
     * Returns a {@link ReadableSet} view to the keys contained
     * in this map.
     *
     * @return a readable view
     */
    default ReadableSet<K> readOnlyKeySet() {
        return new ReadableSetFacade<>(
                () -> new MappedIterator<>(ReadableMap.this.iterator(), Map.Entry::getKey),
                this::size,
                this::containsKey,
                0);
    }

    /**
     * Returns a {@link ReadableCollection} view to the values contained
     * in this map.
     *
     * @return a readable view
     */
    default ReadableCollection<V> readOnlyValues() {
        return new ReadableCollectionFacade<>(
                () -> new MappedIterator<>(ReadableMap.this.iterator(), Map.Entry::getValue),
                this::size,
                this::containsValue, characteristics()
        );
    }

    /**
     * Wraps this map in the {@link Map} interface - without copying.
     *
     * @return the wrapped map
     */
    default Map<K, V> asMap() {
        return new MapFacade<>(this);
    }

    /**
     * Returns a string representation of the specified map.
     * <p>
     * The string representation is consistent with the one produced
     * by {@link AbstractMap#toString()}.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return a string representation
     */
    static <K, V> String mapToString(final ReadableMap<K, V> map) {
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
     * Compares a readable map with an object for equality.  Returns
     * {@code true} if the given object is also a readable map and the two maps
     * represent the same entries.
     *
     * @param map a map
     * @param o   an object
     * @param <K> the key type
     * @param <V> the value type
     * @return {@code true} if the object is equal to the map
     */
    static <K, V> boolean mapEquals(ReadableMap<K, V> map, Object o) {
        if (o == map) {
            return true;
        }

        if (!(o instanceof ReadableMap)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        ReadableMap<K, V> that = (ReadableMap<K, V>) o;
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
     * @param <K>     the key type
     * @param <V>     the value type
     * @return the sum of the hash codes of the elements in the set
     * @see Map#hashCode()
     */
    static <K, V> int iteratorToHashCode(Iterator<Map.Entry<K, V>> entries) {
        return ReadableSet.iteratorToHashCode(entries);
    }

    /**
     * Compares the specified object with this map for equality.
     * <p>
     * Returns {@code true} if the given object is also a readable map and the
     * two maps represent the same entries, ignorig the sequence of the
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

    /**
     * Returns an iterator over the entries contained in this map.
     *
     * @return an iterator
     */
    @Override
    Iterator<Map.Entry<K, V>> iterator();

    /**
     * Returns a spliterator over the entries contained in this map.
     *
     * @return a spliterator
     */
    @Override
    default Spliterator<Map.Entry<K, V>> spliterator() {
        //noinspection MagicConstant
        return Spliterators.spliterator(iterator(), size(), characteristics());
    }

    /**
     * Returns the spliterator characteristics of the key set.
     * <p>
     * The default implementation in this interface
     * returns {@link Spliterator#SIZED}|{@link Spliterator#DISTINCT}.
     *
     * @return characteristics.
     */
    default int characteristics() {
        return Spliterator.SIZED | Spliterator.DISTINCT;
    }
}
