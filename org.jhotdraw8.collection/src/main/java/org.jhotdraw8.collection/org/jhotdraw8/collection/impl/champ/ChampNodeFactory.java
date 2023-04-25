/*
 * @(#)NodeFactory.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;

/**
 * Provides factory methods for {@link ChampNode}s.
 */
class ChampNodeFactory {

    /**
     * Don't let anyone instantiate this class.
     */
    private ChampNodeFactory() {
    }

    static <K> @NonNull ChampBitmapIndexedNode<K> newBitmapIndexedNode(
            @Nullable IdentityObject mutator, int nodeMap,
            int dataMap, @NonNull Object[] nodes) {
        return mutator == null
                ? new ChampBitmapIndexedNode<>(nodeMap, dataMap, nodes)
                : new ChampMutableBitmapIndexedNode<>(mutator, nodeMap, dataMap, nodes);
    }

    static <K> @NonNull ChampHashCollisionNode<K> newHashCollisionNode(
            @Nullable IdentityObject mutator, int hash, @NonNull Object @NonNull [] entries) {
        return mutator == null
                ? new ChampHashCollisionNode<>(hash, entries)
                : new ChampMutableHashCollisionNode<>(mutator, hash, entries);
    }
}