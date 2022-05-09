/*
 * @(#)MutableHashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.UniqueId;

final class MutableHashCollisionNode<K, V> extends HashCollisionNode<K, V> {
    private final static long serialVersionUID = 0L;
    private final @NonNull UniqueId mutator;

    MutableHashCollisionNode(@NonNull UniqueId mutator, int hash, Object[] entries, int entryLength) {
        super(hash, entries, entryLength);
        this.mutator = mutator;
    }

    @Override
    protected @NonNull UniqueId getMutator() {
        return mutator;
    }
}
