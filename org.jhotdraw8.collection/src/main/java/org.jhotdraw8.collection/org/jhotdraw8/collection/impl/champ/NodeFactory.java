/*
 * @(#)NodeFactory.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.impl.IdentityObject;

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
            @Nullable IdentityObject owner, int nodeMap,
            int dataMap, @NonNull Object[] nodes) {
        return owner == null
                ? new BitmapIndexedNode<>(nodeMap, dataMap, nodes)
                : new MutableBitmapIndexedNode<>(owner, nodeMap, dataMap, nodes);
    }

    static <K> @NonNull HashCollisionNode<K> newHashCollisionNode(
            @Nullable IdentityObject owner, int hash, @NonNull Object @NonNull [] entries) {
        return owner == null
                ? new HashCollisionNode<>(hash, entries)
                : new MutableHashCollisionNode<>(owner, hash, entries);
    }
}