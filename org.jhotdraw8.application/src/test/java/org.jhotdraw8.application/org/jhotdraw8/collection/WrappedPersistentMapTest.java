/*
 * @(#)PersistentTrieMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Map;

public class WrappedPersistentMapTest extends AbstractPersistentMapTest {

    @Override
    protected @NonNull WrappedPersistentMap<HashCollider, HashCollider> of() {
        return WrappedPersistentMap.of();
    }


    @Override
    @SafeVarargs
    protected final @NonNull WrappedPersistentMap<HashCollider, HashCollider> of(Map.@NonNull Entry<HashCollider, HashCollider>... entries) {
        return WrappedPersistentMap.of(entries);
    }


    @Override
    protected @NonNull WrappedPersistentMap<HashCollider, HashCollider> copyOf(@NonNull Map<? extends HashCollider, ? extends HashCollider> map) {
        return WrappedPersistentMap.copyOf(map);
    }


    @Override
    protected @NonNull WrappedPersistentMap<HashCollider, HashCollider> copyOf(@NonNull ReadOnlyMap<? extends HashCollider, ? extends HashCollider> map) {
        return WrappedPersistentMap.copyOf(map);
    }


    @Override
    protected @NonNull WrappedPersistentMap<HashCollider, HashCollider> copyOf(@NonNull Iterable<? extends Map.Entry<? extends HashCollider, ? extends HashCollider>> entries) {
        return WrappedPersistentMap.ofEntries(entries);

    }
}
