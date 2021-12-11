/*
 * @(#)PersistentTrieSet.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;


/**
 * Implements the persistent set interface with a Compressed
 * Hash-Array Mapped Prefix-trie (CHAMP).
 * <p>
 * Creating a new delta persistent set with a single element added or removed
 * is performed in {@code O(1)} time and space.
 * <dl>
 * References:
 * <dl>
 *     <dt>This code has been derived from "The Capsule Hash Trie Collections Library".</dt>
 *     <dd>Copyright (c) Michael Steindorfer, Centrum Wiskunde & Informatica, and Contributors.
 *         BSD 2-Clause License.
 *         <a href="https://github.com/usethesource/capsule">github.com</a>.</dd>
 * </dl>
 *
 * @param <E> the element type
 */
public class PersistentTrieSet<E> implements PersistentSet<E> {

    private static final Node<?> EMPTY_NODE = new BitmapIndexedNode<>(null, 0, 0, new Object[]{});

    private static final PersistentTrieSet<?> EMPTY_SET = new PersistentTrieSet<>(EMPTY_NODE, 0, 0);

    private final Node<E> root;
    private final int hashCode;
    private final int size;

    private PersistentTrieSet(Node<E> root, int hashCode, int size) {
        this.root = root;
        this.hashCode = hashCode;
        this.size = size;
    }

    public static <K> PersistentTrieSet<K> copyOf(@NonNull Iterable<? extends K> set) {
        if (set instanceof PersistentTrieSet) {
            //noinspection unchecked
            return (PersistentTrieSet<K>) set;
        }
        TransientTrieSet<K> tr = new TransientTrieSet<>(of());
        for (final K key : set) {
            tr.add(key);
        }
        return tr.freeze();
    }

    @SuppressWarnings("unchecked")
    private static <K> Node<K> emptyNode() {
        return (Node<K>) PersistentTrieSet.EMPTY_NODE;
    }

    @SafeVarargs
    public static <K> PersistentTrieSet<K> of(@NonNull K... keys) {
        return PersistentTrieSet.<K>of().withAddAll(Arrays.asList(keys));
    }

    @SuppressWarnings("unchecked")
    public static <K> PersistentTrieSet<K> of() {
        return (PersistentTrieSet<K>) PersistentTrieSet.EMPTY_SET;
    }

    private TransientTrieSet<E> asTransient() {
        return new TransientTrieSet<>(this);
    }

