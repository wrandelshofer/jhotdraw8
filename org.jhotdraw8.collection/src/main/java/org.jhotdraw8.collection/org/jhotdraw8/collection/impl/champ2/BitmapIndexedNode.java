/*
 * @(#)BitmapIndexedNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ2;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ListHelper;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.impl.champ2.ChampNodeFactory.newBitmapIndexedNode;

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
 * @param <D> the data type
 */
public class BitmapIndexedNode<D> extends Node<D> {
    static final @NonNull BitmapIndexedNode<?> EMPTY_NODE = newBitmapIndexedNode((0), (0), new Object[]{});

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

    @NonNull BitmapIndexedNode<D> copyAndInsertData(int bitpos,
                                                    D data) {
        int idx = dataIndex(bitpos);
        Object[] dst = ListHelper.copyComponentAdd(this.mixed, idx, 1);
        dst[idx] = data;
        return newBitmapIndexedNode(nodeMap, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<D> copyAndMigrateFromDataToNode(int bitpos, Node<D> node) {

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
        return newBitmapIndexedNode(nodeMap | bitpos, dataMap ^ bitpos, dst);
    }

    @NonNull BitmapIndexedNode<D> copyAndMigrateFromNodeToData(int bitpos, @NonNull Node<D> node) {
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
        return newBitmapIndexedNode(nodeMap ^ bitpos, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<D> copyAndSetNode(int bitpos,
                                                 Node<D> node) {

        int idx = this.mixed.length - 1 - nodeIndex(bitpos);
        // copy 'src' and set 1 element(s) at position 'idx'
        final Object[] dst = ListHelper.copySet(this.mixed, idx, node);
        return newBitmapIndexedNode(nodeMap, dataMap, dst);
    }

    @Override
    int dataArity() {
        return Integer.bitCount(dataMap);
    }

    int dataIndex(int bitpos) {
        return Integer.bitCount(dataMap & (bitpos - 1));
    }

    int index(int map, int bitpos) {
        return Integer.bitCount(map & (bitpos - 1));
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
                (a, b) -> ((Node<D>) a).equivalent(b) ? 0 : 1);
    }


    @Override
    @Nullable
    public Object find(D key, int dataHash, int shift, @NonNull BiPredicate<D, D> equalsFunction) {
        int bitpos = bitpos(mask(dataHash, shift));
        if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).find(key, dataHash, shift + BIT_PARTITION_SIZE, equalsFunction);
        }
        if ((dataMap & bitpos) != 0) {
            D k = getData(dataIndex(bitpos));
            if (equalsFunction.test(k, key)) {
                return k;
            }
        }
        return NO_DATA;
    }


    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    D getData(int index) {
        return (D) mixed[index];
    }


    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    Node<D> getNode(int index) {
        return (Node<D>) mixed[mixed.length - 1 - index];
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
    Node<D> nodeAt(int bitpos) {
        return (Node<D>) mixed[mixed.length - 1 - nodeIndex(bitpos)];
    }

    @SuppressWarnings("unchecked")
    @NonNull
    D dataAt(int bitpos) {
        return (D) mixed[dataIndex(bitpos)];
    }

    int nodeIndex(int bitpos) {
        return Integer.bitCount(nodeMap & (bitpos - 1));
    }

    public int nodeMap() {
        return nodeMap;
    }

    @Override
    @NonNull
    public BitmapIndexedNode<D> remove(D data,
                                       int dataHash, int shift,
                                       @NonNull ChangeEvent<D> details, @NonNull BiPredicate<D, D> equalsFunction) {
        int mask = mask(dataHash, shift);
        int bitpos = bitpos(mask);
        if ((dataMap & bitpos) != 0) {
            return removeData(data, dataHash, shift, details, bitpos, equalsFunction);
        }
        if ((nodeMap & bitpos) != 0) {
            return removeSubNode(data, dataHash, shift, details, bitpos, equalsFunction);
        }
        return this;
    }

    private @NonNull BitmapIndexedNode<D> removeData(D data, int dataHash, int shift, @NonNull ChangeEvent<D> details, int bitpos, @NonNull BiPredicate<D, D> equalsFunction) {
        int dataIndex = dataIndex(bitpos);
        int entryLength = 1;
        if (!equalsFunction.test(getData(dataIndex), data)) {
            return this;
        }
        D currentVal = getData(dataIndex);
        details.setRemoved(currentVal);
        if (dataArity() == 2 && !hasNodes()) {
            int newDataMap =
                    (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(dataHash, 0));
            Object[] nodes = {getData(dataIndex ^ 1)};
            return newBitmapIndexedNode(0, newDataMap, nodes);
        }
        int idx = dataIndex * entryLength;
        Object[] dst = ListHelper.copyComponentRemove(this.mixed, idx, entryLength);
        return newBitmapIndexedNode(nodeMap, dataMap ^ bitpos, dst);
    }

    private @NonNull BitmapIndexedNode<D> removeSubNode(D data, int dataHash, int shift,
                                                        @NonNull ChangeEvent<D> details,
                                                        int bitpos, @NonNull BiPredicate<D, D> equalsFunction) {
        Node<D> subNode = nodeAt(bitpos);
        Node<D> updatedSubNode =
                subNode.remove(data, dataHash, shift + BIT_PARTITION_SIZE, details, equalsFunction);
        if (subNode == updatedSubNode) {
            return this;
        }
        if (!updatedSubNode.hasNodes() && updatedSubNode.hasDataArityOne()) {
            if (!hasData() && nodeArity() == 1) {
                return (BitmapIndexedNode<D>) updatedSubNode;
            }
            return copyAndMigrateFromNodeToData(bitpos, updatedSubNode);
        }
        return copyAndSetNode(bitpos, updatedSubNode);
    }

    @Override
    @NonNull
    public BitmapIndexedNode<D> update(@Nullable D newData,
                                       int dataHash, int shift,
                                       @NonNull ChangeEvent<D> details,
                                       @NonNull BiFunction<D, D, D> updateFunction,
                                       @NonNull BiPredicate<D, D> equalsFunction,
                                       @NonNull ToIntFunction<D> hashFunction) {
        int mask = mask(dataHash, shift);
        int bitpos = bitpos(mask);
        if ((dataMap & bitpos) != 0) {
            final int dataIndex = dataIndex(bitpos);
            final D oldData = getData(dataIndex);
            if (equalsFunction.test(oldData, newData)) {
                D updatedData = updateFunction.apply(oldData, newData);
                if (updatedData == oldData) {
                    details.found(oldData);
                    return this;
                }
                details.setReplaced(oldData, updatedData);
                return copyAndSetData(dataIndex, updatedData);
            }
            Node<D> updatedSubNode =
                    mergeTwoDataEntriesIntoNode(
                            oldData, hashFunction.applyAsInt(oldData),
                            newData, dataHash, shift + BIT_PARTITION_SIZE);
            details.setAdded(newData);
            return copyAndMigrateFromDataToNode(bitpos, updatedSubNode);
        } else if ((nodeMap & bitpos) != 0) {
            Node<D> subNode = nodeAt(bitpos);
            Node<D> updatedSubNode = subNode
                    .update(newData, dataHash, shift + BIT_PARTITION_SIZE, details, updateFunction, equalsFunction, hashFunction);
            return subNode == updatedSubNode ? this : copyAndSetNode(bitpos, updatedSubNode);
        }
        details.setAdded(newData);
        return copyAndInsertData(bitpos, newData);
    }

    @NonNull
    private BitmapIndexedNode<D> copyAndSetData(int dataIndex, D updatedData) {
        Object[] newMixed = ListHelper.copySet(this.mixed, dataIndex, updatedData);
        return newBitmapIndexedNode(nodeMap, dataMap, newMixed);
    }


    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull BitmapIndexedNode<D> addAll(Node<D> other, int shift,
                                                   @NonNull BulkChangeEvent bulkChange,
                                                   @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        var that = (BitmapIndexedNode<D>) other;
        if (this == that) {
            bulkChange.inBothCollections += this.calculateSize();
            return this;
        }

        // union mask contains all the bits from input masks
        var newBitMap = nodeMap | dataMap | that.nodeMap | that.dataMap;
        // first allocate the node and then fill it in
        // we are doing a union, so all the array elements are guaranteed to exist
        var buffer = new Object[Integer.bitCount(newBitMap)];
        // for each bit set in the resulting mask,
        // either left, right or both masks contain the same bit
        // Note: we shouldn't overrun MAX_SHIFT because both sides are correct TrieNodes, right?
        int mapToDo = newBitMap;
        int newDataMap = this.dataMap | that.dataMap;
        int newNodeMap = this.nodeMap | that.nodeMap;
        for (int i = 0; i < buffer.length; i++, mapToDo ^= Integer.lowestOneBit(mapToDo)) {
            int mask = Integer.numberOfTrailingZeros(mapToDo);
            int bitpos = bitpos(mask);

            boolean thisHasData = (this.dataMap & bitpos) != 0;
            boolean thatHasData = (that.dataMap & bitpos) != 0;
            boolean thisHasNode = (this.nodeMap & bitpos) != 0;
            boolean thatHasNode = (that.nodeMap & bitpos) != 0;

            if (!(thisHasNode || thisHasData)) {
                if (thatHasData) {
                    buffer[index(newDataMap, bitpos)] = that.getData(that.dataIndex(bitpos));
                } else {
                    buffer[buffer.length - 1 - index(newNodeMap, bitpos)] = that.getNode(that.nodeIndex(bitpos));
                }
            } else if (!(thatHasNode || thatHasData)) {
                if (thisHasData) {
                    buffer[index(newDataMap, bitpos)] = this.getData(dataIndex(bitpos));
                } else {
                    buffer[buffer.length - 1 - index(newNodeMap, bitpos)] = this.getNode(nodeIndex(bitpos));
                }
            } else if (thisHasNode && thatHasNode) {
                Node<D> thisNode = this.getNode(this.nodeIndex(bitpos));
                Node<D> thatNode = that.getNode(that.nodeIndex(bitpos));
                buffer[buffer.length - 1 - index(newNodeMap, bitpos)] = thisNode.addAll(thatNode, shift + BIT_PARTITION_SIZE, bulkChange, updateFunction, equalsFunction, hashFunction, details);
            } else if (thisHasData && thatHasNode) {
                D thisData = this.getData(this.dataIndex(bitpos));
                Node<D> thatNode = that.getNode(that.nodeIndex(bitpos));
                details.reset();
                buffer[buffer.length - 1 - index(newNodeMap, bitpos)] = ((Node<D>) thatNode).update((D) thisData, hashFunction.applyAsInt((D) thisData), shift + BIT_PARTITION_SIZE, details, updateFunction, equalsFunction, hashFunction);
                if (!details.isModified()) bulkChange.inBothCollections++;
                newDataMap ^= bitpos;
            } else if (thisHasNode) {
                //assert thatHasData;
                D thatData = that.getData(that.dataIndex(bitpos));
                Node<D> thisNode = this.getNode(this.nodeIndex(bitpos));
                details.reset();
                buffer[buffer.length - 1 - index(newNodeMap, bitpos)] = ((Node<D>) thisNode).update((D) thatData, hashFunction.applyAsInt((D) thatData), shift + BIT_PARTITION_SIZE, details, updateFunction, equalsFunction, hashFunction);
                if (!details.isModified()) bulkChange.inBothCollections++;
                newDataMap ^= bitpos;
            } else {
                D thisData = this.getData(this.dataIndex(bitpos));
                D thatData = that.getData(that.dataIndex(bitpos));
                if (equalsFunction.test((D) thisData, (D) thatData)) {
                    bulkChange.inBothCollections++;
                    buffer[index(newDataMap, bitpos)] = thisData;
                } else {
                    newDataMap ^= bitpos;
                    newNodeMap |= bitpos;
                    buffer[buffer.length - 1 - index(newNodeMap, bitpos)] = mergeTwoDataEntriesIntoNode((D) thisData, hashFunction.applyAsInt((D) thisData), (D) thatData, hashFunction.applyAsInt((D) thatData), shift + BIT_PARTITION_SIZE);
                }
            }
        }
        return newBitmapIndexedNode(newNodeMap, newDataMap, buffer);
    }


    protected int calculateSize() {
        int size = dataArity();
        for (int i = 0, n = nodeArity(); i < n; i++) {
            Node<D> node = getNode(i);
            size += node.calculateSize();
        }
        return size;
    }
}