/*
 * @(#)SimpleMutableSequencedSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.SequencedSet;
import java.util.Set;

public class SimpleMutableSequencedSetTest extends AbstractSequencedSetTest {
    @Override
    protected <E> @NonNull SequencedSet<E> newInstance() {
        return new SimpleMutableSequencedSet<>();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(int numElements, float loadFactor) {
        return new SimpleMutableSequencedSet<>();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Set<E> m) {
        return new SimpleMutableSequencedSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySet<E> m) {
        return new SimpleMutableSequencedSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(Set<E> m) {
        return ((SimpleMutableSequencedSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(SequencedSet<E> m) {
        return new SimpleMutableSequencedSet<>(m);
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(ReadOnlySequencedSet<E> m) {
        return new SimpleMutableSequencedSet<>(m);
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toClonedInstance(SequencedSet<E> m) {
        return ((SimpleMutableSequencedSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull SequencedSet<E> newInstance(Iterable<E> m) {
        return new SimpleMutableSequencedSet<>(m);
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
