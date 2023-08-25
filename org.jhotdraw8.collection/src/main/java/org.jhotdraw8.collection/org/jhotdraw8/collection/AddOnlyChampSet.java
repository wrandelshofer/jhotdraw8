/*
 * @(#)AddOnlyChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableAddOnlySet;

import java.util.Arrays;
import java.util.Objects;

/**
 * Implements the {@link ImmutableAddOnlySet} interface using a Compressed
 * Hash-Array Mapped Prefix-tree (CHAMP).
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <E> the element type
 */
public abstract class AddOnlyChampSet<E> implements ImmutableAddOnlySet<E> {
    private static final int ENTRY_LENGTH = 1;
    private static final int HASH_CODE_LENGTH = 32;
    private static final int BIT_PARTITION_SIZE = 4;
    private static final int BIT_PARTITION_MASK = (1 << BIT_PARTITION_SIZE) - 1;

    /**
     * Constructs a new empty set.
     */
    AddOnlyChampSet() {
    }

    private static char mask(int bitpos) {
        return (char) (1 << bitpos);
    }

    private static int bitpos(int keyHash, int shift) {
        return (keyHash >>> shift) & BIT_PARTITION_MASK;
    }

    private static <K> @NonNull AddOnlyChampSet<K> mergeTwoKeyValPairs(@NonNull K key0, int keyHash0,
                                                                       @NonNull K key1, int keyHash1, int shift) {
        assert !(key0.equals(key1));

        if (shift >= HASH_CODE_LENGTH) {
            HashCollisionNode<K> unchecked = new HashCollisionNode<>(keyHash0, key0, key1);
            return unchecked;
        }

        int mask0 = bitpos(keyHash0, shift);
        int mask1 = bitpos(keyHash1, shift);

        if (mask0 != mask1) {
            // both nodes fit on same level
            final char dataMap = (char) (mask(mask0) | mask(mask1));

            if (mask0 < mask1) {
                return new BitmapIndexedNode<>((char) 0, dataMap, key0, key1);
            } else {
                return new BitmapIndexedNode<>((char) 0, dataMap, key1, key0);
            }
        } else {
            final AddOnlyChampSet<K> node =
                    mergeTwoKeyValPairs(key0, keyHash0, key1, keyHash1, shift + BIT_PARTITION_SIZE);
            // values fit on next level
            final char nodeMap = mask(mask0);
            return new BitmapIndexedNode<>(nodeMap, (char) 0, node);
        }
    }

    /**
     * Returns an empty set.
     *
     * @param <E> the element type.
     * @return an empty set.
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull AddOnlyChampSet<E> of() {
        return (AddOnlyChampSet<E>) BitmapIndexedNode.EMPTY_NODE;
    }

    /**
     * Returns a set that contains the specified elements.
     *
     * @param elements the specified elements
     * @param <E>      the element type.
     * @return a set of the specified elements.
     */
    @SuppressWarnings({"unchecked", "varargs"})
    @SafeVarargs
    public static <E> @NonNull AddOnlyChampSet<E> of(E @NonNull ... elements) {
        AddOnlyChampSet<E> set = (AddOnlyChampSet<E>) BitmapIndexedNode.EMPTY_NODE;
        for (E e : elements) {
            set = set.add(e);
        }
        return set;
    }

    @Override
    public @NonNull AddOnlyChampSet<E> add(@NonNull E key) {
        return updated(key, key.hashCode(), 0);
    }

    abstract @NonNull AddOnlyChampSet<E> updated(@NonNull E key, int keyHash, int shift);

    private static final class BitmapIndexedNode<K> extends AddOnlyChampSet<K> {
        private static final @NonNull AddOnlyChampSet<?> EMPTY_NODE = new BitmapIndexedNode<>((char) 0, (char) 0);
        @NonNull
        final Object[] nodes;
        /**
         * We use char as an unsigned short.
         */
        private final char nodeMap;
        /**
         * We use char as an unsigned short.
         */
        private final char dataMap;

        BitmapIndexedNode(char nodeMap,
                          char dataMap, @NonNull Object... nodes) {
            this.nodeMap = nodeMap;
            this.dataMap = dataMap;
            this.nodes = nodes;
        }

