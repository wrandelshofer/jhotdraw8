/*
 * @(#)MutableBitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IdentityObject;

class MutableBitmapIndexedNode<K> extends BitmapIndexedNode<K> {
    private static final long serialVersionUID = 0L;
    private final @NonNull IdentityObject mutator;

    MutableBitmapIndexedNode(@NonNull IdentityObject mutator, int nodeMap, int dataMap, @NonNull Object @NonNull [] nodes) {
        super(nodeMap, dataMap, nodes);
        this.mutator = mutator;
    }

    @Override
    protected @NonNull IdentityObject getMutator() {
        return mutator;
    }
}
