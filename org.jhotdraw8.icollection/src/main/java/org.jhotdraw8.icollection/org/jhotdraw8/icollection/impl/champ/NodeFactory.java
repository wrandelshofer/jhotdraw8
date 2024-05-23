/*
 * @(#)NodeFactory.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

/**
 * Provides factory methods for {@link Node}s.
 */
class NodeFactory {

    /**
     * Don't let anyone instantiate this class.
     */
    private NodeFactory() {
    }

    static <K> BitmapIndexedNode<K> newBitmapIndexedNode(
            @Nullable IdentityObject owner, int nodeMap,
            int dataMap, Object[] nodes) {
        return owner == null
                ? new BitmapIndexedNode<>(nodeMap, dataMap, nodes)
                : new MutableBitmapIndexedNode<>(owner, nodeMap, dataMap, nodes);
    }

    static <K> HashCollisionNode<K> newHashCollisionNode(
            @Nullable IdentityObject owner, int hash, Object[] entries) {
        return owner == null
                ? new HashCollisionNode<>(hash, entries)
                : new MutableHashCollisionNode<>(owner, hash, entries);
    }
}