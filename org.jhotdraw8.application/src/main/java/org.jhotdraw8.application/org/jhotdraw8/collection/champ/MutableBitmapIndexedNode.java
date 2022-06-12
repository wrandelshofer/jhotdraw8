/*
 * @(#)MutableBitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.UniqueId;

final class MutableBitmapIndexedNode<K> extends BitmapIndexedNode<K> {
    private final static long serialVersionUID = 0L;
    private final @NonNull UniqueId mutator;

    MutableBitmapIndexedNode(@NonNull UniqueId mutator, int nodeMap, int dataMap, @NonNull Object @NonNull [] nodes) {
        super(nodeMap, dataMap, nodes);
        this.mutator = mutator;
    }

    @Override
    protected @NonNull UniqueId getMutator() {
        return mutator;
    }
}
