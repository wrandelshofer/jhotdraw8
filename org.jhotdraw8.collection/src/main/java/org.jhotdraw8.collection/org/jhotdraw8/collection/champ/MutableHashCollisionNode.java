/*
 * @(#)MutableHashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IdentityObject;

class MutableHashCollisionNode<K> extends HashCollisionNode<K> {
    private final static long serialVersionUID = 0L;
    private final @NonNull IdentityObject mutator;

    MutableHashCollisionNode(@NonNull IdentityObject mutator, int hash, Object @NonNull [] entries) {
        super(hash, entries);
        this.mutator = mutator;
    }

    @Override
    protected @NonNull IdentityObject getMutator() {
        return mutator;
    }
}
