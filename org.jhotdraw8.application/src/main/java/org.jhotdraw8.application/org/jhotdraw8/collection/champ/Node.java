/*
 * @(#)Node.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.UniqueId;

import java.util.Objects;

/**
 * Represents a node in a CHAMP trie.
 * <p>
 * A node can store entries which have a key, a value (optionally) and a
 * sequence number (optionally).
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class Node<K, V> {
    static final int HASH_CODE_LENGTH = 32;
    /**
     * Bit partition size in the range [1,5].
     * <p>
     * The bit-mask must fit into the 32 bits of an int field ({@code 32 = 1<<5}).
     * (You can use a size of 6, if you replace the bit-mask fields with longs).
     */
    static final int BIT_PARTITION_SIZE = 5;
    static final int BIT_PARTITION_MASK = (1 << BIT_PARTITION_SIZE) - 1;

    /**
     * Offset to the key inside a data entry.
     */
    static final int ENTRY_KEY = 0;
    /**
     * Offset to the value inside a data entry.
     * This offset is only valid, if the {@code entryLength} is {@literal > 1}.
     */
    static final int ENTRY_VALUE = 1;

    /**
     * Represents no value.
     * We can not use {@code null}, because we allow storing null-keys and
     * null-values in the trie.
     */
    public static final Object NO_VALUE = new Object();
    /**
     * Indicates that no sequence number must be inserted.
     */
    public final static int NO_SEQUENCE_NUMBER = Integer.MAX_VALUE;
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
    static int bitpos(final int mask) {
        return 1 << mask;
    }

    /**
     * Given a bitmap and a bit-position, returns the index
     * in the array.
     * <p>
     * For example, if the bitmap is 0b1101 and
     * bit-position is 0b0100, then the index is 1.
     *
     * @param bitmap a bit-map
     * @param bitpos a bit-position
     * @return the array index
     */
    static int index(final int bitmap, final int bitpos) {
        return Integer.bitCount(bitmap & (bitpos - 1));
    }

    static int mask(final int keyHash, final int shift) {
        return (keyHash >>> shift) & BIT_PARTITION_MASK;
    }

    static <K, V> Node<K, V> mergeTwoDataEntrysIntoNode(UniqueId mutator,
                                                        final K k0, final V v0, int seq0, final int keyHash0,
                                                        final K k1, final V v1, int seq1, final int keyHash1,
                                                        final int shift, final int entryLength) {
        assert !Objects.equals(k0, k1);

        if (shift >= HASH_CODE_LENGTH) {
            Object[] entries = new Object[entryLength * 2];
            entries[0] = k0;
            entries[entryLength] = k1;
            if (entryLength > 1) {
                entries[1] = v0;
                entries[entryLength + 1] = v1;
            }
            if (seq1 != NO_SEQUENCE_NUMBER) {
                entries[entryLength - 1] = seq0;
                entries[entryLength * 2 - 1] = seq1;
            }
            return ChampTrie.newHashCollisionNode(mutator, keyHash0, entries, entryLength);
        }

        final int mask0 = mask(keyHash0, shift);
        final int mask1 = mask(keyHash1, shift);

        if (mask0 != mask1) {
            // both nodes fit on same level
            final int dataMap = bitpos(mask0) | bitpos(mask1);

            Object[] entries = new Object[entryLength * 2];
            if (mask0 < mask1) {
                entries[0] = k0;
                entries[entryLength] = k1;
                if (entryLength > 1) {
                    entries[1] = v0;
                    entries[entryLength + 1] = v1;
                }
                if (seq1 != NO_SEQUENCE_NUMBER) {
                    entries[entryLength - 1] = seq0;
                    entries[entryLength * 2 - 1] = seq1;
                }
                return ChampTrie.newBitmapIndexedNode(mutator, (0), dataMap, entries, entryLength);
            } else {
                entries[0] = k1;
                entries[entryLength] = k0;
                if (entryLength > 1) {
                    entries[1] = v1;
                    entries[entryLength + 1] = v0;
                }
                if (seq1 != NO_SEQUENCE_NUMBER) {
                    entries[entryLength * 2 - 1] = seq0;
                    entries[entryLength - 1] = seq1;
                }
                return ChampTrie.newBitmapIndexedNode(mutator, (0), dataMap, entries, entryLength);
            }
        } else {
            final Node<K, V> node = mergeTwoDataEntrysIntoNode(mutator,
                    k0, v0, seq0, keyHash0,
                    k1, v1, seq1, keyHash1,
                    shift + BIT_PARTITION_SIZE,
                    entryLength);
            // values fit on next level

            final int nodeMap = bitpos(mask0);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, (0), new Object[]{node}, entryLength);
        }
    }

    abstract int dataArity(int entryLength);

    abstract boolean hasDataArityOne(int entryLength);

    /**
     * Returns the data index for the given keyHash and shift, or -1.
     *
     * @param key         a key
     * @param keyHash     the key hash
     * @param shift       the shift
     * @param entryLength the entry length
     * @return the data index or -1
     */
    abstract int dataIndex(@Nullable K key, final int keyHash, final int shift, final int entryLength);

    /**
     * Checks if this trie is equivalent to the specified other trie.
     *
     * @param other       the other trie
     * @param entryLength the length of an entry in the trie
     * @param numFields   the number of fields that must be compared
     *                    (the entryLength - 1 if the entry has a sequence number)
     * @return true if equivalent
     */
    abstract boolean equivalent(final @NonNull Object other, int entryLength, int numFields);

    /**
     * Finds a value by a key.
     *
     * @param key         a key
     * @param keyHash     the hash code of the key
     * @param shift       the shift for this node
     * @param entryLength the entry length
     * @param numFields   the number of fiels in an entry,
     * @return the value, returns {@link #NO_VALUE} if the value is not present.
     */
    abstract Object findByKey(final K key, final int keyHash, final int shift, int entryLength, int numFields);


    abstract Object[] getDataEntry(final int index, int entryLength);

    abstract K getKey(final int index, int entryLength);

    abstract SequencedMapEntry<K, V> getKeyValueSeqEntry(final int index, int entryLength, int numFields);

    UniqueId getMutator() {
        return null;
    }

    abstract Node<K, V> getNode(final int index, int entryLength);

    abstract V getValue(final int index, int entryLength, int numFields);

    abstract boolean hasData();

    abstract boolean hasNodes();

    boolean isAllowedToEdit(@Nullable UniqueId y) {
        UniqueId x = getMutator();
        return x != null && x == y;
    }

    Object[] newEntry(K key, V value, int entryLength) {
        Object[] newEntry = new Object[entryLength];
        newEntry[0] = key;
        if (entryLength > 1) {
            newEntry[1] = value;
        }
        return newEntry;
    }

    abstract int nodeArity();

    /**
     * Returns the node index for the given keyHash and shift, or -1.
     *
     * @param keyHash the key hash
     * @param shift   the shift
     * @return the node index or -1
     */
    abstract int nodeIndex(final int keyHash, final int shift);

    abstract Node<K, V> remove(final @Nullable UniqueId mutator, final K key,
                               final int keyHash, final int shift, final ChangeEvent<V> details, int entryLength, int numFields);

    abstract Node<K, V> update(final UniqueId mutator, final K key, final V val,
                               final int keyHash, final int shift, final ChangeEvent<V> details, int entryLength, int sequenceNumber, int numFields);
}
