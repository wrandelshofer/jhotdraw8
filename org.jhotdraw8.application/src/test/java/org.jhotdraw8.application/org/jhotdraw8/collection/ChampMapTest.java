package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

public class ChampMapTest extends AbstractMapTest {
    @Override
    protected <K, V> @NonNull Map<K, V> newInstance() {
        return new ChampMap<>();
    }

    @Override
    protected <K, V> @NonNull Map<K, V> newInstance(int numElements, float loadFactor) {
        return new ChampMap<>();
    }

    @Override
    protected <K, V> @NonNull Map<K, V> newInstance(Map<K, V> m) {
        return new ChampMap<>(m);
    }

    @Override
    protected <K, V> @NonNull Map<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m) {
        return new ChampMap<>(m);
    }


    @Override
    protected <K, V> @NonNull Map<K, V> toClonedInstance(Map<K, V> m) {
        return ((ChampMap<K, V>) m).clone();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlyArgOfDifferentTypeShouldBeEqualToArg(Data data) {
        Map<HashCollider, HashCollider> actual = new ChampMap<>(data.a());
        assertEqualMap(data.a(), actual);
    }
}
