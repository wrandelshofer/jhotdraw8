package org.jhotdraw8.icollection.readonly;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;

/**
 * A read-only interface to a sorted map. A sorted map is a map that  provides a total ordering on its keys.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReadOnlySortedMap<K, V> extends ReadOnlySequencedMap<K, V> {
    /**
     * Returns the comparator used to order the keys in this map, or null if this set uses
     * the natural ordering of its keys.
     *
     * @return comparator or null
     */
    @Nullable
    Comparator<? super K> comparator();

    /**
     * Compares a read-only map with an object for equality.  Returns
     * {@code true} if the given object is also a read-only map and the two maps
     * represent the same entries.
     * <p>
     * This operation is more efficient than {@link ReadOnlyMap#mapEquals(ReadOnlyMap, Object)}.
     *
     * @param map a map
     * @param o   an object
     * @param <K> the key type
     * @param <V> the value type
     * @return {@code true} if the object is equal to the map
     */
    @SuppressWarnings("unchecked")
    static <K, V> boolean sortedMapEquals(@NonNull ReadOnlySortedMap<K, V> map, Object o) {
        if (o instanceof ReadOnlySortedMap<?, ?> r && Objects.equals(map.comparator(), r.comparator())) {
            if (map.size() != r.size()) return false;
            Iterator<Map.Entry<K, V>> a = map.iterator();
            Iterator<Map.Entry<K, V>> b = (Iterator<Map.Entry<K, V>>) (Iterator<?>) r.iterator();
            for (int i = 0, n = map.size(); i < n; i++) {
                Map.Entry<K, V> ae = a.next();
                Map.Entry<K, V> be = b.next();
                if (!ae.equals(be)) return false;
            }
            return true;
        }
        return ReadOnlyMap.mapEquals(map, o);
    }


    /**
     * Returns a spliterator over the entries contained in this map.
     *
     * @return a spliterator
     */
    @NonNull
    @Override
    default Spliterator<Map.Entry<K, V>> spliterator() {
        return new IteratorSpliterator<Map.Entry<K, V>>(
                iterator(), size(), characteristics(),
                comparator() == null ? null : Map.Entry.comparingByKey(comparator()));
    }


    @Override
    default int characteristics() {
        return Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
    }
}
