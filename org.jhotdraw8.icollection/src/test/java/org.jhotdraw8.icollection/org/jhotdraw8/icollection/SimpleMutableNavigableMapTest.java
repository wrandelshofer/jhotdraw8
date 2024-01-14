package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleMutableNavigableMapTest extends AbstractSortedMapTest {
    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    @Override
    protected <K, V> @NonNull SortedMap<K, V> newInstance() {
        return new SimpleMutableNavigableMap<>();
    }

    @Override
    protected <K, V> @NonNull SortedMap<K, V> newInstance(int numElements, float loadFactor) {
        return new SimpleMutableNavigableMap<>();
    }

    @Override
    protected <K, V> @NonNull SortedMap<K, V> newInstance(Map<K, V> m) {
        return new SimpleMutableNavigableMap<>(m);
    }

    @Override
    protected <K, V> @NonNull SortedMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m) {
        return new SimpleMutableNavigableMap<>(m);
    }


    @Override
    protected <K, V> @NonNull SortedMap<K, V> toClonedInstance(Map<K, V> m) {
        return ((SimpleMutableNavigableMap<K, V>) m).clone();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlyArgOfDifferentTypeShouldBeEqualToArg(MapData data) {
        Map<Key, Value> actual = new SimpleMutableNavigableMap<>(data.a());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorShouldYieldExpectedEntries(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        List<Map.Entry<Key, Value>> actualList = new ArrayList<>();
        LinkedHashMap<Key, Value> actualMap = new LinkedHashMap<>();
        ((SimpleMutableNavigableMap<Key, Value>) instance).iterator().forEachRemaining(actualList::add);
        ((SimpleMutableNavigableMap<Key, Value>) instance).iterator().forEachRemaining(e -> actualMap.put(e.getKey(), e.getValue()));
        assertEquals(data.a.size(), actualList.size());
        assertEqualMap(data.a, actualMap);
    }
}
