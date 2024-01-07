package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.immutable.ImmutableSortedMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractImmutableSortedMapTest extends AbstractImmutableMapTest {

    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> @NonNull ImmutableSortedMap<K, V> newInstance();


    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> @NonNull ImmutableSortedMap<K, V> newInstance(@NonNull Map<K, V> m);

    protected abstract <K, V> @NonNull ImmutableSortedMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> m);


    protected abstract <K, V> @NonNull ImmutableSortedMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> @NonNull ImmutableSortedMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m);

    @Test
    public void spliteratorShouldHaveSequencedMapCharacteristics() throws Exception {
        ImmutableSortedMap<Key, Key> instance = newInstance();

        assertEquals(Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readOnlyKeySet().spliterator().characteristics());
        assertEquals(Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readOnlyEntrySet().spliterator().characteristics());
        assertEquals(Spliterator.ORDERED | Spliterator.SIZED, (Spliterator.SORTED | Spliterator.ORDERED | Spliterator.SIZED) & instance.readOnlyValues().spliterator().characteristics());
        assertNull(instance.readOnlyEntrySet().spliterator().getComparator());
        assertNull(instance.readOnlyKeySet().spliterator().getComparator());
        //assertNull(instance.values().spliterator().getComparator());
    }
}
