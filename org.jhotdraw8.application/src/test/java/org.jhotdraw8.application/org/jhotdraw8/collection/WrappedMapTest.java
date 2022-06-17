package org.jhotdraw8.collection;

import java.util.HashMap;
import java.util.Map;

public class WrappedMapTest extends AbstractMapTest {
    @Override
    protected Map<HashCollider, HashCollider> newInstance() {
        return new WrappedMap<HashCollider, HashCollider>(new HashMap<>());
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(int numElements, float loadFactor) {
        return new WrappedMap<HashCollider, HashCollider>(new HashMap<>((int) (numElements / loadFactor), loadFactor));
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(Map<HashCollider, HashCollider> m) {
        return new WrappedMap<HashCollider, HashCollider>(new HashMap<>(m));
    }

    @Override
    protected Map<HashCollider, HashCollider> newInstance(ReadOnlyMap<HashCollider, HashCollider> m) {
        return new WrappedMap<HashCollider, HashCollider>(new HashMap<>(m.asMap()));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ImmutableMap<HashCollider, HashCollider> toImmutableInstance(Map<HashCollider, HashCollider> m) {
        return new WrappedImmutableMap<>(m,
                HashMap::new);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map<HashCollider, HashCollider> toClonedInstance(Map<HashCollider, HashCollider> m) {
        return new WrappedMap<HashCollider, HashCollider>(new HashMap<>(m));
    }

    @Override
    Map<HashCollider, HashCollider> newInstance(Iterable<Map.Entry<HashCollider, HashCollider>> m) {
        HashMap<HashCollider, HashCollider> i = new HashMap<>();
        for (Map.Entry<HashCollider, HashCollider> e : m) {
            i.put(e.getKey(), e.getValue());
        }
        return new WrappedMap<HashCollider, HashCollider>(i);
    }
}
