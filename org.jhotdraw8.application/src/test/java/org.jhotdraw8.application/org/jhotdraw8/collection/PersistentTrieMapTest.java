package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Map;

public class PersistentTrieMapTest extends AbstractPersistentMapTest {
    @Override
    protected @NonNull PersistentTrie5Map<HashCollider, HashCollider> of() {
        return PersistentTrie5Map.of();
    }

    @Override
    protected @NonNull PersistentTrie5Map<HashCollider, HashCollider> of(Map.@NonNull Entry<HashCollider, HashCollider>... entries) {
        return PersistentTrie5Map.of(entries);
    }

    @Override
    protected @NonNull PersistentTrie5Map<HashCollider, HashCollider> copyOf(@NonNull Map<? extends HashCollider, ? extends HashCollider> map) {
        return PersistentTrie5Map.copyOf(map);
    }

    @Override
    protected @NonNull PersistentTrie5Map<HashCollider, HashCollider> copyOf(@NonNull ReadOnlyMap<? extends HashCollider, ? extends HashCollider> map) {
        return PersistentTrie5Map.copyOf(map);
    }

    @Override
    protected @NonNull PersistentTrie5Map<HashCollider, HashCollider> copyOf(@NonNull Iterable<? extends Map.Entry<? extends HashCollider, ? extends HashCollider>> entries) {
        return PersistentTrie5Map.of(entries);
    }
}
