/*
 * @(#)PersistentTrieSet.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.collection.capsule.core.trie.SetNodeResultImpl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * References:
 * <dl>
 *     <dt>Michael Steindorfer, capsule</dt>
 *     <dd>Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 *   All rights reserved.
 *  This file is licensed under the BSD 2-Clause License, which accompanies this project
 *  and is available under <a href="https://opensource.org/licenses/BSD-2-Clause">BSD-2</a>.</dd>
 * </dl>
 */
public class PersistentTrieSet<K> implements PersistentSet<K> {

    private static final CompactSetNode<?> EMPTY_NODE = new BitmapIndexedSetNode<>(null,
            0, 0, new Object[]{});

    private static final PersistentTrieSet<?> EMPTY_SET = new PersistentTrieSet<>(EMPTY_NODE, 0, 0);

    private final AbstractSetNode<K> rootNode;
    private final int cachedHashCode;
    private final int cachedSize;

    PersistentTrieSet(AbstractSetNode<K> rootNode, int cachedHashCode, int cachedSize) {
        this.rootNode = rootNode;
        this.cachedHashCode = cachedHashCode;
        this.cachedSize = cachedSize;
    }

    @SuppressWarnings("unchecked")
    public static <K> PersistentTrieSet<K> of() {
        return (PersistentTrieSet<K>) PersistentTrieSet.EMPTY_SET;
    }

    public static <K> PersistentTrieSet<K> of(K key0) {
        final int keyHash0 = key0.hashCode();

        final int dataMap = CompactSetNode.bitpos(CompactSetNode.mask(keyHash0, 0));

        final CompactSetNode<K> newRootNode = CompactSetNode.nodeOf(null, dataMap, key0, keyHash0);

        return new PersistentTrieSet<>(newRootNode, keyHash0, 1);
    }

    public static <K> PersistentTrieSet<K> of(K key0, K key1) {
        assert !Objects.equals(key0, key1);

        final int keyHash0 = key0.hashCode();
        final int keyHash1 = key1.hashCode();

        CompactSetNode<K> newRootNode =
                CompactSetNode.mergeTwoKeyValPairs(key0, keyHash0, key1, keyHash1, 0);

        return new PersistentTrieSet<>(newRootNode, keyHash0 + keyHash1, 2);
    }

    public static <K> PersistentTrieSet<K> of(K... keys) {
        @SuppressWarnings("unchecked")
        TransientTrieSet<K> tr = new TransientTrieSet<K>((PersistentTrieSet<K>) PersistentTrieSet.EMPTY_SET);
        for (final K key : keys) {
            tr.__insert(key);
        }
        return tr.freeze();
    }


    @Override
    public boolean contains(final Object o) {
        try {
            @SuppressWarnings("unchecked") final K key = (K) o;
            return rootNode.contains(key, key.hashCode(), 0);
        } catch (ClassCastException unused) {
            return false;
        }
    }


    public PersistentTrieSet<K> withAdd(final K key) {
        final int keyHash = key.hashCode();
        final SetNodeResultImpl<K> details = new SetNodeResultImpl<>();

        final AbstractSetNode<K> newRootNode = rootNode.updated(null, key,
                keyHash, 0, details);

        if (details.isModified()) {
            return new PersistentTrieSet<>(newRootNode, cachedHashCode + keyHash, cachedSize + 1);
        }

        return this;
    }

    public PersistentTrieSet<K> withAddAll(final Iterable<? extends K> set) {
        final TransientTrieSet<K> tmpTransient = (TransientTrieSet<K>) this.asTransient();
        boolean modified = false;

        for (final K key : set) {
            modified |= tmpTransient.__insert(key);
        }

        return modified ? (PersistentTrieSet<K>) tmpTransient.freeze() : this;
    }

