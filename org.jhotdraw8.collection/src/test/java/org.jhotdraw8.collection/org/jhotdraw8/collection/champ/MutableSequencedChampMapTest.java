/*
 * @(#)SequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractSequencedMapTest;
import org.jhotdraw8.collection.HashCollider;
import org.jhotdraw8.collection.MapData;
import org.jhotdraw8.collection.immutable.ImmutableSequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

public class MutableSequencedChampMapTest extends AbstractSequencedMapTest {
    @Override
    protected <K, V> @NonNull MutableSequencedChampMap<K, V> newInstance() {
        return new MutableSequencedChampMap<>();
    }

    @Override
    protected <K, V> @NonNull MutableSequencedChampMap<K, V> newInstance(int numElements, float loadFactor) {
        return new MutableSequencedChampMap<>();
    }

    @Override
    protected <K, V> @NonNull MutableSequencedChampMap<K, V> newInstance(@NonNull Map<K, V> m) {
        return new MutableSequencedChampMap<>(m);
    }

    @Override
    protected <K, V> @NonNull MutableSequencedChampMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m) {
        return new MutableSequencedChampMap<>(m);
    }

    @Override
    protected <K, V> @NonNull ImmutableSequencedMap<K, V> toImmutableInstance(@NonNull Map<K, V> m) {
        return ((MutableSequencedChampMap<K, V>) m).toImmutable();
    }

    @Override
    protected <K, V> @NonNull SequencedMap<K, V> toClonedInstance(@NonNull Map<K, V> m) {
        return ((MutableSequencedChampMap<K, V>) m).clone();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testConstructorWithReadOnlyArgYieldsExpectedMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = new MutableSequencedChampMap<>(data.a());
        assertEqualMap(data.a(), instance);
    }
}
