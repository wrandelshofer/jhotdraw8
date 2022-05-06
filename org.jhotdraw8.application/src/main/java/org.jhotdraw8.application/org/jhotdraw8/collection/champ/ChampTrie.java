/*
 * @(#)ChampTrie.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.UniqueId;

import java.util.Map;
import java.util.Objects;

/**
 * Provides static utility methods for CHAMP tries.
 */
public class ChampTrie {

    /**
     * Don't let anyone instantiate this class.
     */
    private ChampTrie() {
    }

    static <K, V> BitmapIndexedNode<K, V> newBitmapIndexedNode(
            @Nullable UniqueId mutator, final int nodeMap,
            final int dataMap, final @NonNull Object[] nodes, int entryLength) {
        return mutator == null
                ? new BitmapIndexedNode<>(nodeMap, dataMap, nodes, entryLength)
                : new MutableBitmapIndexedNode<>(mutator, nodeMap, dataMap, nodes, entryLength);
    }

    static <K, V> HashCollisionNode<K, V> newHashCollisionNode(
            @Nullable UniqueId mutator, int hash, @NonNull Object[] entries, int entryLength) {
        return mutator == null
                ? new HashCollisionNode<>(hash, entries, entryLength)
                : new MutableHashCollisionNode<>(mutator, hash, entries, entryLength);
    }


    /**
     * Renumbers the sequence numbers in all nodes from {@code 0} to {@code size}.
     * <p>
     * Afterwards the sequence number for the next inserted entry must be
     * set to the value {@code size};
     *
     * @param root    the root of the trie
     * @param mutator the mutator which will own all nodes of the trie
     * @param <K>     the key type
     * @param <V>     the value type
     * @return the new root
     */
    public static <K, V> BitmapIndexedNode<K, V> renumber(int size, @NonNull BitmapIndexedNode<K, V> root, @NonNull UniqueId mutator, int entryLength) {
        BitmapIndexedNode<K, V> newRoot = root;
        ChangeEvent<V> details = new ChangeEvent<>();
        int count = Integer.MIN_VALUE;
        for (SequencedTrieIterator<K, V> i = new SequencedTrieIterator<>(size, root, entryLength); i.hasNext(); ) {
            Map.Entry<K, V> entry = i.nextEntry();
            newRoot = newRoot.update(mutator,
                    entry.getKey(), entry.getValue(),
                    Objects.hashCode(entry.getKey()), 0, details,
                    entryLength, count, entryLength - 1
            );
            count++;
        }
        return newRoot;
    }

}