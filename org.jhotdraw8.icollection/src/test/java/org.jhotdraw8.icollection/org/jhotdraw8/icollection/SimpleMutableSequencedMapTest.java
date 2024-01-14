/*
 * @(#)SequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.SequencedMap;

public class SimpleMutableSequencedMapTest extends AbstractSequencedMapTest {
    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    @Override
    protected <K, V> @NonNull SimpleMutableSequencedMap<K, V> newInstance() {
        return new SimpleMutableSequencedMap<>();
    }

    @Override
    protected <K, V> @NonNull SimpleMutableSequencedMap<K, V> newInstance(int numElements, float loadFactor) {
        return new SimpleMutableSequencedMap<>();
    }

    @Override
    protected <K, V> @NonNull SimpleMutableSequencedMap<K, V> newInstance(@NonNull Map<K, V> m) {
        return new SimpleMutableSequencedMap<>(m);
    }

    @Override
    protected <K, V> @NonNull SimpleMutableSequencedMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m) {
        return new SimpleMutableSequencedMap<>(m);
    }


    @Override
    protected <K, V> @NonNull SequencedMap<K, V> toClonedInstance(@NonNull Map<K, V> m) {
        return ((SimpleMutableSequencedMap<K, V>) m).clone();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testConstructorWithReadOnlyArgYieldsExpectedMap(@NonNull MapData data) throws Exception {
        Map<Key, Value> instance = new SimpleMutableSequencedMap<>(data.a());
        assertEqualMap(data.a(), instance);
    }
}