    @Override
    public boolean contains(final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return root.contains(key, key.hashCode(), 0);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (other instanceof PersistentTrieSet) {
            PersistentTrieSet<?> that = (PersistentTrieSet<?>) other;
            if (this.size != that.size || this.hashCode != that.hashCode) {
                return false;
            }
            return root.equivalent(that.root);
        } else if (other instanceof java.util.Set) {
            java.util.Set<?> that = (java.util.Set<?>) other;
            if (this.size() != that.size()) {
                return false;
            }
            return containsAll(that);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new TrieIterator<>(root);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        String body = stream().map(Object::toString).reduce((o1, o2) -> String.join(", ", o1, o2))
                .orElse("");
        return String.format("{%s}", body);
    }

    public @NonNull PersistentTrieSet<E> withAdd(final @NonNull E key) {
        final int keyHash = key.hashCode();
        final ChangeEvent changeEvent = new ChangeEvent();
        final Node<E> newRootNode = root.updated(null, key,
                keyHash, 0, changeEvent);
        if (changeEvent.isModified) {
            return new PersistentTrieSet<>(newRootNode, hashCode + keyHash, size + 1);
        }

        return this;
    }

    public @NonNull PersistentTrieSet<E> withAddAll(final @NonNull Iterable<? extends E> set) {
        final TransientTrieSet<E> t = this.asTransient();
        boolean modified = false;
        for (final E key : set) {
            modified |= t.add(key);
        }
        return modified ? t.freeze() : this;
    }

    public @NonNull PersistentTrieSet<E> withRemove(final @NonNull E key) {
        final int keyHash = key.hashCode();
        final ChangeEvent changeEvent = new ChangeEvent();
        final Node<E> newRootNode = root.removed(null, key,
                keyHash, 0, changeEvent);
        if (changeEvent.isModified) {
            return new PersistentTrieSet<>(newRootNode, hashCode - keyHash, size - 1);
        }

        return this;
    }

    public @NonNull PersistentTrieSet<E> withRemoveAll(final @NonNull Iterable<? extends E> set) {
        final TransientTrieSet<E> t = this.asTransient();
        boolean modified = false;
        for (final E key : set) {
            modified |= t.remove(key);
        }
        return modified ? t.freeze() : this;
    }

    public @NonNull PersistentTrieSet<E> withRetainAll(final @NonNull Collection<? extends E> set) {
        final TransientTrieSet<E> t = this.asTransient();
        boolean modified = false;
        for (E key : this) {
            if (!set.contains(key)) {
                t.remove(key);
                modified = true;
            }
        }
        return modified ? t.freeze() : this;
    }

    private enum SizeClass {
        SIZE_EMPTY,
        SIZE_ONE,
        SIZE_MORE_THAN_ONE
    }

    private static abstract class Node<K> {
        static final int TUPLE_LENGTH = 1;
        static final int HASH_CODE_LENGTH = 32;
        static final int BIT_PARTITION_SIZE = 5;
        static final int BIT_PARTITION_MASK = 0b11111;
        transient final @Nullable Nonce bulkMutator;

        Node(@Nullable Nonce bulkMutator) {
            this.bulkMutator = bulkMutator;
        }

        static int bitpos(final int mask) {
            return 1 << mask;
        }

        static int index(final int bitmap, final int bitpos) {
            return Integer.bitCount(bitmap & (bitpos - 1));
        }

        static int index(final int bitmap, final int mask, final int bitpos) {
            return (bitmap == -1) ? mask : index(bitmap, bitpos);
        }

        static boolean isAllowedToEdit(Nonce x, Nonce y) {
            return x != null && (x == y);
        }

        static int mask(final int keyHash, final int shift) {
            return (keyHash >>> shift) & BIT_PARTITION_MASK;
        }

        static <K> Node<K> mergeTwoKeyValPairs(Nonce bulkMutator, final K key0, final int keyHash0,
                                               final K key1, final int keyHash1, final int shift) {
            assert !(key0.equals(key1));

            if (shift >= HASH_CODE_LENGTH) {
                //noinspection unchecked
                return new HashCollisionNode<>(bulkMutator, keyHash0, (K[]) new Object[]{key0, key1});
            }

            final int mask0 = mask(keyHash0, shift);
            final int mask1 = mask(keyHash1, shift);

            if (mask0 != mask1) {
                // both nodes fit on same level
                final int dataMap = bitpos(mask0) | bitpos(mask1);
                if (mask0 < mask1) {
                    return new BitmapIndexedNode<>(bulkMutator, 0, dataMap, new Object[]{key0, key1});
                } else {
                    return new BitmapIndexedNode<>(bulkMutator, 0, dataMap, new Object[]{key1, key0});
                }
            } else {
                final Node<K> node =
                        mergeTwoKeyValPairs(bulkMutator, key0, keyHash0, key1, keyHash1, shift + BIT_PARTITION_SIZE);
                // values fit on next level
                final int nodeMap = bitpos(mask0);
                return new BitmapIndexedNode<>(bulkMutator, nodeMap, 0, new Object[]{node});
            }
        }

        abstract boolean contains(final K key, final int keyHash, final int shift);

        abstract K getKey(final int index);

        abstract Node<K> getNode(final int index);

        abstract boolean hasNodes();

        abstract boolean hasPayload();

        abstract int nodeArity();

        abstract int payloadArity();

        abstract Node<K> removed(final Nonce bulkMutator, final K key, final int keyHash, final int shift,
                                 final ChangeEvent changeEvent);

        abstract SizeClass sizePredicate();

        abstract Node<K> updated(final Nonce bulkMutator, final K key, final int keyHash, final int shift,
                                 final ChangeEvent changeEvent);

        abstract boolean equivalent(final @NonNull Node<?> other);

    }

    private static final class BitmapIndexedNode<K> extends Node<K> {
        final Object[] nodes;
        final int nodeMap;
        final int dataMap;

        BitmapIndexedNode(final @Nullable Nonce bulkMutator, final int nodeMap,
                          final int dataMap, final Object[] nodes) {
            super(bulkMutator);
            this.nodeMap = nodeMap;
            this.dataMap = dataMap;
            this.nodes = nodes;
        }

        @Override
        boolean contains(final K key, final int keyHash, final int shift) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            final int dataMap = dataMap();
            if ((dataMap & bitpos) != 0) {
                final int index = index(dataMap, mask, bitpos);
                return Objects.equals(getKey(index), key);
            }

            final int nodeMap = nodeMap();
            if ((nodeMap & bitpos) != 0) {
                final int index = index(nodeMap, mask, bitpos);
                return getNode(index).contains(key, keyHash, shift + BIT_PARTITION_SIZE);
            }

            return false;
        }

        Node<K> copyAndInsertValue(final Nonce bulkMutator, final int bitpos,
                                   final K key) {
            final int idx = TUPLE_LENGTH * dataIndex(bitpos);

            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length + 1];

            // copy 'src' and insert 1 element(s) at position 'idx'
            System.arraycopy(src, 0, dst, 0, idx);
            dst[idx] = key;
            System.arraycopy(src, idx, dst, idx + 1, src.length - idx);

            return new BitmapIndexedNode<>(bulkMutator, nodeMap(), dataMap() | bitpos, dst);
        }

