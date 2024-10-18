package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.persistent.PersistentSortedMap;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractImmutableSortedMapTest extends AbstractPersistentMapTest {

    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> PersistentSortedMap<K, V> newInstance();


    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> PersistentSortedMap<K, V> newInstance(Map<K, V> m);

    protected abstract <K, V> PersistentSortedMap<K, V> newInstance(ReadableMap<K, V> m);


    protected abstract <K, V> PersistentSortedMap<K, V> toClonedInstance(PersistentMap<K, V> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> PersistentSortedMap<K, V> newInstance(Iterable<Map.Entry<K, V>> m);

    @Test
    public void spliteratorShouldHaveSequencedMapCharacteristics() throws Exception {
        PersistentSortedMap<Key, Key> instance = newInstance();

        assertEquals(Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readOnlyKeySet().spliterator().characteristics());
        assertEquals(Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readOnlyEntrySet().spliterator().characteristics());
        assertEquals(Spliterator.ORDERED | Spliterator.SIZED, (Spliterator.SORTED | Spliterator.ORDERED | Spliterator.SIZED) & instance.readOnlyValues().spliterator().characteristics());
        assertNull(instance.readOnlyEntrySet().spliterator().getComparator());
        assertNull(instance.readOnlyKeySet().spliterator().getComparator());
        //assertNull(instance.values().spliterator().getComparator());
    }
}
