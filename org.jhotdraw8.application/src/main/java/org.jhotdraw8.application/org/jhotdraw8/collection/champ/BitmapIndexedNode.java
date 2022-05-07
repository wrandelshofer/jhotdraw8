/*
 * @(#)BitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ArrayHelper;
import org.jhotdraw8.collection.UniqueId;

import java.util.Objects;

/**
 * Represents a bitmap-indexed node in a CHAMP trie.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class BitmapIndexedNode<K, V> extends Node<K, V> {
    static final BitmapIndexedNode<?, ?> EMPTY_NODE = ChampTrie.newBitmapIndexedNode(null, (0), (0), new Object[]{}, 1);

    public final Object[] mixed;
    private final int nodeMap;
    private final int dataMap;

    protected BitmapIndexedNode(final int nodeMap,
                                final int dataMap, final @NonNull Object[] mixed, int entryLength) {
        this.nodeMap = nodeMap;
        this.dataMap = dataMap;
        this.mixed = mixed;
        assert mixed.length == nodeArity() + dataArity(entryLength) * entryLength;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> BitmapIndexedNode<K, V> emptyNode() {
        return (BitmapIndexedNode<K, V>) EMPTY_NODE;
    }

    BitmapIndexedNode<K, V> copyAndInsertValue(final UniqueId mutator, final int bitpos,
                                               final K key, final V val, int entryLength, int sequenceNumber) {
        final int idx = entryLength * dataIndex(bitpos);
        final Object[] dst = ArrayHelper.copyComponentAdd(this.mixed, idx, entryLength);
        dst[idx] = key;
        if (entryLength > 1) {
            dst[idx + 1] = val;
        }
        if (sequenceNumber != NO_SEQUENCE_NUMBER) {
            dst[idx + entryLength - 1] = sequenceNumber;
        }

        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap | bitpos, dst, entryLength);
    }

    BitmapIndexedNode<K, V> copyAndMigrateFromDataToNode(final UniqueId mutator,
                                                         final int bitpos, final Node<K, V> node, int entryLength) {

        final int idxOld = entryLength * dataIndex(bitpos);
        final int idxNew = this.mixed.length - entryLength - nodeIndex(bitpos);
        assert idxOld <= idxNew;

        // copy 'src' and remove entryLength element(s) at position 'idxOld' and
        // insert 1 element(s) at position 'idxNew'
        final Object[] src = this.mixed;
        final Object[] dst = new Object[src.length - entryLength + 1];
        System.arraycopy(src, 0, dst, 0, idxOld);
        System.arraycopy(src, idxOld + entryLength, dst, idxOld, idxNew - idxOld);
        System.arraycopy(src, idxNew + entryLength, dst, idxNew + 1, src.length - idxNew - entryLength);
        dst[idxNew] = node;
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap | bitpos, dataMap ^ bitpos, dst, entryLength);
    }

    BitmapIndexedNode<K, V> copyAndMigrateFromNodeToData(final UniqueId mutator,
                                                         final int bitpos, final Node<K, V> node, int entryLength) {

        final int idxOld = this.mixed.length - 1 - nodeIndex(bitpos);
        final int idxNew = entryLength * dataIndex(bitpos);

        // copy 'src' and remove 1 element(s) at position 'idxOld' and
        // insert entryLength element(s) at position 'idxNew'
        final Object[] src = this.mixed;
        final Object[] dst = new Object[src.length - 1 + entryLength];
        assert idxOld >= idxNew;
        System.arraycopy(src, 0, dst, 0, idxNew);
        System.arraycopy(src, idxNew, dst, idxNew + entryLength, idxOld - idxNew);
        System.arraycopy(src, idxOld + 1, dst, idxOld + entryLength, src.length - idxOld - 1);
        Object[] entry = node.getDataEntry(0, entryLength);
        System.arraycopy(entry, 0, dst, idxNew, entryLength);
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap ^ bitpos, dataMap | bitpos, dst, entryLength);
    }

    BitmapIndexedNode<K, V> copyAndSetNode(final UniqueId mutator, final int bitpos,
                                           final Node<K, V> node, int entryLength) {

        final int idx = this.mixed.length - 1 - nodeIndex(bitpos);
        if (isAllowedToEdit(mutator)) {
            // no copying if already editable
            this.mixed[idx] = node;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            final Object[] dst = ArrayHelper.copySet(this.mixed, idx, node);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst, entryLength);
        }
    }

    BitmapIndexedNode<K, V> copyAndSetValue(final UniqueId mutator, final int bitpos,
                                            final V val, int entryLength) {
        final int idx = entryLength * dataIndex(bitpos) + 1;
        if (isAllowedToEdit(mutator)) {
            // no copying if already editable
            this.mixed[idx] = val;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            final Object[] dst = ArrayHelper.copySet(this.mixed, idx, val);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst, entryLength);
        }
    }

    @Override
    int dataArity(int entryLength) {
        return Integer.bitCount(dataMap);
    }

    int dataIndex(final int bitpos) {
        return Integer.bitCount(dataMap & (bitpos - 1));
    }

    @Override
    int dataIndex(K key, final int keyHash, final int shift, int entryLength) {
        final int mask = mask(keyHash, shift);
        final int bitpos = bitpos(mask);
        return (this.dataMap & bitpos) != 0
                ? index(this.dataMap, bitpos)
                : -1;
    }

    public int dataMap() {
        return dataMap;
    }

    @Override
    protected boolean equivalent(final @NonNull Object other, int entryLength, int numFields) {
        if (this == other) {
            return true;
        }
        BitmapIndexedNode<?, ?> that = (BitmapIndexedNode<?, ?>) other;
        Object[] thatNodes = that.mixed;

        // nodes array: we compare local data from 0 to splitAt (excluded)
        // and then we compare the nested nodes from splitAt to length (excluded)
        int splitAt = entryLength * dataArity(entryLength);

        if (numFields < entryLength) {
            for (int i = 0; i < splitAt; i += entryLength) {
                for (int f = 0; f < numFields; f++) {
                    if (!Objects.equals(mixed[i + f], thatNodes[i + f])) {
                        return false;
                    }
                }
            }
            return ArrayHelper.equals(mixed, splitAt, mixed.length, thatNodes, splitAt, thatNodes.length,
                    (a, b) -> ((Node<?, ?>) a).equivalent(b, entryLength, numFields));
        }

        return nodeMap() == that.nodeMap()
                && dataMap() == that.dataMap()
                && ArrayHelper.equals(mixed, 0, splitAt, thatNodes, 0, splitAt)
                && ArrayHelper.equals(mixed, splitAt, mixed.length, thatNodes, splitAt, thatNodes.length,
                (a, b) -> ((Node<?, ?>) a).equivalent(b, entryLength, numFields));
    }


    @Override
    public Object findByKey(final K key, final int keyHash, final int shift, int entryLength, int numFields) {
        final int bitpos = bitpos(mask(keyHash, shift));
        if ((dataMap & bitpos) != 0) {
            final int index = dataIndex(bitpos);
            if (Objects.equals(getKey(index, entryLength), key)) {
                return getValue(index, entryLength, numFields);
            }
        } else if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).findByKey(key, keyHash, shift + BIT_PARTITION_SIZE, entryLength, numFields);
        }
        return NO_VALUE;
    }

    @Override
    Object[] getDataEntry(int index, int entryLength) {
        Object[] entry = new Object[entryLength];
        System.arraycopy(mixed, entryLength * index, entry, 0, entryLength);
        return entry;
    }

    @SuppressWarnings("unchecked")
    K getKey(final int index, int entryLength) {
        return (K) mixed[entryLength * index];
    }

    @Override
    @SuppressWarnings("unchecked")
    SequencedMapEntry<K, V> getKeyValueSeqEntry(final int index, int entryLength, int numFields) {
        return new SequencedMapEntry<>(
                (K) mixed[entryLength * index],
                numFields > 1 ? (V) mixed[entryLength * index + 1] : null,
                numFields < entryLength ? (Integer) mixed[entryLength * index + entryLength - 1] : 0
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    Node<K, V> getNode(final int index, int entryLength) {
        return (Node<K, V>) mixed[mixed.length - 1 - index];
    }

    int getSequenceNumber(final int index, int entryLength, int numFields) {
        return numFields < entryLength ? (int) mixed[entryLength * index + entryLength - 1] : NO_SEQUENCE_NUMBER;
    }

    @Override
    @SuppressWarnings("unchecked")
    V getValue(final int index, int entryLength, int numFields) {
        return numFields > 1 ? (V) mixed[entryLength * index + 1] : null;
    }

    @Override
    boolean hasData() {
        return dataMap != 0;
    }

    @Override
    boolean hasDataArityOne(int entryLength) {
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
    Node<K, V> nodeAt(final int bitpos) {
        return (Node<K, V>) mixed[mixed.length - 1 - nodeIndex(bitpos)];
    }

    int nodeIndex(final int bitpos) {
        return Integer.bitCount(nodeMap & (bitpos - 1));
    }

    @Override
    int nodeIndex(int keyHash, int shift) {
        final int mask = mask(keyHash, shift);
        final int bitpos = bitpos(mask);
        return (this.nodeMap & bitpos) != 0
                ? index(this.nodeMap, bitpos)
                : -1;
    }

    public int nodeMap() {
        return nodeMap;
    }

    @Override
    public BitmapIndexedNode<K, V> remove(final @Nullable UniqueId mutator, final K key,
                                          final int keyHash, final int shift,
                                          final @NonNull ChangeEvent<V> details, int entryLength, int numFields) {
        final int mask = mask(keyHash, shift);
        final int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) {
            return removeData(mutator, key, keyHash, shift, details, entryLength, bitpos, numFields);
        } else if ((nodeMap & bitpos) != 0) {
            return removeSubNode(mutator, key, keyHash, shift, details, entryLength, bitpos, numFields);
        }

        return this;
    }

    private BitmapIndexedNode<K, V> removeData(@Nullable UniqueId mutator, K key, int keyHash, int shift, ChangeEvent<V> details, int entryLength, int bitpos, int numFields) {
        final int dataIndex = dataIndex(bitpos);

        if (!Objects.equals(getKey(dataIndex, entryLength), key)) {
            return this;
        }

        final V currentVal = getValue(dataIndex, entryLength, numFields);
        details.updated(currentVal);

        if (dataArity(entryLength) == 2 && !hasNodes()) {
            // Create new node with remaining entry. The new node will
            // a) either become the new root returned, or
            // b) unwrapped and inlined during returning.
            final int newDataMap =
                    (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(keyHash, 0));

            Object[] nodes = getDataEntry(dataIndex ^ 1, entryLength);
            return ChampTrie.newBitmapIndexedNode(mutator, 0, newDataMap, nodes, entryLength);
        } else {
            // copy 'src' and remove entryLength element(s) at position 'idx'
            int idx = dataIndex * entryLength;
            final Object[] dst = ArrayHelper.copyComponentRemove(this.mixed, idx, entryLength);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap ^ bitpos, dst, entryLength);
        }
    }

    private BitmapIndexedNode<K, V> removeSubNode(@Nullable UniqueId mutator, K key, int keyHash, int shift,
                                                  @NonNull ChangeEvent<V> details, int entryLength,
                                                  int bitpos, int numFields) {
        final Node<K, V> subNode = nodeAt(bitpos);
        final Node<K, V> subNodeNew =
                subNode.remove(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details, entryLength, numFields);

        if (subNode == subNodeNew) {
            return this;
        }

        if (!subNodeNew.hasNodes() && subNodeNew.hasDataArityOne(entryLength)) {
            if (!hasData() && nodeArity() == 1) {
                // escalate (singleton or empty) result
                return (BitmapIndexedNode<K, V>) subNodeNew;
            } else {
                // inline data entry (move to front)
                return copyAndMigrateFromNodeToData(mutator, bitpos, subNodeNew, entryLength);
            }
        }
        return copyAndSetNode(mutator, bitpos, subNodeNew, entryLength);
    }

    @SuppressWarnings("unchecked")
    @Override
    public BitmapIndexedNode<K, V> update(final UniqueId mutator,
                                          final K key, final V val,
                                          final int keyHash, final int shift,
                                          final ChangeEvent<V> details, int entryLength, int sequenceNumber, int numFields) {
        final int mask = mask(keyHash, shift);
        final int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) { // inplace value
            final int dataIndex = dataIndex(bitpos);
            final K currentKey = getKey(dataIndex, entryLength);
            final V currentVal = getValue(dataIndex, entryLength, numFields);
            if (Objects.equals(currentKey, key)) {
                if (numFields == 1 || Objects.equals(currentVal, val)) {
                    details.found(currentVal);
                    return this;
                }
                // update mapping
                details.updated(currentVal);
                return copyAndSetValue(mutator, bitpos, val, entryLength);
            } else {
                int currentSequenceNumber = getSequenceNumber(dataIndex, entryLength, numFields);
                final Node<K, V> subNodeNew =
                        mergeTwoDataEntrysIntoNode(mutator,
                                currentKey, currentVal, currentSequenceNumber, Objects.hashCode(currentKey),
                                key, val, sequenceNumber, keyHash, shift + BIT_PARTITION_SIZE,
                                entryLength);

                details.modified();
                return copyAndMigrateFromDataToNode(mutator, bitpos, subNodeNew, entryLength);
            }
        } else if ((nodeMap & bitpos) != 0) { // node (not value)
            final Node<K, V> subNode = nodeAt(bitpos);
            final Node<K, V> subNodeNew =
                    subNode.update(mutator, key, val, keyHash, shift + BIT_PARTITION_SIZE, details, entryLength, sequenceNumber, numFields);

            if (details.isModified()) {
                return copyAndSetNode(mutator, bitpos, subNodeNew, entryLength);
            } else {
                return this;
            }
        } else {
            // no value
            details.modified();
            return copyAndInsertValue(mutator, bitpos, key, val, entryLength, sequenceNumber);
        }
    }
}