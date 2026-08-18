/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentLinkedHashElementSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> PersistentLinkedHashElementSet<E> newInstance() {
        return PersistentLinkedHashElementSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentLinkedHashElementSet<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentLinkedHashElementSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableLinkedHashElementSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentLinkedHashElementSet<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentLinkedHashElementSet.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentLinkedHashElementSet<E> newInstance(Iterable<E> m) {
        return PersistentLinkedHashElementSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return false;
    }

}