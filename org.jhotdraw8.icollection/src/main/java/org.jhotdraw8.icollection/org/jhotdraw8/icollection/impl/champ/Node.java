/*
 * @(#)Node.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Represents a node in a CHAMP trie.
///
/// A node can store object arrays of a specifiable length.
/// The first element of the array is the key, the other elements are values.
public abstract class Node {
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

    protected final int dataArrayIndex(int dataIndex, int ENTRY_LENGTH) {
        return dataIndex * ENTRY_LENGTH;
    }

    protected final int nodeArrayIndex(int nodeIndex, Object[] mx) {
        return mx.length - 1 - nodeIndex;
    }

    static int mask(int keyHash, int shift) {
        return (keyHash >>> shift) & BIT_PARTITION_MASK;
    }

    boolean isNodeEmpty() {
        return !hasData() && !hasNodes();
    }

    boolean hasMany(int ENTRY_LENGTH) {
        return hasNodes() || dataArity(ENTRY_LENGTH) > 1;
    }

    Node mergeTwoDataEntriesIntoNode(@Nullable IdentityObject mutator,
                                     Object[] entry0, int keyHash0,
                                     Object[] entry1, int keyHash1,
                                     int shift, int ENTRY_LENGTH) {

        if (shift >= HASH_CODE_LENGTH) {
            Object[] entries = new Object[ENTRY_LENGTH * 2];
            System.arraycopy(entry0, 0, entries, 0, ENTRY_LENGTH);
            System.arraycopy(entry1, 0, entries, ENTRY_LENGTH, ENTRY_LENGTH);
            return ChampTrie.newHashCollisionNode(mutator, keyHash0, entries, ENTRY_LENGTH);
        }

        int mask0 = mask(keyHash0, shift);
        int mask1 = mask(keyHash1, shift);

        if (mask0 != mask1) {
            // both nodes fit on same level
            int dataMap = bitpos(mask0) | bitpos(mask1);

            Object[] dst = new Object[ENTRY_LENGTH * 2];
            if (mask0 < mask1) {
                System.arraycopy(entry0, 0, dst, 0, ENTRY_LENGTH);
                System.arraycopy(entry1, 0, dst, ENTRY_LENGTH, ENTRY_LENGTH);
            } else {
                System.arraycopy(entry1, 0, dst, 0, ENTRY_LENGTH);
                System.arraycopy(entry0, 0, dst, ENTRY_LENGTH, ENTRY_LENGTH);
            }
            return ChampTrie.newBitmapIndexedNode(mutator, (0), dataMap, dst, ENTRY_LENGTH);
        } else {
            Node node = mergeTwoDataEntriesIntoNode(mutator,
                    entry0, keyHash0,
                    entry1, keyHash1,
                    shift + BIT_PARTITION_SIZE,
                    ENTRY_LENGTH);
            // values fit on next level

            int nodeMap = bitpos(mask0);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, (0), new Object[]{node}, ENTRY_LENGTH);
        }
    }

    abstract int dataArity(int ENTRY_LENGTH);

    abstract boolean hasDataArityOne();

    /// Checks if this trie is equivalent to the specified other trie.
    ///
    /// @param other        the other trie
    /// @param ENTRY_LENGTH
    /// @return true if equivalent
    abstract boolean equivalent(Object other, int ENTRY_LENGTH);

    /// Finds an entry by a key.
    ///
    /// @param key          a key
    /// @param keyHash      the hash code of the key
    /// @param shift        the shift for this node
    /// @param ENTRY_LENGTH
    /// @return the entry, returns [#NO_DATA] if the entry is not present.
    abstract Object findEntry(Object key, int keyHash, int shift, int ENTRY_LENGTH);

    /// Finds a value by a key.
    ///
    /// @param key          a key
    /// @param keyHash      the hash code of the key
    /// @param shift        the shift for this node
    /// @param ENTRY_LENGTH
    /// @param DATA_INDEX
    /// @return the value, returns [#NO_DATA] if the entry is not present.
    abstract Object findData(Object key, int keyHash, int shift, int ENTRY_LENGTH, int DATA_INDEX);


    public final Object[] getDataEntry(int index, int ENTRY_LENGTH) {
        Object[] entry = new Object[ENTRY_LENGTH];
        System.arraycopy(array, ENTRY_LENGTH * index, entry, 0, ENTRY_LENGTH);
        return entry;
    }

    @SuppressWarnings("unchecked")
    public final Object getKey(int index, int ENTRY_LENGTH) {
        return (Object) array[index * ENTRY_LENGTH];
    }


    @Nullable IdentityObject getMutator() {
        return null;
    }

    final Node getNode(int index) {
        return (Node) array[array.length - 1 - index];
    }

    public final Object[] getEntry(int index, int ENTRY_LENGTH) {
        return Arrays.copyOfRange(array, index * ENTRY_LENGTH, index * ENTRY_LENGTH + ENTRY_LENGTH);
    }

    public final Object getData(int index, int ENTRY_LENGTH, int DATA_INDEX) {
        return array[index * ENTRY_LENGTH + DATA_INDEX];
    }

    abstract boolean hasData();

    abstract boolean hasNodes();

    boolean isAllowedToUpdate(@Nullable IdentityObject y) {
        IdentityObject x = getMutator();
        return x != null && x == y;
    }

    abstract int nodeArity();

    abstract Node remove(@Nullable IdentityObject mutator, Object key,
                         int keyHash, int shift, ChangeEvent details, int ENTRY_LENGTH);


    public abstract Node put(@Nullable IdentityObject mutator, Object key, Object[] entry,
                             int keyHash, int shift, ChangeEvent details,
                             BiFunction<Object[], Object[], Object[]> updateFunction,
                             ToIntFunction<Object> hashFunction
            , int ENTRY_LENGTH);

    /// Retains data elements in this trie for which the provided predicate returns true.
    ///
    /// @param owner
    /// @param predicate  a predicate that returns true for data elements that should be retained
    /// @param shift      the shift of this node and the other node
    /// @param bulkChange updates the field [BulkChangeEvent#removed]
    /// @return the updated trie
    protected abstract Node removeIf(@Nullable IdentityObject owner,
                                     Predicate<Object> predicate, int shift,
                                     BulkChangeEvent bulkChange, int ENTRY_LENGTH);

    protected abstract Node putAll(@Nullable IdentityObject owner, Node otherNode, int shift,
                                   BulkChangeEvent bulkChange,
                                   ToIntFunction<Object> hashFunction,
                                   ChangeEvent details, int ENTRY_LENGTH);

    protected abstract int calculateSize(int ENTRY_LENGTH);
}
