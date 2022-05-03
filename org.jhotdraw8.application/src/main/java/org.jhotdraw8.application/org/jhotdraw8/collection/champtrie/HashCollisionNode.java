/*
 * @(#)HashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champtrie;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ArrayHelper;
import org.jhotdraw8.collection.UniqueIdentity;
import org.jhotdraw8.util.function.TriFunction;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

class HashCollisionNode<K, V> extends Node<K, V> {
    private final int hash;
    @NonNull Object[] nodes;

    HashCollisionNode(final int hash, final Object[] nodes, int entryLength) {
        this.nodes = nodes;
        this.hash = hash;

        assert nodes.length > entryLength;
    }

    @Override
    int dataArity(int entryLength) {
        return nodes.length / entryLength;
    }

    @Override
    boolean hasDataArityOne(int entryLength) {
        return false;
    }

    @Override
    int dataIndex(K key, final int keyHash, final int shift, int entryLength) {
        if (this.hash != keyHash) {
            return -1;
        }
        for (int i = 0, index = 0; i < nodes.length; i += entryLength, index++) {
            Object k = nodes[i];
            if (Objects.equals(k, key)) {
                return index;
            }
        }
        return -1;
    }

    @Override
    boolean equivalent(@NonNull Object other, int entryLength, boolean hasSequenceNumber) {
        if (this == other) {
            return true;
        }
        HashCollisionNode<?, ?> that = (HashCollisionNode<?, ?>) other;
        @NonNull Object[] thatNodes = that.nodes;
        if (hash != that.hash
                || thatNodes.length != nodes.length) {
            return false;
        }

        // Linear scan for each key, because of arbitrary element order.
        boolean hasValue = !hasSequenceNumber && entryLength > 1 || hasSequenceNumber && entryLength > 2;
        if (hasValue) {
            outerLoop:
            for (int i = 0; i < thatNodes.length; i += entryLength) {
                final Object otherKey = thatNodes[i];
                final Object otherVal = thatNodes[i + 1];
                for (int j = 0; j < nodes.length; j += entryLength) {
                    final Object key = nodes[j];
                    final Object val = nodes[j + 1];

                    if (Objects.equals(key, otherKey) && Objects.equals(val, otherVal)) {
                        continue outerLoop;
                    }
                }
                return false;
            }
        } else {
            outerLoop:
            for (int i = 0; i < thatNodes.length; i += entryLength) {
                final Object otherKey = thatNodes[i];
                for (int j = 0; j < nodes.length; j += entryLength) {
                    final Object key = nodes[j];
                    if (Objects.equals(key, otherKey)) {
                        continue outerLoop;
                    }
                }
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    Object findByKey(final K key, final int keyHash, final int shift, int entryLength) {
        for (int i = 0; i < nodes.length; i += entryLength) {
            final K otherKey = (K) nodes[i];
            if (Objects.equals(key, otherKey)) {
                return entryLength > 1 ? nodes[i + 1] : null;
            }
        }
        return (V) NO_VALUE;
    }

    @Override
    Object[] getDataEntry(int index, int entryLength) {
        Object[] entry = new Object[entryLength];
        System.arraycopy(nodes, entryLength * index, entry, 0, entryLength);
        return entry;
    }

    @SuppressWarnings("unchecked")
    K getKey(final int index, int entryLength) {
        return (K) nodes[index * entryLength];
    }

    @SuppressWarnings("unchecked")
    @Override
    Map.Entry<K, V> getKeyValueEntry(final int index, @NonNull BiFunction<K, V, Map.Entry<K, V>> factory, int entryLength) {
        return factory.apply((K) nodes[index * entryLength], entryLength > 1 ? (V) nodes[index * entryLength + 1] : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    SequencedMapEntry<K, V> getKeyValueSeqEntry(final int index, @NonNull TriFunction<K, V, Integer, SequencedMapEntry<K, V>> factory, int entryLength) {
        return factory.apply(
                (K) nodes[index * entryLength],
                entryLength > 1 ? (V) nodes[index * entryLength + 1] : null,
                (Integer) nodes[index * entryLength + entryLength - 1]
        );
    }

    @Override
    Node<K, V> getNode(int index, int entryLength) {
        throw new IllegalStateException("Is leaf node.");
    }

    @SuppressWarnings("unchecked")
    @Override
    V getValue(final int index, int entryLength) {
        return entryLength > 1 ? (V) nodes[index * entryLength + 1] : null;
    }

    @Override
    boolean hasData() {
        return true;
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
    int nodeIndex(int keyHash, int shift) {
        return -1;
    }

    @Override
    Node<K, V> remove(final @Nullable UniqueIdentity mutator, final K key,
                      final int keyHash, final int shift, final ChangeEvent<V> details,
                      int entryLength) {
        for (int idx = 0, i = 0; i < nodes.length; i += entryLength, idx++) {
            if (Objects.equals(nodes[i], key)) {
                @SuppressWarnings("unchecked") final V currentVal = entryLength > 1 ? (V) nodes[i + 1] : null;
                details.updated(currentVal);

                if (nodes.length == entryLength) {
                    return BitmapIndexedNode.emptyNode();
                } else if (nodes.length == entryLength * 2) {
                    // Create root node with singleton element.
                    // This node will be a) either be the new root
                    // returned, or b) unwrapped and inlined.
                    final Object[] theOtherEntry = getDataEntry(idx ^ 1, entryLength);
                    return ChampTrie.newBitmapIndexedNode(mutator, 0, bitpos(mask(keyHash, 0)), theOtherEntry, entryLength);
                } else {

                    // copy keys and vals and remove entryLength elements at position idx
                    final Object[] entriesNew = ArrayHelper.copyComponentRemove(this.nodes, idx * entryLength, entryLength);

                    if (isAllowedToEdit(mutator)) {
                        this.nodes = entriesNew;
                        return this;
                    }
                    return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, entryLength);
                }
            }
        }
        return this;
    }

    @Override
    Node<K, V> update(final UniqueIdentity mutator, final K key, final V val,
                      final int keyHash, final int shift, final ChangeEvent<V> details, int entryLength, int sequenceNumber) {
        assert this.hash == keyHash;

        for (int idx = 0, i = 0; i < nodes.length; i += entryLength, idx++) {
            if (Objects.equals(nodes[i], key)) {
                @SuppressWarnings("unchecked") final V currentVal = sequenceNumber == NO_SEQUENCE_NUMBER && entryLength > 1
                        || sequenceNumber != NO_SEQUENCE_NUMBER && entryLength > 2
                        ? (V) nodes[i + 1] : null;
                if (Objects.equals(currentVal, val)) {
                    details.found(currentVal);
                    return this;
                } else {
                    final Object[] dst = ArrayHelper.copySet(this.nodes, idx * entryLength + 1, val);
                    final Node<K, V> thisNew = ChampTrie.newHashCollisionNode(mutator, this.hash, dst, entryLength);
                    details.updated(currentVal);
                    return thisNew;
                }
            }
        }

        // copy keys and vals and add 1 element at the end
        final Object[] entriesNew = ArrayHelper.copyComponentAdd(this.nodes, this.nodes.length, entryLength);
        entriesNew[this.nodes.length] = key;
        if (entryLength > 1) {
            entriesNew[this.nodes.length + 1] = val;
        }
        if (sequenceNumber != NO_SEQUENCE_NUMBER) {
            entriesNew[this.nodes.length + entryLength - 1] = sequenceNumber;
        }
        details.modified();
        if (isAllowedToEdit(mutator)) {
            this.nodes = entriesNew;
            return this;
        } else {
            return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, entryLength);
        }
    }
}
