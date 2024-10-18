package org.jhotdraw8.icollection.readable;

import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;

/**
 * A readable interface to a sorted map. A sorted map is a map that  provides a total ordering on its keys.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReadableSortedMap<K, V> extends ReadableSequencedMap<K, V> {
    /**
     * Returns the comparator used to order the keys in this map, or null if this set uses
     * the natural ordering of its keys.
     *
     * @return comparator or null
     */
    @Nullable
    Comparator<? super K> comparator();

    /**
     * Compares a readable map with an object for equality.  Returns
     * {@code true} if the given object is also a readable map and the two maps
     * represent the same entries.
     * <p>
     * This operation is more efficient than {@link ReadableMap#mapEquals(ReadableMap, Object)}.
     *
     * @param map a map
     * @param o   an object
     * @param <K> the key type
     * @param <V> the value type
     * @return {@code true} if the object is equal to the map
     */
    @SuppressWarnings("unchecked")
    static <K, V> boolean sortedMapEquals(ReadableSortedMap<K, V> map, Object o) {
        if (o instanceof ReadableSortedMap<?, ?> r && Objects.equals(map.comparator(), r.comparator())) {
            if (map.size() != r.size()) {
                return false;
            }
            Iterator<Map.Entry<K, V>> a = map.iterator();
            Iterator<Map.Entry<K, V>> b = (Iterator<Map.Entry<K, V>>) (Iterator<?>) r.iterator();
            for (int i = 0, n = map.size(); i < n; i++) {
                Map.Entry<K, V> ae = a.next();
                Map.Entry<K, V> be = b.next();
                if (!ae.equals(be)) {
                    return false;
                }
            }
            return true;
        }
        return ReadableMap.mapEquals(map, o);
    }


    /**
     * Returns a spliterator over the entries contained in this map.
     *
     * @return a spliterator
     */
    @Override
    default Spliterator<Map.Entry<K, V>> spliterator() {
        return new IteratorSpliterator<>(
                iterator(), size(), characteristics(),
                comparator() == null ? null : Map.Entry.comparingByKey(comparator()));
    }


    @Override
    default int characteristics() {
        return Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
    }
}