    public PersistentTrieSet<K> withRemove(final K key) {
        final int keyHash = key.hashCode();
        final SetNodeResultImpl<K> details = new SetNodeResultImpl<>();

        final AbstractSetNode<K> newRootNode = rootNode.removed(null, key,
                keyHash, 0, details);

        if (details.isModified()) {
            return new PersistentTrieSet<>(newRootNode, cachedHashCode - keyHash, cachedSize - 1);
        }

        return this;
    }

    public PersistentTrieSet<K> withRemoveAll(final java.util.Collection<? extends K> set) {
        final TransientTrieSet<K> tmpTransient = (TransientTrieSet<K>) this.asTransient();
        boolean modified = false;
        for (final K key : set) {
            modified |= tmpTransient.__remove(key);
        }

        return modified ? (PersistentTrieSet<K>) tmpTransient.freeze() : this;
    }

    @SuppressWarnings("unchecked")
    public PersistentTrieSet<K> withRetainAll(final java.util.Collection<? extends K> set) {
        final TransientTrieSet<K> tmpTransient = this.asTransient();
        boolean modified = false;

        for (K key : this) {
            if (!set.contains(key)) {
                tmpTransient.__remove(key);
                modified = true;
            }
        }

        return modified ? (PersistentTrieSet<K>) tmpTransient.freeze() : this;
    }

    @Override
    public int size() {
        return cachedSize;
    }

    @Override
    public boolean isEmpty() {
        return cachedSize == 0;
    }

    @Override
    public Iterator<K> iterator() {
        return keyIterator();
    }

    public Iterator<K> keyIterator() {
        return new SetKeyIterator<>(rootNode);
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

            if (this.cachedSize != that.cachedSize) {
                return false;
            }

            if (this.cachedHashCode != that.cachedHashCode) {
                return false;
            }

            return rootNode.equals(that.rootNode);
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
        return cachedHashCode;
    }

    @Override
    public String toString() {
        String body = stream().map(k -> k.toString()).reduce((o1, o2) -> String.join(", ", o1, o2))
                .orElse("");
        return String.format("{%s}", body);
    }

    private TransientTrieSet<K> asTransient() {
        return new TransientTrieSet<>(this);
    }

    protected static abstract class AbstractSetNode<K> implements
            SetNodeX<K, AbstractSetNode<K>>, Iterable<K>,
            java.io.Serializable {

        private static final long serialVersionUID = 42L;

        static final int TUPLE_LENGTH = 1;

        static <T> boolean isAllowedToEdit(AtomicReference<?> x, AtomicReference<?> y) {
            return x != null && y != null && (x == y || x.get() == y.get());
        }


        abstract boolean hasNodes();

        abstract int nodeArity();

        abstract AbstractSetNode<K> getNode(final int index);

        @Deprecated
        Iterator<? extends AbstractSetNode<K>> nodeIterator() {
            return new Iterator<>() {

                int nextIndex = 0;
                final int nodeArity = AbstractSetNode.this.nodeArity();

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public AbstractSetNode<K> next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return AbstractSetNode.this.getNode(nextIndex++);
                }

                @Override
                public boolean hasNext() {
                    return nextIndex < nodeArity;
                }
            };
        }


        @Override
        public Iterator<K> iterator() {
            return new SetKeyIterator<>(this);
        }

