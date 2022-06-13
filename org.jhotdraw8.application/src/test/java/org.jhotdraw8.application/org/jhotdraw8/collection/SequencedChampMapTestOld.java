/*
 * @(#)SeqChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.Map;

public class SequencedChampMapTestOld extends AbstractSequencedMapTest {


    @Override
    protected @NonNull Map<HashCollider, HashCollider> of() {
        return new SequencedChampMap<>();
    }

    @Override
    protected @NonNull Map<HashCollider, HashCollider> copyOf(@NonNull Map<HashCollider, HashCollider> map) {
        return new SequencedChampMap<>(map);
    }

    @Override
    protected @NonNull Map<HashCollider, HashCollider> copyOf(@NonNull Collection<Map.Entry<HashCollider, HashCollider>> map) {
        return new SequencedChampMap<>(map);
    }
}