        Node<K> copyAndMigrateFromInlineToNode(final Nonce bulkMutator,
                                               final int bitpos, final Node<K> node) {

            final int idxOld = TUPLE_LENGTH * dataIndex(bitpos);
            final int idxNew = this.nodes.length - TUPLE_LENGTH - nodeIndex(bitpos);

            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1 + 1];

            // copy 'src' and remove 1 element(s) at position 'idxOld' and
            // insert 1 element(s) at position 'idxNew'
            assert idxOld <= idxNew;
            System.arraycopy(src, 0, dst, 0, idxOld);
            System.arraycopy(src, idxOld + 1, dst, idxOld, idxNew - idxOld);
            dst[idxNew] = node;
            System.arraycopy(src, idxNew + 1, dst, idxNew + 1, src.length - idxNew - 1);

            return new BitmapIndexedNode<>(bulkMutator, nodeMap() | bitpos, dataMap() ^ bitpos, dst);
        }

        Node<K> copyAndMigrateFromNodeToInline(final Nonce bulkMutator,
                                               final int bitpos, final Node<K> node) {

            final int idxOld = this.nodes.length - 1 - nodeIndex(bitpos);
            final int idxNew = TUPLE_LENGTH * dataIndex(bitpos);

            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1 + 1];

            // copy 'src' and remove 1 element(s) at position 'idxOld' and
            // insert 1 element(s) at position 'idxNew'
            assert idxOld >= idxNew;
            System.arraycopy(src, 0, dst, 0, idxNew);
            dst[idxNew] = node.getKey(0);
            System.arraycopy(src, idxNew, dst, idxNew + 1, idxOld - idxNew);
            System.arraycopy(src, idxOld + 1, dst, idxOld + 1, src.length - idxOld - 1);

            return new BitmapIndexedNode<>(bulkMutator, nodeMap() ^ bitpos, dataMap() | bitpos, dst);
        }

        Node<K> copyAndRemoveValue(final Nonce bulkMutator, final int bitpos) {
            final int idx = TUPLE_LENGTH * dataIndex(bitpos);

            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1];

            // copy 'src' and remove 1 element(s) at position 'idx'
            System.arraycopy(src, 0, dst, 0, idx);
            System.arraycopy(src, idx + 1, dst, idx, src.length - idx - 1);

            return new BitmapIndexedNode<>(bulkMutator, nodeMap(), dataMap() ^ bitpos, dst);
        }

        Node<K> copyAndSetNode(final Nonce bulkMutator, final int bitpos,
                               final Node<K> newNode) {

            final int nodeIndex = nodeIndex(bitpos);
            final int idx = this.nodes.length - 1 - nodeIndex;

            if (isAllowedToEdit(this.bulkMutator, bulkMutator)) {
                // no copying if already editable
                this.nodes[idx] = newNode;
                return this;
            } else {
                // copy 'src' and set 1 element(s) at position 'idx'
                final Object[] src = this.nodes;
                final Object[] dst = new Object[src.length];
                System.arraycopy(src, 0, dst, 0, src.length);
                dst[idx] = newNode;

                return new BitmapIndexedNode<>(bulkMutator, nodeMap(), dataMap(), dst);
            }
        }

        int dataIndex(final int bitpos) {
            return Integer.bitCount(dataMap() & (bitpos - 1));
        }

        int dataMap() {
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
                    && Arrays.equals(nodes, 0, splitAt, that.nodes, 0, splitAt)
                    && Arrays.equals(nodes, splitAt, nodes.length, that.nodes, splitAt, that.nodes.length,
                    (a, b) -> ((Node<?>) a).equivalent((Node<?>) b) ? 0 : 1);
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

        @Override
        int nodeArity() {
            return Integer.bitCount(nodeMap());
        }

        Node<K> nodeAt(final int bitpos) {
            return getNode(nodeIndex(bitpos));
        }

        int nodeIndex(final int bitpos) {
            return Integer.bitCount(nodeMap() & (bitpos - 1));
        }

        int nodeMap() {
            return nodeMap;
        }

        @Override
        int payloadArity() {
            return Integer.bitCount(dataMap());
        }

        @Override
        Node<K> removed(final Nonce bulkMutator, final K key, final int keyHash,
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
                            return new BitmapIndexedNode<>(bulkMutator, 0, newDataMap, new Object[]{getKey(1)});
                        } else {
                            return new BitmapIndexedNode<>(bulkMutator, 0, newDataMap, new Object[]{getKey(0)});
                        }
                    } else {
                        return copyAndRemoveValue(bulkMutator, bitpos);
                    }
                } else {
                    return this;
                }
            } else if ((nodeMap() & bitpos) != 0) { // node (not value)
                final Node<K> subNode = nodeAt(bitpos);
                final Node<K> subNodeNew =
                        subNode.removed(bulkMutator, key, keyHash, shift + BIT_PARTITION_SIZE, changeEvent);

                if (!changeEvent.isModified) {
                    return this;
                }

                switch (subNodeNew.sizePredicate()) {
                case SIZE_EMPTY:
                    throw new IllegalStateException("Sub-node must have at least one element.");
                case SIZE_ONE:
                    if (this.payloadArity() == 0 && this.nodeArity() == 1) {
                        // escalate (singleton or empty) result
                        return subNodeNew;
                    } else {
                        // inline value (move to front)
                        return copyAndMigrateFromNodeToInline(bulkMutator, bitpos, subNodeNew);
                    }
                default:
                    // modify current node (set replacement node)
                    return copyAndSetNode(bulkMutator, bitpos, subNodeNew);
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
        Node<K> updated(final Nonce bulkMutator, final K key,
                        final int keyHash, final int shift, final ChangeEvent changeEvent) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap() & bitpos) != 0) { // inplace value
                final int dataIndex = dataIndex(bitpos);
                final K currentKey = getKey(dataIndex);

                if (Objects.equals(currentKey, key)) {
                    return this;
                } else {
                    final Node<K> subNodeNew = mergeTwoKeyValPairs(bulkMutator, currentKey,
                            currentKey.hashCode(), key, keyHash, shift + BIT_PARTITION_SIZE);

                    changeEvent.isModified = true;
                    return copyAndMigrateFromInlineToNode(bulkMutator, bitpos, subNodeNew);
                }
            } else if ((nodeMap() & bitpos) != 0) { // node (not value)
                final Node<K> subNode = nodeAt(bitpos);
                final Node<K> subNodeNew =
                        subNode.updated(bulkMutator, key, keyHash, shift + BIT_PARTITION_SIZE, changeEvent);

                if (changeEvent.isModified) {
                    /*
                     * NOTE: subNode and subNodeNew may be referential equal if updated transiently in-place.
                     * Therefore diffing nodes is not an option. Changes to content and meta-data need to be
                     * explicitly tracked and passed when descending from recursion (i.e., {@code details}).
                     */
                    return copyAndSetNode(bulkMutator, bitpos, subNodeNew);
                } else {
                    return this;
                }
            } else {
                // no value
                changeEvent.isModified = true;
                return copyAndInsertValue(bulkMutator, bitpos, key);
            }
        }
    }

    private static final class HashCollisionNode<K> extends Node<K> {
        private final int hash;
        private @NonNull K[] keys;

        HashCollisionNode(Nonce bulkMutator, final int hash, final K[] keys) {
            super(bulkMutator);
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
        public boolean equivalent(final @NonNull Node other) {
            if (this == other) {
                return true;
            }

            HashCollisionNode<?> that = (HashCollisionNode<?>) other;
            if (hash != that.hash
                    || payloadArity() != that.payloadArity()) {
                return false;
            }

            // Linear scan for each key, because of arbitrary element order.
            outerLoop:
            for (int i = 0, n = that.payloadArity(); i < n; i++) {
                final Object otherKey = that.getKey(i);

                for (final K key : keys) {
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
        int payloadArity() {
            return keys.length;
        }

        @Override
        Node<K> removed(final Nonce bulkMutator, final K key,
                        final int keyHash, final int shift, final ChangeEvent changeEvent) {
            for (int idx = 0; idx < keys.length; idx++) {
                if (Objects.equals(keys[idx], key)) {
                    changeEvent.isModified = true;

                    if (payloadArity() == 1) {
                        return emptyNode();
                    } else if (payloadArity() == 2) {
                        // Create root node with singleton element. This node will be a) either be the new root
                        // returned, or b) unwrapped and inlined.
                        final K theOtherKey = (idx == 0) ? keys[1] : keys[0];

                        return PersistentTrieSet.<K>emptyNode().updated(bulkMutator, theOtherKey, keyHash, 0,
                                new ChangeEvent());
                    } else {
                        // copy 'this.keys' and remove 1 element(s) at position 'idx'
                        @SuppressWarnings("unchecked") final K[] keysNew = (K[]) new Object[this.keys.length - 1];
                        System.arraycopy(this.keys, 0, keysNew, 0, idx);
                        System.arraycopy(this.keys, idx + 1, keysNew, idx, this.keys.length - idx - 1);
                        if (isAllowedToEdit(this.bulkMutator, bulkMutator)) {
                            this.keys = keysNew;
                        } else {
                            return new HashCollisionNode<>(bulkMutator, keyHash, keysNew);
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
        public Node<K> updated(final Nonce bulkMutator, final K key,
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
            if (isAllowedToEdit(this.bulkMutator, bulkMutator)) {
                this.keys = keysNew;
                return this;
            }
            return new HashCollisionNode<>(bulkMutator, keyHash, keysNew);
        }
    }

    /**
     * Iterator skeleton that uses a fixed stack in depth.
     */
    private static class TrieIterator<K> implements Iterator<K> {

        private static final int MAX_DEPTH = 7;
        private final int[] nodeCursorsAndLengths = new int[MAX_DEPTH * 2];
        protected int currentValueCursor;
        protected int currentValueLength;
        protected Node<K> currentValueNode;
        @SuppressWarnings("unchecked")
        Node<K>[] nodes = new Node[MAX_DEPTH];
        private int currentStackLevel = -1;

        TrieIterator(Node<K> rootNode) {
            if (rootNode.hasNodes()) {
                currentStackLevel = 0;

                nodes[0] = rootNode;
                nodeCursorsAndLengths[0] = 0;
                nodeCursorsAndLengths[1] = rootNode.nodeArity();
            }

            if (rootNode.hasPayload()) {
                currentValueNode = rootNode;
                currentValueCursor = 0;
                currentValueLength = rootNode.payloadArity();
            }
        }

        public boolean hasNext() {
            if (currentValueCursor < currentValueLength) {
                return true;
            } else {
                return searchNextValueNode();
            }
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else {
                return currentValueNode.getKey(currentValueCursor++);
            }
        }

        private boolean searchNextValueNode() {
            while (currentStackLevel >= 0) {
                final int currentCursorIndex = currentStackLevel * 2;
                final int currentLengthIndex = currentCursorIndex + 1;

                final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
                final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];

                if (nodeCursor < nodeLength) {
                    final Node<K> nextNode = nodes[currentStackLevel].getNode(nodeCursor);
                    nodeCursorsAndLengths[currentCursorIndex]++;

                    if (nextNode.hasNodes()) {
                        // put node on next stack level for depth-first traversal
                        final int nextStackLevel = ++currentStackLevel;
                        final int nextCursorIndex = nextStackLevel * 2;
                        final int nextLengthIndex = nextCursorIndex + 1;

                        nodes[nextStackLevel] = nextNode;
                        nodeCursorsAndLengths[nextCursorIndex] = 0;
                        nodeCursorsAndLengths[nextLengthIndex] = nextNode.nodeArity();
                    }

                    if (nextNode.hasPayload()) {
                        currentValueNode = nextNode;
                        currentValueCursor = 0;
                        currentValueLength = nextNode.payloadArity();
                        return true;
                    }
                } else {
                    currentStackLevel--;
                }
            }

            return false;
        }
    }

    private static class TransientTrieSet<K> {
        private Nonce bulkMutator;
        protected Node<K> root;
        protected int hashCode;
        protected int size;

        TransientTrieSet(PersistentTrieSet<K> trieSet) {
            this.root = trieSet.root;
            this.hashCode = trieSet.hashCode;
            this.size = trieSet.size;
            this.bulkMutator = new Nonce();
        }

        public boolean add(final K key) {
            final int keyHash = key.hashCode();
            final ChangeEvent changeEvent = new ChangeEvent();
            final Node<K> newRootNode = root.updated(this.bulkMutator, key, keyHash, 0, changeEvent);
            if (changeEvent.isModified) {
                root = newRootNode;
                hashCode += keyHash;
                size += 1;
                return true;
            }
            return false;
        }

        public boolean remove(final K key) {
            final int keyHash = key.hashCode();
            final ChangeEvent changeEvent = new ChangeEvent();
            final Node<K> newRootNode = root.removed(this.bulkMutator, key, keyHash, 0, changeEvent);
            if (changeEvent.isModified) {
                root = newRootNode;
                hashCode = hashCode - keyHash;
                size = size - 1;
                return true;
            }
            return false;
        }

        public PersistentTrieSet<K> freeze() {
            bulkMutator = new Nonce();
            return size == 0 ? PersistentTrieSet.of() : new PersistentTrieSet<>(root, hashCode, size);
        }
    }

    private static class ChangeEvent {
        private boolean isModified;
    }
}