        @Override
        public Spliterator<K> spliterator() {
            return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.DISTINCT);
        }

        public Stream<K> stream() {
            return StreamSupport.stream(spliterator(), false);
        }

    }

    protected static abstract class CompactSetNode<K> extends AbstractSetNode<K> {

        static final int HASH_CODE_LENGTH = 32;

        static final int BIT_PARTITION_SIZE = 5;
        static final int BIT_PARTITION_MASK = 0b11111;

        static int mask(final int keyHash, final int shift) {
            return (keyHash >>> shift) & BIT_PARTITION_MASK;
        }

        static int bitpos(final int mask) {
            return 1 << mask;
        }

        abstract int nodeMap();

        abstract int dataMap();

        @Override
        abstract CompactSetNode<K> getNode(final int index);

        abstract CompactSetNode<K> copyAndRemoveValue(final AtomicReference<Thread> mutator,
                                                      final int bitpos);

        abstract CompactSetNode<K> copyAndSetNode(final AtomicReference<Thread> mutator,
                                                  final int bitpos, final AbstractSetNode<K> node);

        abstract CompactSetNode<K> copyAndMigrateFromInlineToNode(final AtomicReference<Thread> mutator,
                                                                  final int bitpos, final AbstractSetNode<K> node);

        abstract CompactSetNode<K> copyAndMigrateFromNodeToInline(final AtomicReference<Thread> mutator,
                                                                  final int bitpos, final AbstractSetNode<K> node);

        static <K> CompactSetNode<K> mergeTwoKeyValPairs(final K key0, final int keyHash0,
                                                         final K key1, final int keyHash1, final int shift) {
            assert !(key0.equals(key1));

            if (shift >= HASH_CODE_LENGTH) {
                // throw new
                // IllegalStateException("Hash collision not yet fixed.");
                //noinspection unchecked
                return new HashCollisionSetNode<>(keyHash0, (K[]) new Object[]{key0, key1});
            }

            final int mask0 = mask(keyHash0, shift);
            final int mask1 = mask(keyHash1, shift);

            if (mask0 != mask1) {
                // both nodes fit on same level
                final int dataMap = bitpos(mask0) | bitpos(mask1);

                if (mask0 < mask1) {
                    return nodeOf(null, dataMap, key0, keyHash0, key1, keyHash1);
                } else {
                    return nodeOf(null, dataMap, key1, keyHash1, key0, keyHash0);
                }
            } else {
                final CompactSetNode<K> node =
                        mergeTwoKeyValPairs(key0, keyHash0, key1, keyHash1, shift + BIT_PARTITION_SIZE);
                // values fit on next level

                final int nodeMap = bitpos(mask0);
                return nodeOf(null, nodeMap, node);
            }
        }

        static <K> CompactSetNode<K> nodeOf(final AtomicReference<Thread> mutator,
                                            final int nodeMap, final int dataMap, final Object[] nodes) {
            return new BitmapIndexedSetNode<>(mutator, nodeMap, dataMap, nodes);
        }

        @SuppressWarnings("unchecked")
        static <K> CompactSetNode<K> nodeOf(AtomicReference<Thread> mutator) {
            return (CompactSetNode<K>) EMPTY_NODE;
        }

        static <K> CompactSetNode<K> nodeOf(AtomicReference<Thread> mutator,
                                            final int dataMap, final K key, final int keyHash) {
            return nodeOf(mutator, 0, dataMap, new Object[]{key});
        }

        static <K> CompactSetNode<K> nodeOf(AtomicReference<Thread> mutator, final int dataMap,
                                            final K key0, final int keyHash0, final K key1, final int keyHash1) {
            return nodeOf(mutator, 0, dataMap, new Object[]{key0, key1});
        }

        static <K> CompactSetNode<K> nodeOf(AtomicReference<Thread> mutator,
                                            final int nodeMap, final AbstractSetNode<K> node) {
            return nodeOf(mutator, nodeMap, 0, new Object[]{node});
        }

        static int index(final int bitmap, final int bitpos) {
            return Integer.bitCount(bitmap & (bitpos - 1));
        }

        static int index(final int bitmap, final int mask, final int bitpos) {
            return (bitmap == -1) ? mask : index(bitmap, bitpos);
        }

        int dataIndex(final int bitpos) {
            return Integer.bitCount(dataMap() & (bitpos - 1));
        }

        int nodeIndex(final int bitpos) {
            return Integer.bitCount(nodeMap() & (bitpos - 1));
        }

        CompactSetNode<K> nodeAt(final int bitpos) {
            return getNode(nodeIndex(bitpos));
        }

        @Override
        public boolean contains(final K key, final int keyHash, final int shift) {
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


        @Override
        public AbstractSetNode<K> updated(final AtomicReference<Thread> mutator, final K key,
                                          final int keyHash, final int shift, final SetNodeResultImpl<K> details) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap() & bitpos) != 0) { // inplace value
                final int dataIndex = dataIndex(bitpos);
                final K currentKey = getKey(dataIndex);

                if (Objects.equals(currentKey, key)) {
                    return this;
                } else {
                    final AbstractSetNode<K> subNodeNew = mergeTwoKeyValPairs(currentKey,
                            currentKey.hashCode(), key, keyHash, shift + BIT_PARTITION_SIZE);

                    details.modified();
                    details.updateDeltaSize(1);
                    details.updateDeltaHashCode(keyHash);
                    return copyAndMigrateFromInlineToNode(mutator, bitpos, subNodeNew);
                }
            } else if ((nodeMap() & bitpos) != 0) { // node (not value)
                final AbstractSetNode<K> subNode = nodeAt(bitpos);
                final AbstractSetNode<K> subNodeNew =
                        subNode.updated(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details);

                if (details.isModified()) {
                    /*
                     * NOTE: subNode and subNodeNew may be referential equal if updated transiently in-place.
                     * Therefore diffing nodes is not an option. Changes to content and meta-data need to be
                     * explicitly tracked and passed when descending from recursion (i.e., {@code details}).
                     */
                    return copyAndSetNode(mutator, bitpos, subNodeNew);
                } else {
                    return this;
                }
            } else {
                // no value
                details.modified();
                details.updateDeltaSize(1);
                details.updateDeltaHashCode(keyHash);
                return copyAndInsertValue(mutator, bitpos, key);
            }
        }

        abstract CompactSetNode<K> copyAndInsertValue(final AtomicReference<Thread> mutator,
                                                      final int bitpos, final K key);


        @Override
        public AbstractSetNode<K> removed(final AtomicReference<Thread> mutator, final K key, final int keyHash,
                                          final int shift, final SetNodeResultImpl<K> details) {
            final int mask = mask(keyHash, shift);
            final int bitpos = bitpos(mask);

            if ((dataMap() & bitpos) != 0) { // inplace value
                final int dataIndex = dataIndex(bitpos);

                if (Objects.equals(getKey(dataIndex), key)) {
                    details.modified();
                    details.updateDeltaSize(-1);
                    details.updateDeltaHashCode(-keyHash);

                    if (this.payloadArity() == 2 && this.nodeArity() == 0) {
                        /*
                         * Create new node with remaining pair. The new node will a) either become the new root
                         * returned, or b) unwrapped and inlined during returning.
                         */
                        final int newDataMap =
                                (shift == 0) ? (int) (dataMap() ^ bitpos) : bitpos(mask(keyHash, 0));

                        if (dataIndex == 0) {
                            return CompactSetNode.<K>nodeOf(mutator, newDataMap, getKey(1), getKeyHash(1));
                        } else {
                            return CompactSetNode.<K>nodeOf(mutator, newDataMap, getKey(0), getKeyHash(0));
                        }
                    } else {
                        return copyAndRemoveValue(mutator, bitpos);
                    }
                } else {
                    return this;
                }
            } else if ((nodeMap() & bitpos) != 0) { // node (not value)
                final AbstractSetNode<K> subNode = nodeAt(bitpos);
                final AbstractSetNode<K> subNodeNew =
                        subNode.removed(mutator, key, keyHash, shift + BIT_PARTITION_SIZE, details);

                if (!details.isModified()) {
                    return this;
                }

                switch (subNodeNew.sizePredicate()) {
                case 0: {
                    throw new IllegalStateException("Sub-node must have at least one element.");
                }
                case 1: {
                    if (this.payloadArity() == 0 && this.nodeArity() == 1) {
                        // escalate (singleton or empty) result
                        return subNodeNew;
                    } else {
                        // inline value (move to front)
                        return copyAndMigrateFromNodeToInline(mutator, bitpos, subNodeNew);
                    }
                }
                default: {
                    // modify current node (set replacement node)
                    return copyAndSetNode(mutator, bitpos, subNodeNew);
                }
                }
            }

            return this;
        }

    }

    protected static abstract class CompactMixedSetNode<K> extends CompactSetNode<K> {

        private final int nodeMap;
        private final int dataMap;

        CompactMixedSetNode(final AtomicReference<Thread> mutator, final int nodeMap,
                            final int dataMap) {
            this.nodeMap = nodeMap;
            this.dataMap = dataMap;
        }

        @Override
        final int nodeMap() {
            return nodeMap;
        }

        @Override
        final int dataMap() {
            return dataMap;
        }

    }

    private static final class BitmapIndexedSetNode<K> extends CompactMixedSetNode<K> {

        transient final AtomicReference<Thread> mutator;
        final Object[] nodes;

        private BitmapIndexedSetNode(final AtomicReference<Thread> mutator, final int nodeMap,
                                     final int dataMap, final Object[] nodes) {
            super(mutator, nodeMap, dataMap);

            this.mutator = mutator;
            this.nodes = nodes;
        }


        @SuppressWarnings("unchecked")
        @Override
        public K getKey(final int index) {
            return (K) nodes[TUPLE_LENGTH * index];
        }

        @Override
        public int getKeyHash(int index) {
            return getKey(index).hashCode();
        }

        @SuppressWarnings("unchecked")
        @Override
        CompactSetNode<K> getNode(final int index) {
            return (CompactSetNode<K>) nodes[nodes.length - 1 - index];
        }


        @Override
        public boolean hasPayload() {
            return dataMap() != 0;
        }

        @Override
        public int payloadArity() {
            return Integer.bitCount(dataMap());
        }

        @Override
        boolean hasNodes() {
            return nodeMap() != 0;
        }

        @Override
        int nodeArity() {
            return Integer.bitCount(nodeMap());
        }

        @Override
        public byte sizePredicate() {
            if (this.nodeArity() == 0) {
                switch (this.payloadArity()) {
                case 0:
                    return SIZE_EMPTY;
                case 1:
                    return SIZE_ONE;
                default:
                    return SIZE_MORE_THAN_ONE;
                }
            } else {
                return SIZE_MORE_THAN_ONE;
            }
        }


        @Override
        CompactSetNode<K> copyAndSetNode(final AtomicReference<Thread> mutator, final int bitpos,
                                         final AbstractSetNode<K> newNode) {

            final int nodeIndex = nodeIndex(bitpos);
            final int idx = this.nodes.length - 1 - nodeIndex;

            if (isAllowedToEdit(this.mutator, mutator)) {
                // no copying if already editable
                this.nodes[idx] = newNode;
                return this;
            } else {
                final Object[] src = this.nodes;
                final Object[] dst = new Object[src.length];

                // copy 'src' and set 1 element(s) at position 'idx'
                System.arraycopy(src, 0, dst, 0, src.length);
                dst[idx] = newNode;

                return nodeOf(mutator, nodeMap(), dataMap(), dst);
            }
        }

        @Override
        CompactSetNode<K> copyAndInsertValue(final AtomicReference<Thread> mutator, final int bitpos,
                                             final K key) {
            final int idx = TUPLE_LENGTH * dataIndex(bitpos);

            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length + 1];

            // copy 'src' and insert 1 element(s) at position 'idx'
            System.arraycopy(src, 0, dst, 0, idx);
            dst[idx] = key;
            System.arraycopy(src, idx, dst, idx + 1, src.length - idx);

            return nodeOf(mutator, nodeMap(), dataMap() | bitpos, dst);
        }

        @Override
        CompactSetNode<K> copyAndRemoveValue(final AtomicReference<Thread> mutator, final int bitpos) {
            final int idx = TUPLE_LENGTH * dataIndex(bitpos);

            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1];

            // copy 'src' and remove 1 element(s) at position 'idx'
            System.arraycopy(src, 0, dst, 0, idx);
            System.arraycopy(src, idx + 1, dst, idx, src.length - idx - 1);

            return nodeOf(mutator, nodeMap(), dataMap() ^ bitpos, dst);
        }

        @Override
        CompactSetNode<K> copyAndMigrateFromInlineToNode(final AtomicReference<Thread> mutator,
                                                         final int bitpos, final AbstractSetNode<K> node) {

            final int idxOld = TUPLE_LENGTH * dataIndex(bitpos);
            final int idxNew = this.nodes.length - TUPLE_LENGTH - nodeIndex(bitpos);

            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1 + 1];

            // copy 'src' and remove 1 element(s) at position 'idxOld' and
            // insert 1 element(s) at position 'idxNew' (TODO: carefully test)
            assert idxOld <= idxNew;
            System.arraycopy(src, 0, dst, 0, idxOld);
            System.arraycopy(src, idxOld + 1, dst, idxOld, idxNew - idxOld);
            dst[idxNew] = node;
            System.arraycopy(src, idxNew + 1, dst, idxNew + 1, src.length - idxNew - 1);

            return nodeOf(mutator, nodeMap() | bitpos, dataMap() ^ bitpos, dst);
        }

        @Override
        CompactSetNode<K> copyAndMigrateFromNodeToInline(final AtomicReference<Thread> mutator,
                                                         final int bitpos, final AbstractSetNode<K> node) {

            final int idxOld = this.nodes.length - 1 - nodeIndex(bitpos);
            final int idxNew = TUPLE_LENGTH * dataIndex(bitpos);

            final Object[] src = this.nodes;
            final Object[] dst = new Object[src.length - 1 + 1];

            // copy 'src' and remove 1 element(s) at position 'idxOld' and
            // insert 1 element(s) at position 'idxNew' (TODO: carefully test)
            assert idxOld >= idxNew;
            System.arraycopy(src, 0, dst, 0, idxNew);
            dst[idxNew] = node.getKey(0);
            System.arraycopy(src, idxNew, dst, idxNew + 1, idxOld - idxNew);
            System.arraycopy(src, idxOld + 1, dst, idxOld + 1, src.length - idxOld - 1);

            return nodeOf(mutator, nodeMap() ^ bitpos, dataMap() | bitpos, dst);
        }

    }

    private static final class HashCollisionSetNode<K> extends CompactSetNode<K> {
        private final K[] keys;
        private final int hash;

        HashCollisionSetNode(final int hash, final K[] keys) {
            this.keys = keys;
            this.hash = hash;
            assert payloadArity() >= 2;
        }

        @Override
        public boolean contains(final K key, final int keyHash, final int shift) {
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
        public AbstractSetNode<K> updated(final AtomicReference<Thread> mutator, final K key,
                                          final int keyHash, final int shift, final SetNodeResultImpl<K> details) {
            assert this.hash == keyHash;

            for (K k : keys) {
                if (Objects.equals(k, key)) {
                    return this;
                }
            }

            @SuppressWarnings("unchecked") final K[] keysNew = (K[]) new Object[this.keys.length + 1];

            // copy 'this.keys' and insert 1 element(s) at position
            // 'keys.length'
            System.arraycopy(this.keys, 0, keysNew, 0, keys.length);
            keysNew[keys.length] = key;

            details.modified();
            details.updateDeltaSize(1);
            details.updateDeltaHashCode(keyHash);
            return new HashCollisionSetNode<>(keyHash, keysNew);
        }

        @Override
        public AbstractSetNode<K> removed(final AtomicReference<Thread> mutator, final K key,
                                          final int keyHash, final int shift, final SetNodeResultImpl<K> details) {
            for (int idx = 0; idx < keys.length; idx++) {
                if (Objects.equals(keys[idx], key)) {
                    details.modified();
                    details.updateDeltaSize(-1);
                    details.updateDeltaHashCode(-keyHash);

                    if (payloadArity() == 1) {
                        return nodeOf(mutator);
                    } else if (payloadArity() == 2) {
                        /*
                         * Create root node with singleton element. This node will be a) either be the new root
                         * returned, or b) unwrapped and inlined.
                         */
                        final K theOtherKey = (idx == 0) ? keys[1] : keys[0];

                        return CompactSetNode.<K>nodeOf(mutator).updated(mutator, theOtherKey, keyHash, 0,
                                new SetNodeResultImpl<>());
                    } else {
                        @SuppressWarnings("unchecked") final K[] keysNew = (K[]) new Object[this.keys.length - 1];

                        // copy 'this.keys' and remove 1 element(s) at position
                        // 'idx'
                        System.arraycopy(this.keys, 0, keysNew, 0, idx);
                        System.arraycopy(this.keys, idx + 1, keysNew, idx, this.keys.length - idx - 1);

                        return new HashCollisionSetNode<>(keyHash, keysNew);
                    }
                }
            }
            return this;
        }

        @Override
        public boolean hasPayload() {
            return true;
        }

        @Override
        public int payloadArity() {
            return keys.length;
        }

        @Override
        boolean hasNodes() {
            return false;
        }

        @Override
        int nodeArity() {
            return 0;
        }

        @Override
        public byte sizePredicate() {
            return SIZE_MORE_THAN_ONE;
        }

        @Override
        public K getKey(final int index) {
            return keys[index];
        }

        @Override
        public int getKeyHash(int index) {
            return getKey(index).hashCode();
        }

        @Override
        public CompactSetNode<K> getNode(int index) {
            throw new IllegalStateException("Is leaf node.");
        }

        @Override
        CompactSetNode<K> copyAndInsertValue(final AtomicReference<Thread> mutator, final int bitpos,
                                             final K key) {
            throw new UnsupportedOperationException();
        }

        @Override
        CompactSetNode<K> copyAndRemoveValue(final AtomicReference<Thread> mutator,
                                             final int bitpos) {
            throw new UnsupportedOperationException();
        }

        @Override
        CompactSetNode<K> copyAndSetNode(final AtomicReference<Thread> mutator, final int bitpos,
                                         final AbstractSetNode<K> node) {
            throw new UnsupportedOperationException();
        }

        @Override
        CompactSetNode<K> copyAndMigrateFromInlineToNode(final AtomicReference<Thread> mutator,
                                                         final int bitpos, final AbstractSetNode<K> node) {
            throw new UnsupportedOperationException();
        }

        @Override
        CompactSetNode<K> copyAndMigrateFromNodeToInline(final AtomicReference<Thread> mutator,
                                                         final int bitpos, final AbstractSetNode<K> node) {
            throw new UnsupportedOperationException();
        }

        @Override
        int nodeMap() {
            throw new UnsupportedOperationException();
        }

        @Override
        int dataMap() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Iterator skeleton that uses a fixed stack in depth.
     */
    private static class SetKeyIterator<K> implements Iterator<K> {

        private static final int MAX_DEPTH = 7;

        protected int currentValueCursor;
        protected int currentValueLength;
        protected AbstractSetNode<K> currentValueNode;

        private int currentStackLevel = -1;
        private final int[] nodeCursorsAndLengths = new int[MAX_DEPTH * 2];

        AbstractSetNode<K>[] nodes = new AbstractSetNode[MAX_DEPTH];

        SetKeyIterator(AbstractSetNode<K> rootNode) {
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

        /*
         * search for next node that contains values
         */
        private boolean searchNextValueNode() {
            while (currentStackLevel >= 0) {
                final int currentCursorIndex = currentStackLevel * 2;
                final int currentLengthIndex = currentCursorIndex + 1;

                final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
                final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];

                if (nodeCursor < nodeLength) {
                    final AbstractSetNode<K> nextNode = nodes[currentStackLevel].getNode(nodeCursor);
                    nodeCursorsAndLengths[currentCursorIndex]++;

                    if (nextNode.hasNodes()) {
                        /*
                         * put node on next stack level for depth-first traversal
                         */
                        final int nextStackLevel = ++currentStackLevel;
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

        public boolean hasNext() {
            if (currentValueCursor < currentValueLength) {
                return true;
            } else {
                return searchNextValueNode();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else {
                return currentValueNode.getKey(currentValueCursor++);
            }
        }
    }

    static class TransientTrieSet<K> {
        protected AbstractSetNode<K> rootNode;
        protected int cachedHashCode;
        protected int cachedSize;
        final private AtomicReference<Thread> mutator;

        TransientTrieSet(PersistentTrieSet<K> trieSet) {
            this.rootNode = trieSet.rootNode;
            this.cachedHashCode = trieSet.cachedHashCode;
            this.cachedSize = trieSet.cachedSize;
            this.mutator = new AtomicReference<>(Thread.currentThread());
        }

        protected boolean __insertWithCapability(AtomicReference<Thread> mutator, K key) {
            if (mutator.get() == null) {
                throw new IllegalStateException("Transient already frozen.");
            }

            final int keyHash = key.hashCode();
            final SetNodeResultImpl<K> details = new SetNodeResultImpl<>();

            final AbstractSetNode<K> newRootNode =
                    rootNode.updated(mutator, key, keyHash, 0, details);

            if (details.isModified()) {

                rootNode = newRootNode;
                cachedHashCode += keyHash;
                cachedSize += 1;

                return true;

            }

            return false;
        }

        protected boolean __removeWithCapability(AtomicReference<Thread> mutator, final K key) {
            if (mutator.get() == null) {
                throw new IllegalStateException("Transient already frozen.");
            }

            final int keyHash = key.hashCode();
            final SetNodeResultImpl<K> details = new SetNodeResultImpl<>();

            final AbstractSetNode<K> newRootNode =
                    rootNode.removed(mutator, key, keyHash, 0, details);

            if (details.isModified()) {
                rootNode = newRootNode;
                cachedHashCode = cachedHashCode - keyHash;
                cachedSize = cachedSize - 1;

                return true;
            }

            return false;
        }

        public boolean __insert(final K key) {
            return __insertWithCapability(this.mutator, key);
        }

        public boolean __remove(final K key) {
            return __removeWithCapability(this.mutator, key);
        }

        public PersistentTrieSet<K> freeze() {
            if (mutator.get() == null) {
                throw new IllegalStateException("Transient already frozen.");
            }

            mutator.set(null);
            return new PersistentTrieSet<>(rootNode, cachedHashCode, cachedSize);
        }
    }

    interface SetNodeX<K, R extends SetNodeX<K, R>> {
        byte SIZE_EMPTY = 0b00;
        byte SIZE_ONE = 0b01;
        byte SIZE_MORE_THAN_ONE = 0b10;

        /**
         * Abstract predicate over a node's size. Value can be either {@value #SIZE_EMPTY},
         * {@value #SIZE_ONE}, or {@value #SIZE_MORE_THAN_ONE}.
         *
         * @return size predicate
         */
        byte sizePredicate();

        boolean contains(final K key, final int keyHash, final int shift);


        R updated(final AtomicReference<Thread> mutator, final K key, final int keyHash, final int shift,
                  final SetNodeResultImpl<K> details);

        R removed(final AtomicReference<Thread> mutator, final K key, final int keyHash, final int shift,
                  final SetNodeResultImpl<K> details);

        boolean hasPayload();

        int payloadArity();

        K getKey(final int index);

        int getKeyHash(final int index);
    }
}
