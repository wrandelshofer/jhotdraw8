/*
 * @(#)MutableBitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.icollection.impl.IdentityObject;

final class MutableBitmapIndexedNode<K, V> extends BitmapIndexedNode<K, V> {

    private final IdentityObject mutator;

    MutableBitmapIndexedNode(IdentityObject mutator, int nodeMap, int dataMap, Object[] nodes) {
        super(nodeMap, dataMap, nodes);
        this.mutator = mutator;
    }

    @Override
    protected IdentityObject getMutator() {
        return mutator;
    }
}
