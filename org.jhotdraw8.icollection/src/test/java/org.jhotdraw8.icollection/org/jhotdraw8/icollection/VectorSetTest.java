/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableSet;

import java.util.SequencedSet;
import java.util.Set;

public class VectorSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> @NonNull VectorSet<E> newInstance() {
        return VectorSet.of();
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toMutableInstance(ImmutableSet<E> m) {
        return ((VectorSet<E>) m).toMutable();
    }

    @Override
    protected <E> @NonNull VectorSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableVectorSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull VectorSet<E> toClonedInstance(ImmutableSet<E> m) {
        return VectorSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull VectorSet<E> newInstance(Iterable<E> m) {
        return VectorSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}