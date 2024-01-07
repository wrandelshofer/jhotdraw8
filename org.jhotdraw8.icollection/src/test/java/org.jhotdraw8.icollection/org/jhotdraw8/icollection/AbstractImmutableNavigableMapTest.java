package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.immutable.ImmutableNavigableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractImmutableNavigableMapTest extends AbstractImmutableSortedMapTest {

    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> @NonNull ImmutableNavigableMap<K, V> newInstance();


    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> @NonNull ImmutableNavigableMap<K, V> newInstance(@NonNull Map<K, V> m);

    protected abstract <K, V> @NonNull ImmutableNavigableMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> m);


    protected abstract <K, V> @NonNull ImmutableNavigableMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> @NonNull ImmutableNavigableMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m);

    @Test
    public void spliteratorShouldHaveSequencedMapCharacteristics() throws Exception {
        ImmutableNavigableMap<Key, Key> instance = newInstance();

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
