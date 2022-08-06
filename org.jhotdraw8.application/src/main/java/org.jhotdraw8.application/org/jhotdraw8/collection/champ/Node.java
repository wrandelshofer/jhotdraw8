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
 * @param <E> the element type
 */
abstract class Node<E> {
    /**
     * Represents no value.
     * We can not use {@code null}, because we allow storing null-keys in the
     * trie.
     */
    public static final Object NO_VALUE = new Object();
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
     * Given a masked keyHash, returns its bit-position
     * in the bit-map.
     * <p>
     * For example, if the bit partition is 5 bits, then
     * we 2^5 == 32 distinct bit-positions.
     * If the masked keyHash is 3 then the bit-position is
     * the bit with index 3. That is, 1<<3 = 0b0100.
     *
     * @param mask masked key hash
     * @return bit position
     */
    static int bitpos(int mask) {
        return 1 << mask;
    }

    static int mask(int keyHash, int shift) {
        return (keyHash >>> shift) & BIT_PARTITION_MASK;
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
     * Finds a value by a key.
     *
     * @param key     a key
     * @param keyHash the hash code of the key
     * @param shift   the shift for this node
     * @return the value, returns {@link #NO_VALUE} if the value is not present.
     */
    abstract Object findByKey(E key, int keyHash, int shift, @NonNull BiPredicate<E, E> equalsFunction);

    abstract @Nullable E getKey(int index);

    @Nullable UniqueId getMutator() {
        return null;
    }

    abstract @NonNull Node<E> getNode(int index);

    abstract boolean hasData();

    abstract boolean hasDataArityOne();

    abstract boolean hasNodes();

    boolean isAllowedToEdit(@Nullable UniqueId y) {
        UniqueId x = getMutator();
        return x != null && x == y;
    }

    abstract int nodeArity();

    abstract @NonNull Node<E> remove(@Nullable UniqueId mutator, E key,
                                     int keyHash, int shift,
                                     @NonNull ChangeEvent<E> details,
                                     @NonNull BiPredicate<E, E> equalsFunction);

    /**
     * Inserts or updates a key in the trie.
     *
     * @param mutator        a mutator that uniquely owns mutated nodes
     * @param key            a key
     * @param keyHash        the hash-code of the key
     * @param shift          the shift of the current node
     * @param details        update details on output
     * @param updateFunction only used on update:
     *                       given the existing key (oldk) and the new key (newk),
     *                       this function decides whether it replaces the old
     *                       key with the new key
     * @return the updated trie
     */
    abstract @NonNull Node<E> update(@Nullable UniqueId mutator, E key,
                                     int keyHash, int shift, @NonNull ChangeEvent<E> details,
                                     @NonNull BiFunction<E, E, E> updateFunction,
                                     @NonNull BiPredicate<E, E> equalsFunction,
                                     @NonNull ToIntFunction<E> hashFunction);
}
