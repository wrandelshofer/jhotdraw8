package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

public class SequencedChampMapTest extends AbstractSequencedMapTest {
    @Override
    protected <K, V> @NonNull SequencedChampMap<K, V> newInstance() {
        return new SequencedChampMap<>();
    }

    @Override
    protected <K, V> @NonNull SequencedChampMap<K, V> newInstance(int numElements, float loadFactor) {
        return new SequencedChampMap<>();
    }

    @Override
    protected <K, V> @NonNull SequencedChampMap<K, V> newInstance(Map<K, V> m) {
        return new SequencedChampMap<>(m);
    }

    @Override
    protected <K, V> @NonNull SequencedChampMap<K, V> newInstance(Iterable<Map.Entry<K, V>> m) {
        return new SequencedChampMap<>(m);
    }

    @Override
    protected <K, V> @NonNull ImmutableSequencedMap<K, V> toImmutableInstance(Map<K, V> m) {
        return ((SequencedChampMap<K, V>) m).toImmutable();
    }

    @Override
    protected <K, V> @NonNull SequencedMap<K, V> toClonedInstance(Map<K, V> m) {
        return ((SequencedChampMap<K, V>) m).clone();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testConstructorWithReadOnlyArgYieldsExpectedMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = new SequencedChampMap<>(data.a());
        assertEqualMap(data.a(), instance);
    }
}
