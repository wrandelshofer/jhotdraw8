/*
 * @(#)ImmutableSeqChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Set;

public class ImmutableSequencedChampSetTest extends AbstractImmutableSequencedSetTest {

    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance() {
        return ImmutableChampSet.of();
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
        return ImmutableChampSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull ImmutableSet<E> newInstance(Iterable<E> m) {
        return ImmutableChampSet.copyOf(m);
    }

}
