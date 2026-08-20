/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentTreeSetTest extends AbstractPersistentNavigableSetTest {


    @Override
    protected <E> PersistentTreeSet<E> newInstance() {
        return PersistentTreeSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentTreeSet<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentTreeSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableTreeSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentTreeSet<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentTreeSet.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentTreeSet<E> newInstance(Iterable<E> m) {
        return PersistentTreeSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}