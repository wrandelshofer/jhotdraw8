/*
 * @(#)MutableHashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champtrie;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.UniqueIdentity;

final class MutableHashCollisionNode<K, V> extends HashCollisionNode<K, V> {
    private final static long serialVersionUID = 0L;
    transient final @NonNull UniqueIdentity mutator;

    MutableHashCollisionNode(@NonNull UniqueIdentity mutator, int hash, Object[] entries, int entryLength) {
        super(hash, entries, entryLength);
        this.mutator = mutator;
    }

    protected @NonNull UniqueIdentity getMutator() {
        return mutator;
    }
}
