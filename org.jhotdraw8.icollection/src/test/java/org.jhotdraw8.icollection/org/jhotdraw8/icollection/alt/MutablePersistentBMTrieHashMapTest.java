/*
 * @(#)SequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt;

import org.jhotdraw8.icollection.AbstractSequencedMapTest;
import org.jhotdraw8.icollection.Key;
import org.jhotdraw8.icollection.MapData;
import org.jhotdraw8.icollection.Value;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.SequencedMap;

public class MutablePersistentBMTrieHashMapTest extends AbstractSequencedMapTest {
    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    @Override
    protected <K, V> MutableBMTrieHashMap<K, V> newInstance() {
        return new MutableBMTrieHashMap<>();
    }

    @Override
    protected <K, V> MutableBMTrieHashMap<K, V> newInstance(int numElements, float loadFactor) {
        return new MutableBMTrieHashMap<>();
    }

    @Override
    protected <K, V> MutableBMTrieHashMap<K, V> newInstance(Map<K, V> m) {
        return new MutableBMTrieHashMap<>(m);
    }

    @Override
    protected <K, V> MutableBMTrieHashMap<K, V> newInstance(Iterable<Map.Entry<K, V>> m) {
        return new MutableBMTrieHashMap<>(m);
    }


    @Override
    protected <K, V> SequencedMap<K, V> toClonedInstance(Map<K, V> m) {
        return ((MutableBMTrieHashMap<K, V>) m).clone();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testConstructorWithReadOnlyArgYieldsExpectedMap(MapData data) throws Exception {
        Map<Key, Value> instance = new MutableBMTrieHashMap<>(data.a());
        assertEqualMap(data.a(), instance);
    }
}
