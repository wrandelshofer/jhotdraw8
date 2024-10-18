package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.persistent.PersistentNavigableMap;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractImmutableNavigableMapTest extends AbstractImmutableSortedMapTest {

    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> PersistentNavigableMap<K, V> newInstance();


    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> PersistentNavigableMap<K, V> newInstance(Map<K, V> m);

    protected abstract <K, V> PersistentNavigableMap<K, V> newInstance(ReadableMap<K, V> m);


    protected abstract <K, V> PersistentNavigableMap<K, V> toClonedInstance(PersistentMap<K, V> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> PersistentNavigableMap<K, V> newInstance(Iterable<Map.Entry<K, V>> m);

    @Test
    public void spliteratorShouldHaveSequencedMapCharacteristics() throws Exception {
        PersistentNavigableMap<Key, Key> instance = newInstance();

        assertEquals(Spliterator.IMMUTABLE | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED)
                        & instance.readOnlyKeySet().spliterator().characteristics());
        assertEquals(Spliterator.IMMUTABLE | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED)
                        & instance.spliterator().characteristics());
        assertEquals(Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED)
                        & instance.readOnlyEntrySet().spliterator().characteristics());
        assertEquals(Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.SIZED) & instance.readOnlyValues().spliterator().characteristics());
        assertNull(instance.readOnlyEntrySet().spliterator().getComparator());
        assertNull(instance.readOnlyKeySet().spliterator().getComparator());
    }
}
