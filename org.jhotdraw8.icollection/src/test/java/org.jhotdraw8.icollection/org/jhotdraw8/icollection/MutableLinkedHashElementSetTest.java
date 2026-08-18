/*
 * @(#)MutableChampVectorSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jhotdraw8.icollection.readable.ReadableSet;

import java.util.SequencedSet;
import java.util.Set;

public class MutableLinkedHashElementSetTest extends AbstractSequencedSetTest {


    @Override
    protected <E> SequencedSet<E> newInstance() {
        return new MutableLinkedHashElementSet<>();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(int numElements, float loadFactor) {
        return new MutableLinkedHashElementSet<>();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(Set<E> m) {
        return new MutableLinkedHashElementSet<>(m);
    }

    @Override
    protected <E> SequencedSet<E> newInstance(ReadableSet<E> m) {
        return new MutableLinkedHashElementSet<>(m);
    }

    @Override
    protected <E> SequencedSet<E> toClonedInstance(Set<E> m) {
        return ((MutableLinkedHashElementSet<E>) m).clone();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(SequencedSet<E> m) {
        return new MutableLinkedHashElementSet<>(m);
    }

    @Override
    protected <E> SequencedSet<E> newInstance(ReadableSequencedSet<E> m) {
        return new MutableLinkedHashElementSet<>(m);
    }


    @Override
    protected <E> SequencedSet<E> toClonedInstance(SequencedSet<E> m) {
        return ((MutableLinkedHashElementSet<E>) m).clone();
    }

    @Override
    protected <E> SequencedSet<E> newInstance(Iterable<E> m) {
        return new MutableLinkedHashElementSet<>(m);
    }

    public void addingLastWithContainedElementShouldMoveElementToLast(SetData data) throws Exception {
        super.addLastWithContainedElementShouldMoveElementToLast(data);
    }

    public void iteratorRemoveShouldRemovingElement(SetData data) {
        super.iteratorRemoveShouldRemoveElement(data);
    }

    public void reversedAddingFirstWithContainedElementShouldMoveElementToLast(SetData data) throws Exception {
        super.reversedAddFirstWithContainedElementShouldMoveElementToLast(data);
    }

    @Override
    protected boolean supportsNullKeys() {
        return false;
    }

}
