/*
 * @(#)MutableHashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.icollection.impl.IdentityObject;

final class MutableHashCollisionNode<K, V> extends HashCollisionNode<K, V> {

    private final IdentityObject mutator;

    MutableHashCollisionNode(IdentityObject mutator, int hash, Object[] entries, int entryLength) {
        super(hash, entries);
        this.mutator = mutator;
    }

    @Override
    protected IdentityObject getMutator() {
        return mutator;
    }
}
