/*
 * @(#)MutableChampVectorSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.SequencedSet;
import java.util.Set;

public class ReversedMutableChampVectorSetTest extends AbstractSequencedSetTest {
    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance() {
        return new MutableChampVectorSet<E>().reversed();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(int numElements, float loadFactor) {
        return new MutableChampVectorSet<E>().reversed();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Set<E> m) {
        return new MutableChampVectorSet<>(m).reversed();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySet<E> m) {
        MutableChampVectorSet<E> es = new MutableChampVectorSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m.asSet());
        return es1;
    }

    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(Set<E> m) {
        return ((MutableChampVectorSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(SequencedSet<E> m) {
        MutableChampVectorSet<E> es = new MutableChampVectorSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m);
        return es1;
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySequencedSet<E> m) {
        MutableChampVectorSet<E> es = new MutableChampVectorSet<>();
        SequencedSet<E> es1 = es.reversed();
        es1.addAll(m.asSet());
        return es1;
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(SequencedSet<E> m) {
        return ((MutableChampVectorSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Iterable<E> m) {
        MutableChampVectorSet<E> es = new MutableChampVectorSet<>();
        SequencedSet<E> es1 = es.reversed();
        m.forEach(es1::add);
        return es1;
    }
}
