/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Arrays;
import java.util.Map;

public class ImmutableChampMapTest extends AbstractImmutableMapTest {
    @Override
    protected @NonNull ImmutableChampMap<HashCollider, HashCollider> of() {
        return ImmutableChampMap.of();
    }

    @Override
    @SafeVarargs
    protected final @NonNull ImmutableMap<HashCollider, HashCollider> of(Map.@NonNull Entry<HashCollider, HashCollider>... entries) {
        return ImmutableChampMap.<HashCollider, HashCollider>of().copyPutAll(Arrays.asList(entries));
    }

    @Override
    protected @NonNull ImmutableMap<HashCollider, HashCollider> copyOf(@NonNull Map<? extends HashCollider, ? extends HashCollider> map) {
        return ImmutableChampMap.<HashCollider, HashCollider>of().copyPutAll(map);
    }

    @Override
    protected @NonNull ImmutableMap<HashCollider, HashCollider> copyOf(@NonNull ReadOnlyMap<? extends HashCollider, ? extends HashCollider> map) {
        return ImmutableChampMap.<HashCollider, HashCollider>of().copyPutAll(map);
    }

    @Override
    protected @NonNull ImmutableMap<HashCollider, HashCollider> copyOf(@NonNull Iterable<? extends Map.Entry<? extends HashCollider, ? extends HashCollider>> entries) {
        return ImmutableChampMap.<HashCollider, HashCollider>of().copyPutAll(entries);
    }
}
