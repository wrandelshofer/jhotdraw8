/*
 * @(#)ImmutableChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractImmutableSetTest;
import org.jhotdraw8.collection.HashCollider;
import org.jhotdraw8.collection.SetData;
import org.jhotdraw8.collection.immutable.ImmutableSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChampImmutableSetTest extends AbstractImmutableSetTest {


    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance() {
        return ChampImmutableSet.of();
    }


    @Override
    protected <E> @NonNull Set<E> toMutableInstance(ImmutableSet<E> m) {
        return m.toMutable();
    }

    @Override
    protected <E> @NonNull ImmutableSet<E> toImmutableInstance(Set<E> m) {
        return ((ChampSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull ImmutableSet<E> toClonedInstance(ImmutableSet<E> m) {
        return ChampImmutableSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance(Iterable<E> m) {
        return ChampImmutableSet.copyOf(m);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testToMutableAddAllWithImmutableTypeAndAllNewKeysShouldReturnTrue(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = newInstance(data.a);
        ImmutableSet<HashCollider> instance2 = newInstance(data.c);
        ChampSet<HashCollider> mutableInstance = (ChampSet<HashCollider>) instance.toMutable();
        assertTrue(mutableInstance.addAll(instance2));

        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, toImmutableInstance(mutableInstance));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testOfArrayArgShouldYieldExpectedResult(@NonNull SetData data) throws Exception {
        ImmutableSet<HashCollider> instance = ChampImmutableSet.of(data.a().toArray(new HashCollider[0]));
        assertEqualSet(data.a, instance);
    }
}