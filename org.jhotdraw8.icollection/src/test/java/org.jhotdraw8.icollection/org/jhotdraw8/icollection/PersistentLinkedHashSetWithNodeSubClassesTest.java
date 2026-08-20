/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentLinkedHashSetWithNodeSubClassesTest extends AbstractPersistentSequencedSetTest {


    @Override
    protected <E> PersistentLinkedHashSetWithNodeSubClasses<E> newInstance() {
        return PersistentLinkedHashSetWithNodeSubClasses.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentLinkedHashSetWithNodeSubClasses<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentLinkedHashSetWithNodeSubClasses<E> toImmutableInstance(Set<E> m) {
        return ((MutableLinkedHashSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentLinkedHashSetWithNodeSubClasses<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentLinkedHashSetWithNodeSubClasses.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentLinkedHashSetWithNodeSubClasses<E> newInstance(Iterable<E> m) {
        return PersistentLinkedHashSetWithNodeSubClasses.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return false;
    }

}