package org.jhotdraw8.collection;

import java.util.Map;

public class ChampMapTest extends AbstractMapTest {
    @Override
    protected Map<HashCollider, HashCollider> newInstance() {
        return new ChampMap<>();
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(int numElements, float loadFactor) {
        return new ChampMap<>();
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(Map<HashCollider, HashCollider> m) {
        return new ChampMap<>(m);
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(ReadOnlyMap<HashCollider, HashCollider> m) {
        return new ChampMap<>(m);
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(Iterable<Map.Entry<HashCollider, HashCollider>> m) {
        return new ChampMap<>(m);
    }

    @Override
    protected ImmutableChampMap<HashCollider, HashCollider> toImmutableInstance(Map<HashCollider, HashCollider> m) {
        return ((ChampMap<HashCollider, HashCollider>) m).toImmutable();
    }

    @Override
    protected Map<HashCollider, HashCollider> toClonedInstance(Map<HashCollider, HashCollider> m) {
        return ((ChampMap<HashCollider, HashCollider>) m).clone();
    }


}
