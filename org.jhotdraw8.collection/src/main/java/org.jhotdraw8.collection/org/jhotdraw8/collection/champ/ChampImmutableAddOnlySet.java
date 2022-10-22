/*
 * @(#)ImmutableAddOnlyChampSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableAddOnlySet;

import java.util.Arrays;
import java.util.Objects;

/**
 * An immutable CHAMP set that only provides a {@link ImmutableAddOnlySet#add}
 * method.
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
 *
 * @param <E> the element type
 */
public abstract class ChampImmutableAddOnlySet<E> implements ImmutableAddOnlySet<E> {
    private static final int ENTRY_LENGTH = 1;
    private static final int HASH_CODE_LENGTH = 32;
    private static final int BIT_PARTITION_SIZE = 4;
    private static final int BIT_PARTITION_MASK = (1 << BIT_PARTITION_SIZE) - 1;

    /**
     * Creates a new empty set.
     */
    ChampImmutableAddOnlySet() {
    }

    private static char bitpos(int mask) {
        return (char) (1 << mask);
    }

    private static int mask(int keyHash, int shift) {
        return (keyHash >>> shift) & BIT_PARTITION_MASK;
    }

    private static <K> @NonNull ChampImmutableAddOnlySet<K> mergeTwoKeyValPairs(@NonNull K key0, int keyHash0,
                                                                                @NonNull K key1, int keyHash1, int shift) {
        assert !(key0.equals(key1));

        if (shift >= HASH_CODE_LENGTH) {
            @SuppressWarnings({"unchecked"})
            HashCollisionNode<K> unchecked = new HashCollisionNode<>(keyHash0, key0, key1);
            return unchecked;
        }

        int mask0 = mask(keyHash0, shift);
        int mask1 = mask(keyHash1, shift);

        if (mask0 != mask1) {
            // both nodes fit on same level
            final char dataMap = (char) (bitpos(mask0) | bitpos(mask1));

            if (mask0 < mask1) {
                return new BitmapIndexedNode<>((char) 0, dataMap, key0, key1);
            } else {
                return new BitmapIndexedNode<>((char) 0, dataMap, key1, key0);
            }
        } else {
            final ChampImmutableAddOnlySet<K> node =
                    mergeTwoKeyValPairs(key0, keyHash0, key1, keyHash1, shift + BIT_PARTITION_SIZE);
            // values fit on next level
            final char nodeMap = bitpos(mask0);
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
    public static <E> @NonNull ChampImmutableAddOnlySet<E> of() {
        return (ChampImmutableAddOnlySet<E>) BitmapIndexedNode.EMPTY_NODE;
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
    public static <E> @NonNull ChampImmutableAddOnlySet<E> of(E @NonNull ... elements) {
        ChampImmutableAddOnlySet<E> set = (ChampImmutableAddOnlySet<E>) BitmapIndexedNode.EMPTY_NODE;
        for (E e : elements) {
            set = set.add(e);
        }
        return set;
    }

    @Override
    public @NonNull ChampImmutableAddOnlySet<E> add(@NonNull E key) {
        return updated(key, key.hashCode(), 0);
    }

    abstract @NonNull ChampImmutableAddOnlySet<E> updated(@NonNull E key, int keyHash, int shift);

    private static final class BitmapIndexedNode<K> extends ChampImmutableAddOnlySet<K> {
        private static final @NonNull ChampImmutableAddOnlySet<?> EMPTY_NODE = new BitmapIndexedNode<>((char) 0, (char) 0);
        @NonNull Object[] nodes;
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

        @NonNull ChampImmutableAddOnlySet<K> copyAndInsertValue(int bitpos,
                                                                @NonNull K key) {
            final int idx = ENTRY_LENGTH * dataIndex(bitpos);

            // copy 'src' and insert 1 element(s) at position 'idx'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length + 1];
            System.arraycopy(src, 0, dst, 0, idx);
            System.arraycopy(src, idx, dst, idx + 1, src.length - idx);
            dst[idx] = key;

            return new BitmapIndexedNode<>(nodeMap, (char) (dataMap | bitpos), dst);
        }

        @NonNull ChampImmutableAddOnlySet<K> copyAndMigrateFromInlineToNode(int bitpos, @NonNull ChampImmutableAddOnlySet<K> node) {

            final int idxOld = ENTRY_LENGTH * dataIndex(bitpos);
            final int idxNew = this.nodes.length - ENTRY_LENGTH - nodeIndex(bitpos);
            assert idxOld <= idxNew;

            // copy 'src' and remove 1 element(s) at position 'idxOld' and
            // insert 1 element(s) at position 'idxNew'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1 + 1];
            System.arraycopy(src, 0, dst, 0, idxOld);
            System.arraycopy(src, idxOld + 1, dst, idxOld, idxNew - idxOld);
            System.arraycopy(src, idxNew + 1, dst, idxNew + 1, src.length - idxNew - 1);
            dst[idxNew] = node;

            return new BitmapIndexedNode<>((char) (nodeMap | bitpos), (char) (dataMap ^ bitpos), dst);
        }

        @NonNull ChampImmutableAddOnlySet<K> copyAndSetNode(int bitpos,
                                                            @NonNull ChampImmutableAddOnlySet<K> newNode) {

            final int nodeIndex = nodeIndex(bitpos);
            final int idx = this.nodes.length - 1 - nodeIndex;

            // copy 'src' and set 1 element(s) at position 'idx'
            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length];
            System.arraycopy(src, 0, dst, 0, src.length);
            dst[idx] = newNode;

            return new BitmapIndexedNode<>(nodeMap, dataMap, dst);
        }

        int dataIndex(int bitpos) {
            return Integer.bitCount(dataMap & (bitpos - 1));
        }

        @SuppressWarnings("unchecked")
        @NonNull K getKey(int index) {
            return (K) nodes[ENTRY_LENGTH * index];
        }

        @SuppressWarnings("unchecked")
        @NonNull ChampImmutableAddOnlySet<K> getNode(int index) {
            return (ChampImmutableAddOnlySet<K>) nodes[nodes.length - 1 - index];
        }

        @NonNull ChampImmutableAddOnlySet<K> nodeAt(int bitpos) {
            return getNode(nodeIndex(bitpos));
        }

        int nodeIndex(int bitpos) {
            return Integer.bitCount(nodeMap & (bitpos - 1));
        }

        @Override
        @NonNull ChampImmutableAddOnlySet<K> updated(@NonNull K key, int keyHash,
                                                     int shift) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap & bitpos) != 0) { // in-place value
                final int dataIndex = dataIndex(bitpos);
                final K currentKey = getKey(dataIndex);

                if (Objects.equals(currentKey, key)) {
                    return this;
                } else {
                    final ChampImmutableAddOnlySet<K> subNodeNew = mergeTwoKeyValPairs(currentKey,
                            currentKey.hashCode(), key, keyHash, shift + BIT_PARTITION_SIZE);
                    return copyAndMigrateFromInlineToNode(bitpos, subNodeNew);
                }
            } else if ((nodeMap & bitpos) != 0) { // node (not value)
                final ChampImmutableAddOnlySet<K> subNode = nodeAt(bitpos);
                final ChampImmutableAddOnlySet<K> subNodeNew =
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

    private static final class HashCollisionNode<K> extends ChampImmutableAddOnlySet<K> {
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
        ChampImmutableAddOnlySet<K> updated(@NonNull K key,
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
