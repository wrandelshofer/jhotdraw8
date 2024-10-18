/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class ChampVectorSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> ChampVectorSet<E> newInstance() {
        return ChampVectorSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((ChampVectorSet<E>) m).toMutable();
    }

    @Override
    protected <E> ChampVectorSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableChampVectorSet<E>) m).toPersistent();
    }

    @Override
    protected <E> ChampVectorSet<E> toClonedInstance(PersistentSet<E> m) {
        return ChampVectorSet.copyOf(m.asSet());
    }

    @Override
    protected <E> ChampVectorSet<E> newInstance(Iterable<E> m) {
        return ChampVectorSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}