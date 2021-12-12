/*
 * @(#)PersistentTrieSet.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Arrays;
import java.util.Objects;


/**
 * An add-only persistent set implemented with a Compressed Hash-Array Mapped
 * Prefix-tree (CHAMP).
 * <p>
 * References:
 * <dl>
 *     <dt>This class has been derived from "The Capsule Hash Trie Collections Library".</dt>
 *     <dd>Copyright (c) Michael Steindorfer, Centrum Wiskunde & Informatica, and Contributors.
 *         BSD 2-Clause License.
 *         <a href="https://github.com/usethesource/capsule">github.com</a>.</dd>
 * </dl>
 *
 * @param <E> the element type
 */
public class AddOnlyPersistentTrieSet<E> implements AddOnlyPersistentSet<E> {

    private static final @NonNull Node<?> EMPTY_NODE = new BitmapIndexedNode<>(0, 0);
    private static final @NonNull AddOnlyPersistentTrieSet<?> EMPTY_SET = new AddOnlyPersistentTrieSet<>(EMPTY_NODE);
    private final @NonNull Node<E> root;

    private AddOnlyPersistentTrieSet(@NonNull Node<E> root) {
        this.root = root;
    }

    @SuppressWarnings("unchecked")
    public static <K> @NonNull AddOnlyPersistentTrieSet<K> of() {
        return (AddOnlyPersistentTrieSet<K>) AddOnlyPersistentTrieSet.EMPTY_SET;
    }

    public static <K> @NonNull AddOnlyPersistentTrieSet<K> of(@NonNull K key0) {
        final int keyHash0 = key0.hashCode();
        final int dataMap = Node.bitpos(Node.mask(keyHash0, 0));
        final Node<K> newRootNode = new BitmapIndexedNode<>(0, dataMap, key0);
        return new AddOnlyPersistentTrieSet<>(newRootNode);
    }

    @Override
    public @NonNull AddOnlyPersistentTrieSet<E> copyAdd(final @NonNull E key) {
        final Node<E> newRootNode = root.updated(key, key.hashCode(), 0);
        return newRootNode != root ? new AddOnlyPersistentTrieSet<>(newRootNode) : this;
    }

    protected static abstract class Node<K> {
        static final int TUPLE_LENGTH = 1;
        static final int HASH_CODE_LENGTH = 32;
        static final int BIT_PARTITION_SIZE = 5;
        static final int BIT_PARTITION_MASK = 0b11111;

        static int bitpos(final int mask) {
            return 1 << mask;
        }

        static int mask(final int keyHash, final int shift) {
            return (keyHash >>> shift) & BIT_PARTITION_MASK;
        }

        static <K> @NonNull Node<K> mergeTwoKeyValPairs(final @NonNull K key0, final int keyHash0,
                                                        final @NonNull K key1, final int keyHash1, final int shift) {
            assert !(key0.equals(key1));

            if (shift >= HASH_CODE_LENGTH) {
                return new HashCollisionNode<>(keyHash0, key0, key1);
            }

            final int mask0 = mask(keyHash0, shift);
            final int mask1 = mask(keyHash1, shift);

            if (mask0 != mask1) {
                // both nodes fit on same level
                final int dataMap = bitpos(mask0) | bitpos(mask1);

                if (mask0 < mask1) {
                    return new BitmapIndexedNode<>(0, dataMap, key0, key1);
                } else {
                    return new BitmapIndexedNode<>(0, dataMap, key1, key0);
                }
            } else {
                final Node<K> node =
                        mergeTwoKeyValPairs(key0, keyHash0, key1, keyHash1, shift + BIT_PARTITION_SIZE);
                // values fit on next level
                final int nodeMap = bitpos(mask0);
                return new BitmapIndexedNode<>(nodeMap, 0, node);
            }
        }

        abstract @NonNull Node<K> updated(final @NonNull K key, final int keyHash, final int shift);
    }

    private static final class BitmapIndexedNode<K> extends Node<K> {
        final @NonNull Object[] nodes;
        private final int nodeMap;
        private final int dataMap;

