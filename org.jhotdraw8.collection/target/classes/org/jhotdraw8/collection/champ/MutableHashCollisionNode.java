/*
 * @(#)MutableHashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.UniqueId;

class MutableHashCollisionNode<K> extends HashCollisionNode<K> {
    private final static long serialVersionUID = 0L;
    private final @NonNull UniqueId mutator;

    MutableHashCollisionNode(@NonNull UniqueId mutator, int hash, Object @NonNull [] entries) {
        super(hash, entries);
        this.mutator = mutator;
    }

    @Override
    protected @NonNull UniqueId getMutator() {
        return mutator;
    }
}
