/*
 * @(#)SequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.SequencedMap;

public class MutableVectorHashMapTest extends AbstractSequencedMapTest {
    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    @Override
    protected <K, V> MutableVectorMap<K, V> newInstance() {
        return new MutableVectorMap<>();
    }

    @Override
    protected <K, V> MutableVectorMap<K, V> newInstance(int numElements, float loadFactor) {
        return new MutableVectorMap<>();
    }

    @Override
    protected <K, V> MutableVectorMap<K, V> newInstance(Map<K, V> m) {
        return new MutableVectorMap<>(m);
    }

    @Override
    protected <K, V> MutableVectorMap<K, V> newInstance(Iterable<Map.Entry<K, V>> m) {
        return new MutableVectorMap<>(m);
    }


    @Override
    protected <K, V> SequencedMap<K, V> toClonedInstance(Map<K, V> m) {
        return ((MutableVectorMap<K, V>) m).clone();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testConstructorWithReadOnlyArgYieldsExpectedMap(MapData data) throws Exception {
        Map<Key, Value> instance = new MutableVectorMap<>(data.a());
        assertEqualMap(data.a(), instance);
    }
}
