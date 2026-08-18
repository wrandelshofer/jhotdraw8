/*
 * @(#)ChampTrie.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

/// Provides static utility methods for CHAMP tries.
public class ChampTrie {

    /// Don't let anyone instantiate this class.
    private ChampTrie() {
    }

    static <K, V> BitmapIndexedNode newBitmapIndexedNode(
            @Nullable IdentityObject mutator, int nodeMap,
            int dataMap, Object[] array, int ENTRY_LENGTH) {
        return mutator == null
                ? new BitmapIndexedNode(nodeMap, dataMap, array, ENTRY_LENGTH)
                : new MutableBitmapIndexedNode(mutator, nodeMap, dataMap, array, ENTRY_LENGTH);
    }

    static <K, V> HashCollisionNode newHashCollisionNode(
            @Nullable IdentityObject mutator, int hash, Object[] array, int entryLength) {
        return mutator == null
                ? new HashCollisionNode(hash, array)
                : new MutableHashCollisionNode(mutator, hash, array, entryLength);
    }

}