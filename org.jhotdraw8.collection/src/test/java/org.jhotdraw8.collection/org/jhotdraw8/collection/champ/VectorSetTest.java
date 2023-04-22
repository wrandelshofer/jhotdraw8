/*
 * @(#)ImmutableVectorChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractImmutableSequencedSetTest;
import org.jhotdraw8.collection.SetData;
import org.jhotdraw8.collection.immutable.ImmutableSet;
import org.jhotdraw8.collection.sequenced.SequencedSet;
import org.junit.Ignore;

import java.util.Set;

@Ignore("BROKEN - VectorSet is not fully implemented yet!")
public class VectorSetTest extends AbstractImmutableSequencedSetTest {


    @Override
    protected <E> @NonNull VectorSet<E> newInstance() {
        return VectorSet.of();
    }


    @Override
    protected <E> @NonNull SequencedSet<E> toMutableInstance(ImmutableSet<E> m) {
        return ((VectorSet<E>) m).toMutable();
    }

    @Override
    protected <E> @NonNull VectorSet<E> toImmutableInstance(Set<E> m) {
        return ((MutableVectorSet<E>) m).toImmutable();
    }

    @Override
    protected <E> @NonNull VectorSet<E> toClonedInstance(ImmutableSet<E> m) {
        return VectorSet.copyOf(m.asSet());
    }

    @Override
    protected <E> @NonNull VectorSet<E> newInstance(Iterable<E> m) {
        return VectorSet.copyOf(m);
    }

    @Ignore("BROKEN")
    @Override
    public void copyRemoveWithContainedKeyShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        super.copyRemoveWithContainedKeyShouldReturnNewInstance(data);
    }

    @Ignore("BROKEN")
    @Override
    public void copyAddWithNewElementShouldReturnNewInstance(@NonNull SetData data) throws Exception {
        super.copyAddWithNewElementShouldReturnNewInstance(data);
    }
}