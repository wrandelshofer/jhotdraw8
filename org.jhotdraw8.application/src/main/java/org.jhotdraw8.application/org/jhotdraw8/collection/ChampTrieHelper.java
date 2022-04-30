/*
 * @(#)TrieMapHelper.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * This is a package private class that provides the data structures for a
 * Compressed Hash-Array Mapped Prefix-tree (CHAMP).
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. BSD-2-Clause License</dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 */
class ChampTrieHelper {
    static final BitmapIndexedNode<?, ?> EMPTY_NODE = newBitmapIndexedNode(null, (0), (0), new Object[]{}, 1);
    public static final int TUPLE_VALUE = 1;

    /**
     * Don't let anyone instantiate this class.
     */
    private ChampTrieHelper() {
    }

    static final int HASH_CODE_LENGTH = 32;

    /**
     * Bit partition size in the range [1,5].
     * <p>
     * The bit-mask must fit into the 32 bits of an int field ({@code 32 = 1<<5}).
     * (You can use a size of 6, if you replace the bit-mask fields with longs).
     */
    static final int BIT_PARTITION_SIZE = 5;

    static final int BIT_PARTITION_MASK = (1 << BIT_PARTITION_SIZE) - 1;

    static abstract class Node<K, V> implements Serializable {
        private final static long serialVersionUID = 0L;

        Node() {

        }

        abstract void dumpAsGraphviz(Appendable a, final int shift, final int tupleLength, int keyHash, ToIntFunction<K> hashFunction) throws IOException;

        protected UniqueIdentity getMutator() {
            return null;
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

        boolean isAllowedToEdit(@Nullable UniqueIdentity y) {
            UniqueIdentity x = getMutator();
            return x != null && x == y;
        }

        static int mask(final int keyHash, final int shift) {
            return (keyHash >>> shift) & BIT_PARTITION_MASK;
        }

        static <K, V> Node<K, V> mergeTwoKeyValTuples(UniqueIdentity mutator,
                                                      final int index0,
                                                      final Object[] tuple0,
                                                      final K key0, final V val0,
                                                      final int keyHash0,
                                                      final int index1,
                                                      final Object[] tuple1,
                                                      final K key1, final V val1,
                                                      final int keyHash1,
                                                      final int shift,
                                                      final int tupleLength) {
            assert !(key0.equals(key1));

            if (shift >= HASH_CODE_LENGTH) {
                Object[] entries = new Object[tupleLength * 2];
                System.arraycopy(tuple0, index0, entries, 0, tupleLength);
                System.arraycopy(tuple1, index1, entries, tupleLength, tupleLength);
                return newHashCollisionNode(mutator, keyHash0, entries, tupleLength);
            }

            final int mask0 = mask(keyHash0, shift);
            final int mask1 = mask(keyHash1, shift);

            if (mask0 != mask1) {
                // both nodes fit on same level
                final int dataMap = bitpos(mask0) | bitpos(mask1);

                if (mask0 < mask1) {
                    Object[] entries = new Object[tupleLength * 2];
                    System.arraycopy(tuple0, index0, entries, 0, tupleLength);
                    System.arraycopy(tuple1, index1, entries, tupleLength, tupleLength);
                    return newBitmapIndexedNode(mutator, (0), dataMap, entries, tupleLength);
                } else {
                    Object[] entries = new Object[tupleLength * 2];
                    System.arraycopy(tuple1, index1, entries, 0, tupleLength);
                    System.arraycopy(tuple0, index0, entries, tupleLength, tupleLength);
                    return newBitmapIndexedNode(mutator, (0), dataMap, entries, tupleLength);
                }
            } else {
                final Node<K, V> node = mergeTwoKeyValTuples(mutator,
                        index0, tuple0, key0, val0, keyHash0,
                        index1, tuple1, key1, val1, keyHash1,
                        shift + BIT_PARTITION_SIZE,
                        tupleLength);
                // values fit on next level

                final int nodeMap = bitpos(mask0);
                return newBitmapIndexedNode(mutator, nodeMap, (0), new Object[]{node}, tupleLength);
            }
        }

        abstract @NonNull Node<K, V> copyAddAll(@NonNull Node<K, V> that, final int shift, BulkChangeEvent bulkChange, UniqueIdentity mutator, int tupleLength, ToIntFunction<K> hashFunction);

        abstract boolean equivalent(final @NonNull Object other, int tupleLength);

        abstract boolean containsKey(final K key, final int keyHash, final int shift, int tupleLength);

        abstract public SearchResult<V> findByKey(final K key, final int keyHash, final int shift, int tupleLength);

        abstract K getKey(final int index, int tupleLength);

        abstract Object[] getTuple(final int index, int tupleLength);

        Object[] updateTuple(K key, V value, Object[] oldTuple, int tupleLength) {
            Object[] newTuple = oldTuple.clone();
            newTuple[0] = key;
            newTuple[1] = value;
            return newTuple;
        }

        Object[] newTuple(K key, V value, Object[] oldTuple, int tupleLength, int tupleElementIndex) {
            Object[] newTuple = oldTuple.clone();
            newTuple[0] = key;
            if (tupleLength > 1) {
                newTuple[tupleElementIndex] = value;
            }
            return newTuple;
        }

        abstract Map.Entry<K, V> getKeyValueEntry(final int index, @NonNull BiFunction<K, V, Map.Entry<K, V>> factory, int tupleLength);

        abstract Node<K, V> getNode(final int index, int tupleLength);

        abstract V getValue(final int index, int tupleLength);

        abstract boolean hasNodes();

        abstract boolean hasPayload();

        abstract int nodeArity();

        /**
         * Returns the node index for the given keyHash and shift, or -1.
         *
         * @param keyHash the key hash
         * @param shift   the shift
         * @return the node index or -1
         */
        abstract int nodeIndex(final int keyHash, final int shift);

        abstract int payloadArity(int tupleLength);

        /**
         * Returns the payload index for the given keyHash and shift, or -1.
         *
         * @param key
         * @param keyHash the key hash
         * @param shift   the shift
         * @param tupleLength the tuple length
         * @return the payload index or -1
         */
        abstract int payloadIndex(@Nullable K key, final int keyHash, final int shift, final int tupleLength);

        abstract public Node<K, V> removed(final @Nullable UniqueIdentity mutator, final K key,
                                           final int keyHash, final int shift, final ChangeEvent<V> details, int tupleLength, ToIntFunction<K> hashFunction);

        abstract SizeClass sizePredicate(int tupleLength);

        abstract public Node<K, V> updated(final UniqueIdentity mutator, final K key, final V val,
                                           final int keyHash, final int shift, final ChangeEvent<V> details, int tupleLength, ToIntFunction<K> hashFunction, int tupleElementIndex);
    }

