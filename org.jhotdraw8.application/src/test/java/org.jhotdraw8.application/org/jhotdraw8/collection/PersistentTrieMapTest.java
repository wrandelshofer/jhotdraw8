/*
 * @(#)PersistentTrieMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Map;

public class PersistentTrieMapTest extends AbstractPersistentMapTest {
    @Override
    protected @NonNull PersistentTrieMap<HashCollider, HashCollider> of() {
        return PersistentTrieMap.of();
    }

    @Override
    @SafeVarargs
    protected final @NonNull PersistentTrieMap<HashCollider, HashCollider> of(Map.@NonNull Entry<HashCollider, HashCollider>... entries) {
        return PersistentTrieMap.ofEntries(entries);
    }

    @Override
    protected @NonNull PersistentTrieMap<HashCollider, HashCollider> copyOf(@NonNull Map<? extends HashCollider, ? extends HashCollider> map) {
        return PersistentTrieMap.copyOf(map);
    }

    @Override
    protected @NonNull PersistentTrieMap<HashCollider, HashCollider> copyOf(@NonNull ReadOnlyMap<? extends HashCollider, ? extends HashCollider> map) {
        return PersistentTrieMap.copyOf(map);
    }

    @Override
    protected @NonNull PersistentTrieMap<HashCollider, HashCollider> copyOf(@NonNull Iterable<? extends Map.Entry<? extends HashCollider, ? extends HashCollider>> entries) {
        return PersistentTrieMap.ofEntries(entries);
    }
}
