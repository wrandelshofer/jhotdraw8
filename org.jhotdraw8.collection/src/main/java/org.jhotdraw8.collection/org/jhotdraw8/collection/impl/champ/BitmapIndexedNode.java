/*
 * @(#)BitmapIndexedNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.ListHelper;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.impl.champ.ChampNodeFactory.newBitmapIndexedNode;


/**
 * Represents a bitmap-indexed node in a CHAMP trie.
 * <p>
 * References:
 * <p>
 * This class has been derived from 'The Capsule Hash Trie Collections Library'.
 * <dl>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <K> the data type
 */
public class BitmapIndexedNode<K> extends Node<K> {
    static final @NonNull BitmapIndexedNode<?> EMPTY_NODE = newBitmapIndexedNode(null, (0), (0), new Object[]{});

    public final Object @NonNull [] mixed;
    private final int nodeMap;
    private final int dataMap;

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

    @NonNull BitmapIndexedNode<K> copyAndInsertData(@Nullable IdentityObject owner, int bitpos,
                                                    K data) {
        int idx = dataIndex(bitpos);
        Object[] dst = ListHelper.copyComponentAdd(this.mixed, idx, 1);
        dst[idx] = data;
        return newBitmapIndexedNode(owner, nodeMap, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K> copyAndMigrateFromDataToNode(@Nullable IdentityObject owner,
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
        return newBitmapIndexedNode(owner, nodeMap | bitpos, dataMap ^ bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K> copyAndMigrateFromNodeToData(@Nullable IdentityObject owner,
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
        dst[idxNew] = node.getData(0);
        return newBitmapIndexedNode(owner, nodeMap ^ bitpos, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K> copyAndSetNode(@Nullable IdentityObject owner, int bitpos,
                                                 Node<K> node) {

        int idx = this.mixed.length - 1 - nodeIndex(bitpos);
        if (isAllowedToUpdate(owner)) {
            // no copying if already editable
            this.mixed[idx] = node;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            final Object[] dst = ListHelper.copySet(this.mixed, idx, node);
            return newBitmapIndexedNode(owner, nodeMap, dataMap, dst);
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
    protected boolean equivalent(@NonNull Object other) {
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
                && Arrays.equals(mixed, 0, splitAt, thatNodes, 0, splitAt)
                && Arrays.equals(mixed, splitAt, mixed.length, thatNodes, splitAt, thatNodes.length,
                (a, b) -> ((Node<K>) a).equivalent(b) ? 0 : 1);
    }


    @Override
    @Nullable
    public Object find(K data, int keyHash, int shift, @NonNull BiPredicate<K, K> equalsFunction) {
        int bitpos = bitpos(mask(keyHash, shift));
        if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).find(data, keyHash, shift + BIT_PARTITION_SIZE, equalsFunction);
        }
        if ((dataMap & bitpos) != 0) {
            K k = getData(dataIndex(bitpos));
            if (equalsFunction.test(k, data)) {
                return k;
            }
        }
        return NO_DATA;
    }


    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    K getData(int index) {
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

    @SuppressWarnings("unchecked")
    @NonNull
    K dataAt(int bitpos) {
        return (K) mixed[dataIndex(bitpos)];
    }

    int nodeIndex(int bitpos) {
        return Integer.bitCount(nodeMap & (bitpos - 1));
    }

    public int nodeMap() {
        return nodeMap;
    }

    @Override
    @NonNull
    public BitmapIndexedNode<K> remove(@Nullable IdentityObject owner,
                                       K data,
                                       int dataHash, int shift,
                                       @NonNull ChangeEvent<K> details, @NonNull BiPredicate<K, K> equalsFunction) {
        int mask = mask(dataHash, shift);
        int bitpos = bitpos(mask);
        if ((dataMap & bitpos) != 0) {
            return removeData(owner, data, dataHash, shift, details, bitpos, equalsFunction);
        }
        if ((nodeMap & bitpos) != 0) {
            return removeSubNode(owner, data, dataHash, shift, details, bitpos, equalsFunction);
        }
        return this;
    }

    private @NonNull BitmapIndexedNode<K> removeData(@Nullable IdentityObject owner, K data, int dataHash, int shift, @NonNull ChangeEvent<K> details, int bitpos, @NonNull BiPredicate<K, K> equalsFunction) {
        int dataIndex = dataIndex(bitpos);
        int entryLength = 1;
        if (!equalsFunction.test(getData(dataIndex), data)) {
            return this;
        }
        K currentVal = getData(dataIndex);
        details.setRemoved(currentVal);
        if (dataArity() == 2 && !hasNodes()) {
            int newDataMap =
                    (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(dataHash, 0));
            Object[] nodes = {getData(dataIndex ^ 1)};
            return newBitmapIndexedNode(owner, 0, newDataMap, nodes);
        }
        int idx = dataIndex * entryLength;
        Object[] dst = ListHelper.copyComponentRemove(this.mixed, idx, entryLength);
        return newBitmapIndexedNode(owner, nodeMap, dataMap ^ bitpos, dst);
    }

    private @NonNull BitmapIndexedNode<K> removeSubNode(@Nullable IdentityObject owner, K data, int dataHash, int shift,
                                                        @NonNull ChangeEvent<K> details,
                                                        int bitpos, @NonNull BiPredicate<K, K> equalsFunction) {
        Node<K> subNode = nodeAt(bitpos);
        Node<K> updatedSubNode =
                subNode.remove(owner, data, dataHash, shift + BIT_PARTITION_SIZE, details, equalsFunction);
        if (subNode == updatedSubNode) {
            return this;
        }
        if (!updatedSubNode.hasNodes() && updatedSubNode.hasDataArityOne()) {
            if (!hasData() && nodeArity() == 1) {
                return (BitmapIndexedNode<K>) updatedSubNode;
            }
            return copyAndMigrateFromNodeToData(owner, bitpos, updatedSubNode);
        }
        return copyAndSetNode(owner, bitpos, updatedSubNode);
    }

    @Override
    @NonNull
    public BitmapIndexedNode<K> put(@Nullable IdentityObject owner,
                                    @Nullable K newData,
                                    int dataHash, int shift,
                                    @NonNull ChangeEvent<K> details,
                                    @NonNull BiFunction<K, K, K> updateFunction,
                                    @NonNull BiPredicate<K, K> equalsFunction,
                                    @NonNull ToIntFunction<K> hashFunction) {
        int mask = mask(dataHash, shift);
        int bitpos = bitpos(mask);
        if ((dataMap & bitpos) != 0) {
            final int dataIndex = dataIndex(bitpos);
            final K oldData = getData(dataIndex);
            if (equalsFunction.test(oldData, newData)) {
                K updatedData = updateFunction.apply(oldData, newData);
                if (updatedData == oldData) {
                    details.found(oldData);
                    return this;
                }
                details.setReplaced(oldData, updatedData);
                return copyAndSetData(owner, dataIndex, updatedData);
            }
            Node<K> updatedSubNode =
                    mergeTwoDataEntriesIntoNode(owner,
                            oldData, hashFunction.applyAsInt(oldData),
                            newData, dataHash, shift + BIT_PARTITION_SIZE);
            details.setAdded(newData);
            return copyAndMigrateFromDataToNode(owner, bitpos, updatedSubNode);
        } else if ((nodeMap & bitpos) != 0) {
            Node<K> subNode = nodeAt(bitpos);
            Node<K> updatedSubNode = subNode
                    .put(owner, newData, dataHash, shift + BIT_PARTITION_SIZE, details, updateFunction, equalsFunction, hashFunction);
            return subNode == updatedSubNode ? this : copyAndSetNode(owner, bitpos, updatedSubNode);
        }
        details.setAdded(newData);
        return copyAndInsertData(owner, bitpos, newData);
    }

    @NonNull
    private BitmapIndexedNode<K> copyAndSetData(@Nullable IdentityObject owner, int dataIndex, K updatedData) {
        if (isAllowedToUpdate(owner)) {
            this.mixed[dataIndex] = updatedData;
            return this;
        }
        Object[] newMixed = ListHelper.copySet(this.mixed, dataIndex, updatedData);
        return newBitmapIndexedNode(owner, nodeMap, dataMap, newMixed);
    }
}