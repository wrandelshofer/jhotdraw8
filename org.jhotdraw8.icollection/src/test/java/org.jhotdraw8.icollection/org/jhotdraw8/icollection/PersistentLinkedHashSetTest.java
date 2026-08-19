/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentLinkedHashSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> PersistentLinkedHashSet<E> newInstance() {
        return PersistentLinkedHashSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentLinkedHashSet<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentLinkedHashSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableLinkedHashSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentLinkedHashSet<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentLinkedHashSet.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentLinkedHashSet<E> newInstance(Iterable<E> m) {
        return PersistentLinkedHashSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return false;
    }

}