/*
 * @(#)ImmutableChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OldPersistentHashSetTest extends AbstractImmutableSetTest {


    @Override
    protected <E> PersistentSet<E> newInstance() {
        return OldPersistentHashSet.of();
    }


    @Override
    protected <E> Set<E> toMutableInstance(PersistentSet<E> m) {
        return m.toMutable();
    }

    @Override
    protected <E> PersistentSet<E> toImmutableInstance(Set<E> m) {
        return ((OldMutableHashSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentSet<E> toClonedInstance(PersistentSet<E> m) {
        return OldPersistentHashSet.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentSet<E> newInstance(Iterable<E> m) {
        return OldPersistentHashSet.copyOf(m);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testToMutableAddingAllWithImmutableTypeAndAllNewKeysShouldReturnTrue(SetData data) throws Exception {
        PersistentSet<Key> instance = newInstance(data.a);
        PersistentSet<Key> instance2 = newInstance(data.c);
        OldMutableHashSet<Key> mutableInstance = (OldMutableHashSet<Key>) instance.toMutable();
        assertTrue(mutableInstance.addAll(instance2));

        LinkedHashSet<Key> expected = new LinkedHashSet<>(data.a.asSet());
        expected.addAll(data.c.asSet());
        assertEqualSet(expected, toImmutableInstance(mutableInstance));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testOfArrayArgShouldYieldExpectedResult(SetData data) throws Exception {
        PersistentSet<Key> instance = OldPersistentHashSet.of(data.a().toArray(new Key[0]));
        assertEqualSet(data.a, instance);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}