/*
 * @(#)BitmapIndexedNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.impl.ArrayHelper;
import org.jhotdraw8.icollection.impl.MutabilityOwnership;
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
                                int dataMap, Object[] mixed, int DATA_LENGTH) {
        this.nodeMap = nodeMap;
        this.dataMap = dataMap;
        this.array = mixed;
        assert mixed.length == nodeArity() + dataArity(DATA_LENGTH) * DATA_LENGTH;
    }

    public static BitmapIndexedNode emptyNode() {
        return EMPTY_NODE;
    }

    BitmapIndexedNode copyAndInsertData(@Nullable MutabilityOwnership mutator, int bitpos,
                                        Object[] entry, int DATA_LENGTH) {
        int idx = DATA_LENGTH * dataIndex(bitpos);
        Object[] dst = ArrayHelper.copyComponentAdd(this.array, idx, DATA_LENGTH);
        System.arraycopy(entry, 0, dst, idx, DATA_LENGTH);

        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap | bitpos, dst, DATA_LENGTH);
    }

    BitmapIndexedNode copyAndMigrateFromDataToNode(@Nullable MutabilityOwnership mutator,
                                                   int bitpos, Node node, int DATA_LENGTH) {

        int idxOld = DATA_LENGTH * dataIndex(bitpos);
        int idxNew = this.array.length - DATA_LENGTH - nodeIndex(bitpos);
        assert idxOld <= idxNew;

        // copy 'src' and remove entryLength element(s) at position 'idxOld' and
        // insert 1 element(s) at position 'idxNew'
        Object[] src = this.array;
        Object[] dst = new Object[src.length - DATA_LENGTH + 1];
        System.arraycopy(src, 0, dst, 0, idxOld);
        System.arraycopy(src, idxOld + DATA_LENGTH, dst, idxOld, idxNew - idxOld);
        System.arraycopy(src, idxNew + DATA_LENGTH, dst, idxNew + 1, src.length - idxNew - DATA_LENGTH);
        dst[idxNew] = node;
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap | bitpos, dataMap ^ bitpos, dst, DATA_LENGTH);
    }

    BitmapIndexedNode copyAndMigrateFromNodeToData(@Nullable MutabilityOwnership mutator,
                                                   int bitpos, Node node, int DATA_LENGTH) {

        int idxOld = this.array.length - 1 - nodeIndex(bitpos);
        int idxNew = DATA_LENGTH * dataIndex(bitpos);

        // copy 'src' and remove 1 element(s) at position 'idxOld' and
        // insert entryLength element(s) at position 'idxNew'
        Object[] src = this.array;
        Object[] dst = new Object[src.length - 1 + DATA_LENGTH];
        assert idxOld >= idxNew;
        System.arraycopy(src, 0, dst, 0, idxNew);
        System.arraycopy(src, idxNew, dst, idxNew + DATA_LENGTH, idxOld - idxNew);
        System.arraycopy(src, idxOld + 1, dst, idxOld + DATA_LENGTH, src.length - idxOld - 1);
        Object[] entry = node.getDataEntry(0, DATA_LENGTH);
        System.arraycopy(entry, 0, dst, idxNew, DATA_LENGTH);
        return ChampTrie.newBitmapIndexedNode(mutator, nodeMap ^ bitpos, dataMap | bitpos, dst, DATA_LENGTH);
    }

    BitmapIndexedNode copyAndSetNode(@Nullable MutabilityOwnership mutator, int bitpos,
                                     Node node, int DATA_LENGTH) {

        int idx = this.array.length - 1 - nodeIndex(bitpos);
        if (isAllowedToUpdate(mutator)) {
            // no copying if already editable
            this.array[idx] = node;
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            Object[] dst = ArrayHelper.copySet(this.array, idx, node);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst, DATA_LENGTH);
        }
    }

    BitmapIndexedNode copyAndSetEntry(@Nullable MutabilityOwnership mutator, int bitpos,
                                      Object[] val, int DATA_LENGTH) {
        int idx = DATA_LENGTH * dataIndex(bitpos);
        if (isAllowedToUpdate(mutator)) {
            // no copying if already editable
            System.arraycopy(val, 0, this.array, idx, DATA_LENGTH);
            return this;
        } else {
            // copy 'src' and set 1 element(s) at position 'idx'
            Object[] dst = this.array.clone();
            System.arraycopy(val, 0, dst, idx, DATA_LENGTH);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap, dst, DATA_LENGTH);
        }
    }

    @Override
    int dataArity(int DATA_LENGTH) {
        return Integer.bitCount(dataMap);
    }

    int dataIndex(int bitpos) {
        return Integer.bitCount(dataMap & (bitpos - 1));
    }

    public int dataMap() {
        return dataMap;
    }

    @Override
    public boolean equivalent(Object other, int DATA_LENGTH) {
        if (this == other) {
            return true;
        }
        BitmapIndexedNode that = (BitmapIndexedNode) other;
        Object[] thatNodes = that.array;

        // nodes array: we compare local data from 0 to splitAt (excluded)
        // and then we compare the nested nodes from splitAt to length (excluded)
        int splitAt = DATA_LENGTH * dataArity(DATA_LENGTH);

        return nodeMap() == that.nodeMap()
                && dataMap() == that.dataMap()
                && ArrayHelper.equals(array, 0, splitAt, thatNodes, 0, splitAt)
                && ArrayHelper.equals(array, splitAt, array.length, thatNodes, splitAt, thatNodes.length,
                (a, b) -> ((Node) a).equivalent(b, DATA_LENGTH));
    }


    @Override
    public @Nullable Object findData(Object key, int keyHash, int shift, int DATA_LENGTH) {
        int bitpos = bitpos(mask(keyHash, shift));
        if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).findData(key, keyHash, shift + BIT_PARTITION_SIZE, DATA_LENGTH);
        }
        if ((dataMap & bitpos) != 0) {
            int index = dataIndex(bitpos);
            if (Objects.equals(getKey(index, DATA_LENGTH), key)) {
                return getData(index, DATA_LENGTH);
            }
        }
        return NO_DATA;
    }

    @Override
    public @Nullable Object findValue(Object key, int keyHash, int shift, int DATA_LENGTH, int VALUE_INDEX) {
        int bitpos = bitpos(mask(keyHash, shift));
        if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).findValue(key, keyHash, shift + BIT_PARTITION_SIZE, DATA_LENGTH, VALUE_INDEX);
        }
        if ((dataMap & bitpos) != 0) {
            int index = dataIndex(bitpos);
            if (Objects.equals(getKey(index, DATA_LENGTH), key)) {
                return getData(index, DATA_LENGTH, VALUE_INDEX);
            }
        }
        return NO_DATA;
    }

    @Override
    public boolean contains(Object key, int keyHash, int shift, int DATA_LENGTH) {
        int bitpos = bitpos(mask(keyHash, shift));
        if ((nodeMap & bitpos) != 0) {
            return nodeAt(bitpos).contains(key, keyHash, shift + BIT_PARTITION_SIZE, DATA_LENGTH);
        }
        if ((dataMap & bitpos) != 0) {
            int index = dataIndex(bitpos);
            return Objects.equals(getKey(index, DATA_LENGTH), key);
        }
        return false;
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
    public BitmapIndexedNode remove(@Nullable MutabilityOwnership mutator, Object key,
                                    int keyHash, int shift,
                                    ChangeEvent details, int DATA_LENGTH) {
        int mask = mask(keyHash, shift);
        int bitpos = bitpos(mask);

        if ((dataMap & bitpos) != 0) {
            return removeData(mutator, key, keyHash, shift, details, bitpos, DATA_LENGTH);
        } else if ((nodeMap & bitpos) != 0) {
            return removeSubNode(mutator, key, keyHash, shift, details, bitpos, DATA_LENGTH);
        }

        return this;
    }

    private BitmapIndexedNode removeData(@Nullable MutabilityOwnership mutator, Object key, int keyHash, int shift, ChangeEvent details, int bitpos, int DATA_LENGTH) {
        int dataIndex = dataIndex(bitpos);

        if (!Objects.equals(getKey(dataIndex, DATA_LENGTH), key)) {
            return this;
        }

        Object[] currentEntry = getData(dataIndex, DATA_LENGTH);
        details.setRemoved(currentEntry);

        if (dataArity(DATA_LENGTH) == 2 && !hasNodes()) {
            // Create new node with remaining entry. The new node will
            // a) either become the new root returned, or
            // b) unwrapped and inlined during returning.
            int newDataMap =
                    (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(keyHash, 0));

            Object[] nodes = getDataEntry(dataIndex ^ 1, DATA_LENGTH);
            return ChampTrie.newBitmapIndexedNode(mutator, 0, newDataMap, nodes, DATA_LENGTH);
        } else {
            // copy 'src' and remove entryLength element(s) at position 'idx'
            int idx = dataIndex * DATA_LENGTH;
            Object[] dst = ArrayHelper.copyComponentRemove(this.array, idx, DATA_LENGTH);
            return ChampTrie.newBitmapIndexedNode(mutator, nodeMap, dataMap ^ bitpos, dst, DATA_LENGTH);
        }
    }

    private BitmapIndexedNode removeSubNode(@Nullable MutabilityOwnership mutator, Object key, int keyHash, int shift,
                                            ChangeEvent details,
                                            int bitpos, int DATA_LENGTH) {
        Node subNode = nodeAt(bitpos);
        Node subNodeNew =
                subNode.remove(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details, DATA_LENGTH);

        if (subNode == subNodeNew) {
            return this;
        }

        if (!subNodeNew.hasNodes() && subNodeNew.hasDataArityOne()) {
            if (!hasData() && nodeArity() == 1) {
                // escalate (singleton or empty) result
                return (BitmapIndexedNode) subNodeNew;
            } else {
                // inline data entry (move to front)
                return copyAndMigrateFromNodeToData(mutator, bitpos, subNodeNew, DATA_LENGTH);
            }
        }
        return copyAndSetNode(mutator, bitpos, subNodeNew, DATA_LENGTH);
    }

    @Override
    public BitmapIndexedNode put(@Nullable MutabilityOwnership mutator,
                                 Object key, Object[] newData, int keyHash,
                                 int shift, ChangeEvent details,
                                 BiFunction<Object[], Object[], Object[]> updateFunction,
                                 ToIntFunction<Object> hashFunction,
                                 int DATA_LENGTH) {
        int mask = mask(keyHash, shift);
        int bitpos = bitpos(mask);
        if ((dataMap & bitpos) != 0) {
            int dataIndex = dataIndex(bitpos);
            Object currentKey = getKey(dataIndex, DATA_LENGTH);
            Object[] currentEntry = getData(dataIndex, DATA_LENGTH);
            if (Objects.equals(currentKey, key)) {
                Object[] updatedEntry = updateFunction.apply(currentEntry, newData);
                if (currentEntry == updatedEntry) {
                    details.setFound(currentEntry);
                    return this;
                }
                details.setReplaced(currentEntry, updatedEntry);
                return copyAndSetEntry(mutator, bitpos, updatedEntry, DATA_LENGTH);
            } else {
                Node updatedSubNode =
                        mergeTwoDataEntriesIntoNode(mutator,
                                currentEntry, hashFunction.applyAsInt(currentKey),
                                newData, keyHash, shift + BIT_PARTITION_SIZE,
                                DATA_LENGTH);

                details.setAdded(newData);
                return copyAndMigrateFromDataToNode(mutator, bitpos, updatedSubNode, DATA_LENGTH);
            }
        } else if ((nodeMap & bitpos) != 0) {
            Node subNode = nodeAt(bitpos);
            Node updatedSubNode =
                    subNode.put(mutator, key, newData, keyHash, shift + BIT_PARTITION_SIZE, details,
                            updateFunction, hashFunction, DATA_LENGTH);
            return subNode == updatedSubNode ? this : copyAndSetNode(mutator, bitpos, updatedSubNode, DATA_LENGTH);
        } else {
            details.setAdded(newData);
            return copyAndInsertData(mutator, bitpos, newData, DATA_LENGTH);
        }
    }

    protected int calculateSize(int DATA_LENGTH) {
        int size = dataArity(DATA_LENGTH);
        for (int i = 0, n = nodeArity(); i < n; i++) {
            Node node = getNode(i);
            size += node.calculateSize(DATA_LENGTH);
        }
        return size;
    }

    @Override
    public BitmapIndexedNode putAll(MutabilityOwnership owner, Node other, int shift,
                                    BulkChangeEvent bulkChange,
                                    ToIntFunction<Object> hashFunction,
                                    ChangeEvent details, int DATA_LENGTH) {
        var that = (BitmapIndexedNode) other;
        if (this == that) {
            bulkChange.inBoth += this.calculateSize(DATA_LENGTH);
            return this;
        }

        var newBitMap = nodeMap | dataMap | that.nodeMap | that.dataMap;
        var buffer = new Object[Integer.bitCount(nodeMap | that.nodeMap) +
                Integer.bitCount(dataMap | that.dataMap) * DATA_LENGTH];
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
                    System.arraycopy(that.array, that.dataArrayIndex(that.dataIndex(bitpos), DATA_LENGTH),
                            buffer, dataArrayIndex(index(newDataMap, bitpos), DATA_LENGTH), DATA_LENGTH);
                } else {
                    buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = that.getNode(that.nodeIndex(bitpos));
                }
            } else if (!(thatIsNode || thatIsData)) {
                // add 'mixed' (data or node) from this trie
                if (thisIsData) {
                    System.arraycopy(this.array, this.dataArrayIndex(dataIndex(bitpos), DATA_LENGTH),
                            buffer, dataArrayIndex(index(newDataMap, bitpos), DATA_LENGTH), DATA_LENGTH);
                } else {
                    buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = this.getNode(nodeIndex(bitpos));
                }
            } else if (thisIsNode && thatIsNode) {
                // add a new node that joins this node and that node
                Node thisNode = this.getNode(this.nodeIndex(bitpos));
                Node thatNode = that.getNode(that.nodeIndex(bitpos));
                buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] =
                        thisNode.putAll(owner, thatNode, shift + BIT_PARTITION_SIZE, bulkChange,
                                hashFunction, details, DATA_LENGTH);
            } else if (thisIsData && thatIsNode) {
                // add a new node that joins this data and that node
                Object[] thisEntry = this.getData(this.dataIndex(bitpos), DATA_LENGTH);
                Object thisEntryKey = thisEntry[0];
                Node thatNode = that.getNode(that.nodeIndex(bitpos));
                details.reset();
                buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = thatNode.put(null,
                        thisEntryKey, thisEntry, hashFunction.applyAsInt(thisEntryKey),
                        shift + BIT_PARTITION_SIZE, details,
                        (a, b) -> b,// our node must take precedence
                        hashFunction, DATA_LENGTH);
                if (details.isUnchanged()) {
                    bulkChange.inBoth++;
                } else if (details.isReplaced()) {
                    bulkChange.replaced = true;
                    bulkChange.inBoth++;
                }
                newDataMap ^= bitpos;
            } else if (thisIsNode) {
                // add a new node that joins this node and that data
                Object[] thatEntry = that.getData(that.dataIndex(bitpos), DATA_LENGTH);
                Object thatEntryKey = thatEntry[0];
                Node thisNode = this.getNode(this.nodeIndex(bitpos));
                details.reset();
                buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = thisNode.put(owner, thatEntryKey,
                        thatEntry, hashFunction.applyAsInt(thatEntryKey),
                        shift + BIT_PARTITION_SIZE, details,
                        (a, b) -> a,// our node must take precedence
                        hashFunction, DATA_LENGTH);
                if (!details.isModified()) {
                    bulkChange.inBoth++;
                }
                newDataMap ^= bitpos;
            } else {
                // add a new node that joins this data and that data
                int thisDataIndex = this.dataIndex(bitpos);
                Object thisEntryKey = this.getData(thisDataIndex, DATA_LENGTH, 0);
                int thatDataIndex = that.dataIndex(bitpos);
                Object thatEntryKey = that.getData(thatDataIndex, DATA_LENGTH, 0);
                if (Objects.equals(thisEntryKey, thatEntryKey)) {
                    bulkChange.inBoth++;
                    System.arraycopy(this.array, dataArrayIndex(thisDataIndex, DATA_LENGTH),
                            buffer, dataArrayIndex(index(newDataMap, bitpos), DATA_LENGTH), DATA_LENGTH);
                } else {
                    newDataMap ^= bitpos;
                    newNodeMap ^= bitpos;
                    Object[] thisEntry = this.getData(thisDataIndex, DATA_LENGTH);
                    Object[] thatEntry = that.getData(thatDataIndex, DATA_LENGTH);
                    buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = mergeTwoDataEntriesIntoNode(
                            owner, thisEntry, hashFunction.applyAsInt(thisEntryKey),
                            thatEntry, hashFunction.applyAsInt(thatEntryKey),
                            shift + BIT_PARTITION_SIZE, DATA_LENGTH);
                }
            }
        }
        /*
        int newDataCount = Integer.bitCount(newDataMap);
        int newNodeCount = Integer.bitCount(newNodeMap);
        if (buffer.length > newDataCount * DATA_LENGTH + newNodeCount) {
            Object[] tmp = buffer;
            buffer = new Object[newDataCount * DATA_LENGTH + newNodeCount];
            System.arraycopy(tmp, 0, buffer, 0, newDataCount * DATA_LENGTH);
            System.arraycopy(tmp, tmp.length - newNodeCount, buffer, newDataCount * DATA_LENGTH, newNodeCount);
        }
        return new BitmapIndexedNode(newNodeMap,newDataMap,buffer,  DATA_LENGTH);
        */
        return newCroppedBitmapIndexedNode(buffer, newDataMap, newNodeMap, DATA_LENGTH);
    }

    @Override
    public BitmapIndexedNode removeIf(@Nullable MutabilityOwnership owner, Predicate<Object> predicate, int shift, BulkChangeEvent bulkChange, int DATA_LENGTH) {
        var newBitMap = nodeMap | dataMap;
        var buffer = new Object[Integer.bitCount(newBitMap) * DATA_LENGTH];
        int newDataMap = this.dataMap;
        int newNodeMap = this.nodeMap;
        for (int mapToDo = newBitMap; mapToDo != 0; mapToDo ^= Integer.lowestOneBit(mapToDo)) {
            int mask = Integer.numberOfTrailingZeros(mapToDo);
            int bitpos = bitpos(mask);
            boolean thisIsNode = (this.nodeMap & bitpos) != 0;
            if (thisIsNode) {
                Node thisNode = this.getNode(this.nodeIndex(bitpos));
                Node result = thisNode.removeIf(owner, predicate, shift + BIT_PARTITION_SIZE, bulkChange, DATA_LENGTH);
                if (result.isNodeEmpty()) {
                    newNodeMap ^= bitpos;
                } else if (result.hasMany(DATA_LENGTH)) {
                    buffer[nodeArrayIndex(index(newNodeMap, bitpos), buffer)] = result;
                } else {
                    newNodeMap ^= bitpos;
                    newDataMap ^= bitpos;
                    System.arraycopy(result.array, 0, buffer, dataArrayIndex(index(newDataMap, bitpos), DATA_LENGTH), DATA_LENGTH);
                }
            } else {
                Object thisKey = this.getKey(this.dataIndex(bitpos), DATA_LENGTH);
                if (!predicate.test(thisKey)) {
                    System.arraycopy(array, dataArrayIndex(this.dataIndex(bitpos), DATA_LENGTH), buffer, dataArrayIndex(index(newDataMap, bitpos), DATA_LENGTH), DATA_LENGTH);
                } else {
                    newDataMap ^= bitpos;
                    bulkChange.removed++;
                }
            }
        }
        return newCroppedBitmapIndexedNode(buffer, newDataMap, newNodeMap, DATA_LENGTH);
    }

    private BitmapIndexedNode newCroppedBitmapIndexedNode(Object[] buffer, int newDataMap, int newNodeMap, int DATA_LENGTH) {
        int dataCount = Integer.bitCount(newDataMap);
        int nodeCount = Integer.bitCount(newNodeMap);
        int newLength = dataCount * DATA_LENGTH + nodeCount;
        if (newLength != buffer.length) {
            Object[] temp = buffer;
            buffer = new Object[newLength];
            System.arraycopy(temp, 0, buffer, 0, dataCount * DATA_LENGTH);
            System.arraycopy(temp, temp.length - nodeCount, buffer, dataCount * DATA_LENGTH, nodeCount);
        }
        return new BitmapIndexedNode(newNodeMap, newDataMap, buffer, DATA_LENGTH);
    }
}