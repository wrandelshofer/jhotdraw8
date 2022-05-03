/*
 * @(#)MutableBitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champtrie;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.UniqueIdentity;

final class MutableBitmapIndexedNode<K, V> extends BitmapIndexedNode<K, V> {
    private final static long serialVersionUID = 0L;
    transient final @NonNull UniqueIdentity mutator;

    MutableBitmapIndexedNode(@NonNull UniqueIdentity mutator, int nodeMap, int dataMap, @NonNull Object[] nodes, int entryLength) {
        super(nodeMap, dataMap, nodes, entryLength);
        this.mutator = mutator;
    }

    protected @NonNull UniqueIdentity getMutator() {
        return mutator;
    }
}
