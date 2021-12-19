package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Map;

public class PersistentTrie6MapTest extends AbstractPersistentMapTest {
    @Override
    protected @NonNull PersistentMap<HashCollider, HashCollider> of() {
        return PersistentTrie6Map.of();
    }

    @Override
    protected @NonNull PersistentMap<HashCollider, HashCollider> of(Map.@NonNull Entry<HashCollider, HashCollider>... entries) {
        return PersistentTrie6Map.of(entries);
    }

    @Override
    protected @NonNull PersistentMap<HashCollider, HashCollider> copyOf(@NonNull Map<? extends HashCollider, ? extends HashCollider> map) {
        return PersistentTrie6Map.of(map);
    }

    @Override
    protected @NonNull PersistentMap<HashCollider, HashCollider> copyOf(@NonNull ReadOnlyMap<? extends HashCollider, ? extends HashCollider> map) {
        return PersistentTrie6Map.of(map);
    }

    @Override
    protected @NonNull PersistentMap<HashCollider, HashCollider> copyOf(@NonNull Iterable<? extends Map.Entry<? extends HashCollider, ? extends HashCollider>> entries) {
        return PersistentTrie6Map.of(entries);
    }
}
