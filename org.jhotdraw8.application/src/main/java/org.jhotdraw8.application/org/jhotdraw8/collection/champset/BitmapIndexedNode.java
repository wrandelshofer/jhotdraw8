/*
 * @(#)BitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champset;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ArrayHelper;
import org.jhotdraw8.collection.UniqueId;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

/**
 * Represents a bitmap-indexed node in a CHAMP trie.
 *
 * @param <K> the key type
 */
public class BitmapIndexedNode<K> extends Node<K> {
    static final @NonNull BitmapIndexedNode<?> EMPTY_NODE = ChampTrie.newBitmapIndexedNode(null, (0), (0), new Object[]{});

    public final Object @NonNull [] mixed;
    private final int nodeMap;
    private final int dataMap;

    protected BitmapIndexedNode(final int nodeMap,
                                final int dataMap, final @NonNull Object @NonNull [] mixed) {
        this.nodeMap = nodeMap;
        this.dataMap = dataMap;
        this.mixed = mixed;
        assert mixed.length == nodeArity() + dataArity();
    }

    @SuppressWarnings("unchecked")
    public static <K> @NonNull BitmapIndexedNode<K> emptyNode() {
        return (BitmapIndexedNode<K>) EMPTY_NODE;
    }

    @NonNull BitmapIndexedNode<K> copyAndInsertValue(final @Nullable UniqueId mutator, final int bitpos,
                                                     final K key) {
        final int idx = dataIndex(bitpos);
        final Object[] dst = ArrayHelper.copyComponentAdd(this.mixed, idx, 1);
        dst[idx] = key;
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K> copyAndMigrateFromDataToNode(final @Nullable UniqueId mutator,
                                                               final int bitpos, final Node<K> node) {

        final int idxOld = dataIndex(bitpos);
        final int idxNew = this.mixed.length - 1 - nodeIndex(bitpos);
        assert idxOld <= idxNew;

        // copy 'src' and remove entryLength element(s) at position 'idxOld' and
        // insert 1 element(s) at position 'idxNew'
        final Object[] src = this.mixed;
        final Object[] dst = new Object[src.length];
        System.arraycopy(src, 0, dst, 0, idxOld);
        System.arraycopy(src, idxOld + 1, dst, idxOld, idxNew - idxOld);
        System.arraycopy(src, idxNew + 1, dst, idxNew + 1, src.length - idxNew - 1);
        dst[idxNew] = node;
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap | bitpos, dataMap ^ bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K> copyAndMigrateFromNodeToData(final @Nullable UniqueId mutator,
                                                               final int bitpos, final @NonNull Node<K> node) {
        final int idxOld = this.mixed.length - 1 - nodeIndex(bitpos);
        final int idxNew = dataIndex(bitpos);

        // copy 'src' and remove 1 element(s) at position 'idxOld' and
        // insert entryLength element(s) at position 'idxNew'
        final Object[] src = this.mixed;
        final Object[] dst = new Object[src.length];
        assert idxOld >= idxNew;
        System.arraycopy(src, 0, dst, 0, idxNew);
        System.arraycopy(src, idxNew, dst, idxNew + 1, idxOld - idxNew);
        System.arraycopy(src, idxOld + 1, dst, idxOld + 1, src.length - idxOld - 1);
        dst[idxNew] = node.getKey(0);
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap ^ bitpos, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K> copyAndSetNode(final @Nullable UniqueId mutator, final int bitpos,
                                                 final Node<K> node) {

        final int idx = this.mixed.length - 1 - nodeIndex(bitpos);
        if (isAllowedToEdit(mutator)) {
            // no copying if already editable
            this.mixed[idx] = node;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            final Object[] dst = ArrayHelper.copySet(this.mixed, idx, node);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst);
        }
    }

    @NonNull BitmapIndexedNode<K> copyAndSetValue(final @Nullable UniqueId mutator, final int bitpos,
                                                  final K val, int entryLength) {
        final int idx = entryLength * dataIndex(bitpos) + 1;
        if (isAllowedToEdit(mutator)) {
            // no copying if already editable
            this.mixed[idx] = val;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            final Object[] dst = ArrayHelper.copySet(this.mixed, idx, val);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst);
        }
    }

    @Override
    int dataArity() {
        return Integer.bitCount(dataMap);
    }

    int dataIndex(final int bitpos) {
        return Integer.bitCount(dataMap & (bitpos - 1));
    }

    public int dataMap() {
        return dataMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equivalent(final @NonNull Object other) {
        if (this == other) {
            return true;
        }
        BitmapIndexedNode<?> that = (BitmapIndexedNode<?>) other;
        Object[] thatNodes = that.mixed;
        // nodes array: we compare local data from 0 to splitAt (excluded)
        // and then we compare the nested nodes from splitAt to length (excluded)
        int splitAt = dataArity();
        return nodeMap() == that.nodeMap()
                && dataMap() == that.dataMap()
                && ArrayHelper.equals(mixed, 0, splitAt, thatNodes, 0, splitAt)
                && ArrayHelper.equals(mixed, splitAt, mixed.length, thatNodes, splitAt, thatNodes.length,
                (a, b) -> ((Node<K>) a).equivalent(b));
    }


    @Override
    public @Nullable Object findByKey(final K key, final int keyHash, final int shift, @NonNull BiPredicate<K, K> equalsFunction) {
        final int bitpos = bitpos(mask(keyHash, shift));
        if ((dataMap & bitpos) != 0) {
            final int index = dataIndex(bitpos);
            if (equalsFunction.test(getKey(index), key)) {
                return getKey(index);
            }
        } else if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).findByKey(key, keyHash, shift + BIT_PARTITION_SIZE, equalsFunction);
        }
        return NO_VALUE;
    }


    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    K getKey(final int index) {
        return (K) mixed[index];
    }


    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    Node<K> getNode(final int index) {
        return (Node<K>) mixed[mixed.length - 1 - index];
    }

