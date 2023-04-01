/*
 * @(#)ImmutableSequencedChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractImmutableSequencedSetTest;
import org.jhotdraw8.collection.immutable.ImmutableSet;
import org.jhotdraw8.collection.sequenced.SequencedSet;

import java.util.Set;

public class ChampChampImmutableSequencedSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> @NonNull ChampChampImmutableSequencedSet<E> newInstance() {
        return ChampChampImmutableSequencedSet.of();
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toMutableInstance(ImmutableSet<E> m) {
        return ((ChampChampImmutableSequencedSet<E>) m).toMutable();
    }

    @Override
    protected <E> @NonNull ChampChampImmutableSequencedSet<E> toImmutableInstance(Set<E> m) {
        return ((ChampChampSequencedSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull ChampChampImmutableSequencedSet<E> toClonedInstance(ImmutableSet<E> m) {
        return ChampChampImmutableSequencedSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull ChampChampImmutableSequencedSet<E> newInstance(Iterable<E> m) {
        return ChampChampImmutableSequencedSet.copyOf(m);
    }


}