/*
 * @(#)MutableHashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt.impl.champmap;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;

final class MutableHashCollisionNode<K, V> extends HashCollisionNode<K, V> {

    private final MutabilityOwnership mutator;

    MutableHashCollisionNode(MutabilityOwnership mutator, int hash, Object[] entries, int entryLength) {
        super(hash, entries);
        this.mutator = mutator;
    }

    @Override
    protected MutabilityOwnership getMutator() {
        return mutator;
    }
}
