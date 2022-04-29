/*
 * @(#)TrieSetHelper.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * This is a package private class that provides shared code for
 * {@link PersistentTrieSet} and {@link TrieSet}.
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
class TrieSetHelper {
    static final int TUPLE_LENGTH = 1;
    static final int HASH_CODE_LENGTH = 32;
    static final int BIT_PARTITION_SIZE = 5;
    static final int BIT_PARTITION_MASK = 0b11111;
    static final BitmapIndexedNode<?> EMPTY_NODE = new BitmapIndexedNode<>(0, 0, new Object[]{});

    /**
     * Don't let anyone instantiate this class.
     */
    private TrieSetHelper() {
    }

    @SuppressWarnings("unchecked")
    static <K> @NonNull BitmapIndexedNode<K> emptyNode() {
        return (BitmapIndexedNode<K>) EMPTY_NODE;
    }

    static class ChangeEvent {
        boolean isModified;
    }

    static class BulkChangeEvent {
        int sizeChange;
    }

    static class BitmapIndexedNode<K> extends Node<K> {
        private final static long serialVersionUID = 0L;
        final @NonNull Object[] nodes;
        final int nodeMap;
        final int dataMap;

        BitmapIndexedNode(final int nodeMap,
                          final int dataMap, final @NonNull Object[] nodes) {
            this.nodeMap = nodeMap;
            this.dataMap = dataMap;
            this.nodes = nodes;
        }

        @Override
        boolean contains(final K key, final int keyHash, final int shift) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((this.dataMap & bitpos) != 0) {
                final int index = index(this.dataMap, bitpos);
                return Objects.equals(getKey(index), key);
            }

            final int nodeMap = nodeMap();
            if ((nodeMap & bitpos) != 0) {
                final int index = index(nodeMap, bitpos);
                return getNode(index).contains(key, keyHash, shift + BIT_PARTITION_SIZE);
            }

            return false;
        }

        /**
         * Creates a copy of this trie with all elements of the specified
         * trie added to it.
         * <p>
         *
         * @param o          the trie to be added to this trie
         * @param shift      the shift for both tries
         * @param bulkChange The field {@code sizeChange} must be set to the
         *                   size of trie {@code o}
         * @param mutator    the mutator
         * @return
         */
        @Override
        @NonNull BitmapIndexedNode<K> copyAddAll(@NonNull Node<K> o, int shift, @NonNull BulkChangeEvent bulkChange, @Nullable UniqueIdentity mutator) {
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
            BitmapIndexedNode<K> that = (BitmapIndexedNode<K>) o;

            int newNodeLength = Integer.bitCount(this.nodeMap | this.dataMap | that.nodeMap | that.dataMap);
            Object[] nodesNew = new Object[newNodeLength];
            int nodeMapNew = this.nodeMap | that.nodeMap;
            int dataMapNew = this.dataMap | that.dataMap;
            int thisNodeMapToDo = this.nodeMap;
            int thatNodeMapToDo = that.nodeMap;

            // case 0:
            // we will not have to do any changes
            ChangeEvent changeEvent = new ChangeEvent();
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
                    K thisKey = this.getKey(index(this.dataMap, bitpos));
                    K thatKey = that.getKey(index(that.dataMap, bitpos));
                    if (Objects.equals(thisKey, thatKey)) {
                        // case 5.1:
                        nodesNew[dataIndex++] = thisKey;
                        bulkChange.sizeChange--;
                    } else {
                        // case 5.2:
                        dataMapNew ^= bitpos;
                        nodeMapNew |= bitpos;
                        int thatKeyHash = Objects.hashCode(thatKey);
                        Node<K> subNodeNew = mergeTwoKeyValPairs(mutator, thisKey, Objects.hashCode(thisKey), thatKey, thatKeyHash, shift + BIT_PARTITION_SIZE);
                        nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = subNodeNew;
                        changed = true;
                    }
                } else if (thisHasData) {
                    K thisKey = this.getKey(index(this.dataMap, bitpos));
                    boolean thatHasNode = (that.nodeMap & bitpos) != 0;
                    if (thatHasNode) {
                        // case 9:
                        dataMapNew ^= bitpos;
                        thatNodeMapToDo ^= bitpos;
                        int thisKeyHash = Objects.hashCode(thisKey);
                        changeEvent.isModified = false;
                        Node<K> subNode = that.nodeAt(bitpos);
                        Node<K> subNodeNew = subNode.updated(mutator, thisKey, thisKeyHash, shift + BIT_PARTITION_SIZE, changeEvent);
                        nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = subNodeNew;
                        changed = true;
                    } else {
                        // case 1:
                        nodesNew[dataIndex++] = thisKey;
                    }
                } else {
                    assert thatHasData;
                    K thatKey = that.getKey(index(that.dataMap, bitpos));
                    int thatKeyHash = Objects.hashCode(thatKey);
                    boolean thisHasNode = (this.nodeMap & bitpos) != 0;
                    if (thisHasNode) {
                        // case 6:
                        dataMapNew ^= bitpos;
                        thisNodeMapToDo ^= bitpos;
                        changeEvent.isModified = false;
                        Node<K> subNode = this.getNode(index(this.nodeMap, bitpos));
                        Node<K> subNodeNew = subNode.updated(mutator, thatKey, thatKeyHash, shift + BIT_PARTITION_SIZE, changeEvent);
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
                    Node<K> thisSubNode = this.getNode(index(this.nodeMap, bitpos));
                    Node<K> thatSubNode = that.getNode(index(that.nodeMap, bitpos));
                    Node<K> subNodeNew = thisSubNode.copyAddAll(thatSubNode, shift + BIT_PARTITION_SIZE, bulkChange, mutator);
                    changed |= subNodeNew != thisSubNode;
                    nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = subNodeNew;

                } else if (thatHasNodeToDo) {
                    // case 8
                    Node<K> thatSubNode = that.getNode(index(that.nodeMap, bitpos));
                    nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = thatSubNode;
                    changed = true;
                } else {
                    // case 2
                    assert thisHasNodeToDo;
                    Node<K> thisSubNode = this.getNode(index(this.nodeMap, bitpos));
                    nodesNew[nodeIndexAt(nodesNew, nodeMapNew, bitpos)] = thisSubNode;
                }
            }

            // Step 3: create new node if it has changed
            // ------
            if (changed) {
                return newBitmapIndexedNode(mutator, nodeMapNew, dataMapNew, nodesNew);
            }

            return this;
        }

        private BitmapIndexedNode<K> copyAndInsertValue(final UniqueIdentity mutator, final int bitpos,
                                                        final K key) {
            final int idx = TUPLE_LENGTH * dataIndex(bitpos);

            // copy 'src' and insert 1 element(s) at position 'idx'
            final Object[] dst = ArrayHelper.copyAdd(this.nodes, idx, key);
            return newBitmapIndexedNode(mutator, nodeMap(), dataMap() | bitpos, dst);

        }

        private BitmapIndexedNode<K> copyAndMigrateFromInlineToNode(final UniqueIdentity mutator,
                                                                    final int bitpos, final Node<K> node) {

            final int idxOld = TUPLE_LENGTH * dataIndex(bitpos);
            final int idxNew = this.nodes.length - TUPLE_LENGTH - nodeIndex(bitpos);
            assert idxOld <= idxNew;

            // copy 'src' and remove 1 element(s) at position 'idxOld' and
            // insert 1 element(s) at position 'idxNew'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1 + 1];
            System.arraycopy(src, 0, dst, 0, idxOld);
            System.arraycopy(src, idxOld + 1, dst, idxOld, idxNew - idxOld);
            System.arraycopy(src, idxNew + 1, dst, idxNew + 1, src.length - idxNew - 1);
            dst[idxNew] = node;

            return newBitmapIndexedNode(mutator, nodeMap() | bitpos, dataMap() ^ bitpos, dst);

        }

        private BitmapIndexedNode<K> copyAndMigrateFromNodeToInline(final UniqueIdentity mutator,
                                                                    final int bitpos, final Node<K> node) {

            final int idxOld = this.nodes.length - 1 - nodeIndex(bitpos);
            final int idxNew = TUPLE_LENGTH * dataIndex(bitpos);
            assert idxOld >= idxNew;

            // copy 'src' and remove 1 element(s) at position 'idxOld' and
            // insert 1 element(s) at position 'idxNew'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1 + 1];
            System.arraycopy(src, 0, dst, 0, idxNew);
            System.arraycopy(src, idxNew, dst, idxNew + 1, idxOld - idxNew);
            System.arraycopy(src, idxOld + 1, dst, idxOld + 1, src.length - idxOld - 1);
            dst[idxNew] = node.getKey(0);

            return newBitmapIndexedNode(mutator, nodeMap() ^ bitpos, dataMap() | bitpos, dst);
        }

        private BitmapIndexedNode<K> copyAndRemoveValue(final UniqueIdentity mutator, final int bitpos) {
            final int idx = TUPLE_LENGTH * dataIndex(bitpos);

            // copy 'src' and remove 1 element(s) at position 'idx'
            final Object[] dst = ArrayHelper.copyRemove(this.nodes, idx);
            return newBitmapIndexedNode(mutator, nodeMap(), dataMap() ^ bitpos, dst);
        }

        private BitmapIndexedNode<K> copyAndSetNode(final UniqueIdentity mutator, final int bitpos,
                                                    final Node<K> newNode) {

            final int nodeIndex = nodeIndex(bitpos);
            final int idx = this.nodes.length - 1 - nodeIndex;

            if (isAllowedToEdit(mutator)) {
                // no copying if already editable
                this.nodes[idx] = newNode;
                return this;
            } else {
                // copy 'src' and set 1 element(s) at position 'idx'
                final Object[] dst = ArrayHelper.copySet(this.nodes, idx, newNode);
                return newBitmapIndexedNode(mutator, nodeMap(), dataMap(), dst);
            }
        }

        private int dataIndex(final int bitpos) {
            return Integer.bitCount(dataMap() & (bitpos - 1));
        }

        private int dataMap() {
            return dataMap;
        }

        @Override
        public boolean equivalent(final @NonNull Node<?> other) {
            if (this == other) {
                return true;
            }
            BitmapIndexedNode<?> that = (BitmapIndexedNode<?>) other;

            // nodes array: we compare local payload from 0 to splitAt (excluded)
            // and then we compare the nested nodes from splitAt to length (excluded)
            int splitAt = payloadArity();
            return nodeMap() == that.nodeMap()
                    && dataMap() == that.dataMap()
                    && ArrayHelper.equals(nodes, 0, splitAt, that.nodes, 0, splitAt)
                    && ArrayHelper.equals(nodes, splitAt, nodes.length, that.nodes, splitAt, that.nodes.length,
                    (a, b) -> ((Node<?>) a).equivalent((Node<?>) b));
        }

        @SuppressWarnings("unchecked")
        @Override
        K getKey(final int index) {
            return (K) nodes[TUPLE_LENGTH * index];
        }

        @SuppressWarnings("unchecked")
        @Override
        Node<K> getNode(final int index) {
            return (Node<K>) nodes[nodes.length - 1 - index];
        }

        @Override
        boolean hasNodes() {
            return nodeMap() != 0;
        }

        @Override
        boolean hasPayload() {
            return dataMap() != 0;
        }

        private Node<K> mergeTwoKeyValPairs(UniqueIdentity mutator,
                                            final K key0, final int keyHash0,
                                            final K key1, final int keyHash1,
                                            final int shift) {
            assert !(key0.equals(key1));

            if (shift >= HASH_CODE_LENGTH) {
                @SuppressWarnings("unchecked")
                HashCollisionNode<K> unchecked = newHashCollisionNode(mutator, keyHash0, (K[]) new Object[]{key0, key1});
                return unchecked;
            }

            final int mask0 = mask(keyHash0, shift);
            final int mask1 = mask(keyHash1, shift);

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

        @Override
        int nodeArity() {
            return Integer.bitCount(nodeMap());
        }

        private Node<K> nodeAt(final int bitpos) {
            return getNode(nodeIndex(bitpos));
        }

        @Override
        int nodeIndex(int keyHash, int shift) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);
            return (this.nodeMap & bitpos) != 0
                    ? index(this.nodeMap, bitpos)
                    : -1;
        }

        private int nodeIndex(final int bitpos) {
            return Integer.bitCount(nodeMap() & (bitpos - 1));
        }

        private int nodeIndexAt(Object[] array, int nodeMap, final int bitpos) {
            return array.length - 1 - Integer.bitCount(nodeMap & (bitpos - 1));
        }

        private int nodeMap() {
            return nodeMap;
        }

        @Override
        int payloadArity() {
            return Integer.bitCount(dataMap());
        }

        @Override
        int payloadIndex(K key, final int keyHash, final int shift) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);
            return (this.dataMap & bitpos) != 0
                    ? index(this.dataMap, bitpos)
                    : -1;
        }

        @Override
        BitmapIndexedNode<K> removed(final UniqueIdentity mutator, final K key, final int keyHash,
                                     final int shift, final ChangeEvent changeEvent) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap() & bitpos) != 0) { // inplace value
                final int dataIndex = dataIndex(bitpos);

                if (Objects.equals(getKey(dataIndex), key)) {
                    changeEvent.isModified = true;
                    if (this.payloadArity() == 2 && this.nodeArity() == 0) {
                        // Create new node with remaining pair. The new node will a) either become the new root
                        // returned, or b) unwrapped and inlined during returning.
                        final int newDataMap =
                                (shift == 0) ? (dataMap() ^ bitpos) : bitpos(mask(keyHash, 0));

                        if (dataIndex == 0) {
                            return newBitmapIndexedNode(mutator, 0, newDataMap, new Object[]{getKey(1)});

                        } else {
                            return newBitmapIndexedNode(mutator, 0, newDataMap, new Object[]{getKey(0)});

                        }
                    } else {
                        return copyAndRemoveValue(mutator, bitpos);
                    }
                } else {
                    return this;
                }
            } else if ((nodeMap() & bitpos) != 0) { // node (not value)
                final Node<K> subNode = nodeAt(bitpos);
                final Node<K> subNodeNew =
                        subNode.removed(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, changeEvent);

                if (!changeEvent.isModified) {
                    return this;
                }

                switch (subNodeNew.sizePredicate()) {
                case SIZE_EMPTY:
                    throw new IllegalStateException("Sub-node must have at least one element.");
                case SIZE_ONE:
                    if (this.payloadArity() == 0 && this.nodeArity() == 1) {
                        // escalate (singleton or empty) result
                        return (BitmapIndexedNode<K>) subNodeNew;
                    } else {
                        // inline value (move to front)
                        return copyAndMigrateFromNodeToInline(mutator, bitpos, subNodeNew);
                    }
                default:
                    // modify current node (set replacement node)
                    return copyAndSetNode(mutator, bitpos, subNodeNew);
                }
            }

            return this;
        }

        @Override
        public SizeClass sizePredicate() {
            if (this.nodeArity() == 0) {
                switch (this.payloadArity()) {
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
        BitmapIndexedNode<K> updated(final UniqueIdentity mutator, final K key,
                                     final int keyHash, final int shift, final ChangeEvent changeEvent) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap() & bitpos) != 0) { // inplace value
                final int dataIndex = dataIndex(bitpos);
                final K currentKey = getKey(dataIndex);

                if (Objects.equals(currentKey, key)) {
                    return this;
                } else {
                    final Node<K> subNodeNew = mergeTwoKeyValPairs(mutator, currentKey,
                            currentKey.hashCode(), key, keyHash, shift + BIT_PARTITION_SIZE);

                    changeEvent.isModified = true;
                    return copyAndMigrateFromInlineToNode(mutator, bitpos, subNodeNew);
                }
            } else if ((nodeMap() & bitpos) != 0) { // node (not value)
                final Node<K> subNode = nodeAt(bitpos);
                final Node<K> subNodeNew =
                        subNode.updated(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, changeEvent);

                if (changeEvent.isModified) {
                    // NOTE: subNode and subNodeNew may be referential equal if updated transiently in-place.
                    // Therefore, diffing nodes is not an option. Changes to content and meta-data need to be
                    // explicitly tracked and passed when descending from recursion (i.e., {@code details}).
                    return copyAndSetNode(mutator, bitpos, subNodeNew);
                } else {
                    return this;
                }
            } else {
                // no value
                changeEvent.isModified = true;
                return copyAndInsertValue(mutator, bitpos, key);
            }
        }

    }

    static abstract class Node<K> implements Serializable {
        private final static long serialVersionUID = 0L;

        Node() {
        }


        protected @Nullable UniqueIdentity getMutator() {
            return null;
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
         * @param mask
         * @return
         */
        static int bitpos(final int mask) {
            return 1 << mask;
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

        boolean isAllowedToEdit(@Nullable UniqueIdentity y) {
            UniqueIdentity x = getMutator();
            return x != null && x == y;
        }

        /**
         * Given a keyHash and a shift, returns shifted portion of the
         * keyHash masked by the {@link #BIT_PARTITION_MASK}.
         * <p>
         * For example, if the shift is 10, and the bit partition is 5,
         * returns bits 10 to 15 shifted by 10 to the right.
         *
         * @param keyHash a keyHash
         * @param shift   a shift
         * @return shifted and masked keyHash
         */
        static int mask(final int keyHash, final int shift) {
            return (keyHash >>> shift) & BIT_PARTITION_MASK;
        }

        abstract boolean contains(final K key, final int keyHash, final int shift);

        abstract @NonNull Node<K> copyAddAll(@NonNull Node<K> that, final int shift, BulkChangeEvent bulkChange, UniqueIdentity mutator);

        abstract boolean equivalent(final @NonNull Node<?> other);

        abstract K getKey(final int index);

        abstract Node<K> getNode(final int index);

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

        abstract int payloadArity();

        /**
         * Returns the payload index for the given keyHash and shift, or -1.
         *
         * @param key
         * @param keyHash the key hash
         * @param shift   the shift
         * @return the payload index or -1
         */
        abstract int payloadIndex(@Nullable K key, final int keyHash, final int shift);

        abstract Node<K> removed(final UniqueIdentity mutator, final K key, final int keyHash, final int shift,
                                 final ChangeEvent changeEvent);

        abstract SizeClass sizePredicate();

        abstract Node<K> updated(final UniqueIdentity mutator, final K key, final int keyHash, final int shift,
                                 final ChangeEvent changeEvent);

    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static class HashCollisionNode<K> extends Node<K> {
        private final static long serialVersionUID = 0L;
        private final int hash;
        private @NonNull K[] keys;

        HashCollisionNode(final int hash, final K[] keys) {
            this.keys = keys;
            this.hash = hash;
            assert payloadArity() >= 2;
        }

        @Override
        boolean contains(final K key, final int keyHash, final int shift) {
            if (this.hash == keyHash) {
                for (K k : keys) {
                    if (Objects.equals(k, key)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        @NonNull Node<K> copyAddAll(@NonNull Node<K> o, int shift, BulkChangeEvent bulkChange, UniqueIdentity mutator) {
            if (o == this) {
                return this;
            }
            // The other node must be a HashCollisionNode
            HashCollisionNode<K> that = (HashCollisionNode<K>) o;

            List<K> list = new ArrayList<>(this.keys.length + that.keys.length);

            // Step 1: Add all this.keys to list
            list.addAll(Arrays.asList(this.keys));

            // Step 2: Add all that.keys to list which are not in this.keys
            //         This is quadratic.
            //         If the sets are disjoint, we can do nothing about it.
            //         If the sets intersect, we can mark those which are
            //         equal in a bitset, so that we do not need to check
            //         them over and over again.
            BitSet bs = new BitSet(this.keys.length);
            outer:
            for (int j = 0; j < that.keys.length; j++) {
                K key = that.keys[j];
                for (int i = bs.nextClearBit(0); i >= 0 && i < this.keys.length; i = bs.nextClearBit(i + 1)) {
                    if (Objects.equals(key, this.keys[i])) {
                        bs.set(i);
                        bulkChange.sizeChange--;
                        continue outer;
                    }
                }
                list.add(key);
            }

            if (list.size() > this.keys.length) {
                @SuppressWarnings("unchecked")
                HashCollisionNode<K> unchecked = newHashCollisionNode(mutator, hash, (K[]) list.toArray());
                return unchecked;
            }

            return this;
        }

        @Override
        public boolean equivalent(final @NonNull Node<?> other) {
            if (this == other) {
                return true;
            }

            HashCollisionNode<?> that = (HashCollisionNode<?>) other;
            if (hash != that.hash
                    || payloadArity() != that.payloadArity()) {
                return false;
            }

            // Linear scan for each key, because of arbitrary element order.
            // ...maybe we could use a bit set to mark keys that we have
            //    found in both sets? But that will cost memory!
            outerLoop:
            for (int i = 0, n = that.payloadArity(); i < n; i++) {
                final Object otherKey = that.getKey(i);

                for (int j = 0, m = keys.length; j < m; j++) {
                    final K key = keys[j];
                    if (Objects.equals(key, otherKey)) {
                        continue outerLoop;
                    }
                }
                return false;
            }
            return true;
        }

        @Override
        K getKey(final int index) {
            return keys[index];
        }

        @Override
        Node<K> getNode(int index) {
            throw new IllegalStateException("Is leaf node.");
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
        int nodeIndex(int keyHash, int shift) {
            return -1;
        }

        @Override
        int payloadArity() {
            return keys.length;
        }

        @Override
        int payloadIndex(K key, final int keyHash, final int shift) {
            if (this.hash != keyHash) {
                return -1;
            }
            for (int i = 0, keysLength = keys.length; i < keysLength; i++) {
                K k = keys[i];
                if (Objects.equals(k, key)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        Node<K> removed(final UniqueIdentity mutator, final K key,
                        final int keyHash, final int shift, final ChangeEvent changeEvent) {
            for (int idx = 0; idx < keys.length; idx++) {
                if (Objects.equals(keys[idx], key)) {
                    changeEvent.isModified = true;

                    if (payloadArity() == 1) {
                        return emptyNode();
                    } else if (payloadArity() == 2) {
                        // Create root node with singleton element.
                        // This node will be a) either be the new root
                        // returned, or b) unwrapped and inlined.
                        final K theOtherKey = (idx == 0) ? keys[1] : keys[0];
                        return newBitmapIndexedNode(mutator, 0, bitpos(BitmapIndexedNode.mask(keyHash, 0)), new Object[]{theOtherKey});
                    } else {
                        // copy 'this.keys' and remove 1 element(s) at position 'idx'
                        final K[] keysNew = ArrayHelper.copyRemove(this.keys, idx);
                        if (isAllowedToEdit(mutator)) {
                            this.keys = keysNew;
                        } else {
                            return newHashCollisionNode(mutator, keyHash, keysNew);
                        }
                    }
                }
            }
            return this;
        }

        @Override
        SizeClass sizePredicate() {
            return SizeClass.SIZE_MORE_THAN_ONE;
        }

        @Override
        public Node<K> updated(final UniqueIdentity mutator, final K key,
                               final int keyHash, final int shift, final ChangeEvent changeEvent) {
            assert this.hash == keyHash;
            for (K k : keys) {
                if (Objects.equals(k, key)) {
                    return this;
                }
            }
            final K[] keysNew = Arrays.copyOf(keys, keys.length + 1);
            keysNew[keys.length] = key;
            changeEvent.isModified = true;
            if (isAllowedToEdit(mutator)) {
                this.keys = keysNew;
                return this;
            }
            return newHashCollisionNode(mutator, keyHash, keysNew);
        }
    }

    static class MutableHashCollisionNode<K> extends HashCollisionNode<K> {
        private final static long serialVersionUID = 0L;
        transient final @Nullable UniqueIdentity mutator;

        public MutableHashCollisionNode(@NonNull UniqueIdentity mutator, int hash, K[] keys) {
            super(hash, keys);
            this.mutator = mutator;
        }

        protected UniqueIdentity getMutator() {
            return mutator;
        }

    }

    static class MutableBitmapIndexedNode<K> extends BitmapIndexedNode<K> {
        private final static long serialVersionUID = 0L;
        transient final @Nullable UniqueIdentity mutator;

        public MutableBitmapIndexedNode(@NonNull UniqueIdentity mutator, final int nodeMap,
                                        final int dataMap, final @NonNull Object[] nodes) {
            super(nodeMap, dataMap, nodes);
            this.mutator = mutator;
        }

        protected UniqueIdentity getMutator() {
            return mutator;
        }

    }


    /**
     * Iterator skeleton that uses a fixed stack in depth.
     */
    static class TrieIterator<K> implements Iterator<K> {

        private static final int MAX_DEPTH = 7;
        private final @NonNull int[] nodeCursorsAndLengths = new int[MAX_DEPTH * 2];
        protected int nextValueCursor;
        protected int nextValueLength;
        protected @Nullable Node<K> nextValueNode;
        private int nextStackLevel = -1;
        protected @Nullable K current;
        boolean canRemove = false;

        @SuppressWarnings({"unchecked"})
        Node<K>[] nodes = new Node[MAX_DEPTH];

        TrieIterator(Node<K> rootNode) {
            if (rootNode.hasNodes()) {
                nextStackLevel = 0;

                nodes[0] = rootNode;
                nodeCursorsAndLengths[0] = 0;
                nodeCursorsAndLengths[1] = rootNode.nodeArity();
            }

            if (rootNode.hasPayload()) {
                nextValueNode = rootNode;
                nextValueCursor = 0;
                nextValueLength = rootNode.payloadArity();
            }
        }

        public boolean hasNext() {
            if (nextValueCursor < nextValueLength) {
                return true;
            } else {
                return searchNextValueNode();
            }
        }

        /**
         * Moves the iterator so that it stands before the specified element.
         *
         * @param k        an element
         * @param rootNode the root node of the set
         */
        protected void moveTo(final @Nullable K k, final @NonNull Node<K> rootNode) {
            int hash = Objects.hashCode(k);
            Node<K> node = rootNode;

            int shift = 0;
            nextStackLevel = -1;
            nextValueNode = null;
            nextValueCursor = 0;
            nextValueLength = 0;
            Arrays.fill(nodes, null);
            Arrays.fill(nodeCursorsAndLengths, 0);

            for (int depth = 0; depth < MAX_DEPTH; depth++) {
                nodes[depth] = node;

                int nodeIndex = node.nodeIndex(hash, shift);
                if (nodeIndex != -1) {
                    final int nextCursorIndex = depth * 2;
                    final int nextLengthIndex = nextCursorIndex + 1;
                    nodeCursorsAndLengths[nextCursorIndex] = 0;
                    nodeCursorsAndLengths[nextLengthIndex] = node.nodeArity();
                    node = node.getNode(nodeIndex);
                } else {
                    int payloadIndex = node.payloadIndex(k, hash, shift);
                    if (payloadIndex != -1) {
                        nextValueNode = node;
                        nextValueCursor = payloadIndex;
                        nextValueLength = node.payloadArity();
                        nextStackLevel = depth;
                    }
                    break;
                }

                shift += BIT_PARTITION_SIZE;
            }
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else {
                canRemove = true;
                return current = nextValueNode.getKey(nextValueCursor++);
            }
        }

        private boolean searchNextValueNode() {
            while (nextStackLevel >= 0) {
                final int currentCursorIndex = nextStackLevel * 2;
                final int currentLengthIndex = currentCursorIndex + 1;

                final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
                final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];

                if (nodeCursor < nodeLength) {
                    final Node<K> nextNode = nodes[nextStackLevel].getNode(nodeCursor);
                    nodeCursorsAndLengths[currentCursorIndex]++;

                    if (nextNode.hasNodes()) {
                        // put node on next stack level for depth-first traversal
                        final int nextStackLevel = ++this.nextStackLevel;
                        final int nextCursorIndex = nextStackLevel * 2;
                        final int nextLengthIndex = nextCursorIndex + 1;

                        nodes[nextStackLevel] = nextNode;
                        nodeCursorsAndLengths[nextCursorIndex] = 0;
                        nodeCursorsAndLengths[nextLengthIndex] = nextNode.nodeArity();
                    }

                    if (nextNode.hasPayload()) {
                        nextValueNode = nextNode;
                        nextValueCursor = 0;
                        nextValueLength = nextNode.payloadArity();
                        return true;
                    }
                } else {
                    nextStackLevel--;
                }
            }

            return false;
        }
    }

    static <K> HashCollisionNode<K> newHashCollisionNode(@Nullable UniqueIdentity mutator, int hash, @NonNull K[] nodes) {
        return mutator == null
                ? new HashCollisionNode<>(hash, nodes)
                : new MutableHashCollisionNode<>(mutator, hash, nodes);
    }

    static <K> BitmapIndexedNode<K> newBitmapIndexedNode(@Nullable UniqueIdentity mutator, final int nodeMap,
                                                         final int dataMap, final @NonNull Object[] nodes) {
        return mutator == null
                ? new BitmapIndexedNode<K>(nodeMap, dataMap, nodes)
                : new MutableBitmapIndexedNode<K>(mutator, nodeMap, dataMap, nodes);
    }


}