        BitmapIndexedNode(final int nodeMap,
                          final int dataMap, final @NonNull Object... nodes) {
            this.nodeMap = nodeMap;
            this.dataMap = dataMap;
            this.nodes = nodes;
        }

        @NonNull Node<K> copyAndInsertValue(final int bitpos,
                                            final @NonNull K key) {
            final int idx = TUPLE_LENGTH * dataIndex(bitpos);

            // copy 'src' and insert 1 element(s) at position 'idx'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length + 1];
            System.arraycopy(src, 0, dst, 0, idx);
            System.arraycopy(src, idx, dst, idx + 1, src.length - idx);
            dst[idx] = key;

            return new BitmapIndexedNode<>(nodeMap, dataMap | bitpos, dst);
        }

        @NonNull Node<K> copyAndMigrateFromInlineToNode(final int bitpos, final @NonNull Node<K> node) {

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

            return new BitmapIndexedNode<>(nodeMap | bitpos, dataMap ^ bitpos, dst);
        }

        @NonNull Node<K> copyAndSetNode(final int bitpos,
                                        final @NonNull Node<K> newNode) {

            final int nodeIndex = nodeIndex(bitpos);
            final int idx = this.nodes.length - 1 - nodeIndex;

            // copy 'src' and set 1 element(s) at position 'idx'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length];
            System.arraycopy(src, 0, dst, 0, src.length);
            dst[idx] = newNode;

            return new BitmapIndexedNode<>(nodeMap, dataMap, dst);
        }

        int dataIndex(final int bitpos) {
            return Integer.bitCount(dataMap & (bitpos - 1));
        }

        @SuppressWarnings("unchecked")
        @NonNull K getKey(final int index) {
            return (K) nodes[TUPLE_LENGTH * index];
        }

        @SuppressWarnings("unchecked")
        @NonNull Node<K> getNode(final int index) {
            return (Node<K>) nodes[nodes.length - 1 - index];
        }

        @NonNull Node<K> nodeAt(final int bitpos) {
            return getNode(nodeIndex(bitpos));
        }

        int nodeIndex(final int bitpos) {
            return Integer.bitCount(nodeMap & (bitpos - 1));
        }

        @Override
        @NonNull Node<K> updated(final @NonNull K key, final int keyHash,
                                 final int shift) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap & bitpos) != 0) { // in-place value
                final int dataIndex = dataIndex(bitpos);
                final K currentKey = getKey(dataIndex);

                if (Objects.equals(currentKey, key)) {
                    return this;
                } else {
                    final Node<K> subNodeNew = mergeTwoKeyValPairs(currentKey,
                            currentKey.hashCode(), key, keyHash, shift + BIT_PARTITION_SIZE);
                    return copyAndMigrateFromInlineToNode(bitpos, subNodeNew);
                }
            } else if ((nodeMap & bitpos) != 0) { // node (not value)
                final Node<K> subNode = nodeAt(bitpos);
                final Node<K> subNodeNew =
                        subNode.updated(key, keyHash, shift + BIT_PARTITION_SIZE);

                if (subNode != subNodeNew) {
                    return copyAndSetNode(bitpos, subNodeNew);
                } else {
                    return this;
                }
            } else {
                // no value
                return copyAndInsertValue(bitpos, key);
            }
        }

    }

    private static final class HashCollisionNode<K> extends Node<K> {
        private final @NonNull K[] keys;
        private final int hash;

        @SafeVarargs
        HashCollisionNode(final int hash, @NonNull final K... keys) {
            this.keys = keys;
            this.hash = hash;
        }

        @Override
        @NonNull
        Node<K> updated(final @NonNull K key,
                        final int keyHash, final int shift) {
            assert this.hash == keyHash;

            for (K k : keys) {
                if (Objects.equals(k, key)) {
                    return this;
                }
            }

            final K[] keysNew = Arrays.copyOf(keys, keys.length + 1);
            keysNew[keys.length] = key;
            return new HashCollisionNode<>(keyHash, keysNew);
        }
    }
}
