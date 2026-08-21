package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;

import org.jspecify.annotations.Nullable;

public final class MutableTrieNode<K, V> extends TrieNode<K, V> {
    private @Nullable MutabilityOwnership ownedBy;

    public MutableTrieNode(int dataMap, int nodeMap, Object[] buffer, @Nullable MutabilityOwnership ownedBy) {
        super(dataMap, nodeMap, buffer);
        this.ownedBy = ownedBy;
    }

    @Override
    public @Nullable MutabilityOwnership ownedBy() {
        return ownedBy;
    }
}
