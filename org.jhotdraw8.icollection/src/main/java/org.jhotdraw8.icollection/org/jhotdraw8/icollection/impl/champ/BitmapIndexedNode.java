/*
 * @(#)BitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.impl.ArrayHelper;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Represents a bitmap-indexed node in a CHAMP trie.
///
public class BitmapIndexedNode extends Node {
    static final BitmapIndexedNode EMPTY_NODE = ChampTrie.newBitmapIndexedNode(null, (0), (0), new Object[]{}, 1);

    private final int nodeMap;
    private final int dataMap;

    protected BitmapIndexedNode(int nodeMap,
                                int dataMap, Object[] mixed, int ENTRY_LENGTH) {
        this.nodeMap = nodeMap;
        this.dataMap = dataMap;
        this.array = mixed;
        assert mixed.length == nodeArity() + dataArity(ENTRY_LENGTH) * ENTRY_LENGTH;
    }

    public static BitmapIndexedNode emptyNode() {
        return EMPTY_NODE;
    }

    BitmapIndexedNode copyAndInsertValue(@Nullable IdentityObject mutator, int bitpos,
                                         Object[] entry, int ENTRY_LENGTH) {
        int idx = ENTRY_LENGTH * dataIndex(bitpos);
        Object[] dst = ArrayHelper.copyComponentAdd(this.array, idx, ENTRY_LENGTH);
        System.arraycopy(entry, 0, dst, idx, ENTRY_LENGTH);

        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap | bitpos, dst, ENTRY_LENGTH);
    }

    BitmapIndexedNode copyAndMigrateFromDataToNode(@Nullable IdentityObject mutator,
                                                   int bitpos, Node node, int ENTRY_LENGTH) {

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
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap | bitpos, dataMap ^ bitpos, dst, ENTRY_LENGTH);
    }

    BitmapIndexedNode copyAndMigrateFromNodeToData(@Nullable IdentityObject mutator,
                                                   int bitpos, Node node, int ENTRY_LENGTH) {

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
        Object[] entry = node.getDataEntry(0, ENTRY_LENGTH);
        System.arraycopy(entry, 0, dst, idxNew, ENTRY_LENGTH);
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap ^ bitpos, dataMap | bitpos, dst, ENTRY_LENGTH);
    }

    BitmapIndexedNode copyAndSetNode(@Nullable IdentityObject mutator, int bitpos,
                                     Node node, int ENTRY_LENGTH) {

        int idx = this.array.length - 1 - nodeIndex(bitpos);
        if (isAllowedToUpdate(mutator)) {
            // no copying if already editable
            this.array[idx] = node;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            Object[] dst = ArrayHelper.copySet(this.array, idx, node);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst, ENTRY_LENGTH);
        }
    }

    BitmapIndexedNode copyAndSetEntry(@Nullable IdentityObject mutator, int bitpos,
                                      Object[] val, int ENTRY_LENGTH) {
        int idx = ENTRY_LENGTH * dataIndex(bitpos);
        if (isAllowedToUpdate(mutator)) {
            // no copying if already editable
            System.arraycopy(val, 0, this.array, idx, ENTRY_LENGTH);
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            Object[] dst = this.array.clone();
            System.arraycopy(val, 0, dst, idx, ENTRY_LENGTH);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst, ENTRY_LENGTH);
        }
    }

    @Override
    int dataArity(int ENTRY_LENGTH) {
        return Integer.bitCount(dataMap);
    }

    int dataIndex(int bitpos) {
        return Integer.bitCount(dataMap & (bitpos - 1));
    }

    public int dataMap() {
        return dataMap;
    }

    @Override
    public boolean equivalent(Object other, int ENTRY_LENGTH) {
        if (this == other) {
            return true;
        }
        BitmapIndexedNode that = (BitmapIndexedNode) other;
        Object[] thatNodes = that.array;

        // nodes array: we compare local data from 0 to splitAt (excluded)
        // and then we compare the nested nodes from splitAt to length (excluded)
        int splitAt = ENTRY_LENGTH * dataArity(ENTRY_LENGTH);

        return nodeMap() == that.nodeMap()
                && dataMap() == that.dataMap()
                && ArrayHelper.equals(array, 0, splitAt, thatNodes, 0, splitAt)
                && ArrayHelper.equals(array, splitAt, array.length, thatNodes, splitAt, thatNodes.length,
                (a, b) -> ((Node) a).equivalent(b, ENTRY_LENGTH));
    }


    @Override
    public @Nullable Object findEntry(Object key, int keyHash, int shift, int ENTRY_LENGTH) {
        int bitpos = bitpos(mask(keyHash, shift));
        if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).findEntry(key, keyHash, shift + BIT_PARTITION_SIZE, ENTRY_LENGTH);
        }
        if ((dataMap & bitpos) != 0) {
            int index = dataIndex(bitpos);
            if (Objects.equals(getKey(index, ENTRY_LENGTH), key)) {
                return getEntry(index, ENTRY_LENGTH);
            }
        }
        return NO_DATA;
    }

    @Override
    public @Nullable Object findData(Object key, int keyHash, int shift, int ENTRY_LENGTH, int DATA_INDEX) {
        int bitpos = bitpos(mask(keyHash, shift));
        if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).findEntry(key, keyHash, shift + BIT_PARTITION_SIZE, ENTRY_LENGTH);
        }
        if ((dataMap & bitpos) != 0) {
            int index = dataIndex(bitpos);
            if (Objects.equals(getKey(index, ENTRY_LENGTH), key)) {
                return getData(index, ENTRY_LENGTH, DATA_INDEX);
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
    Node nodeAt(int bitpos) {
        return (Node) array[array.length - 1 - nodeIndex(bitpos)];
    }

    int nodeIndex(int bitpos) {
        return Integer.bitCount(nodeMap & (bitpos - 1));
    }

    public int nodeMap() {
        return nodeMap;
    }

    @Override
    public BitmapIndexedNode remove(@Nullable IdentityObject mutator, Object key,
                                    int keyHash, int shift,
                                    ChangeEvent details, int ENTRY_LENGTH) {
        int mask = mask(keyHash, shift);
        int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) {
            return removeData(mutator, key, keyHash, shift, details, bitpos, ENTRY_LENGTH);
        } else if ((nodeMap & bitpos) != 0) {
            return removeSubNode(mutator, key, keyHash, shift, details, bitpos, ENTRY_LENGTH);
        }

        return this;
    }

    private BitmapIndexedNode removeData(@Nullable IdentityObject mutator, Object key, int keyHash, int shift, ChangeEvent details, int bitpos, int ENTRY_LENGTH) {
        int dataIndex = dataIndex(bitpos);

        if (!Objects.equals(getKey(dataIndex, ENTRY_LENGTH), key)) {
            return this;
        }

        Object[] currentEntry = getEntry(dataIndex, ENTRY_LENGTH);
        details.setRemoved(currentEntry);

        if (dataArity(ENTRY_LENGTH) == 2 && !hasNodes()) {
            // Create new node with remaining entry. The new node will
            // a) either become the new root returned, or
            // b) unwrapped and inlined during returning.
            int newDataMap =
                    (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(keyHash, 0));

            Object[] nodes = getDataEntry(dataIndex ^ 1, ENTRY_LENGTH);
            return ChampTrie.newBitmapIndexedNode(mutator, 0, newDataMap, nodes, ENTRY_LENGTH);
        } else {
            // copy 'src' and remove entryLength element(s) at position 'idx'
            int idx = dataIndex * ENTRY_LENGTH;
            Object[] dst = ArrayHelper.copyComponentRemove(this.array, idx, ENTRY_LENGTH);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap ^ bitpos, dst, ENTRY_LENGTH);
        }
    }

    private BitmapIndexedNode removeSubNode(@Nullable IdentityObject mutator, Object key, int keyHash, int shift,
                                            ChangeEvent details,
                                            int bitpos, int ENTRY_LENGTH) {
        Node subNode = nodeAt(bitpos);
        Node subNodeNew =
                subNode.remove(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details, ENTRY_LENGTH);

        if (subNode == subNodeNew) {
            return this;
        }

        if (!subNodeNew.hasNodes() && subNodeNew.hasDataArityOne()) {
            if (!hasData() && nodeArity() == 1) {
                // escalate (singleton or empty) result
                return (BitmapIndexedNode) subNodeNew;
            } else {
                // inline data entry (move to front)
                return copyAndMigrateFromNodeToData(mutator, bitpos, subNodeNew, ENTRY_LENGTH);
            }
        }
        return copyAndSetNode(mutator, bitpos, subNodeNew, ENTRY_LENGTH);
    }

    @Override
    public BitmapIndexedNode put(@Nullable IdentityObject mutator,
                                 Object key, Object[] newEntry, int keyHash,
                                 int shift, ChangeEvent details,
                                 BiFunction<Object[], Object[], Object[]> updateFunction,
                                 ToIntFunction<Object> hashFunction,
                                 int ENTRY_LENGTH) {
        int mask = mask(keyHash, shift);
        int bitpos = bitpos(mask);
        if ((dataMap & bitpos) != 0) {
            int dataIndex = dataIndex(bitpos);
            Object currentKey = getKey(dataIndex, ENTRY_LENGTH);
            Object[] currentEntry = getEntry(dataIndex, ENTRY_LENGTH);
            if (Objects.equals(currentKey, key)) {
                Object[] updatedEntry = updateFunction.apply(currentEntry, newEntry);
                if (currentEntry == updatedEntry) {
                    details.setFound(currentEntry);
                    return this;
                }
                details.setReplaced(currentEntry, updatedEntry);
                return copyAndSetEntry(mutator, bitpos, updatedEntry, ENTRY_LENGTH);
            } else {
                Node updatedSubNode =
                        mergeTwoDataEntriesIntoNode(mutator,
                                currentEntry, hashFunction.applyAsInt(currentKey),
                                newEntry, keyHash, shift + BIT_PARTITION_SIZE,
                                ENTRY_LENGTH);

                details.setAdded(newEntry);
                return copyAndMigrateFromDataToNode(mutator, bitpos, updatedSubNode, ENTRY_LENGTH);
            }
        } else if ((nodeMap & bitpos) != 0) {
            Node subNode = nodeAt(bitpos);
            Node updatedSubNode =
                    subNode.put(mutator, key, newEntry, keyHash, shift + BIT_PARTITION_SIZE, details,
                            updateFunction, hashFunction, ENTRY_LENGTH);
            return subNode == updatedSubNode ? this : copyAndSetNode(mutator, bitpos, updatedSubNode, ENTRY_LENGTH);
        } else {
            details.setAdded(newEntry);
            return copyAndInsertValue(mutator, bitpos, newEntry, ENTRY_LENGTH);
        }
    }

    protected int calculateSize(int ENTRY_LENGTH) {
        int size = dataArity(ENTRY_LENGTH);
        for (int i = 0, n = nodeArity(); i < n; i++) {
            Node node = getNode(i);
            size += node.calculateSize(ENTRY_LENGTH);
        }
        return size;
    }

    @Override
    public BitmapIndexedNode putAll(IdentityObject owner, Node other, int shift,
                                    BulkChangeEvent bulkChange,
                                    ToIntFunction<Object> hashFunction,
                                    ChangeEvent details, int ENTRY_LENGTH) {
        var that = (BitmapIndexedNode) other;
        if (this == that) {
            bulkChange.inBoth += this.calculateSize(ENTRY_LENGTH);
            return this;
        }

        var newBitMap = nodeMap | dataMap | that.nodeMap | that.dataMap;
        var buffer = new Object[Integer.bitCount(nodeMap | that.nodeMap) +
                Integer.bitCount(dataMap | that.dataMap) * ENTRY_LENGTH];
        int newDataMap = this.dataMap | that.dataMap;
        int newNodeMap = this.nodeMap | that.nodeMap;
        for (int mapToDo = newBitMap; mapToDo != 0; mapToDo ^= Integer.lowestOneBit(mapToDo)) {
            int mask = Integer.numberOfTrailingZeros(mapToDo);
            int bitpos = bitpos(mask);

            boolean thisIsData = (this.dataMap & bitpos) != 0;
            boolean thatIsData = (that.dataMap & bitpos) != 0;
            boolean thisIsNode = (this.nodeMap & bitpos) != 0;
            boolean thatIsNode = (that.nodeMap & bitpos) != 0;

            if (!(thisIsNode || thisIsData)) {
                // add 'mixed' (data or node) from that trie
                if (thatIsData) {
                    System.arraycopy(that.array, that.dataArrayIndex(that.dataIndex(bitpos), ENTRY_LENGTH),
                            buffer, dataArrayIndex(index(newDataMap, bitpos), ENTRY_LENGTH), ENTRY_LENGTH);
                } else {
                    buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = that.getNode(that.nodeIndex(bitpos));
                }
            } else if (!(thatIsNode || thatIsData)) {
                // add 'mixed' (data or node) from this trie
                if (thisIsData) {
                    System.arraycopy(this.array, this.dataArrayIndex(dataIndex(bitpos), ENTRY_LENGTH),
                            buffer, dataArrayIndex(index(newDataMap, bitpos), ENTRY_LENGTH), ENTRY_LENGTH);
                } else {
                    buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = this.getNode(nodeIndex(bitpos));
                }
            } else if (thisIsNode && thatIsNode) {
                // add a new node that joins this node and that node
                Node thisNode = this.getNode(this.nodeIndex(bitpos));
                Node thatNode = that.getNode(that.nodeIndex(bitpos));
                buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] =
                        thisNode.putAll(owner, thatNode, shift + BIT_PARTITION_SIZE, bulkChange,
                                hashFunction, details, ENTRY_LENGTH);
            } else if (thisIsData && thatIsNode) {
                // add a new node that joins this data and that node
                Object[] thisEntry = this.getEntry(this.dataIndex(bitpos), ENTRY_LENGTH);
                Object thisEntryKey = thisEntry[0];
                Node thatNode = that.getNode(that.nodeIndex(bitpos));
                details.reset();
                buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = thatNode.put(null,
                        thisEntryKey, thisEntry, hashFunction.applyAsInt(thisEntryKey),
                        shift + BIT_PARTITION_SIZE, details,
                        (a, b) -> b,// our node must take precedence
                        hashFunction, ENTRY_LENGTH);
                if (details.isUnchanged()) {
                    bulkChange.inBoth++;
                } else if (details.isReplaced()) {
                    bulkChange.replaced = true;
                    bulkChange.inBoth++;
                }
                newDataMap ^= bitpos;
            } else if (thisIsNode) {
                // add a new node that joins this node and that data
                Object[] thatEntry = that.getEntry(that.dataIndex(bitpos), ENTRY_LENGTH);
                Object thatEntryKey = thatEntry[0];
                Node thisNode = this.getNode(this.nodeIndex(bitpos));
                details.reset();
                buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = thisNode.put(owner, thatEntryKey,
                        thatEntry, hashFunction.applyAsInt(thatEntryKey),
                        shift + BIT_PARTITION_SIZE, details,
                        (a, b) -> a,// our node must take precedence
                        hashFunction, ENTRY_LENGTH);
                if (!details.isModified()) {
                    bulkChange.inBoth++;
                }
                newDataMap ^= bitpos;
            } else {
                // add a new node that joins this data and that data
                int thisDataIndex = this.dataIndex(bitpos);
                Object thisEntryKey = this.getData(thisDataIndex, ENTRY_LENGTH, 0);
                int thatDataIndex = that.dataIndex(bitpos);
                Object thatEntryKey = that.getData(thatDataIndex, ENTRY_LENGTH, 0);
                if (Objects.equals(thisEntryKey, thatEntryKey)) {
                    bulkChange.inBoth++;
                    System.arraycopy(this.array, dataArrayIndex(thisDataIndex, ENTRY_LENGTH),
                            buffer, dataArrayIndex(index(newDataMap, bitpos), ENTRY_LENGTH), ENTRY_LENGTH);
                } else {
                    newDataMap ^= bitpos;
                    newNodeMap ^= bitpos;
                    Object[] thisEntry = this.getEntry(thisDataIndex, ENTRY_LENGTH);
                    Object[] thatEntry = that.getEntry(thatDataIndex, ENTRY_LENGTH);
                    buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = mergeTwoDataEntriesIntoNode(
                            owner, thisEntry, hashFunction.applyAsInt(thisEntryKey),
                            thatEntry, hashFunction.applyAsInt(thatEntryKey),
                            shift + BIT_PARTITION_SIZE, ENTRY_LENGTH);
                }
            }
        }
        /*
        int newDataCount = Integer.bitCount(newDataMap);
        int newNodeCount = Integer.bitCount(newNodeMap);
        if (buffer.length > newDataCount * ENTRY_LENGTH + newNodeCount) {
            Object[] tmp = buffer;
            buffer = new Object[newDataCount * ENTRY_LENGTH + newNodeCount];
            System.arraycopy(tmp, 0, buffer, 0, newDataCount * ENTRY_LENGTH);
            System.arraycopy(tmp, tmp.length - newNodeCount, buffer, newDataCount * ENTRY_LENGTH, newNodeCount);
        }
        return new BitmapIndexedNode(newNodeMap,newDataMap,buffer,  ENTRY_LENGTH);
        */
        return newCroppedBitmapIndexedNode(buffer, newDataMap, newNodeMap, ENTRY_LENGTH);
    }

    @Override
    public BitmapIndexedNode removeIf(@Nullable IdentityObject owner, Predicate<Object> predicate, int shift, BulkChangeEvent bulkChange, int ENTRY_LENGTH) {
        var newBitMap = nodeMap | dataMap;
        var buffer = new Object[Integer.bitCount(newBitMap) * ENTRY_LENGTH];
        int newDataMap = this.dataMap;
        int newNodeMap = this.nodeMap;
        for (int mapToDo = newBitMap; mapToDo != 0; mapToDo ^= Integer.lowestOneBit(mapToDo)) {
            int mask = Integer.numberOfTrailingZeros(mapToDo);
            int bitpos = bitpos(mask);
            boolean thisIsNode = (this.nodeMap & bitpos) != 0;
            if (thisIsNode) {
                Node thisNode = this.getNode(this.nodeIndex(bitpos));
                Node result = thisNode.removeIf(owner, predicate, shift + BIT_PARTITION_SIZE, bulkChange, ENTRY_LENGTH);
                if (result.isNodeEmpty()) {
                    newNodeMap ^= bitpos;
                } else if (result.hasMany(ENTRY_LENGTH)) {
                    buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = result;
                } else {
                    newNodeMap ^= bitpos;
                    newDataMap ^= bitpos;
                    System.arraycopy(result.array, 0, buffer, dataArrayIndex(index(newDataMap, bitpos), ENTRY_LENGTH), ENTRY_LENGTH);
                }
            } else {
                Object thisKey = this.getKey(this.dataIndex(bitpos), ENTRY_LENGTH);
                if (!predicate.test(thisKey)) {
                    buffer[dataArrayIndex(index(newDataMap, bitpos), ENTRY_LENGTH)] = thisKey;
                } else {
                    newDataMap ^= bitpos;
                    bulkChange.removed++;
                }
            }
        }
        return newCroppedBitmapIndexedNode(buffer, newDataMap, newNodeMap, ENTRY_LENGTH);
    }

    private BitmapIndexedNode newCroppedBitmapIndexedNode(Object[] buffer, int newDataMap, int newNodeMap, int ENTRY_LENGTH) {
        int dataCount = Integer.bitCount(newDataMap);
        int nodeCount = Integer.bitCount(newNodeMap);
        int newLength = dataCount * ENTRY_LENGTH + nodeCount;
        if (newLength != buffer.length) {
            Object[] temp = buffer;
            buffer = new Object[newLength];
            System.arraycopy(temp, 0, buffer, 0, dataCount * ENTRY_LENGTH);
            System.arraycopy(temp, temp.length - nodeCount, buffer, dataCount, nodeCount);
        }
        return new BitmapIndexedNode(newNodeMap, newDataMap, buffer, ENTRY_LENGTH);
    }
}