package org.jhotdraw8.collection;

import org.jhotdraw8.collection.readonly.ReadOnlyMap;

import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractReadOnlyMapTest {
    protected <K, V> void assertEqualMap(Map<K, V> expected, ReadOnlyMap<K, V> actual) {
        assertEquals(new LinkedHashSet<>(expected.values()),
                new LinkedHashSet<>(actual.readOnlyValues().asCollection()));
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual.asMap());
        assertEquals(actual.asMap(), expected);
        assertEquals(expected.entrySet(), actual.readOnlyEntrySet().asSet());
        assertEquals(expected.keySet(), actual.readOnlyKeySet().asSet());
    }
}
