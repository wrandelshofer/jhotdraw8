/*
 * @(#)ImmutableSequencedChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableSet;
import org.jhotdraw8.collection.sequenced.SequencedSet;

import java.util.Set;

public class SequencedChampSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> @NonNull SequencedChampSet<E> newInstance() {
        return SequencedChampSet.of();
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toMutableInstance(ImmutableSet<E> m) {
        return ((SequencedChampSet<E>) m).toMutable();
    }

    @Override
    protected <E> @NonNull SequencedChampSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableSequencedChampSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull SequencedChampSet<E> toClonedInstance(ImmutableSet<E> m) {
        return SequencedChampSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull SequencedChampSet<E> newInstance(Iterable<E> m) {
        return SequencedChampSet.copyOf(m);
    }


}