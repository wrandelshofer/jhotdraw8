/*
 * @(#)SequencedChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractSequencedSetTest;
import org.jhotdraw8.collection.SetData;
import org.jhotdraw8.collection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.collection.sequenced.SequencedSet;
import org.junit.Ignore;

import java.util.Set;

@Ignore("BROKEN - MutableVectorSet is not fully implemented yet!")
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

    @Ignore("BROKEN")
    @Override
    public void addLastWithContainedElementShouldMoveElementToLast(@NonNull SetData data) throws Exception {
        super.addLastWithContainedElementShouldMoveElementToLast(data);
    }

    @Ignore("BROKEN")
    @Override
    public void iteratorRemoveShouldRemoveElement(@NonNull SetData data) {
        super.iteratorRemoveShouldRemoveElement(data);
    }

    @Ignore("BROKEN")
    @Override
    public void reversedAddFirstWithContainedElementShouldMoveElementToLast(@NonNull SetData data) throws Exception {
        super.reversedAddFirstWithContainedElementShouldMoveElementToLast(data);
    }
}
