/*
 * @(#)MutableBitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;

final class MutableBitmapIndexedNode<K, V> extends BitmapIndexedNode {

    private final MutabilityOwnership mutator;

    MutableBitmapIndexedNode(MutabilityOwnership mutator, int nodeMap, int dataMap, Object[] nodes, int ENTRY_LENGTH) {
        super(nodeMap, dataMap, nodes, ENTRY_LENGTH);
        this.mutator = mutator;
    }

    @Override
    protected MutabilityOwnership getMutator() {
        return mutator;
    }
}
