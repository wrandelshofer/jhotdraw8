/*
 * @(#)MutableVectorSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.pcollection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.pcollection.readonly.ReadOnlySet;
import org.jhotdraw8.pcollection.sequenced.SequencedSet;

import java.util.Set;

public class MutableVectorSetTest extends AbstractSequencedSetTest {
    @Override
    protected <E> @NonNull SequencedSet<E> newInstance() {
        return new MutableVectorSet<>();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(int numElements, float loadFactor) {
        return new MutableVectorSet<>();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Set<E> m) {
        return new MutableVectorSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySet<E> m) {
        return new MutableVectorSet<>(m);
    }

    @Override
    protected <E> @NonNull ImmutableSequencedSet<E> toImmutableInstance(Set<E> m) {
        return new MutableVectorSet<>(m).toImmutable();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(Set<E> m) {
        return ((MutableVectorSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(SequencedSet<E> m) {
        return new MutableVectorSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySequencedSet<E> m) {
        return new MutableVectorSet<>(m);
    }

    @Override
    protected <E> @NonNull ImmutableSequencedSet<E> toImmutableInstance(SequencedSet<E> m) {
        return ((MutableVectorSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(SequencedSet<E> m) {
        return ((MutableVectorSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Iterable<E> m) {
        return new MutableVectorSet<>(m);
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
}
