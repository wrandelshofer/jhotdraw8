/*
 * @(#)SeqChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import java.util.Set;

public class SequencedChampSetTest extends AbstractSequencedSetTest {
    @Override
    protected SequencedSet<HashCollider> newInstance() {
        return new SequencedChampSet<>();
    }

    @Override
    protected SequencedSet<HashCollider> newInstance(int numElements, float loadFactor) {
        return new SequencedChampSet<>();
    }

    @Override
    protected SequencedSet<HashCollider> newInstance(Set<HashCollider> m) {
        return new SequencedChampSet<>(m);
    }

    @Override
    protected SequencedSet<HashCollider> newInstance(ReadOnlySet<HashCollider> m) {
        return new SequencedChampSet<>(m);
    }

    @Override
    protected ImmutableSequencedSet<HashCollider> toImmutableInstance(Set<HashCollider> m) {
        return new SequencedChampSet<>(m).toImmutable();
    }

    @Override
    protected SequencedSet<HashCollider> toClonedInstance(Set<HashCollider> m) {
        return ((SequencedChampSet<HashCollider>) m).clone();
    }

    @Override
    protected SequencedSet<HashCollider> newInstance(SequencedSet<HashCollider> m) {
        return new SequencedChampSet<>(m);
    }

    @Override
    protected SequencedSet<HashCollider> newInstance(ReadOnlySequencedSet<HashCollider> m) {
        return new SequencedChampSet<>(m);
    }

    @Override
    protected ImmutableSequencedSet<HashCollider> toImmutableInstance(SequencedSet<HashCollider> m) {
        return ((SequencedChampSet<HashCollider>) m).toImmutable();
    }

    @Override
    protected SequencedSet<HashCollider> toClonedInstance(SequencedSet<HashCollider> m) {
        return ((SequencedChampSet<HashCollider>) m).clone();
    }

    @Override
    protected SequencedSet<HashCollider> newInstance(Iterable<HashCollider> m) {
        return new SequencedChampSet<>(m);
    }
}
