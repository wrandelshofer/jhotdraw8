/*
 * @(#)MutableBitmapIndexedNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IdentityObject;

class ChampMutableBitmapIndexedNode<K> extends ChampBitmapIndexedNode<K> {
    private static final long serialVersionUID = 0L;
    private final @NonNull IdentityObject mutator;

    ChampMutableBitmapIndexedNode(@NonNull IdentityObject mutator, int nodeMap, int dataMap, @NonNull Object @NonNull [] nodes) {
        super(nodeMap, dataMap, nodes);
        this.mutator = mutator;
    }

    @Override
    protected @NonNull IdentityObject getMutator() {
        return mutator;
    }
}
