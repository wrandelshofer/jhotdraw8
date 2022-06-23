/*
 * @(#)ImmutableSequencedChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractImmutableSequencedSetTest;
import org.jhotdraw8.collection.SequencedSet;
import org.jhotdraw8.collection.immutable.ImmutableSet;

import java.util.Set;

public class ChampImmutableSequencedSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> @NonNull ChampImmutableSequencedSet<E> newInstance() {
        return ChampImmutableSequencedSet.of();
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toMutableInstance(ImmutableSet<E> m) {
        return ((ChampImmutableSequencedSet<E>) m).toMutable();
    }

    @Override
    protected <E> @NonNull ChampImmutableSequencedSet<E> toImmutableInstance(Set<E> m) {
        return ((ChampSequencedSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull ChampImmutableSequencedSet<E> toClonedInstance(ImmutableSet<E> m) {
        return ChampImmutableSequencedSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull ChampImmutableSequencedSet<E> newInstance(Iterable<E> m) {
        return ChampImmutableSequencedSet.copyOf(m);
    }


}