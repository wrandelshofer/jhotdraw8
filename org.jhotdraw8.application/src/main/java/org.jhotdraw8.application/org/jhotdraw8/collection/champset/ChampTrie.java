/*
 * @(#)ChampTrie.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champset;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.UniqueId;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

/**
 * Provides static utility methods for CHAMP tries.
 */
public class ChampTrie {

    /**
     * Don't let anyone instantiate this class.
     */
    private ChampTrie() {
    }

    static <K> @NonNull BitmapIndexedNode<K> newBitmapIndexedNode(
            @Nullable UniqueId mutator, final int nodeMap,
            final int dataMap, final @NonNull Object[] nodes) {
        return mutator == null
                ? new BitmapIndexedNode<>(nodeMap, dataMap, nodes)
                : new MutableBitmapIndexedNode<>(mutator, nodeMap, dataMap, nodes);
    }

    static <K> @NonNull HashCollisionNode<K> newHashCollisionNode(
            @Nullable UniqueId mutator, int hash, @NonNull Object @NonNull [] entries) {
        return mutator == null
                ? new HashCollisionNode<>(hash, entries)
                : new MutableHashCollisionNode<>(mutator, hash, entries);
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
     * @return the new root
     */
    public static <K> BitmapIndexedNode<SequencedKey<K>> renumber(int size, @NonNull BitmapIndexedNode<SequencedKey<K>> root, @NonNull UniqueId mutator,
                                                                  @NonNull ToIntFunction<SequencedKey<K>> hashFunction,
                                                                  @NonNull BiPredicate<SequencedKey<K>, SequencedKey<K>> equalsFunction) {
        BitmapIndexedNode<SequencedKey<K>> newRoot = root;
        ChangeEvent<SequencedKey<K>> details = new ChangeEvent<>();
        int count = Integer.MIN_VALUE;
        for (SequencedKeyIterator<K> i = new SequencedKeyIterator<>(size, root, false, null, null); i.hasNext(); ) {
            K e = i.next();
            SequencedKey<K> newElement = new SequencedKey<>(e, count);
            newRoot = newRoot.update(mutator,
                    newElement,
                    Objects.hashCode(e), 0, details,
                    (oldk, newk) -> oldk.getSequenceNumber() == newk.getSequenceNumber() ? oldk : newk,
                    equalsFunction, hashFunction);
            count++;
        }
        return newRoot;
    }
}