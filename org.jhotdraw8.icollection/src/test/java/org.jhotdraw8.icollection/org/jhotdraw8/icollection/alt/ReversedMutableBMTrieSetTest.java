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

public class ReversedMutableBMTrieSetTest extends AbstractSequencedSetTest {
    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    @Override
    protected <E> SequencedSet<E> newInstance() {
        return new MutableBMTrieSet<E>().reversed();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(int numElements, float loadFactor) {
        return new MutableBMTrieSet<E>().reversed();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(Set<E> m) {
        return new MutableBMTrieSet<>(m).reversed();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(ReadableSet<E> m) {
        MutableBMTrieSet<E> es = new MutableBMTrieSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m.asSet());
        return es1;
    }

    @Override
    protected <E> SequencedSet<E> toClonedInstance(Set<E> m) {
        return ((MutableBMTrieSet<E>) m).clone();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(SequencedSet<E> m) {
        MutableBMTrieSet<E> es = new MutableBMTrieSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m);
        return es1;
    }

    @Override
    protected <E> SequencedSet<E> newInstance(ReadableSequencedSet<E> m) {
        MutableBMTrieSet<E> es = new MutableBMTrieSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m.asSet());
        return es1;
    }


    @Override
    protected <E> SequencedSet<E> toClonedInstance(SequencedSet<E> m) {
        return ((MutableBMTrieSet<E>) m).clone();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(Iterable<E> m) {
        MutableBMTrieSet<E> es = new MutableBMTrieSet<>();
        SequencedSet<E> es1 = es.reversed();
        m.forEach(es1::add);
        return es1;
    }
}
