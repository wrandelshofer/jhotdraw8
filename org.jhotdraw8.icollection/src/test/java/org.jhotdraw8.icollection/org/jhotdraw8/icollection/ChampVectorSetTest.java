/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableSet;

import java.util.SequencedSet;
import java.util.Set;

public class ChampVectorSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> @NonNull ChampVectorSet<E> newInstance() {
        return ChampVectorSet.of();
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toMutableInstance(ImmutableSet<E> m) {
        return ((ChampVectorSet<E>) m).toMutable();
    }

    @Override
    protected <E> @NonNull ChampVectorSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableChampVectorSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull ChampVectorSet<E> toClonedInstance(ImmutableSet<E> m) {
        return ChampVectorSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull ChampVectorSet<E> newInstance(Iterable<E> m) {
        return ChampVectorSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}