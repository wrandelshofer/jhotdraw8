/*
 * @(#)ImmutableChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChampSetTest extends AbstractImmutableSetTest {


    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance() {
        return ChampSet.of();
    }


    @Override
    protected <E> @NonNull Set<E> toMutableInstance(ImmutableSet<E> m) {
        return m.toMutable();
    }

    @Override
    protected <E> @NonNull ImmutableSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableChampSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull ImmutableSet<E> toClonedInstance(ImmutableSet<E> m) {
        return ChampSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance(Iterable<E> m) {
        return ChampSet.copyOf(m);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testToMutableAddAllWithImmutableTypeAndAllNewKeysShouldReturnTrue(@NonNull SetData data) throws Exception {
        ImmutableSet<Key> instance = newInstance(data.a);
        ImmutableSet<Key> instance2 = newInstance(data.c);
        MutableChampSet<Key> mutableInstance = (MutableChampSet<Key>) instance.toMutable();
        assertTrue(mutableInstance.addAll(instance2));

        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, toImmutableInstance(mutableInstance));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testOfArrayArgShouldYieldExpectedResult(@NonNull SetData data) throws Exception {
        ImmutableSet<Key> instance = ChampSet.of(data.a().toArray(new Key[0]));
        assertEqualSet(data.a, instance);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}