    static String toGraphvizNodeId(int keyHash, int shift) {
        if (shift == 0) {
            return "root";
        }
        String id = Integer.toBinaryString(keyHash);
        StringBuilder buf = new StringBuilder();
        for (int i = id.length(); i < shift; i++) buf.append('0');
        buf.append(id);
        return buf.toString();
    }

    static class BitmapIndexedNode<K, V> extends Node<K, V> {
        private final static long serialVersionUID = 0L;
        protected final Object[] nodes;
        private final int nodeMap;
        private final int dataMap;

        BitmapIndexedNode(final int nodeMap,
                          final int dataMap, final @NonNull Object[] nodes, int tupleLength) {
            this.nodeMap = nodeMap;
            this.dataMap = dataMap;
            this.nodes = nodes;
            assert nodes.length == nodeArity() + payloadArity(tupleLength) * tupleLength;
        }


        @Override
        public void dumpAsGraphviz(Appendable a, int shift, int tupleLength, int keyHash, ToIntFunction<K> hashFunction) throws IOException {
            // Print the node as a record
            String id = toGraphvizNodeId(keyHash, shift);
            a.append('n');
            a.append(id);
            a.append(" [label=\"");
            boolean first = true;

            for (int mask = 0; mask <= BIT_PARTITION_MASK; mask++) {
                int bitpos = bitpos(mask);
                if ((nodeMap & bitpos) != 0 || (dataMap & bitpos) != 0) {
                    if (first) {
                        first = false;
                    } else {
                        a.append('|');
                    }
                    a.append("<f");
                    a.append("" + mask);
                    a.append('>');
                    if ((dataMap & bitpos) != 0) {
                        a.append("" + getKey(index(dataMap, bitpos), tupleLength));
                    } else {
                        a.append(".");
                    }
                }
            }
            a.append("\"];\n");

            for (int mask = 0; mask <= BIT_PARTITION_MASK; mask++) {
                int bitpos = bitpos(mask);
                int subNodeKeyHash = (keyHash << BIT_PARTITION_SIZE) | mask;

                if ((nodeMap & bitpos) != 0) { // node (not value)
                    // Print the sub-node
                    final Node<K, V> subNode = nodeAt(bitpos, tupleLength);
                    subNode.dumpAsGraphviz(a, shift + BIT_PARTITION_SIZE, tupleLength, subNodeKeyHash, hashFunction);

                    // Print an arrow to the sub-node
                    a.append('n');
                    a.append(id);
                    a.append(":f");
                    a.append("" + mask);
                    a.append(" -> n");
                    a.append(toGraphvizNodeId(subNodeKeyHash, shift + BIT_PARTITION_SIZE));
                    a.append(" [label=\"");
                    a.append(toGraphvizNodeId(mask, BIT_PARTITION_SIZE));
                    a.append("\"];\n");
                }
            }
        }

