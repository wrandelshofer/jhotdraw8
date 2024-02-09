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

public class MutableChampVectorSetTest extends AbstractSequencedSetTest {
    @Override
    protected <E> @NonNull SequencedSet<E> newInstance() {
        return new MutableChampVectorSet<>();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(int numElements, float loadFactor) {
        return new MutableChampVectorSet<>();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Set<E> m) {
        return new MutableChampVectorSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySet<E> m) {
        return new MutableChampVectorSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(Set<E> m) {
        return ((MutableChampVectorSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(SequencedSet<E> m) {
        return new MutableChampVectorSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySequencedSet<E> m) {
        return new MutableChampVectorSet<>(m);
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(SequencedSet<E> m) {
        return ((MutableChampVectorSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Iterable<E> m) {
        return new MutableChampVectorSet<>(m);
    }

    @Override
    public void addLastWithContainedElementShouldMoveElementToLast(@NonNull SetData data) throws Exception {
        super.addLastWithContainedElementShouldMoveElementToLast(data);
    }

    @Override
    public void iteratorRemoveShouldRemoveElement(@NonNull SetData data) {
        super.iteratorRemoveShouldRemoveElement(data);
    }

    @Override
    public void reversedAddFirstWithContainedElementShouldMoveElementToLast(@NonNull SetData data) throws Exception {
        super.reversedAddFirstWithContainedElementShouldMoveElementToLast(data);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
