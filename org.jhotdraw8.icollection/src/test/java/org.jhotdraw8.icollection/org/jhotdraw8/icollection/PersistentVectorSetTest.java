/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentVectorSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> PersistentVectorSet<E> newInstance() {
        return PersistentVectorSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentVectorSet<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentVectorSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableVectorSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentVectorSet<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentVectorSet.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentVectorSet<E> newInstance(Iterable<E> m) {
        return PersistentVectorSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}