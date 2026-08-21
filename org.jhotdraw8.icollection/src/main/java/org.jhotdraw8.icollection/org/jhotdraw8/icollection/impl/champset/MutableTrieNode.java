package org.jhotdraw8.icollection.impl.champset;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jspecify.annotations.Nullable;

public final class MutableTrieNode<E> extends TrieNode<E> {
    private @Nullable MutabilityOwnership ownedBy;

    public MutableTrieNode(int bitmap, Object[] buffer, @Nullable MutabilityOwnership ownedBy) {
        super(bitmap, buffer);
        this.ownedBy = ownedBy;
    }

    @Override
    public @Nullable MutabilityOwnership ownedBy() {
        return ownedBy;
    }
}
