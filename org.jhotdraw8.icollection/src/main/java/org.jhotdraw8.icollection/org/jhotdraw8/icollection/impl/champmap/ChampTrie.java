/*
 * @(#)ChampTrie.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.impl.IdentityObject;

/**
 * Provides static utility methods for CHAMP tries.
 */
public class ChampTrie {

    /**
     * Don't let anyone instantiate this class.
     */
    private ChampTrie() {
    }

    static <K, V> @NonNull BitmapIndexedNode<K, V> newBitmapIndexedNode(
            @Nullable IdentityObject mutator, final int nodeMap,
            final int dataMap, final @NonNull Object[] nodes) {
        return mutator == null
                ? new BitmapIndexedNode<>(nodeMap, dataMap, nodes)
                : new MutableBitmapIndexedNode<>(mutator, nodeMap, dataMap, nodes);
    }

    static <K, V> @NonNull HashCollisionNode<K, V> newHashCollisionNode(
            @Nullable IdentityObject mutator, int hash, @NonNull Object @NonNull [] entries, int entryLength) {
        return mutator == null
                ? new HashCollisionNode<>(hash, entries)
                : new MutableHashCollisionNode<>(mutator, hash, entries, entryLength);
    }

}