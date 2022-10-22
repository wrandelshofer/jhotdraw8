/*
 * @(#)ChampTrie.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.UniqueId;

/**
 * Provides factory methods for {@link Node}s.
 */
class NodeFactory {

    /**
     * Don't let anyone instantiate this class.
     */
    private NodeFactory() {
    }

    static <K> @NonNull BitmapIndexedNode<K> newBitmapIndexedNode(
            @Nullable UniqueId mutator, int nodeMap,
            int dataMap, @NonNull Object[] nodes) {
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