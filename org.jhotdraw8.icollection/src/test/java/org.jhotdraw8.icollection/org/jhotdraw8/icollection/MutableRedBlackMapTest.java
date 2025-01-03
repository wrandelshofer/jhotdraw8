package org.jhotdraw8.icollection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableRedBlackMapTest extends AbstractSortedMapTest {
    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    @Override
    protected <K, V> SortedMap<K, V> newInstance() {
        return new MutableRedBlackMap<>();
    }

    @Override
    protected <K, V> SortedMap<K, V> newInstance(int numElements, float loadFactor) {
        return new MutableRedBlackMap<>();
    }

    @Override
    protected <K, V> SortedMap<K, V> newInstance(Map<K, V> m) {
        return new MutableRedBlackMap<>(m);
    }

    @Override
    protected <K, V> SortedMap<K, V> newInstance(Iterable<Map.Entry<K, V>> m) {
        return new MutableRedBlackMap<>(m);
    }


    @Override
    protected <K, V> SortedMap<K, V> toClonedInstance(Map<K, V> m) {
        return ((MutableRedBlackMap<K, V>) m).clone();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlyArgOfDifferentTypeShouldBeEqualToArg(MapData data) {
        Map<Key, Value> actual = new MutableRedBlackMap<>(data.a());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorShouldYieldExpectedEntries(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        List<Map.Entry<Key, Value>> actualList = new ArrayList<>();
        LinkedHashMap<Key, Value> actualMap = new LinkedHashMap<>();
        ((MutableRedBlackMap<Key, Value>) instance).iterator().forEachRemaining(actualList::add);
        ((MutableRedBlackMap<Key, Value>) instance).iterator().forEachRemaining(e -> actualMap.put(e.getKey(), e.getValue()));
        assertEquals(data.a.size(), actualList.size());
        assertEqualMap(data.a, actualMap);
    }
}
