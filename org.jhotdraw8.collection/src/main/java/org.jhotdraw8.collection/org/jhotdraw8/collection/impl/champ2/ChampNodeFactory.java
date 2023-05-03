/*
 * @(#)NodeFactory.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ2;

import org.jhotdraw8.annotation.NonNull;

/**
 * Provides factory methods for {@link Node}s.
 */
class ChampNodeFactory {

    /**
     * Don't let anyone instantiate this class.
     */
    private ChampNodeFactory() {
    }

    static <K> @NonNull BitmapIndexedNode<K> newBitmapIndexedNode(
            int nodeMap,
            int dataMap, @NonNull Object[] nodes) {
        return new BitmapIndexedNode<>(nodeMap, dataMap, nodes);
    }

    static <K> @NonNull HashCollisionNode<K> newHashCollisionNode(
            int hash, @NonNull Object @NonNull [] entries) {
        return new HashCollisionNode<>(hash, entries);
    }
}