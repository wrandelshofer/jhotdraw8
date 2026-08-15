/*
 * @(#)MutableChampVectorSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt;

import org.jhotdraw8.icollection.AbstractSequencedSetTest;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jhotdraw8.icollection.readable.ReadableSet;

import java.util.SequencedSet;
import java.util.Set;

public class ReversedMutablePersistentBMTrieHashSetTest extends AbstractSequencedSetTest {
    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    @Override
    protected <E> SequencedSet<E> newInstance() {
        return new MutableBMTrieHashSet<E>().reversed();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(int numElements, float loadFactor) {
        return new MutableBMTrieHashSet<E>().reversed();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(Set<E> m) {
        return new MutableBMTrieHashSet<>(m).reversed();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(ReadableSet<E> m) {
        MutableBMTrieHashSet<E> es = new MutableBMTrieHashSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m.asSet());
        return es1;
    }

    @Override
    protected <E> SequencedSet<E> toClonedInstance(Set<E> m) {
        return ((MutableBMTrieHashSet<E>) m).clone();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(SequencedSet<E> m) {
        MutableBMTrieHashSet<E> es = new MutableBMTrieHashSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m);
        return es1;
    }

    @Override
    protected <E> SequencedSet<E> newInstance(ReadableSequencedSet<E> m) {
        MutableBMTrieHashSet<E> es = new MutableBMTrieHashSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m.asSet());
        return es1;
    }


    @Override
    protected <E> SequencedSet<E> toClonedInstance(SequencedSet<E> m) {
        return ((MutableBMTrieHashSet<E>) m).clone();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(Iterable<E> m) {
        MutableBMTrieHashSet<E> es = new MutableBMTrieHashSet<>();
        SequencedSet<E> es1 = es.reversed();
        m.forEach(es1::add);
        return es1;
    }
}
