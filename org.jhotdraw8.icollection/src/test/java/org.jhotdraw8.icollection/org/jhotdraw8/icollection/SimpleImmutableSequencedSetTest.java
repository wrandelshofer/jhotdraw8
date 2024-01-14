/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableSet;

import java.util.SequencedSet;
import java.util.Set;

public class SimpleImmutableSequencedSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> @NonNull SimpleImmutableSequencedSet<E> newInstance() {
        return SimpleImmutableSequencedSet.of();
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toMutableInstance(ImmutableSet<E> m) {
        return ((SimpleImmutableSequencedSet<E>) m).toMutable();
    }

    @Override
    protected <E> @NonNull SimpleImmutableSequencedSet<E> toImmutableInstance(Set<E> m) {
        return ((SimpleMutableSequencedSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull SimpleImmutableSequencedSet<E> toClonedInstance(ImmutableSet<E> m) {
        return SimpleImmutableSequencedSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull SimpleImmutableSequencedSet<E> newInstance(Iterable<E> m) {
        return SimpleImmutableSequencedSet.copyOf(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}