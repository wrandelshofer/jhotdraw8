/*
 * @(#)BitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt.impl.champmap;

import org.jhotdraw8.icollection.impl.ArrayHelper;
import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.ToIntFunction;

/// Represents a bitmap-indexed node in a CHAMP trie.
///
/// @param <K> the key type
/// @param <V> the value type
public class BitmapIndexedNode<K, V> extends Node<K, V> {
    static final BitmapIndexedNode<?, ?> EMPTY_NODE = ChampTrie.newBitmapIndexedNode(null, (0), (0), new Object[]{});

    private final int nodeMap;
    private final int dataMap;

    protected BitmapIndexedNode(int nodeMap,
                                int dataMap, Object[] mixed) {
        this.nodeMap = nodeMap;
        this.dataMap = dataMap;
        this.array = mixed;
        assert mixed.length == nodeArity() + dataArity() * ENTRY_LENGTH;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> BitmapIndexedNode<K, V> emptyNode() {
        return (BitmapIndexedNode<K, V>) EMPTY_NODE;
    }

    BitmapIndexedNode<K, V> copyAndInsertValue(@Nullable MutabilityOwnership mutator, int bitpos,
                                               K key, V val) {
        int idx = ENTRY_LENGTH * dataIndex(bitpos);
        Object[] dst = ArrayHelper.copyComponentAdd(this.array, idx, ENTRY_LENGTH);
        dst[idx] = key;
        dst[idx + 1] = val;

        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap | bitpos, dst);
    }

    BitmapIndexedNode<K, V> copyAndMigrateFromDataToNode(@Nullable MutabilityOwnership mutator,
                                                         int bitpos, Node<K, V> node) {

        int idxOld = ENTRY_LENGTH * dataIndex(bitpos);
        int idxNew = this.array.length - ENTRY_LENGTH - nodeIndex(bitpos);
        assert idxOld <= idxNew;

        // copy 'src' and remove entryLength element(s) at position 'idxOld' and
        // insert 1 element(s) at position 'idxNew'
        Object[] src = this.array;
        Object[] dst = new Object[src.length - ENTRY_LENGTH + 1];
        System.arraycopy(src, 0, dst, 0, idxOld);
        System.arraycopy(src, idxOld + ENTRY_LENGTH, dst, idxOld, idxNew - idxOld);
        System.arraycopy(src, idxNew + ENTRY_LENGTH, dst, idxNew + 1, src.length - idxNew - ENTRY_LENGTH);
        dst[idxNew] = node;
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap | bitpos, dataMap ^ bitpos, dst);
    }

    BitmapIndexedNode<K, V> copyAndMigrateFromNodeToData(@Nullable MutabilityOwnership mutator,
                                                         int bitpos, Node<K, V> node) {

        int idxOld = this.array.length - 1 - nodeIndex(bitpos);
        int idxNew = ENTRY_LENGTH * dataIndex(bitpos);

        // copy 'src' and remove 1 element(s) at position 'idxOld' and
        // insert entryLength element(s) at position 'idxNew'
        Object[] src = this.array;
        Object[] dst = new Object[src.length - 1 + ENTRY_LENGTH];
        assert idxOld >= idxNew;
        System.arraycopy(src, 0, dst, 0, idxNew);
        System.arraycopy(src, idxNew, dst, idxNew + ENTRY_LENGTH, idxOld - idxNew);
        System.arraycopy(src, idxOld + 1, dst, idxOld + ENTRY_LENGTH, src.length - idxOld - 1);
        Object[] entry = node.getDataEntry(0);
        System.arraycopy(entry, 0, dst, idxNew, ENTRY_LENGTH);
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap ^ bitpos, dataMap | bitpos, dst);
    }

    BitmapIndexedNode<K, V> copyAndSetNode(@Nullable MutabilityOwnership mutator, int bitpos,
                                           Node<K, V> node) {

        int idx = this.array.length - 1 - nodeIndex(bitpos);
        if (isAllowedToEdit(mutator)) {
            // no copying if already editable
            this.array[idx] = node;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            Object[] dst = ArrayHelper.copySet(this.array, idx, node);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst);
        }
    }

    BitmapIndexedNode<K, V> copyAndSetValue(@Nullable MutabilityOwnership mutator, int bitpos,
                                            V val) {
        int idx = ENTRY_LENGTH * dataIndex(bitpos) + 1;
        if (isAllowedToEdit(mutator)) {
            // no copying if already editable
            this.array[idx] = val;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            Object[] dst = ArrayHelper.copySet(this.array, idx, val);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst);
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

    @Override
    public boolean equivalent(Object other) {
        if (this == other) {
            return true;
        }
        BitmapIndexedNode<?, ?> that = (BitmapIndexedNode<?, ?>) other;
        Object[] thatNodes = that.array;

        // nodes array: we compare local data from 0 to splitAt (excluded)
        // and then we compare the nested nodes from splitAt to length (excluded)
        int splitAt = ENTRY_LENGTH * dataArity();

        return nodeMap() == that.nodeMap()
                && dataMap() == that.dataMap()
                && ArrayHelper.equals(array, 0, splitAt, thatNodes, 0, splitAt)
                && ArrayHelper.equals(array, splitAt, array.length, thatNodes, splitAt, thatNodes.length,
                (a, b) -> ((Node<?, ?>) a).equivalent(b));
    }


    @Override
    public @Nullable Object findByKey(K key, int keyHash, int shift) {
        int bitpos = bitpos(mask(keyHash, shift));
        if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).findByKey(key, keyHash, shift + BIT_PARTITION_SIZE);
        }
        if ((dataMap & bitpos) != 0) {
            int index = dataIndex(bitpos);
            if (Objects.equals(getKey(index), key)) {
                return getValue(index);
            }
        }
        return NO_DATA;
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
    Node<K, V> nodeAt(int bitpos) {
        return (Node<K, V>) array[array.length - 1 - nodeIndex(bitpos)];
    }

    int nodeIndex(int bitpos) {
        return Integer.bitCount(nodeMap & (bitpos - 1));
    }

    public int nodeMap() {
        return nodeMap;
    }

    @Override
    public BitmapIndexedNode<K, V> remove(@Nullable MutabilityOwnership mutator, K key,
                                          int keyHash, int shift,
                                          ChangeEvent<V> details) {
        int mask = mask(keyHash, shift);
        int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) {
            return removeData(mutator, key, keyHash, shift, details, bitpos);
        } else if ((nodeMap & bitpos) != 0) {
            return removeSubNode(mutator, key, keyHash, shift, details, bitpos);
        }

        return this;
    }

    private BitmapIndexedNode<K, V> removeData(@Nullable MutabilityOwnership mutator, K key, int keyHash, int shift, ChangeEvent<V> details, int bitpos) {
        int dataIndex = dataIndex(bitpos);

        if (!Objects.equals(getKey(dataIndex), key)) {
            return this;
        }

        V currentVal = getValue(dataIndex);
        details.updated(currentVal);

        if (dataArity() == 2 && !hasNodes()) {
            // Create new node with remaining entry. The new node will
            // a) either become the new root returned, or
            // b) unwrapped and inlined during returning.
            int newDataMap =
                    (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(keyHash, 0));

            Object[] nodes = getDataEntry(dataIndex ^ 1);
            return ChampTrie.newBitmapIndexedNode(mutator, 0, newDataMap, nodes);
        } else {
            // copy 'src' and remove entryLength element(s) at position 'idx'
            int idx = dataIndex * ENTRY_LENGTH;
            Object[] dst = ArrayHelper.copyComponentRemove(this.array, idx, ENTRY_LENGTH);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap ^ bitpos, dst);
        }
    }

    private BitmapIndexedNode<K, V> removeSubNode(@Nullable MutabilityOwnership mutator, K key, int keyHash, int shift,
                                                  ChangeEvent<V> details,
                                                  int bitpos) {
        Node<K, V> subNode = nodeAt(bitpos);
        Node<K, V> subNodeNew =
                subNode.remove(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details);

        if (subNode == subNodeNew) {
            return this;
        }

        if (!subNodeNew.hasNodes() && subNodeNew.hasDataArityOne()) {
            if (!hasData() && nodeArity() == 1) {
                // escalate (singleton or empty) result
                return (BitmapIndexedNode<K, V>) subNodeNew;
            } else {
                // inline data entry (move to front)
                return copyAndMigrateFromNodeToData(mutator, bitpos, subNodeNew);
            }
        }
        return copyAndSetNode(mutator, bitpos, subNodeNew);
    }

    @Override
    public BitmapIndexedNode<K, V> put(@Nullable MutabilityOwnership mutator,
                                       K key, V val,
                                       int keyHash, int shift,
                                       ChangeEvent<V> details,
                                       ToIntFunction<K> hashFunction) {
        int mask = mask(keyHash, shift);
        int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) { // inplace value
            int dataIndex = dataIndex(bitpos);
            K currentKey = getKey(dataIndex);
            V currentVal = getValue(dataIndex);
            if (Objects.equals(currentKey, key)) {
                if (Objects.equals(currentVal, val)) {
                    details.found(currentVal);
                    return this;
                }
                // update mapping
                details.updated(currentVal);
                return copyAndSetValue(mutator, bitpos, val);
            } else {
                Node<K, V> subNodeNew =
                        mergeTwoDataEntriesIntoNode(mutator,
                                currentKey, currentVal, hashFunction.applyAsInt(currentKey),
                                key, val, keyHash, shift + BIT_PARTITION_SIZE
                        );

                details.modified();
                return copyAndMigrateFromDataToNode(mutator, bitpos, subNodeNew);
            }
        } else if ((nodeMap & bitpos) != 0) { // node (not value)
            Node<K, V> subNode = nodeAt(bitpos);
            Node<K, V> subNodeNew =
                    subNode.put(mutator, key, val, keyHash, shift + BIT_PARTITION_SIZE, details, hashFunction);

            if (details.isModified()) {
                return copyAndSetNode(mutator, bitpos, subNodeNew);
            } else {
                return this;
            }
        } else {
            // no value
            details.modified();
            return copyAndInsertValue(mutator, bitpos, key, val);
        }
    }
}