/*
 * @(#)BitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ArrayHelper;
import org.jhotdraw8.collection.UniqueId;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.champ.NodeFactory.newBitmapIndexedNode;
import static org.jhotdraw8.collection.champ.NodeFactory.newHashCollisionNode;

/**
 * Represents a bitmap-indexed node in a CHAMP trie.
 *
 * @param <K> the key type
 */
class BitmapIndexedNode<K> extends Node<K> {
    static final @NonNull BitmapIndexedNode<?> EMPTY_NODE = newBitmapIndexedNode(null, (0), (0), new Object[]{});

    public final Object @NonNull [] mixed;
    final int nodeMap;
    final int dataMap;

    protected BitmapIndexedNode(int nodeMap,
                                int dataMap, @NonNull Object @NonNull [] mixed) {
        this.nodeMap = nodeMap;
        this.dataMap = dataMap;
        this.mixed = mixed;
        assert mixed.length == nodeArity() + dataArity();
    }

    @SuppressWarnings("unchecked")
    public static <K> @NonNull BitmapIndexedNode<K> emptyNode() {
        return (BitmapIndexedNode<K>) EMPTY_NODE;
    }

    @NonNull BitmapIndexedNode<K> copyAndInsertValue(@Nullable UniqueId mutator, int bitpos,
                                                     K key) {
        int idx = dataIndex(bitpos);
        Object[] dst = ArrayHelper.copyComponentAdd(this.mixed, idx, 1);
        dst[idx] = key;
        return newBitmapIndexedNode(mutator, nodeMap, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K> copyAndMigrateFromDataToNode(@Nullable UniqueId mutator,
                                                               int bitpos, Node<K> node) {

        int idxOld = dataIndex(bitpos);
        int idxNew = this.mixed.length - 1 - nodeIndex(bitpos);
        assert idxOld <= idxNew;

        // copy 'src' and remove entryLength element(s) at position 'idxOld' and
        // insert 1 element(s) at position 'idxNew'
        Object[] src = this.mixed;
        Object[] dst = new Object[src.length];
        System.arraycopy(src, 0, dst, 0, idxOld);
        System.arraycopy(src, idxOld + 1, dst, idxOld, idxNew - idxOld);
        System.arraycopy(src, idxNew + 1, dst, idxNew + 1, src.length - idxNew - 1);
        dst[idxNew] = node;
        return newBitmapIndexedNode(mutator, nodeMap | bitpos, dataMap ^ bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K> copyAndMigrateFromNodeToData(@Nullable UniqueId mutator,
                                                               int bitpos, @NonNull Node<K> node) {
        int idxOld = this.mixed.length - 1 - nodeIndex(bitpos);
        int idxNew = dataIndex(bitpos);

        // copy 'src' and remove 1 element(s) at position 'idxOld' and
        // insert entryLength element(s) at position 'idxNew'
        Object[] src = this.mixed;
        Object[] dst = new Object[src.length];
        assert idxOld >= idxNew;
        System.arraycopy(src, 0, dst, 0, idxNew);
        System.arraycopy(src, idxNew, dst, idxNew + 1, idxOld - idxNew);
        System.arraycopy(src, idxOld + 1, dst, idxOld + 1, src.length - idxOld - 1);
        dst[idxNew] = node.getKey(0);
        return newBitmapIndexedNode(mutator, nodeMap ^ bitpos, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K> copyAndSetNode(@Nullable UniqueId mutator, int bitpos,
                                                 Node<K> node) {

        int idx = this.mixed.length - 1 - nodeIndex(bitpos);
        if (isAllowedToEdit(mutator)) {
            // no copying if already editable
            this.mixed[idx] = node;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            final Object[] dst = ArrayHelper.copySet(this.mixed, idx, node);
            return newBitmapIndexedNode(mutator, nodeMap, dataMap, dst);
        }
    }

    @Override
    int dataArity() {
        return Integer.bitCount(dataMap);
    }

    int dataIndex(int bitpos) {
        return Integer.bitCount(dataMap & (bitpos - 1));
    }

    public int dataMap() {
        return dataMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equivalent(@NonNull Object other) {
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
    public @Nullable Object findByKey(K key, int keyHash, int shift, @NonNull BiPredicate<K, K> equalsFunction) {
        int bitpos = bitpos(mask(keyHash, shift));
        if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).findByKey(key, keyHash, shift + BIT_PARTITION_SIZE, equalsFunction);
        }
        if ((dataMap & bitpos) != 0) {
            K k = getKey(dataIndex(bitpos));
            if (equalsFunction.test(k, key)) {
                return k;
            }
        }
        return NO_VALUE;
    }


    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    K getKey(int index) {
        return (K) mixed[index];
    }


    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    Node<K> getNode(int index) {
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
    Node<K> nodeAt(int bitpos) {
        return (Node<K>) mixed[mixed.length - 1 - nodeIndex(bitpos)];
    }

    int nodeIndex(int bitpos) {
        return Integer.bitCount(nodeMap & (bitpos - 1));
    }

    public int nodeMap() {
        return nodeMap;
    }

    @Override
    public @NonNull BitmapIndexedNode<K> remove(@Nullable UniqueId mutator,
                                                K key,
                                                int keyHash, int shift,
                                                @NonNull ChangeEvent<K> details, @NonNull BiPredicate<K, K> equalsFunction) {
        int mask = mask(keyHash, shift);
        int bitpos = bitpos(mask);
        if ((dataMap & bitpos) != 0) {
            return removeData(mutator, key, keyHash, shift, details, bitpos, equalsFunction);
        }
        if ((nodeMap & bitpos) != 0) {
            return removeSubNode(mutator, key, keyHash, shift, details, bitpos, equalsFunction);
        }
        return this;
    }

    private @NonNull BitmapIndexedNode<K> removeData(@Nullable UniqueId mutator, K key, int keyHash, int shift, @NonNull ChangeEvent<K> details, int bitpos, @NonNull BiPredicate<K, K> equalsFunction) {
        int dataIndex = dataIndex(bitpos);
        int entryLength = 1;
        if (!equalsFunction.test(getKey(dataIndex), key)) {
            return this;
        }
        K currentVal = getKey(dataIndex);
        details.setRemoved(currentVal);
        if (dataArity() == 2 && !hasNodes()) {
            int newDataMap =
                    (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(keyHash, 0));
            Object[] nodes = {getKey(dataIndex ^ 1)};
            return newBitmapIndexedNode(mutator, 0, newDataMap, nodes);
        }
        int idx = dataIndex * entryLength;
        Object[] dst = ArrayHelper.copyComponentRemove(this.mixed, idx, entryLength);
        return newBitmapIndexedNode(mutator, nodeMap, dataMap ^ bitpos, dst);
    }

    private @NonNull BitmapIndexedNode<K> removeSubNode(@Nullable UniqueId mutator, K key, int keyHash, int shift,
                                                        @NonNull ChangeEvent<K> details,
                                                        int bitpos, @NonNull BiPredicate<K, K> equalsFunction) {
        Node<K> subNode = nodeAt(bitpos);
        Node<K> updatedSubNode =
                subNode.remove(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details, equalsFunction);
        if (subNode == updatedSubNode) {
            return this;
        }
        if (!updatedSubNode.hasNodes() && updatedSubNode.hasDataArityOne()) {
            if (!hasData() && nodeArity() == 1) {
                return (BitmapIndexedNode<K>) updatedSubNode;
            }
            return copyAndMigrateFromNodeToData(mutator, bitpos, updatedSubNode);
        }
        return copyAndSetNode(mutator, bitpos, updatedSubNode);
    }

    @Override
    public @NonNull BitmapIndexedNode<K> update(@Nullable UniqueId mutator,
                                                @Nullable K key,
                                                int keyHash, int shift,
                                                @NonNull ChangeEvent<K> details,
                                                @NonNull BiFunction<K, K, K> updateFunction,
                                                @NonNull BiPredicate<K, K> equalsFunction,
                                                @NonNull ToIntFunction<K> hashFunction) {
        int mask = mask(keyHash, shift);
        int bitpos = bitpos(mask);
        if ((dataMap & bitpos) != 0) {
            final int dataIndex = dataIndex(bitpos);
            final K oldKey = getKey(dataIndex);
            if (equalsFunction.test(oldKey, key)) {
                K updatedKey = updateFunction.apply(oldKey, key);
                if (updatedKey == oldKey) {
                    details.found(oldKey);
                    return this;
                }
                details.setUpdated(oldKey);
                return copyAndSetValue(mutator, dataIndex, updatedKey);
            }
            Node<K> updatedSubNode =
                    mergeTwoDataEntriesIntoNode(mutator,
                            oldKey, hashFunction.applyAsInt(oldKey),
                            key, keyHash, shift + BIT_PARTITION_SIZE);
            details.setAdded();
            return copyAndMigrateFromDataToNode(mutator, bitpos, updatedSubNode);
        } else if ((nodeMap & bitpos) != 0) {
            Node<K> subNode = nodeAt(bitpos);
            Node<K> updatedSubNode = subNode
                    .update(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details, updateFunction, equalsFunction, hashFunction);
            return subNode == updatedSubNode ? this : copyAndSetNode(mutator, bitpos, updatedSubNode);
        }
        details.setAdded();
        return copyAndInsertValue(mutator, bitpos, key);
    }

    @NonNull
    private BitmapIndexedNode<K> copyAndSetValue(@Nullable UniqueId mutator, int dataIndex, K updatedKey) {
        if (isAllowedToEdit(mutator)) {
            this.mixed[dataIndex] = updatedKey;
            return this;
        }
        Object[] newMixed = ArrayHelper.copySet(this.mixed, dataIndex, updatedKey);
        return newBitmapIndexedNode(mutator, nodeMap, dataMap, newMixed);
    }

    private int nodeIndexAt(Object[] array, int nodeMap, int bitpos) {
        return array.length - 1 - Integer.bitCount(nodeMap & (bitpos - 1));
    }

    private Node<K> mergeTwoKeyValPairs(UniqueId mutator,
                                        K key0, int keyHash0,
                                        K key1, int keyHash1,
                                        int shift) {

        assert !(key0.equals(key1));

        if (shift >= HASH_CODE_LENGTH) {
            @SuppressWarnings("unchecked")
            HashCollisionNode<K> unchecked = newHashCollisionNode(mutator, keyHash0, new Object[]{key0, key1});
            return unchecked;
        }

        int mask0 = mask(keyHash0, shift);
        int mask1 = mask(keyHash1, shift);

        if (mask0 != mask1) {
            // both nodes fit on same level
            final int dataMap = bitpos(mask0) | bitpos(mask1);
            if (mask0 < mask1) {
                return newBitmapIndexedNode(mutator, 0, dataMap, new Object[]{key0, key1});
            } else {
                return newBitmapIndexedNode(mutator, 0, dataMap, new Object[]{key1, key0});
            }
        } else {
            final Node<K> node = mergeTwoKeyValPairs(mutator, key0, keyHash0, key1, keyHash1, shift + BIT_PARTITION_SIZE);
            // values fit on next level
            final int nodeMap = bitpos(mask0);
            return newBitmapIndexedNode(mutator, nodeMap, 0, new Object[]{node});
        }
    }
}