        /**
         * Creates a copy of this trie with all elements of the specified
         * trie added to it.
         * <p>
         *
         * @param o            the trie to be added to this trie
         * @param shift        the shift for both tries
         * @param bulkChange   Reports data about the bulk change.
         * @param mutator      the mutator
         * @param tupleLength  the tuple length
         * @param hashFunction a function that computes a hash code for a key
         * @return a node that contains all the added key-value pairs
         */
        @Override
        @NonNull BitmapIndexedNode<K, V> copyAddAll(@NonNull Node<K, V> o, int shift, @NonNull BulkChangeEvent bulkChange, @Nullable UniqueIdentity mutator, int tupleLength, ToIntFunction<K> hashFunction) {
            // FIXME this method only works with tupleLength=1

            // Given the same bit-position in this and that:
            // case                   this.dataMap this.nodeMap that.dataMap  that.nodeMap
            // ---------------------------------------------------------------------------
            //.0    do nothing                -          -            -                -
            //.1    put "a" in dataMap        "a"        -            -                -
            //.2    put x in nodeMap          -          x            -                -
            // 3    illegal                   "a"        x            -                -
            //.4    put "b" in dataMap        -          -            "b"              -
            //.5.1  put "a" in dataMap        "a"        -            "a"              -   values are equal
            //.5.2  put {"a","b"} in nodeMap  "a"        -            "b"              -   values are not equal
            //.6    put x ∪ {"b"} in nodeMap  -          x            "b"              -
            // 7    illegal                   "a"        x            "b"              -
            //.8    put y in nodeMap          -          -            -                y
            //.9    put {"a"} ∪ y in nodeMap  "a"        -            -                y
            //.10.1 put x in nodeMap          -          x            -                x   nodes are equivalent
            //.10.2 put x ∪ y in nodeMap      -          x            -                y   nodes are not equivalent
            // 11   illegal                   "a"        x            -                y
            // 12   illegal                   -          -            "b"              y
            // 13   illegal                   "a"        -            "b"              y
            // 14   illegal                   -          x            "b"              y
            // 15   illegal                   "a"        x            "b"              y

            if (o == this) {
                return this;
            }
            BitmapIndexedNode<K, V> that = (BitmapIndexedNode<K, V>) o;

            int newNodeLength = Integer.bitCount(this.nodeMap | this.dataMap | that.nodeMap | that.dataMap);
            Object[] nodesNew = new Object[newNodeLength];
            int nodeMapNew = this.nodeMap | that.nodeMap;
            int dataMapNew = this.dataMap | that.dataMap;
            int thisNodeMapToDo = this.nodeMap;
            int thatNodeMapToDo = that.nodeMap;

            // case 0:
            // we will not have to do any changes
            ChangeEvent<V> changeEvent = new ChangeEvent<>();
            boolean changed = false;


            // Step 1: Merge that.dataMap and this.dataMap into dataMapNew.
            //         We may have to merge data nodes into sub-nodes.
            // -------
            // iterate over all bit-positions in dataMapNew which have a non-zero bit
            int dataIndex = 0;
            for (int mapToDo = dataMapNew; mapToDo != 0; mapToDo ^= Integer.lowestOneBit(mapToDo)) {
                int mask = Integer.numberOfTrailingZeros(mapToDo);
                int bitpos = bitpos(mask);
                boolean thisHasData = (this.dataMap & bitpos) != 0;
                boolean thatHasData = (that.dataMap & bitpos) != 0;
                if (thisHasData && thatHasData) {
                    K thisKey = this.getKey(index(this.dataMap, bitpos), tupleLength);
                    K thatKey = that.getKey(index(that.dataMap, bitpos), tupleLength);
                    if (Objects.equals(thisKey, thatKey)) {
                        // case 5.1:
                        nodesNew[dataIndex++] = thisKey;
                        bulkChange.numInBothCollections++;
                    } else {
                        // case 5.2:
                        dataMapNew ^= bitpos;
                        nodeMapNew |= bitpos;
                        int thatKeyHash = hashFunction.applyAsInt(thatKey);
                        Node<K, V> subNodeNew = mergeTwoKeyValPairs(mutator, thisKey, hashFunction.applyAsInt(thisKey), thatKey, thatKeyHash, shift + BIT_PARTITION_SIZE, tupleLength);
                        nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = subNodeNew;
                        changed = true;
                    }
                } else if (thisHasData) {
                    K thisKey = this.getKey(index(this.dataMap, bitpos), tupleLength);
                    boolean thatHasNode = (that.nodeMap & bitpos) != 0;
                    if (thatHasNode) {
                        // case 9:
                        dataMapNew ^= bitpos;
                        thatNodeMapToDo ^= bitpos;
                        int thisKeyHash = hashFunction.applyAsInt(thisKey);
                        changeEvent.isModified = false;
                        Node<K, V> subNode = that.nodeAt(bitpos, tupleLength);
                        Node<K, V> subNodeNew = subNode.updated(mutator, thisKey, null, thisKeyHash, shift + BIT_PARTITION_SIZE, changeEvent, tupleLength, hashFunction, 1);
                        nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = subNodeNew;
                        changed = true;
                    } else {
                        // case 1:
                        nodesNew[dataIndex++] = thisKey;
                    }
                } else {
                    assert thatHasData;
                    K thatKey = that.getKey(index(that.dataMap, bitpos), tupleLength);
                    int thatKeyHash = hashFunction.applyAsInt(thatKey);
                    boolean thisHasNode = (this.nodeMap & bitpos) != 0;
                    if (thisHasNode) {
                        // case 6:
                        dataMapNew ^= bitpos;
                        thisNodeMapToDo ^= bitpos;
                        changeEvent.isModified = false;
                        Node<K, V> subNode = this.getNode(index(this.nodeMap, bitpos), tupleLength);
                        Node<K, V> subNodeNew = subNode.updated(mutator, thatKey, null, thatKeyHash, shift + BIT_PARTITION_SIZE, changeEvent, tupleLength, hashFunction, 1);
                        if (changeEvent.isModified) {
                            changed = true;
                            nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = subNodeNew;
                        }
                    } else {
                        // case 4:
                        changed = true;
                        nodesNew[dataIndex++] = thatKey;
                    }
                }
            }

            // Step 2: Merge remaining sub-nodes
            // -------
            int nodeMapToDo = thisNodeMapToDo | thatNodeMapToDo;
            for (int mapToDo = nodeMapToDo; mapToDo != 0; mapToDo ^= Integer.lowestOneBit(mapToDo)) {
                int mask = Integer.numberOfTrailingZeros(mapToDo);
                int bitpos = bitpos(mask);
                boolean thisHasNodeToDo = (thisNodeMapToDo & bitpos) != 0;
                boolean thatHasNodeToDo = (thatNodeMapToDo & bitpos) != 0;
                if (thisHasNodeToDo && thatHasNodeToDo) {
                    //cases 10.1 and 10.2
                    Node<K, V> thisSubNode = this.getNode(index(this.nodeMap, bitpos), tupleLength);
                    Node<K, V> thatSubNode = that.getNode(index(that.nodeMap, bitpos), tupleLength);
                    Node<K, V> subNodeNew = thisSubNode.copyAddAll(thatSubNode, shift + BIT_PARTITION_SIZE, bulkChange, mutator, tupleLength, hashFunction);
                    changed |= subNodeNew != thisSubNode;
                    nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = subNodeNew;

                } else if (thatHasNodeToDo) {
                    // case 8
                    Node<K, V> thatSubNode = that.getNode(index(that.nodeMap, bitpos), tupleLength);
                    nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = thatSubNode;
                    changed = true;
                } else {
                    // case 2
                    assert thisHasNodeToDo;
                    Node<K, V> thisSubNode = this.getNode(index(this.nodeMap, bitpos), tupleLength);
                    nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = thisSubNode;
                }
            }

            // Step 3: create new node if it has changed
            // ------
            if (changed) {
                return newBitmapIndexedNode(mutator, nodeMapNew, dataMapNew, nodesNew, tupleLength);
            }

            return this;
        }

        private Node<K, V> mergeTwoKeyValPairs(UniqueIdentity mutator,
                                               final K key0, final int keyHash0,
                                               final K key1, final int keyHash1,
                                               final int shift, int tupleLength) {
            // FIXME This method only works with tupleLength=1

            assert !(key0.equals(key1));

            if (shift >= HASH_CODE_LENGTH) {
                @SuppressWarnings("unchecked")
                HashCollisionNode<K, V> unchecked = newHashCollisionNode(mutator, keyHash0, (K[]) new Object[]{key0, key1}, tupleLength);
                return unchecked;
            }

            final int mask0 = mask(keyHash0, shift);
            final int mask1 = mask(keyHash1, shift);

            if (mask0 != mask1) {
                // both nodes fit on same level
                final int dataMap = bitpos(mask0) | bitpos(mask1);
                if (mask0 < mask1) {
                    return newBitmapIndexedNode(mutator, 0, dataMap, new Object[]{key0, key1}, tupleLength);
                } else {
                    return newBitmapIndexedNode(mutator, 0, dataMap, new Object[]{key1, key0}, tupleLength);
                }
            } else {
                final Node<K, V> node = mergeTwoKeyValPairs(mutator, key0, keyHash0, key1, keyHash1, shift + BIT_PARTITION_SIZE, tupleLength);
                // values fit on next level
                final int nodeMap = bitpos(mask0);
                return newBitmapIndexedNode(mutator, nodeMap, 0, new Object[]{node}, tupleLength);
            }
        }

        BitmapIndexedNode<K, V> copyAndInsertValue(final UniqueIdentity mutator, final int bitpos,
                                                   final K key, final V val, int tupleLength) {
            final int idx = tupleLength * dataIndex(bitpos);

            // copy 'src' and insert 2 element(s) at position 'idx'
            final Object[] dst = ArrayHelper.copyComponentAdd(this.nodes, idx, tupleLength);
            dst[idx] = key;
            if (tupleLength > 1) {
                dst[idx + 1] = val;
            }
            return newBitmapIndexedNode(mutator, nodeMap(), dataMap() | bitpos, dst, tupleLength);
        }

        BitmapIndexedNode<K, V> copyAndMigrateFromInlineToNode(final UniqueIdentity mutator,
                                                               final int bitpos, final Node<K, V> node, int tupleLength) {

            final int idxOld = tupleLength * dataIndex(bitpos);
            final int idxNew = this.nodes.length - tupleLength - nodeIndex(bitpos);
            assert idxOld <= idxNew;

            // copy 'src' and remove tupleLength element(s) at position 'idxOld' and
            // insert 1 element(s) at position 'idxNew'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - tupleLength + 1];
            System.arraycopy(src, 0, dst, 0, idxOld);
            System.arraycopy(src, idxOld + tupleLength, dst, idxOld, idxNew - idxOld);
            System.arraycopy(src, idxNew + tupleLength, dst, idxNew + 1, src.length - idxNew - tupleLength);
            dst[idxNew] = node;

            return newBitmapIndexedNode(mutator, nodeMap() | bitpos, dataMap() ^ bitpos, dst, tupleLength);
        }

