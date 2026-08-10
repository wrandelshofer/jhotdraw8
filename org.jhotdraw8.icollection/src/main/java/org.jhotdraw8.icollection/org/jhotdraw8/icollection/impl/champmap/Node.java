/*
 * @(#)Node.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.ToIntFunction;

/// Represents a node in a CHAMP trie.
///
/// A node can store entries which have a key, a value (optionally) and a
/// sequence number (optionally).
///
/// @param <K> the key type
/// @param <V> the value type
public abstract class Node<K, V> {
    static final int HASH_CODE_LENGTH = 32;
    /// Bit partition size in the range [1,5].
    ///
    /// The bit-mask must fit into the 32 bits of an int field (`32 = 1<<5`).
    /// (You can use a size of 6, if you replace the bit-mask fields with longs).
    static final int BIT_PARTITION_SIZE = 5;
    static final int BIT_PARTITION_MASK = (1 << BIT_PARTITION_SIZE) - 1;

    /// Represents no value.
    /// We can not use `null`, because we allow storing null-keys and
    /// null-values in the trie.
    public static final Object NO_DATA = new IdentityObject();

    static final int MAX_DEPTH = (HASH_CODE_LENGTH + BIT_PARTITION_SIZE - 1) / BIT_PARTITION_SIZE + 1;
    static final int ENTRY_LENGTH = 2;
    Object[] array;

    Node() {

    }

    /// Given a masked keyHash, returns its bit-position
    /// in the bit-map.
    ///
    /// For example, if the bit partition is 5 bits, then
    /// we 2^5 == 32 distinct bit-positions.
    /// If the masked keyHash is 3 then the bit-position is
    /// the bit with index 3. That is, 1<<3 = 0b0100.
    ///
    /// @param mask masked key hash
    /// @return bit position
    static int bitpos(int mask) {
        return 1 << mask;
    }

    /// Given a bitmap and a bit-position, returns the index
    /// in the array.
    ///
    /// For example, if the bitmap is 0b1101 and
    /// bit-position is 0b0100, then the index is 1.
    ///
    /// @param bitmap a bit-map
    /// @param bitpos a bit-position
    /// @return the array index
    static int index(int bitmap, int bitpos) {
        return Integer.bitCount(bitmap & (bitpos - 1));
    }

    static int mask(int keyHash, int shift) {
        return (keyHash >>> shift) & BIT_PARTITION_MASK;
    }

    Node<K, V> mergeTwoDataEntriesIntoNode(IdentityObject mutator,
                                           K k0, V v0, int keyHash0,
                                           K k1, V v1, int keyHash1,
                                           int shift) {
        assert !Objects.equals(k0, k1);

        if (shift >= HASH_CODE_LENGTH) {
            Object[] entries = new Object[ENTRY_LENGTH * 2];
            entries[0] = k0;
            entries[ENTRY_LENGTH] = k1;
            if (ENTRY_LENGTH > 1) {
                entries[1] = v0;
                entries[ENTRY_LENGTH + 1] = v1;
            }
            return ChampTrie.newHashCollisionNode(mutator, keyHash0, entries, ENTRY_LENGTH);
        }

        int mask0 = mask(keyHash0, shift);
        int mask1 = mask(keyHash1, shift);

        if (mask0 != mask1) {
            // both nodes fit on same level
            int dataMap = bitpos(mask0) | bitpos(mask1);

            Object[] entries = new Object[ENTRY_LENGTH * 2];
            if (mask0 < mask1) {
                entries[0] = k0;
                entries[ENTRY_LENGTH] = k1;
                if (ENTRY_LENGTH > 1) {
                    entries[1] = v0;
                    entries[ENTRY_LENGTH + 1] = v1;
                }
            } else {
                entries[0] = k1;
                entries[ENTRY_LENGTH] = k0;
                if (ENTRY_LENGTH > 1) {
                    entries[1] = v1;
                    entries[ENTRY_LENGTH + 1] = v0;
                }
            }
            return ChampTrie.newBitmapIndexedNode(mutator, (0), dataMap, entries);
        } else {
            Node<K, V> node = mergeTwoDataEntriesIntoNode(mutator,
                    k0, v0, keyHash0,
                    k1, v1, keyHash1,
                    shift + BIT_PARTITION_SIZE
            );
            // values fit on next level

            int nodeMap = bitpos(mask0);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, (0), new Object[]{node});
        }
    }

    abstract int dataArity();

    abstract boolean hasDataArityOne();

    /// Checks if this trie is equivalent to the specified other trie.
    ///
    /// @param other the other trie
    /// @return true if equivalent
    abstract boolean equivalent(Object other);

    /// Finds a value by a key.
    ///
    /// @param key     a key
    /// @param keyHash the hash code of the key
    /// @param shift   the shift for this node
    /// @return the value, returns [#NO_DATA] if the value is not present.
    abstract Object findByKey(K key, int keyHash, int shift);


    public final Object[] getDataEntry(int index) {
        Object[] entry = new Object[ENTRY_LENGTH];
        System.arraycopy(array, ENTRY_LENGTH * index, entry, 0, ENTRY_LENGTH);
        return entry;
    }

    @SuppressWarnings("unchecked")
    public final K getKey(int index) {
        return (K) array[index * ENTRY_LENGTH];
    }


    @SuppressWarnings("unchecked")
    public final EditableMapEntry<K, V> getMapEntry(int index) {
        return new EditableMapEntry<>(
                (K) array[index * ENTRY_LENGTH],
                (V) array[index * ENTRY_LENGTH + 1],
                0
        );
    }

    @Nullable IdentityObject getMutator() {
        return null;
    }

    @SuppressWarnings("unchecked")
    final Node<K, V> getNode(int index) {
        return (Node<K, V>) array[array.length - 1 - index];
    }

    @SuppressWarnings("unchecked")
    public final @Nullable V getValue(int index) {
        return (V) array[index * ENTRY_LENGTH + 1];
    }

    abstract boolean hasData();

    abstract boolean hasNodes();

    boolean isAllowedToEdit(@Nullable IdentityObject y) {
        IdentityObject x = getMutator();
        return x != null && x == y;
    }

    abstract int nodeArity();

    abstract Node<K, V> remove(@Nullable IdentityObject mutator, K key,
                               int keyHash, int shift, ChangeEvent<V> details);

    abstract Node<K, V> put(@Nullable IdentityObject mutator, K key, V val,
                            int keyHash, int shift, ChangeEvent<V> details, ToIntFunction<K> hashFunction);


}
