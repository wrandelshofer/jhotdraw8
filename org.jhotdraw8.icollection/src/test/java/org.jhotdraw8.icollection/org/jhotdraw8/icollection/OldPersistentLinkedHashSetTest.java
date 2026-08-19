/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class OldPersistentLinkedHashSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> OldPersistentLinkedHashSet<E> newInstance() {
        return OldPersistentLinkedHashSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((OldPersistentLinkedHashSet<E>) m).toMutable();
    }

    @Override
    protected <E> OldPersistentLinkedHashSet<E> toImmutableInstance(Set<E> m) {
        return ((OldMutableLinkedHashSet<E>) m).toPersistent();
    }

    @Override
    protected <E> OldPersistentLinkedHashSet<E> toClonedInstance(PersistentSet<E> m) {
        return OldPersistentLinkedHashSet.copyOf(m.asSet());
    }

    @Override
    protected <E> OldPersistentLinkedHashSet<E> newInstance(Iterable<E> m) {
        return OldPersistentLinkedHashSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return false;
    }

}