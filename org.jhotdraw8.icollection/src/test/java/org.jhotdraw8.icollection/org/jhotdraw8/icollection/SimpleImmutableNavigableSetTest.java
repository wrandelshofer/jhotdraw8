/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableSet;

import java.util.SequencedSet;
import java.util.Set;

public class SimpleImmutableNavigableSetTest extends AbstractImmutableNavigableSetTest {


    @Override
    protected <E> @NonNull SimpleImmutableNavigableSet<E> newInstance() {
        return SimpleImmutableNavigableSet.of();
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toMutableInstance(ImmutableSet<E> m) {
        return ((SimpleImmutableNavigableSet<E>) m).toMutable();
    }

    @Override
    protected <E> @NonNull SimpleImmutableNavigableSet<E> toImmutableInstance(Set<E> m) {
        return ((SimpleMutableNavigableSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull SimpleImmutableNavigableSet<E> toClonedInstance(ImmutableSet<E> m) {
        return SimpleImmutableNavigableSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull SimpleImmutableNavigableSet<E> newInstance(Iterable<E> m) {
        return SimpleImmutableNavigableSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}