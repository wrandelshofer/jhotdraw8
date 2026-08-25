/*
 * @(#)ChampTrie.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt.impl.champmap;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jspecify.annotations.Nullable;

/// Provides static utility methods for CHAMP tries.
public class ChampTrie {

    /// Don't let anyone instantiate this class.
    private ChampTrie() {
    }

    static <K, V> BitmapIndexedNode<K, V> newBitmapIndexedNode(
            @Nullable MutabilityOwnership mutator, int nodeMap,
            int dataMap, Object[] nodes) {
        return mutator == null
                ? new BitmapIndexedNode<>(nodeMap, dataMap, nodes)
                : new MutableBitmapIndexedNode<>(mutator, nodeMap, dataMap, nodes);
    }

    static <K, V> HashCollisionNode<K, V> newHashCollisionNode(
            @Nullable MutabilityOwnership mutator, int hash, Object[] entries, int entryLength) {
        return mutator == null
                ? new HashCollisionNode<>(hash, entries)
                : new MutableHashCollisionNode<>(mutator, hash, entries, entryLength);
    }

}