        BitmapIndexedNode<K, V> copyAndMigrateFromNodeToInline(final UniqueIdentity mutator,
                                                               final int bitpos, final Node<K, V> node, int tupleLength) {

            final int idxOld = this.nodes.length - 1 - nodeIndex(bitpos);
            final int idxNew = tupleLength * dataIndex(bitpos);

            // copy 'src' and remove 1 element(s) at position 'idxOld' and
            // insert tupleLength element(s) at position 'idxNew'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1 + tupleLength];
            assert idxOld >= idxNew;
            System.arraycopy(src, 0, dst, 0, idxNew);
            System.arraycopy(src, idxNew, dst, idxNew + tupleLength, idxOld - idxNew);
            System.arraycopy(src, idxOld + 1, dst, idxOld + tupleLength, src.length - idxOld - 1);
            Object[] tuple = node.getTuple(0, tupleLength);
            System.arraycopy(tuple, 0, dst, idxNew, tupleLength);

            return newBitmapIndexedNode(mutator, nodeMap() ^ bitpos, dataMap() | bitpos, dst, tupleLength);
        }

        BitmapIndexedNode<K, V> copyAndRemoveValue(final UniqueIdentity mutator,
                                                   final int bitpos, int tupleLength) {
            final int idx = tupleLength * dataIndex(bitpos);

            // copy 'src' and remove tupleLength element(s) at position 'idx'
            final Object[] dst = ArrayHelper.copyComponentRemove(this.nodes, idx, tupleLength);
            return newBitmapIndexedNode(mutator, nodeMap(), dataMap() ^ bitpos, dst, tupleLength);
        }

        BitmapIndexedNode<K, V> copyAndSetNode(final UniqueIdentity mutator, final int bitpos,
                                               final Node<K, V> node, int tupleLength) {

            final int idx = this.nodes.length - 1 - nodeIndex(bitpos);

            if (isAllowedToEdit(mutator)) {
                // no copying if already editable
                this.nodes[idx] = node;
                return this;
            } else {
                // copy 'src' and set 1 element(s) at position 'idx'
                final Object[] dst = ArrayHelper.copySet(this.nodes, idx, node);
                return newBitmapIndexedNode(mutator, nodeMap(), dataMap(), dst, tupleLength);
            }
        }

        BitmapIndexedNode<K, V> copyAndSetValue(final UniqueIdentity mutator, final int bitpos,
                                                final V val, int tupleLength, int tupleElementIndex) {
            if (tupleLength < 2) {
                return this;
            }

            final int idx = tupleLength * dataIndex(bitpos) + tupleElementIndex;

            if (isAllowedToEdit(mutator)) {
                // no copying if already editable
                this.nodes[idx] = val;
                return this;
            } else {
                // copy 'src' and set 1 element(s) at position 'idx'
                final Object[] dst = ArrayHelper.copySet(this.nodes, idx, val);
                return newBitmapIndexedNode(mutator, nodeMap(), dataMap(), dst, tupleLength);
            }
        }

        int dataIndex(final int bitpos) {
            return Integer.bitCount(dataMap() & (bitpos - 1));
        }

        public int dataMap() {
            return dataMap;
        }

        @Override
        public boolean equivalent(final @NonNull Object other, int tupleLength) {
            if (this == other) {
                return true;
            }
            BitmapIndexedNode<?, ?> that = (BitmapIndexedNode<?, ?>) other;

            // nodes array: we compare local payload from 0 to splitAt (excluded)
            // and then we compare the nested nodes from splitAt to length (excluded)
            int splitAt = tupleLength * payloadArity(tupleLength);
            return nodeMap() == that.nodeMap()
                    && dataMap() == that.dataMap()
                    && ArrayHelper.equals(nodes, 0, splitAt, that.nodes, 0, splitAt)
                    && ArrayHelper.equals(nodes, splitAt, nodes.length, that.nodes, splitAt, that.nodes.length,
                    (a, b) -> ((Node<?, ?>) a).equivalent(b, tupleLength));
        }

        @Override
        boolean containsKey(final K key, final int keyHash, final int shift, int tupleLength) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((this.dataMap & bitpos) != 0) {
                final int index = index(this.dataMap, bitpos);
                return Objects.equals(getKey(index, tupleLength), key);
            }

            final int nodeMap = nodeMap();
            if ((nodeMap & bitpos) != 0) {
                final int index = index(nodeMap, bitpos);
                return getNode(index, tupleLength).containsKey(key, keyHash, shift + BIT_PARTITION_SIZE, tupleLength);
            }

            return false;
        }

