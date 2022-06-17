package org.jhotdraw8.collection;

import java.util.Set;

public class ChampSetTest extends AbstractSetTest {
    @Override
    protected Set<HashCollider> newInstance() {
        return new ChampSet<>();
    }

    @Override
    protected Set<HashCollider> newInstance(int numElements, float loadFactor) {
        return new ChampSet<>();
    }

    @Override
    protected Set<HashCollider> newInstance(Set<HashCollider> m) {
        return new ChampSet<>(m);
    }

    @Override
    protected Set<HashCollider> newInstance(ReadOnlySet<HashCollider> m) {
        return new ChampSet<>(m);
    }

    @Override
    protected ImmutableSet<HashCollider> toImmutableInstance(Set<HashCollider> m) {
        return ((ChampSet<HashCollider>) m).toImmutable();
    }

    @Override
    protected Set<HashCollider> toClonedInstance(Set<HashCollider> m) {
        return ((ChampSet<HashCollider>) m).clone();
    }

    @Override
    protected Set<HashCollider> newInstance(Iterable<HashCollider> m) {
        return new ChampSet<>(m);
    }
}
