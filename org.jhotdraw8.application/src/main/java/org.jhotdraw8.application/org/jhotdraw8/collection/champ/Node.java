/*
 * @(#)Node.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.UniqueId;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

/**
 * Represents a node in a CHAMP trie.
 *
 * @param <D> the data type
 */
abstract class Node<D> {
    /**
     * Represents no data.
     * We can not use {@code null}, because we allow storing null-data in the
     * trie.
     */
    public static final Object NO_DATA = new Object();
    static final int HASH_CODE_LENGTH = 32;
    /**
     * Bit partition size in the range [1,5].
     * <p>
     * The bit-mask must fit into the 32 bits of an int field ({@code 32 = 1<<5}).
     * (You can use a size of 6, if you replace the bit-mask fields with longs).
     */
    static final int BIT_PARTITION_SIZE = 5;
    static final int BIT_PARTITION_MASK = (1 << BIT_PARTITION_SIZE) - 1;
    static final int MAX_DEPTH = (HASH_CODE_LENGTH + BIT_PARTITION_SIZE - 1) / BIT_PARTITION_SIZE + 1;


    Node() {
    }

    /**
     * Given a masked dataHash, returns its bit-position
     * in the bit-map.
     * <p>
     * For example, if the bit partition is 5 bits, then
     * we 2^5 == 32 distinct bit-positions.
     * If the masked dataHash is 3 then the bit-position is
     * the bit with index 3. That is, 1<<3 = 0b0100.
     *
     * @param mask masked data hash
     * @return bit position
     */
    static int bitpos(int mask) {
        return 1 << mask;
    }

    static int mask(int dataHash, int shift) {
        return (dataHash >>> shift) & BIT_PARTITION_MASK;
    }

    static <K> @NonNull Node<K> mergeTwoDataEntriesIntoNode(UniqueId mutator,
                                                            K k0, int keyHash0,
                                                            K k1, int keyHash1,
                                                            int shift) {
        assert !Objects.equals(k0, k1);

        if (shift >= HASH_CODE_LENGTH) {
            Object[] entries = new Object[2];
            entries[0] = k0;
            entries[1] = k1;
            return NodeFactory.newHashCollisionNode(mutator, keyHash0, entries);
        }

        int mask0 = mask(keyHash0, shift);
        int mask1 = mask(keyHash1, shift);

        if (mask0 != mask1) {
            // both nodes fit on same level
            int dataMap = bitpos(mask0) | bitpos(mask1);

            Object[] entries = new Object[2];
            if (mask0 < mask1) {
                entries[0] = k0;
                entries[1] = k1;
                return NodeFactory.newBitmapIndexedNode(mutator, (0), dataMap, entries);
            } else {
                entries[0] = k1;
                entries[1] = k0;
                return NodeFactory.newBitmapIndexedNode(mutator, (0), dataMap, entries);
            }
        } else {
            Node<K> node = mergeTwoDataEntriesIntoNode(mutator,
                    k0, keyHash0,
                    k1, keyHash1,
                    shift + BIT_PARTITION_SIZE);
            // values fit on next level

            int nodeMap = bitpos(mask0);
            return NodeFactory.newBitmapIndexedNode(mutator, nodeMap, (0), new Object[]{node});
        }
    }

    abstract int dataArity();

    /**
     * Checks if this trie is equivalent to the specified other trie.
     *
     * @param other the other trie
     * @return true if equivalent
     */
    abstract boolean equivalent(@NonNull Object other);

    /**
     * Finds data in the CHAMP trie, that is equal to the specified data.
     *
     * @param data           a data
     * @param dataHash       the hash code of the data
     * @param shift          the shift for this node
     * @param equalsFunction a function that checks the data for equality
     * @return the found data, returns {@link #NO_DATA} if the data is not
     * in the trie.
     */
    abstract Object findByData(D data, int dataHash, int shift, @NonNull BiPredicate<D, D> equalsFunction);

    abstract @Nullable D getData(int index);

    @Nullable UniqueId getMutator() {
        return null;
    }

    abstract @NonNull Node<D> getNode(int index);

    abstract boolean hasData();

    abstract boolean hasDataArityOne();

    abstract boolean hasNodes();

    boolean isAllowedToEdit(@Nullable UniqueId y) {
        UniqueId x = getMutator();
        return x != null && x == y;
    }

    abstract int nodeArity();

    abstract @NonNull Node<D> remove(@Nullable UniqueId mutator, D data,
                                     int dataHash, int shift,
                                     @NonNull ChangeEvent<D> details,
                                     @NonNull BiPredicate<D, D> equalsFunction);

    /**
     * Inserts or updates a data in the trie.
     *
     * @param mutator        a mutator that uniquely owns mutated nodes
     * @param data           a data
     * @param dataHash       the hash-code of the data
     * @param shift          the shift of the current node
     * @param details        update details on output
     * @param updateFunction only used on update:
     *                       given the existing data (oldk) and the new data (newk),
     *                       this function decides whether it replaces the old
     *                       data with the new data
     * @return the updated trie
     */
    abstract @NonNull Node<D> update(@Nullable UniqueId mutator, D data,
                                     int dataHash, int shift, @NonNull ChangeEvent<D> details,
                                     @NonNull BiFunction<D, D, D> updateFunction,
                                     @NonNull BiPredicate<D, D> equalsFunction,
                                     @NonNull ToIntFunction<D> hashFunction);
}
