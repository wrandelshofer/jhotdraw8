/*
 * @(#)BitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champmap;

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
    static final @NonNull BitmapIndexedNode<?, ?> EMPTY_NODE = ChampTrie.newBitmapIndexedNode(null, (0), (0), new Object[]{});

    public final Object @NonNull [] mixed;
    private final int nodeMap;
    private final int dataMap;

    protected BitmapIndexedNode(final int nodeMap,
                                final int dataMap, final @NonNull Object @NonNull [] mixed) {
        this.nodeMap = nodeMap;
        this.dataMap = dataMap;
        this.mixed = mixed;
        assert mixed.length == nodeArity() + dataArity() * ENTRY_LENGTH;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull BitmapIndexedNode<K, V> emptyNode() {
        return (BitmapIndexedNode<K, V>) EMPTY_NODE;
    }

    @NonNull BitmapIndexedNode<K, V> copyAndInsertValue(final @Nullable UniqueId mutator, final int bitpos,
                                                        final K key, final V val) {
        final int idx = ENTRY_LENGTH * dataIndex(bitpos);
        final Object[] dst = ArrayHelper.copyComponentAdd(this.mixed, idx, ENTRY_LENGTH);
        dst[idx] = key;
        dst[idx + 1] = val;

        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K, V> copyAndMigrateFromDataToNode(final @Nullable UniqueId mutator,
                                                                  final int bitpos, final Node<K, V> node) {

        final int idxOld = ENTRY_LENGTH * dataIndex(bitpos);
        final int idxNew = this.mixed.length - ENTRY_LENGTH - nodeIndex(bitpos);
        assert idxOld <= idxNew;

        // copy 'src' and remove entryLength element(s) at position 'idxOld' and
        // insert 1 element(s) at position 'idxNew'
        final Object[] src = this.mixed;
        final Object[] dst = new Object[src.length - ENTRY_LENGTH + 1];
        System.arraycopy(src, 0, dst, 0, idxOld);
        System.arraycopy(src, idxOld + ENTRY_LENGTH, dst, idxOld, idxNew - idxOld);
        System.arraycopy(src, idxNew + ENTRY_LENGTH, dst, idxNew + 1, src.length - idxNew - ENTRY_LENGTH);
        dst[idxNew] = node;
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap | bitpos, dataMap ^ bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K, V> copyAndMigrateFromNodeToData(final @Nullable UniqueId mutator,
                                                                  final int bitpos, final @NonNull Node<K, V> node) {

        final int idxOld = this.mixed.length - 1 - nodeIndex(bitpos);
        final int idxNew = ENTRY_LENGTH * dataIndex(bitpos);

        // copy 'src' and remove 1 element(s) at position 'idxOld' and
        // insert entryLength element(s) at position 'idxNew'
        final Object[] src = this.mixed;
        final Object[] dst = new Object[src.length - 1 + ENTRY_LENGTH];
        assert idxOld >= idxNew;
        System.arraycopy(src, 0, dst, 0, idxNew);
        System.arraycopy(src, idxNew, dst, idxNew + ENTRY_LENGTH, idxOld - idxNew);
        System.arraycopy(src, idxOld + 1, dst, idxOld + ENTRY_LENGTH, src.length - idxOld - 1);
        Object[] entry = node.getDataEntry(0);
        System.arraycopy(entry, 0, dst, idxNew, ENTRY_LENGTH);
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap ^ bitpos, dataMap | bitpos, dst);
    }

    @NonNull BitmapIndexedNode<K, V> copyAndSetNode(final @Nullable UniqueId mutator, final int bitpos,
                                                    final Node<K, V> node) {

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

    @NonNull BitmapIndexedNode<K, V> copyAndSetValue(final @Nullable UniqueId mutator, final int bitpos,
                                                     final V val) {
        final int idx = ENTRY_LENGTH * dataIndex(bitpos) + 1;
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

    @Override
    int dataIndex(K key, final int keyHash, final int shift) {
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
    protected boolean equivalent(final @NonNull Object other) {
        if (this == other) {
            return true;
        }
        BitmapIndexedNode<?, ?> that = (BitmapIndexedNode<?, ?>) other;
        Object[] thatNodes = that.mixed;

        // nodes array: we compare local data from 0 to splitAt (excluded)
        // and then we compare the nested nodes from splitAt to length (excluded)
        int splitAt = ENTRY_LENGTH * dataArity();

        return nodeMap() == that.nodeMap()
                && dataMap() == that.dataMap()
                && ArrayHelper.equals(mixed, 0, splitAt, thatNodes, 0, splitAt)
                && ArrayHelper.equals(mixed, splitAt, mixed.length, thatNodes, splitAt, thatNodes.length,
                (a, b) -> ((Node<?, ?>) a).equivalent(b));
    }


    @Override
    public @Nullable Object findByKey(final K key, final int keyHash, final int shift) {
        final int bitpos = bitpos(mask(keyHash, shift));
        if ((dataMap & bitpos) != 0) {
            final int index = dataIndex(bitpos);
            if (Objects.equals(getKey(index), key)) {
                return getValue(index);
            }
        } else if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).findByKey(key, keyHash, shift + BIT_PARTITION_SIZE);
        }
        return NO_VALUE;
    }

    @Override
    Object @NonNull [] getDataEntry(int index) {
        Object[] entry = new Object[ENTRY_LENGTH];
        System.arraycopy(mixed, ENTRY_LENGTH * index, entry, 0, ENTRY_LENGTH);
        return entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public K getKey(final int index) {
        return (K) mixed[ENTRY_LENGTH * index];
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    SequencedMapEntry<K, V> getKeyValueSeqEntry(final int index) {
        return new SequencedMapEntry<>(
                (K) mixed[ENTRY_LENGTH * index],
                (V) mixed[ENTRY_LENGTH * index + 1],
                0
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    Node<K, V> getNode(final int index) {
        return (Node<K, V>) mixed[mixed.length - 1 - index];
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public V getValue(final int index) {
        return (V) mixed[ENTRY_LENGTH * index + 1];
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
    Node<K, V> nodeAt(final int bitpos) {
        return (Node<K, V>) mixed[mixed.length - 1 - nodeIndex(bitpos)];
    }

    int nodeIndex(final int bitpos) {
        return Integer.bitCount(nodeMap & (bitpos - 1));
    }


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
    public @NonNull BitmapIndexedNode<K, V> remove(final @Nullable UniqueId mutator, final K key,
                                                   final int keyHash, final int shift,
                                                   final @NonNull ChangeEvent<V> details) {
        final int mask = mask(keyHash, shift);
        final int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) {
            return removeData(mutator, key, keyHash, shift, details, bitpos);
        } else if ((nodeMap & bitpos) != 0) {
            return removeSubNode(mutator, key, keyHash, shift, details, bitpos);
        }

        return this;
    }

    private @NonNull BitmapIndexedNode<K, V> removeData(@Nullable UniqueId mutator, K key, int keyHash, int shift, @NonNull ChangeEvent<V> details, int bitpos) {
        final int dataIndex = dataIndex(bitpos);

        if (!Objects.equals(getKey(dataIndex), key)) {
            return this;
        }

        final V currentVal = getValue(dataIndex);
        details.updated(currentVal);

        if (dataArity() == 2 && !hasNodes()) {
            // Create new node with remaining entry. The new node will
            // a) either become the new root returned, or
            // b) unwrapped and inlined during returning.
            final int newDataMap =
                    (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(keyHash, 0));

            Object[] nodes = getDataEntry(dataIndex ^ 1);
            return ChampTrie.newBitmapIndexedNode(mutator, 0, newDataMap, nodes);
        } else {
            // copy 'src' and remove entryLength element(s) at position 'idx'
            int idx = dataIndex * ENTRY_LENGTH;
            final Object[] dst = ArrayHelper.copyComponentRemove(this.mixed, idx, ENTRY_LENGTH);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap ^ bitpos, dst);
        }
    }

    private @NonNull BitmapIndexedNode<K, V> removeSubNode(@Nullable UniqueId mutator, K key, int keyHash, int shift,
                                                           @NonNull ChangeEvent<V> details,
                                                           int bitpos) {
        final Node<K, V> subNode = nodeAt(bitpos);
        final Node<K, V> subNodeNew =
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
    public @NonNull BitmapIndexedNode<K, V> update(final @Nullable UniqueId mutator,
                                                   final K key, final V val,
                                                   final int keyHash, final int shift,
                                                   final @NonNull ChangeEvent<V> details) {
        final int mask = mask(keyHash, shift);
        final int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) { // inplace value
            final int dataIndex = dataIndex(bitpos);
            final K currentKey = getKey(dataIndex);
            final V currentVal = getValue(dataIndex);
            if (Objects.equals(currentKey, key)) {
                if (Objects.equals(currentVal, val)) {
                    details.found(currentVal);
                    return this;
                }
                // update mapping
                details.updated(currentVal);
                return copyAndSetValue(mutator, bitpos, val);
            } else {
                final Node<K, V> subNodeNew =
                        mergeTwoDataEntriesIntoNode(mutator,
                                currentKey, currentVal, Objects.hashCode(currentKey),
                                key, val, keyHash, shift + BIT_PARTITION_SIZE
                        );

                details.modified();
                return copyAndMigrateFromDataToNode(mutator, bitpos, subNodeNew);
            }
        } else if ((nodeMap & bitpos) != 0) { // node (not value)
            final Node<K, V> subNode = nodeAt(bitpos);
            final Node<K, V> subNodeNew =
                    subNode.update(mutator, key, val, keyHash, shift + BIT_PARTITION_SIZE, details);

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