/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt;

import org.jhotdraw8.icollection.AbstractPersistentSequencedSetTest;
import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentBMTrieSetTest extends AbstractPersistentSequencedSetTest {


    @Override
    protected <E> PersistentBMTrieSet<E> newInstance() {
        return PersistentBMTrieSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentBMTrieSet<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentBMTrieSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableBMTrieSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentBMTrieSet<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentBMTrieSet.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentBMTrieSet<E> newInstance(Iterable<E> m) {
        return PersistentBMTrieSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}