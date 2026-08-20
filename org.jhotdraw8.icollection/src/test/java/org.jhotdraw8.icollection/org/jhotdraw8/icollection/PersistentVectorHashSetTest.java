/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentVectorHashSetTest extends AbstractPersistentSequencedSetTest {


    @Override
    protected <E> PersistentVectorHashSet<E> newInstance() {
        return PersistentVectorHashSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentVectorHashSet<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentVectorHashSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableVectorHashSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentVectorHashSet<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentVectorHashSet.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentVectorHashSet<E> newInstance(Iterable<E> m) {
        return PersistentVectorHashSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}