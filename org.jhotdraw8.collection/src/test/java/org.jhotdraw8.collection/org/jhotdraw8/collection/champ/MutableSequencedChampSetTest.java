/*
 * @(#)SequencedChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractSequencedSetTest;
import org.jhotdraw8.collection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.collection.sequenced.SequencedSet;

import java.util.Set;

public class MutableSequencedChampSetTest extends AbstractSequencedSetTest {
    @Override
    protected <E> @NonNull SequencedSet<E> newInstance() {
        return new MutableSequencedChampSet<>();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(int numElements, float loadFactor) {
        return new MutableSequencedChampSet<>();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Set<E> m) {
        return new MutableSequencedChampSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySet<E> m) {
        return new MutableSequencedChampSet<>(m);
    }

    @Override
    protected <E> @NonNull ImmutableSequencedSet<E> toImmutableInstance(Set<E> m) {
        return new MutableSequencedChampSet<>(m).toImmutable();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(Set<E> m) {
        return ((MutableSequencedChampSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(SequencedSet<E> m) {
        return new MutableSequencedChampSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySequencedSet<E> m) {
        return new MutableSequencedChampSet<>(m);
    }

    @Override
    protected <E> @NonNull ImmutableSequencedSet<E> toImmutableInstance(SequencedSet<E> m) {
        return ((MutableSequencedChampSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(SequencedSet<E> m) {
        return ((MutableSequencedChampSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Iterable<E> m) {
        return new MutableSequencedChampSet<>(m);
    }
}
