/*
 * @(#)MutableHashCollisionNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IdentityObject;

class ChampMutableHashCollisionNode<K> extends ChampHashCollisionNode<K> {
    private static final long serialVersionUID = 0L;
    private final @NonNull IdentityObject mutator;

    ChampMutableHashCollisionNode(@NonNull IdentityObject mutator, int hash, Object @NonNull [] entries) {
        super(hash, entries);
        this.mutator = mutator;
    }

    @Override
    protected @NonNull IdentityObject getMutator() {
        return mutator;
    }
}
