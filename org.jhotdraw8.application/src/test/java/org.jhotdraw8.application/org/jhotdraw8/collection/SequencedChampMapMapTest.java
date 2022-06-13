package org.jhotdraw8.collection;

import java.util.Map;

public class SequencedChampMapMapTest extends AbstractMapTest {
    @Override
    protected Map<HashCollider, HashCollider> newInstance() {
        return new SequencedChampMap<>();
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(int numElements, float loadFactor) {
        return new SequencedChampMap<>();
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(Map<HashCollider, HashCollider> m) {
        return new SequencedChampMap<>(m);
    }

    @Override

    protected Map<HashCollider, HashCollider> newInstance(ReadOnlyMap<HashCollider, HashCollider> m) {
        return new SequencedChampMap<>(m);
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(Iterable<Map.Entry<HashCollider, HashCollider>> m) {
        return new SequencedChampMap<>(m);
    }

    @Override
    protected ImmutableMap<HashCollider, HashCollider> toImmutableInstance(Map<HashCollider, HashCollider> m) {
        return ((SequencedChampMap<HashCollider, HashCollider>) m).toImmutable();
    }

    @Override
    protected Map<HashCollider, HashCollider> toClonedInstance(Map<HashCollider, HashCollider> m) {
        return ((SequencedChampMap<HashCollider, HashCollider>) m).clone();
    }
}
