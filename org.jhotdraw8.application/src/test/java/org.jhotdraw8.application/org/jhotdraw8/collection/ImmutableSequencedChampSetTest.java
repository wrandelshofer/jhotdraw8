/*
 * @(#)PersistentChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Set;

public class ImmutableSequencedChampSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> @NonNull ImmutableSequencedChampSet<E> newInstance() {
        return ImmutableSequencedChampSet.of();
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toMutableInstance(ImmutableSet<E> m) {
        return ((ImmutableSequencedChampSet<E>) m).toMutable();
    }

    @Override
    protected <E> @NonNull ImmutableSequencedChampSet<E> toImmutableInstance(Set<E> m) {
        return ((SequencedChampSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull ImmutableSequencedChampSet<E> toClonedInstance(ImmutableSet<E> m) {
        return ImmutableSequencedChampSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull ImmutableSequencedChampSet<E> newInstance(Iterable<E> m) {
        return ImmutableSequencedChampSet.copyOf(m);
    }


}