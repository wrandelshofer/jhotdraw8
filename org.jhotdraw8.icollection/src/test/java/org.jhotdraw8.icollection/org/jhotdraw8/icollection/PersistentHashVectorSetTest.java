/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentHashVectorSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> PersistentHashVectorSet<E> newInstance() {
        return PersistentHashVectorSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentHashVectorSet<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentHashVectorSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableChampVectorSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentHashVectorSet<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentHashVectorSet.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentHashVectorSet<E> newInstance(Iterable<E> m) {
        return PersistentHashVectorSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}