        @Override
        public SearchResult<V> findByKey(final K key, final int keyHash, final int shift, int tupleLength) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap() & bitpos) != 0) { // inplace value
                final int index = dataIndex(bitpos);
                if (Objects.equals(getKey(index, tupleLength), key)) {
                    final Object[] tuple = getTuple(index, tupleLength);
                    return new SearchResult<>(tuple, true);
                }

                return new SearchResult<>(null, false);
            }

            if ((nodeMap() & bitpos) != 0) { // node (not value)
                final Node<K, V> subNode = nodeAt(bitpos, tupleLength);

                return subNode.findByKey(key, keyHash, shift + BIT_PARTITION_SIZE, tupleLength);
            }

            return new SearchResult<>(null, false);
        }

        @Override
        @SuppressWarnings("unchecked")
        K getKey(final int index, int tupleLength) {
            return (K) nodes[tupleLength * index];
        }

        @Override
        Object[] getTuple(int index, int tupleLength) {
            Object[] tuple = new Object[tupleLength];
            System.arraycopy(nodes, tupleLength * index, tuple, 0, tupleLength);
            return tuple;
        }

        @Override
        @SuppressWarnings("unchecked")
        Map.Entry<K, V> getKeyValueEntry(final int index, BiFunction<K, V, Map.Entry<K, V>> factory, int tupleLength) {
            return factory.apply((K) nodes[tupleLength * index], tupleLength > 1 ? (V) nodes[tupleLength * index + 1] : null);
        }

        @Override
        @SuppressWarnings("unchecked")
        Node<K, V> getNode(final int index, int tupleLength) {
            return (Node<K, V>) nodes[nodes.length - 1 - index];
        }

        @Override
        @SuppressWarnings("unchecked")
        V getValue(final int index, int tupleLength) {
            return tupleLength > 1 ? (V) nodes[tupleLength * index + 1] : null;
        }

        @Override
        boolean hasNodes() {
            return nodeMap() != 0;
        }

        @Override
        boolean hasPayload() {
            return dataMap() != 0;
        }

        @Override
        int nodeArity() {
            return Integer.bitCount(nodeMap());
        }

        Node<K, V> nodeAt(final int bitpos, int tupleLength) {
            return getNode(nodeIndex(bitpos), tupleLength);
        }

        int nodeIndex(final int bitpos) {
            return Integer.bitCount(nodeMap() & (bitpos - 1));
        }

        @Override
        int nodeIndex(int keyHash, int shift) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);
            return (this.nodeMap & bitpos) != 0
                    ? index(this.nodeMap, bitpos)
                    : -1;
        }

        private int nodeIndexAt(Object[] array, int nodeMap, final int bitpos) {
            return array.length - 1 - Integer.bitCount(nodeMap & (bitpos - 1));
        }

        public int nodeMap() {
            return nodeMap;
        }

        @Override
        int payloadArity(int tupleLength) {
            return Integer.bitCount(dataMap());
        }

        @Override
        int payloadIndex(K key, final int keyHash, final int shift, int tupleLength) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);
            return (this.dataMap & bitpos) != 0
                    ? index(this.dataMap, bitpos)
                    : -1;
        }

        @Override
        public BitmapIndexedNode<K, V> removed(final @Nullable UniqueIdentity mutator, final K key,
                                               final int keyHash, final int shift, final ChangeEvent<V> details, int tupleLength, ToIntFunction<K> hashFunction) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap() & bitpos) != 0) { // inplace value
                final int dataIndex = dataIndex(bitpos);

                if (Objects.equals(getKey(dataIndex, tupleLength), key)) {
                    final Object[] currentVal = getTuple(dataIndex, tupleLength);
                    details.updated(currentVal);

                    if (this.payloadArity(tupleLength) == 2 && this.nodeArity() == 0) {
                        // Create new node with remaining pair. The new node will a) either become the new root
                        // returned, or b) unwrapped and inlined during returning.
                        final int newDataMap =
                                (shift == 0) ? (dataMap() ^ bitpos) : bitpos(mask(keyHash, 0));

                        if (dataIndex == 0) {
                            Object[] nodes = getTuple(1, tupleLength);
                            return newBitmapIndexedNode(mutator, (0), newDataMap, nodes, tupleLength);
                        } else {
                            Object[] nodes = getTuple(0, tupleLength);
                            return newBitmapIndexedNode(mutator, (0), newDataMap, nodes, tupleLength);
                        }
                    } else {
                        return copyAndRemoveValue(mutator, bitpos, tupleLength);
                    }
                } else {
                    return this;
                }
            } else if ((nodeMap() & bitpos) != 0) { // node (not value)
                final Node<K, V> subNode = nodeAt(bitpos, tupleLength);
                final Node<K, V> subNodeNew =
                        subNode.removed(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details, tupleLength, hashFunction);

                if (!details.isModified()) {
                    return this;
                }

                switch (subNodeNew.sizePredicate(tupleLength)) {
                case SIZE_EMPTY: {
                    throw new IllegalStateException("Sub-node must have at least one element.");
                }
                case SIZE_ONE: {
                    if (this.payloadArity(tupleLength) == 0 && this.nodeArity() == 1) {
                        // escalate (singleton or empty) result
                        return (BitmapIndexedNode<K, V>) subNodeNew;
                    } else {
                        // inline value (move to front)
                        return copyAndMigrateFromNodeToInline(mutator, bitpos, subNodeNew, tupleLength);
                    }
                }
                default: {
                    // modify current node (set replacement node)
                    return copyAndSetNode(mutator, bitpos, subNodeNew, tupleLength);
                }
                }
            }

            return this;
        }

        @Override
        public SizeClass sizePredicate(int tupleLength) {
            if (this.nodeArity() == 0) {
                switch (this.payloadArity(tupleLength)) {
                case 0:
                    return SizeClass.SIZE_EMPTY;
                case 1:
                    return SizeClass.SIZE_ONE;
                default:
                    return SizeClass.SIZE_MORE_THAN_ONE;
                }
            } else {
                return SizeClass.SIZE_MORE_THAN_ONE;
            }
        }

        @Override
        public BitmapIndexedNode<K, V> updated(final UniqueIdentity mutator,
                                               final K key, final V val,
                                               final int keyHash, final int shift,
                                               final ChangeEvent<V> details, int tupleLength,
                                               ToIntFunction<K> hashFunction, int tupleElementIndex) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap() & bitpos) != 0) { // inplace value
                final int dataIndex = dataIndex(bitpos);
                final K currentKey = getKey(dataIndex, tupleLength);
                final V currentVal = getValue(dataIndex, tupleLength);
                Object[] currentTuple = getTuple(dataIndex, tupleLength);
                Object[] newTuple = newTuple(key, val, currentTuple, tupleLength, tupleElementIndex);
                if (Objects.equals(currentKey, key)) {
                    if (tupleElementIndex == TUPLE_VALUE && Objects.equals(currentVal, val)) {
                        details.found(currentTuple);
                        return this;
                    }
                    // update mapping
                    details.updated(currentTuple);
                    return copyAndSetValue(mutator, bitpos, val, tupleLength, tupleElementIndex);
                } else {
                    final Node<K, V> subNodeNew =
                            mergeTwoKeyValTuples(mutator,
                                    0, currentTuple, currentKey, currentVal, hashFunction.applyAsInt(currentKey),
                                    0, newTuple, key, val, keyHash, shift + BIT_PARTITION_SIZE,
                                    tupleLength);

                    details.modified();
                    return copyAndMigrateFromInlineToNode(mutator, bitpos, subNodeNew, tupleLength);
                }
            } else if ((nodeMap() & bitpos) != 0) { // node (not value)
                final Node<K, V> subNode = nodeAt(bitpos, tupleLength);
                final Node<K, V> subNodeNew =
                        subNode.updated(mutator, key, val, keyHash, shift + BIT_PARTITION_SIZE, details, tupleLength, hashFunction, tupleElementIndex);

                if (details.isModified()) {
                    return copyAndSetNode(mutator, bitpos, subNodeNew, tupleLength);
                } else {
                    return this;
                }
            } else {
                // no value
                details.modified();
                return copyAndInsertValue(mutator, bitpos, key, val, tupleLength);
            }
        }

    }

    private static class HashCollisionNode<K, V> extends Node<K, V> {
        private final static long serialVersionUID = 0L;
        /**
         * The common hash code of all entries in this node.
         */
        private final int hash;
        private @NonNull Object[] nodes;

        HashCollisionNode(final int hash, final Object[] nodes, int tupleLength) {
            this.nodes = nodes;
            this.hash = hash;

            assert payloadArity(tupleLength) >= 2;
        }

        @Override
        int nodeIndex(int keyHash, int shift) {
            return -1;
        }

        @Override
        public void dumpAsGraphviz(Appendable a, int shift, int tupleLength, int keyHash, ToIntFunction<K> hashFunction) throws IOException {
            // Print the node as a record
            a.append(toGraphvizNodeId(keyHash, shift));
            a.append(" [label=\"");
            boolean first = true;

            for (int i = 0, n = payloadArity(tupleLength); i < n; i++) {
                if (first) {
                    first = false;
                } else {
                    a.append('|');
                }
                a.append("<f");
                a.append("" + i);
                a.append('>');
                a.append("" + getKey(i, tupleLength));
            }
            a.append("\"];\n");
        }

        @Override
        @NonNull Node<K, V> copyAddAll(@NonNull Node<K, V> o, int shift, BulkChangeEvent bulkChange, UniqueIdentity mutator, int tupleLength, ToIntFunction<K> hashFunction) {
            if (o == this) {
                return this;
            }
            // The other node must be a HashCollisionNode
            HashCollisionNode<K, V> that = (HashCollisionNode<K, V>) o;

            List<Object> list = new ArrayList<>(this.nodes.length + that.nodes.length);

            // Step 1: Add all this.keys to list
            list.addAll(Arrays.asList(this.nodes));

            // Step 2: Add all that.keys to list which are not in this.keys
            //         This is quadratic.
            //         If the sets are disjoint, we can do nothing about it.
            //         If the sets intersect, we can mark those which are
            //         equal in a bitset, so that we do not need to check
            //         them over and over again.
            BitSet bs = new BitSet(this.nodes.length);
            outer:
            for (int j = 0; j < that.nodes.length; j += tupleLength) {
                @SuppressWarnings("unchecked")
                K key = (K) that.nodes[j];
                for (int i = bs.nextClearBit(0); i >= 0 && i < this.nodes.length; i = bs.nextClearBit(i + 1)) {
                    if (Objects.equals(key, this.nodes[i])) {
                        bs.set(i);
                        bulkChange.numInBothCollections++;
                        continue outer;
                    }
                }
                list.add(key);
            }

            if (list.size() > this.nodes.length) {
                @SuppressWarnings("unchecked")
                HashCollisionNode<K, V> unchecked = newHashCollisionNode(mutator, hash, (K[]) list.toArray(), tupleLength);
                return unchecked;
            }

            return this;
        }

        @Override
        public boolean equivalent(@NonNull Object other, int tupleLength) {
            if (this == other) {
                return true;
            }
            HashCollisionNode<?, ?> that = (HashCollisionNode<?, ?>) other;
            if (hash != that.hash
                    || payloadArity(tupleLength) != that.payloadArity(tupleLength)) {
                return false;
            }

            // Linear scan for each key, because of arbitrary element order.
            outerLoop:
            for (int i = 0, n = that.payloadArity(tupleLength); i < n; i++) {
                final Object otherKey = that.getKey(i, tupleLength);
                final Object otherVal = that.getValue(i, tupleLength);

                for (int j = 0, m = payloadArity(tupleLength); j < m; j++) {
                    final K key = getKey(j, tupleLength);
                    final V val = getValue(j, tupleLength);

                    if (Objects.equals(key, otherKey) && Objects.equals(val, otherVal)) {
                        continue outerLoop;
                    }
                }
                return false;
            }

            return true;
        }

        @Override
        boolean containsKey(final K key, final int keyHash, final int shift, int tupleLength) {
            for (int i = 0, n = payloadArity(tupleLength); i < n; i++) {
                final K _key = getKey(i, tupleLength);
                if (Objects.equals(key, _key)) {
                    return true;
                }
            }
            return false;
        }


        @Override
        public SearchResult<V> findByKey(final K key, final int keyHash, final int shift, int tupleLength) {
            for (int i = 0, n = payloadArity(tupleLength); i < n; i++) {
                final K _key = getKey(i, tupleLength);
                if (Objects.equals(key, _key)) {
                    final Object[] tuple = getTuple(i, tupleLength);
                    return new SearchResult<>(tuple, true);
                }
            }
            return new SearchResult<>(null, false);
        }

        @Override
        @SuppressWarnings("unchecked")
        K getKey(final int index, int tupleLength) {
            return (K) nodes[index * tupleLength];
        }

        @Override
        Object[] getTuple(int index, int tupleLength) {
            Object[] tuple = new Object[tupleLength];
            System.arraycopy(nodes, tupleLength * index, tuple, 0, tupleLength);
            return tuple;
        }

        @SuppressWarnings("unchecked")
        @Override
        Map.Entry<K, V> getKeyValueEntry(final int index, BiFunction<K, V, Map.Entry<K, V>> factory, int tupleLength) {
            return factory.apply((K) nodes[index * tupleLength], tupleLength > 1 ? (V) nodes[index * tupleLength + 1] : null);
        }

        @Override
        public Node<K, V> getNode(int index, int tupleLength) {
            throw new IllegalStateException("Is leaf node.");
        }

        @SuppressWarnings("unchecked")
        @Override
        V getValue(final int index, int tupleLength) {
            return tupleLength > 1 ? (V) nodes[index * tupleLength + 1]:null;
        }

        @Override
        boolean hasNodes() {
            return false;
        }

        @Override
        boolean hasPayload() {
            return true;
        }

        @Override
        int nodeArity() {
            return 0;
        }

        @Override
        int payloadArity(int tupleLength) {
            return nodes.length /tupleLength;
        }

        @Override
        int payloadIndex(K key, final int keyHash, final int shift, int tupleLength) {
            if (this.hash != keyHash) {
                return -1;
            }
            for (int i = 0, n = payloadArity(tupleLength); i < n; i++) {
                K k = getKey(i, tupleLength);
                if (Objects.equals(k, key)) {
                    return i;
                }
            }
            return -1;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Node<K, V> removed(final @Nullable UniqueIdentity mutator, final K key,
                                  final int keyHash, final int shift, final ChangeEvent<V> details, int tupleLength, ToIntFunction<K> hashFunction) {
            for (int idx = 0, n = payloadArity(tupleLength); idx < n; idx++) {
                if (Objects.equals(getKey(idx, tupleLength), key)) {
                    final Object[] currentVal = getTuple(idx, tupleLength);
                    details.updated(currentVal);

                    int payloadArity = payloadArity(tupleLength);
                    if (payloadArity == 1) {
                        return emptyNode();
                    } else if (payloadArity == 2) {
                        // Create root node with singleton element.
                        // This node will be a) either be the new root
                        // returned, or b) unwrapped and inlined.
                        final K theOtherKey = (K) ((idx == 0) ? nodes[tupleLength] : nodes[0]);
                        final V theOtherVal = tupleLength > 1 ? (V) ((idx == 0) ? nodes[tupleLength + 1] : nodes[1]) : null;
                        //final Object[] theOtherTuple=getTuple(idx,tupleLength);
                        //TODO set all tuple values on updated!
                        return ChampTrieHelper.<K, V>emptyNode().updated(mutator, theOtherKey, theOtherVal,
                                keyHash, 0, details, tupleLength, hashFunction, 1);
                    } else {

                        // copy keys and vals and remove tupleLength elements at position idx
                        final Object[] entriesNew = ArrayHelper.copyComponentRemove(this.nodes, idx * tupleLength, tupleLength);

                        if (isAllowedToEdit(mutator)) {
                            this.nodes = entriesNew;
                            return this;
                        }
                        return newHashCollisionNode(mutator, keyHash, entriesNew, tupleLength);
                    }
                }
            }
            return this;
        }

        @Override
        public SizeClass sizePredicate(int tupleLength) {
            return SizeClass.SIZE_MORE_THAN_ONE;
        }

        @Override
        public Node<K, V> updated(final UniqueIdentity mutator, final K key, final V val,
                                  final int keyHash, final int shift, final ChangeEvent<V> details, int tupleLength, ToIntFunction<K> hashFunction, int tupleElementIndex) {
            assert this.hash == keyHash;

            for (int idx = 0, n = payloadArity(tupleLength); idx < n; idx++) {
                if (Objects.equals(getKey(idx, tupleLength), key)) {
                    final V currentVal = getValue(idx, tupleLength);
                    if (Objects.equals(currentVal, val)) {
                        details.found(getTuple(idx, tupleLength));
                        return this;
                    } else {
                        final Object[] dst = ArrayHelper.copySet(this.nodes, idx * tupleLength + 1, val);
                        final Node<K, V> thisNew = newHashCollisionNode(mutator, this.hash, dst, tupleLength);
                        details.updated(getTuple(idx, tupleLength));
                        return thisNew;
                    }
                }
            }

            // copy keys and vals and add 1 element at the end
            final Object[] entriesNew = ArrayHelper.copyComponentAdd(this.nodes, this.nodes.length, tupleLength);
            entriesNew[this.nodes.length] = key;
            if (tupleLength > 1) {
                entriesNew[this.nodes.length + 1] = val;
            }
            details.modified();
            if (isAllowedToEdit(mutator)) {
                this.nodes = entriesNew;
                return this;
            } else {
                return newHashCollisionNode(mutator, keyHash, entriesNew, tupleLength);
            }
        }
    }

    /**
     * Iterator skeleton that uses a fixed stack in depth.
     * <p>
     * Iterates first over inlined values and then continues depth first.
     */
    static abstract class AbstractTrieIterator<K, V> {

        private static final int MAX_DEPTH = (HASH_CODE_LENGTH + BIT_PARTITION_SIZE - 1) / BIT_PARTITION_SIZE + 1;
        private final int[] nodeCursorsAndLengths = new int[MAX_DEPTH * 2];
        protected int nextValueCursor;
        protected int nextValueLength;
        protected Node<K, V> nextValueNode;
        private int nextStackLevel = -1;
        protected Map.Entry<K, V> current;
        boolean canRemove = false;

        @SuppressWarnings({"unchecked"})
        Node<K, V>[] nodes = new Node[MAX_DEPTH];
        protected final int tupleLength;
        protected final ToIntFunction<K> hashFunction;

        AbstractTrieIterator(Node<K, V> rootNode, int tupleLength, ToIntFunction<K> hashFunction) {
            this.tupleLength = tupleLength;
            this.hashFunction = hashFunction;
            if (rootNode.hasNodes()) {
                nextStackLevel = 0;

                nodes[0] = rootNode;
                nodeCursorsAndLengths[0] = 0;
                nodeCursorsAndLengths[1] = rootNode.nodeArity();
            }

            if (rootNode.hasPayload()) {
                nextValueNode = rootNode;
                nextValueCursor = 0;
                nextValueLength = rootNode.payloadArity(tupleLength);
            }
        }

        public boolean hasNext() {
            if (nextValueCursor < nextValueLength) {
                return true;
            } else {
                return searchNextValueNode(tupleLength);
            }
        }

        protected Map.Entry<K, V> nextEntry(@NonNull BiFunction<K, V, Map.Entry<K, V>> factory) {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else {
                canRemove = true;
                return current = nextValueNode.getKeyValueEntry(nextValueCursor++, factory, tupleLength);
            }
        }

        protected void removeEntry(Function<K, Node<K, V>> removeFunction) {
            if (!canRemove) {
                throw new IllegalStateException();
            }

            Map.Entry<K, V> toRemove = current;
            if (hasNext()) {
                Map.Entry<K, V> next = nextEntry(AbstractMap.SimpleImmutableEntry::new);
                Node<K, V> newRoot = removeFunction.apply(toRemove.getKey());
                moveTo(next.getKey(), newRoot, tupleLength, hashFunction);
            } else {
                removeFunction.apply(toRemove.getKey());
            }

            canRemove = false;
            current = null;
        }

        /**
         * Moves the iterator so that it stands before the specified
         * element.
         *
         * @param k            an element
         * @param rootNode     the root node of the set
         * @param tupleLength  the tuple length
         * @param hashFunction a function that computes a hash code for a key
         */
        protected void moveTo(final @Nullable K k, final @NonNull Node<K, V> rootNode, int tupleLength, ToIntFunction<K> hashFunction) {
            int keyHash = hashFunction.applyAsInt(k);
            int shift = 0;
            Node<K, V> node = rootNode;

            nextStackLevel = -1;
            nextValueNode = null;
            nextValueCursor = 0;
            nextValueLength = 0;
            Arrays.fill(nodes, null);
            Arrays.fill(nodeCursorsAndLengths, 0);
            current = null;

            for (int depth = 0; depth < MAX_DEPTH; depth++) {
                nodes[depth] = node;

                int nodeIndex = node.nodeIndex(keyHash, shift);
                if (nodeIndex != -1) {
                    final int nextCursorIndex = depth * 2;
                    final int nextLengthIndex = nextCursorIndex + 1;
                    nodeCursorsAndLengths[nextCursorIndex] = 0;
                    nodeCursorsAndLengths[nextLengthIndex] = node.nodeArity();
                    node = node.getNode(nodeIndex, tupleLength);
                } else {
                    int payloadIndex = node.payloadIndex(k, keyHash, shift, tupleLength);
                    if (payloadIndex != -1) {
                        nextValueNode = node;
                        nextValueCursor = payloadIndex;
                        nextValueLength = node.payloadArity(tupleLength);
                        nextStackLevel = depth;
                    }
                    break;
                }

                shift += BIT_PARTITION_SIZE;
            }
        }

        /*
         * search for next node that contains values
         */
        private boolean searchNextValueNode(int tupleLength) {
            while (nextStackLevel >= 0) {
                final int currentCursorIndex = nextStackLevel * 2;
                final int currentLengthIndex = currentCursorIndex + 1;

                final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
                final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];

                if (nodeCursor < nodeLength) {
                    final Node<K, V> nextNode = nodes[nextStackLevel].getNode(nodeCursor, tupleLength);
                    nodeCursorsAndLengths[currentCursorIndex]++;

                    if (nextNode.hasNodes()) {
                        /*
                         * put node on next stack level for depth-first traversal
                         */
                        final int nextStackLevel = ++this.nextStackLevel;
                        final int nextCursorIndex = nextStackLevel * 2;
                        final int nextLengthIndex = nextCursorIndex + 1;

                        nodes[nextStackLevel] = nextNode;
                        nodeCursorsAndLengths[nextCursorIndex] = 0;
                        nodeCursorsAndLengths[nextLengthIndex] = nextNode.nodeArity();
                    }

                    if (nextNode.hasPayload()) {
                        /*
                         * found next node that contains values
                         */
                        nextValueNode = nextNode;
                        nextValueCursor = 0;
                        nextValueLength = nextNode.payloadArity(tupleLength);
                        return true;
                    }
                } else {
                    nextStackLevel--;
                }
            }

            return false;
        }
    }

    protected static class KeyIterator<K, V> extends AbstractTrieIterator<K, V>
            implements Iterator<K> {


        KeyIterator(Node<K, V> rootNode, int tupleLength, ToIntFunction<K> hashFunction) {
            super(rootNode, tupleLength, hashFunction);
        }

        @Override
        public K next() {
            return nextEntry(AbstractMap.SimpleImmutableEntry::new).getKey();
        }
    }

    protected static class MapEntryIterator<K, V> extends AbstractTrieIterator<K, V>
            implements Iterator<Map.Entry<K, V>> {

        MapEntryIterator(Node<K, V> rootNode, int tupleLength, ToIntFunction<K> hashFunction) {
            super(rootNode, tupleLength, hashFunction);
        }

        @Override
        public Map.Entry<K, V> next() {
            return nextEntry(AbstractMap.SimpleImmutableEntry::new);
        }
    }

    static class SearchResult<V> {
        private final @Nullable Object[] tuple;
        private final boolean keyExists;

        public SearchResult(@Nullable Object[] tuple, boolean keyExists) {
            this.tuple = tuple;
            this.keyExists = keyExists;
        }

        @SuppressWarnings("unchecked")
        public @Nullable V get() {
            if (!keyExists) {
                throw new NoSuchElementException();
            }
            return (V) tuple[TUPLE_VALUE];
        }

        public boolean keyExists() {
            return keyExists;
        }

        public @Nullable V orElse(@Nullable V elseValue) {
            return keyExists ? get() : elseValue;
        }
    }

    static class BulkChangeEvent {
        int numInBothCollections;
    }

    static class ChangeEvent<V> {

        private Object[] oldValue;
        boolean isModified;
        private boolean isReplaced;

        ChangeEvent() {
        }

        @SuppressWarnings("unchecked")
        public V getOldValue() {
            return oldValue==null?null: (V) oldValue[1];
        }

        public boolean hasReplacedValue() {
            return isReplaced;
        }

        public boolean isModified() {
            return isModified;
        }

        // update: inserted/removed single element, element count changed
        public void modified() {
            this.isModified = true;
        }

        public Object[] getOldTuple() {
            return oldValue;
        }

        public void updated(Object[] oldValue) {
            this.oldValue = oldValue;
            this.isModified = true;
            this.isReplaced = true;
        }

        public void found(Object[] oldValue) {
            this.oldValue = oldValue;
        }
    }

    @SuppressWarnings("unchecked")
    static <K, V> ChampTrieHelper.BitmapIndexedNode<K, V> emptyNode() {
        return (ChampTrieHelper.BitmapIndexedNode<K, V>) ChampTrieHelper.EMPTY_NODE;
    }

    private static final class MutableHashCollisionNode<K, V> extends HashCollisionNode<K, V> {
        private final static long serialVersionUID = 0L;
        transient final @Nullable UniqueIdentity mutator;

        MutableHashCollisionNode(@NonNull UniqueIdentity mutator, int hash, Object[] entries, int tupleLength) {
            super(hash, entries, tupleLength);
            this.mutator = mutator;
        }

        protected UniqueIdentity getMutator() {
            return mutator;
        }
    }

    private static final class MutableBitmapIndexedNode<K, V> extends BitmapIndexedNode<K, V> {
        private final static long serialVersionUID = 0L;
        transient final @Nullable UniqueIdentity mutator;

        private MutableBitmapIndexedNode(@NonNull UniqueIdentity mutator, int nodeMap, int dataMap, @NonNull Object[] nodes, int tupleLength) {
            super(nodeMap, dataMap, nodes, tupleLength);
            this.mutator = mutator;
        }

        protected UniqueIdentity getMutator() {
            return mutator;
        }
    }

    static <K, V> HashCollisionNode<K, V> newHashCollisionNode(
            @Nullable UniqueIdentity mutator, int hash, @NonNull Object[] entries, int tupleLength) {
        return mutator == null
                ? new HashCollisionNode<K, V>(hash, entries, tupleLength)
                : new MutableHashCollisionNode<K, V>(mutator, hash, entries, tupleLength);
    }

    static <K, V> BitmapIndexedNode<K, V> newBitmapIndexedNode(
            @Nullable UniqueIdentity mutator, final int nodeMap,
            final int dataMap, final @NonNull Object[] nodes, int tupleLength) {
        return mutator == null
                ? new BitmapIndexedNode<K, V>(nodeMap, dataMap, nodes, tupleLength)
                : new MutableBitmapIndexedNode<K, V>(mutator, nodeMap, dataMap, nodes, tupleLength);
    }


    /**
     * Dumps a tree in the Graphviz DOT language.
     * <p>
     * References:
     * <dl>
     *     <dt>Graphviz. DOT Language.</dt>
     *     <dd><a href="https://graphviz.org/doc/info/lang.html">graphviz.org</a></dd>
     * </dl>
     *
     * @param a            an {@link Appendable}
     * @param root         the root node of the tree
     * @param tupleLength  the tuple length
     * @param hashFunction the hash function
     * @param <K>          the key type
     * @param <V>          the value type
     * @return a Graphviz representation of the tree
     */
    static <K, V> void dumpTreeAsGraphviz(Appendable a, Node<K, V> root, int tupleLength, ToIntFunction<K> hashFunction) throws IOException {
        a.append("digraph ChampTrie {\n");
        a.append("node [shape=record];\n");
        root.dumpAsGraphviz(a, 0, tupleLength, 0, hashFunction);
        a.append("}\n");
    }

}