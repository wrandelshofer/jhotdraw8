/*
 * @(#)ChampTrie.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.UniqueId;

/**
 * Provides static utility methods for CHAMP tries.
 */
class ChampTrie {

    /**
     * Don't let anyone instantiate this class.
     */
    private ChampTrie() {
    }

    static <K> @NonNull BitmapIndexedNode<K> newBitmapIndexedNode(
            @Nullable UniqueId mutator, final int nodeMap,
            final int dataMap, final @NonNull Object[] nodes) {
        return mutator == null
                ? new BitmapIndexedNode<>(nodeMap, dataMap, nodes)
                : new MutableBitmapIndexedNode<>(mutator, nodeMap, dataMap, nodes);
    }

    static <K> @NonNull HashCollisionNode<K> newHashCollisionNode(
            @Nullable UniqueId mutator, int hash, @NonNull Object @NonNull [] entries) {
        return mutator == null
                ? new HashCollisionNode<>(hash, entries)
                : new MutableHashCollisionNode<>(mutator, hash, entries);
    }
}