    @Override
    boolean hasData() {
        return dataMap != 0;
    }

    @Override
    boolean hasDataArityOne() {
        return Integer.bitCount(dataMap) == 1;
    }

    @Override
    boolean hasNodes() {
        return nodeMap != 0;
    }

    @Override
    int nodeArity() {
        return Integer.bitCount(nodeMap);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    Node<K> nodeAt(final int bitpos) {
        return (Node<K>) mixed[mixed.length - 1 - nodeIndex(bitpos)];
    }

    int nodeIndex(final int bitpos) {
        return Integer.bitCount(nodeMap & (bitpos - 1));
    }

    public int nodeMap() {
        return nodeMap;
    }

    @Override
    public @NonNull BitmapIndexedNode<K> remove(final @Nullable UniqueId mutator, final K key,
                                                final int keyHash, final int shift,
                                                final @NonNull ChangeEvent<K> details, @NonNull BiPredicate<K, K> equalsFunction) {
        final int mask = mask(keyHash, shift);
        final int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) {
            return removeData(mutator, key, keyHash, shift, details, bitpos, equalsFunction);
        } else if ((nodeMap & bitpos) != 0) {
            return removeSubNode(mutator, key, keyHash, shift, details, bitpos, equalsFunction);
        }

        return this;
    }

    private @NonNull BitmapIndexedNode<K> removeData(@Nullable UniqueId mutator, K key, int keyHash, int shift, @NonNull ChangeEvent<K> details, int bitpos, @NonNull BiPredicate<K, K> equalsFunction) {
        final int dataIndex = dataIndex(bitpos);
        int entryLength = 1;
        if (!equalsFunction.test(getKey(dataIndex), key)) {
            return this;
        }

        final K currentVal = getKey(dataIndex);
        details.updated(currentVal);

        if (dataArity() == 2 && !hasNodes()) {
            // Create new node with remaining entry. The new node will
            // a) either become the new root returned, or
            // b) unwrapped and inlined during returning.
            final int newDataMap =
                    (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(keyHash, 0));

            Object[] nodes = {getKey(dataIndex ^ 1)};
            return ChampTrie.newBitmapIndexedNode(mutator, 0, newDataMap, nodes);
        } else {
            // copy 'src' and remove entryLength element(s) at position 'idx'
            int idx = dataIndex * entryLength;
            final Object[] dst = ArrayHelper.copyComponentRemove(this.mixed, idx, entryLength);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap ^ bitpos, dst);
        }
    }

    private @NonNull BitmapIndexedNode<K> removeSubNode(@Nullable UniqueId mutator, K key, int keyHash, int shift,
                                                        @NonNull ChangeEvent<K> details,
                                                        int bitpos, @NonNull BiPredicate<K, K> equalsFunction) {
        final Node<K> subNode = nodeAt(bitpos);
        final Node<K> subNodeNew =
                subNode.remove(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details, equalsFunction);

        if (subNode == subNodeNew) {
            return this;
        }

        if (!subNodeNew.hasNodes() && subNodeNew.hasDataArityOne()) {
            if (!hasData() && nodeArity() == 1) {
                // escalate (singleton or empty) result
                return (BitmapIndexedNode<K>) subNodeNew;
            } else {
                // inline data entry (move to front)
                return copyAndMigrateFromNodeToData(mutator, bitpos, subNodeNew);
            }
        }
        return copyAndSetNode(mutator, bitpos, subNodeNew);
    }

    @Override
    public @NonNull BitmapIndexedNode<K> update(@Nullable UniqueId mutator,
                                                @Nullable K key,
                                                int keyHash, int shift,
                                                @NonNull ChangeEvent<K> details,
                                                @NonNull BiFunction<K, K, K> updateFunction,
                                                @NonNull BiPredicate<K, K> equalsFunction,
                                                @NonNull ToIntFunction<K> hashFunction) {
        final int mask = mask(keyHash, shift);
        final int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) { // inplace value
            final int dataIndex = dataIndex(bitpos);
            final K oldKey = getKey(dataIndex);
            if (equalsFunction.test(oldKey, key)) {
                K updatedKey = updateFunction.apply(oldKey, key);
                if (updatedKey == oldKey) {
                    details.found(oldKey);
                    return this;
                } else {
                    // copy entries and replace the entry
                    details.updated(oldKey);
                    if (isAllowedToEdit(mutator)) {
                        this.mixed[dataIndex] = updatedKey;
                        return this;
                    } else {
                        final Object[] newMixed = ArrayHelper.copySet(this.mixed, dataIndex, updatedKey);
                        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, newMixed);
                    }
                }
            } else {
                final Node<K> subNodeNew =
                        mergeTwoDataEntriesIntoNode(mutator,
                                oldKey, hashFunction.applyAsInt(oldKey),
                                key, keyHash, shift + BIT_PARTITION_SIZE);

                details.modified();
                return copyAndMigrateFromDataToNode(mutator, bitpos, subNodeNew);
            }
        } else if ((nodeMap & bitpos) != 0) { // node (not value)
            final Node<K> subNode = nodeAt(bitpos);
            final Node<K> subNodeNew =
                    subNode.update(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details, updateFunction, equalsFunction, hashFunction);

            if (details.isModified()) {
                return copyAndSetNode(mutator, bitpos, subNodeNew);
            } else {
                return this;
            }
        } else {
            // no value
            details.modified();
            return copyAndInsertValue(mutator, bitpos, key);
        }
    }
}