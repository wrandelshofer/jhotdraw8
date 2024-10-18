/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class RedBlackSetTest extends AbstractImmutableNavigableSetTest {


    @Override
    protected <E> RedBlackSet<E> newInstance() {
        return RedBlackSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((RedBlackSet<E>) m).toMutable();
    }

    @Override
    protected <E> RedBlackSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableRedBlackSet<E>) m).toPersistent();
    }

    @Override
    protected <E> RedBlackSet<E> toClonedInstance(PersistentSet<E> m) {
        return RedBlackSet.copyOf(m.asSet());
    }

    @Override
    protected <E> RedBlackSet<E> newInstance(Iterable<E> m) {
        return RedBlackSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}