        @NonNull AddOnlyChampSet<K> copyAndInsertValue(int mask,
                                                       @NonNull K key) {
            final int idx = ENTRY_LENGTH * dataIndex(mask);

            // copy 'src' and insert 1 element(s) at position 'idx'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length + 1];
            System.arraycopy(src, 0, dst, 0, idx);
            System.arraycopy(src, idx, dst, idx + 1, src.length - idx);
            dst[idx] = key;

            return new BitmapIndexedNode<>(nodeMap, (char) (dataMap | mask), dst);
        }

        @NonNull AddOnlyChampSet<K> copyAndMigrateFromInlineToNode(int mask, @NonNull AddOnlyChampSet<K> node) {

            final int idxOld = ENTRY_LENGTH * dataIndex(mask);
            final int idxNew = this.nodes.length - ENTRY_LENGTH - nodeIndex(mask);
            assert idxOld <= idxNew;

            // copy 'src' and remove 1 element(s) at position 'idxOld' and
            // insert 1 element(s) at position 'idxNew'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1 + 1];
            System.arraycopy(src, 0, dst, 0, idxOld);
            System.arraycopy(src, idxOld + 1, dst, idxOld, idxNew - idxOld);
            System.arraycopy(src, idxNew + 1, dst, idxNew + 1, src.length - idxNew - 1);
            dst[idxNew] = node;

            return new BitmapIndexedNode<>((char) (nodeMap | mask), (char) (dataMap ^ mask), dst);
        }

        @NonNull AddOnlyChampSet<K> copyAndSetNode(int mask,
                                                   @NonNull AddOnlyChampSet<K> newNode) {

            final int nodeIndex = nodeIndex(mask);
            final int idx = this.nodes.length - 1 - nodeIndex;

            // copy 'src' and set 1 element(s) at position 'idx'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length];
            System.arraycopy(src, 0, dst, 0, src.length);
            dst[idx] = newNode;

            return new BitmapIndexedNode<>(nodeMap, dataMap, dst);
        }

        int dataIndex(int mask) {
            return Integer.bitCount(dataMap & (mask - 1));
        }

        @SuppressWarnings("unchecked")
        @NonNull K getKey(int index) {
            return (K) nodes[ENTRY_LENGTH * index];
        }

        @SuppressWarnings("unchecked")
        @NonNull AddOnlyChampSet<K> getNode(int index) {
            return (AddOnlyChampSet<K>) nodes[nodes.length - 1 - index];
        }

        @NonNull AddOnlyChampSet<K> nodeAt(int mask) {
            return getNode(nodeIndex(mask));
        }

        int nodeIndex(int mask) {
            return Integer.bitCount(nodeMap & (mask - 1));
        }

        @Override
        @NonNull AddOnlyChampSet<K> updated(@NonNull K key, int keyHash,
                                            int shift) {
            final int bitpos = bitpos(keyHash, shift);
            final int mask = mask(bitpos);

            if ((dataMap & mask) != 0) { // in-place value
                final int dataIndex = dataIndex(mask);
                final K currentKey = getKey(dataIndex);

                if (Objects.equals(currentKey, key)) {
                    return this;
                } else {
                    final AddOnlyChampSet<K> subNodeNew = mergeTwoKeyValPairs(currentKey,
                            currentKey.hashCode(), key, keyHash, shift + BIT_PARTITION_SIZE);
                    return copyAndMigrateFromInlineToNode(mask, subNodeNew);
                }
            } else if ((nodeMap & mask) != 0) { // node (not value)
                final AddOnlyChampSet<K> subNode = nodeAt(mask);
                final AddOnlyChampSet<K> subNodeNew =
                        subNode.updated(key, keyHash, shift + BIT_PARTITION_SIZE);

                if (subNode != subNodeNew) {
                    return copyAndSetNode(mask, subNodeNew);
                } else {
                    return this;
                }
            } else {
                // no value
                return copyAndInsertValue(mask, key);
            }
        }

    }

    private static final class HashCollisionNode<K> extends AddOnlyChampSet<K> {
        private final @NonNull K[] keys;
        private final int hash;


        @SuppressWarnings("varargs")
        @SafeVarargs
        HashCollisionNode(int hash, @NonNull final K... keys) {
            this.keys = keys;
            this.hash = hash;
        }

        @Override
        @NonNull
        AddOnlyChampSet<K> updated(@NonNull K key,
                                   int keyHash, int shift) {
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
