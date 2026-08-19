/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentLinkedHashSetWithLinkedElementTestWithNodeSubClasses extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> PersistentLinkedHashSetWithLinkedElement<E> newInstance() {
        return PersistentLinkedHashSetWithLinkedElement.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentLinkedHashSetWithLinkedElement<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentLinkedHashSetWithLinkedElement<E> toImmutableInstance(Set<E> m) {
        return ((MutableLinkedHashSetWithLinkedElement<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentLinkedHashSetWithLinkedElement<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentLinkedHashSetWithLinkedElement.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentLinkedHashSetWithLinkedElement<E> newInstance(Iterable<E> m) {
        return PersistentLinkedHashSetWithLinkedElement.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return false;
    }

}