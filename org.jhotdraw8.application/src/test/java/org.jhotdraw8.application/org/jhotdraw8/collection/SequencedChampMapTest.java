package org.jhotdraw8.collection;

import java.util.Map;

public class SequencedChampMapTest extends AbstractSequencedMapTest {
    @Override
    protected SequencedChampMap<HashCollider, HashCollider> newInstance() {
        return new SequencedChampMap<>();
    }

    @Override
    protected SequencedChampMap<HashCollider, HashCollider> newInstance(int numElements, float loadFactor) {
        return new SequencedChampMap<>();
    }

    @Override
    protected SequencedChampMap<HashCollider, HashCollider> newInstance(Map<HashCollider, HashCollider> m) {
        return new SequencedChampMap<>(m);
    }

    @Override

    protected SequencedChampMap<HashCollider, HashCollider> newInstance(ReadOnlyMap<HashCollider, HashCollider> m) {
        return new SequencedChampMap<>(m);
    }

    @Override
    protected SequencedChampMap<HashCollider, HashCollider> newInstance(Iterable<Map.Entry<HashCollider, HashCollider>> m) {
        return new SequencedChampMap<>(m);
    }

    @Override
    protected ImmutableSequencedMap<HashCollider, HashCollider> toImmutableInstance(Map<HashCollider, HashCollider> m) {
        return ((SequencedChampMap<HashCollider, HashCollider>) m).toImmutable();
    }

    @Override
    protected SequencedMap<HashCollider, HashCollider> toClonedInstance(Map<HashCollider, HashCollider> m) {
        return ((SequencedChampMap<HashCollider, HashCollider>) m).clone();
    }
}
