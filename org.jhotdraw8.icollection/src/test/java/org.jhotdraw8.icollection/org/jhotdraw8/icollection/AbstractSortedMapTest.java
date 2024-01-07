package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.SortedMap;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractSortedMapTest extends AbstractMapTest {
    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> @NonNull SortedMap<K, V> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract <K, V> @NonNull SortedMap<K, V> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> @NonNull SortedMap<K, V> newInstance(@NonNull Map<K, V> m);


    @Test
    public void spliteratorShouldHaveSortedMapCharacteristics() throws Exception {
        SortedMap<Key, Key> instance = newInstance();

        assertEquals(Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.keySet().spliterator().characteristics());
        assertEquals(Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.entrySet().spliterator().characteristics());
        assertEquals(Spliterator.ORDERED | Spliterator.SIZED, (Spliterator.ORDERED | Spliterator.SIZED) & instance.values().spliterator().characteristics());
        assertNull(instance.entrySet().spliterator().getComparator());
        assertNull(instance.keySet().spliterator().getComparator());
        //assertNull(instance.values().spliterator().getComparator());
    }
}
