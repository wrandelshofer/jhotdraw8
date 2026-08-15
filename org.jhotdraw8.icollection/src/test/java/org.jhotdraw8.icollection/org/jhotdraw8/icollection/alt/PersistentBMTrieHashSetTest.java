/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt;

import org.jhotdraw8.icollection.AbstractImmutableSequencedSetTest;
import org.jhotdraw8.icollection.persistent.PersistentSet;

import java.util.SequencedSet;
import java.util.Set;

public class PersistentBMTrieHashSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> PersistentBMTrieHashSet<E> newInstance() {
        return PersistentBMTrieHashSet.of();
    }


    @Override
    protected <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m) {
        return ((PersistentBMTrieHashSet<E>) m).toMutable();
    }

    @Override
    protected <E> PersistentBMTrieHashSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableBMTrieHashSet<E>) m).toPersistent();
    }

    @Override
    protected <E> PersistentBMTrieHashSet<E> toClonedInstance(PersistentSet<E> m) {
        return PersistentBMTrieHashSet.copyOf(m.asSet());
    }

    @Override
    protected <E> PersistentBMTrieHashSet<E> newInstance(Iterable<E> m) {
        return PersistentBMTrieHashSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}