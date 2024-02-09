/*
 * @(#)MutableVectorSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.SequencedSet;
import java.util.Set;

public class ReversedMutableVectorSetTest extends AbstractSequencedSetTest {
    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance() {
        return new MutableVectorSet<E>().reversed();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(int numElements, float loadFactor) {
        return new MutableVectorSet<E>().reversed();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Set<E> m) {
        return new MutableVectorSet<>(m).reversed();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySet<E> m) {
        MutableVectorSet<E> es = new MutableVectorSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m.asSet());
        return es1;
    }

    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(Set<E> m) {
        return ((MutableVectorSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(SequencedSet<E> m) {
        MutableVectorSet<E> es = new MutableVectorSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m);
        return es1;
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySequencedSet<E> m) {
        MutableVectorSet<E> es = new MutableVectorSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m.asSet());
        return es1;
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(SequencedSet<E> m) {
        return ((MutableVectorSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Iterable<E> m) {
        MutableVectorSet<E> es = new MutableVectorSet<>();
        SequencedSet<E> es1 = es.reversed();
        m.forEach(es1::add);
        return es1;
    }
}
