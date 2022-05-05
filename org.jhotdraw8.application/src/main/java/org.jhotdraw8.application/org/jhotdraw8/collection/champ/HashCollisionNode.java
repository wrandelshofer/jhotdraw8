/*
 * @(#)HashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ArrayHelper;
import org.jhotdraw8.collection.UniqueId;
import org.jhotdraw8.util.function.TriFunction;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

class HashCollisionNode<K, V> extends Node<K, V> {
    private final int hash;
    @NonNull Object[] entries;

    HashCollisionNode(final int hash, final Object[] entries, int entryLength) {
        this.entries = entries;
        this.hash = hash;

        assert entries.length % entryLength == 0;
    }

    @Override
    int dataArity(int entryLength) {
        return entries.length / entryLength;
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
        for (int i = 0, index = 0; i < entries.length; i += entryLength, index++) {
            Object k = entries[i];
            if (Objects.equals(k, key)) {
                return index;
            }
        }
        return -1;
    }

    @Override
    boolean equivalent(@NonNull Object other, int entryLength, int numFields) {
        if (this == other) {
            return true;
        }
        HashCollisionNode<?, ?> that = (HashCollisionNode<?, ?>) other;
        @NonNull Object[] thatEntries = that.entries;
        if (hash != that.hash
                || thatEntries.length != entries.length) {
            return false;
        }

        // Linear scan for each key, because of arbitrary element order.
        @NonNull Object[] thatEntriesCloned = thatEntries.clone();
        int remainingLength = thatEntriesCloned.length;
        outerLoop:
        for (int i = 0; i < entries.length; i += entryLength) {
            final Object key = entries[i];
            for (int j = 0; j < remainingLength; j += entryLength) {
                final Object todoKey = thatEntriesCloned[j];
                if (Objects.equals(todoKey, key)) {
                    for (int f = 1; f < numFields; f++) {
                        if (!Objects.equals(thatEntriesCloned[j + f], entries[i + f])) {
                            return false;
                        }
                    }
                    // We have found an equal entry. We do not need to compare
                    // this entry again. So we replace it with the last entry
                    // from the array and reduce the remaining length.
                    System.arraycopy(thatEntriesCloned, remainingLength - entryLength, thatEntriesCloned, j, entryLength);
                    remainingLength -= entryLength;

                    continue outerLoop;
                }
            }
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    Object findByKey(final K key, final int keyHash, final int shift, int entryLength, int numFields) {
        for (int i = 0; i < entries.length; i += entryLength) {
            if (Objects.equals(key, entries[i])) {
                return entryLength > 1 ? entries[i + 1] : null;
            }
        }
        return (V) NO_VALUE;
    }

    @Override
    Object[] getDataEntry(int index, int entryLength) {
        Object[] entry = new Object[entryLength];
        System.arraycopy(entries, entryLength * index, entry, 0, entryLength);
        return entry;
    }

    @SuppressWarnings("unchecked")
    K getKey(final int index, int entryLength) {
        return (K) entries[index * entryLength];
    }

    @SuppressWarnings("unchecked")
    @Override
    Map.Entry<K, V> getKeyValueEntry(final int index, @NonNull BiFunction<K, V, Map.Entry<K, V>> factory, int entryLength) {
        return factory.apply((K) entries[index * entryLength], entryLength > 1 ? (V) entries[index * entryLength + 1] : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    SequencedMapEntry<K, V> getKeyValueSeqEntry(final int index, @NonNull TriFunction<K, V, Integer, SequencedMapEntry<K, V>> factory, int entryLength) {
        return factory.apply(
                (K) entries[index * entryLength],
                entryLength > 1 ? (V) entries[index * entryLength + 1] : null,
                (Integer) entries[index * entryLength + entryLength - 1]
        );
    }

    @Override
    Node<K, V> getNode(int index, int entryLength) {
        throw new IllegalStateException("Is leaf node.");
    }

    @SuppressWarnings("unchecked")
    @Override
    V getValue(final int index, int entryLength, int numFields) {
        return entryLength > 1 ? (V) entries[index * entryLength + 1] : null;
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
    Node<K, V> remove(final @Nullable UniqueId mutator, final K key,
                      final int keyHash, final int shift, final ChangeEvent<V> details,
                      int entryLength, int numFields) {
        for (int idx = 0, i = 0; i < entries.length; i += entryLength, idx++) {
            if (Objects.equals(entries[i], key)) {
                @SuppressWarnings("unchecked") final V currentVal = entryLength > 1 ? (V) entries[i + 1] : null;
                details.updated(currentVal);

                if (entries.length == entryLength) {
                    return BitmapIndexedNode.emptyNode();
                } else if (entries.length == entryLength * 2) {
                    // Create root node with singleton element.
                    // This node will be a) either be the new root
                    // returned, or b) unwrapped and inlined.
                    final Object[] theOtherEntry = getDataEntry(idx ^ 1, entryLength);
                    return ChampTrie.newBitmapIndexedNode(mutator, 0, bitpos(mask(keyHash, 0)), theOtherEntry, entryLength);
                } else {
                    // copy keys and vals and remove entryLength elements at position idx
                    final Object[] entriesNew = ArrayHelper.copyComponentRemove(this.entries, idx * entryLength, entryLength);
                    if (isAllowedToEdit(mutator)) {
                        this.entries = entriesNew;
                        return this;
                    }
                    return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, entryLength);
                }
            }
        }
        return this;
    }

    @Override
    Node<K, V> update(final UniqueId mutator, final K key, final V val,
                      final int keyHash, final int shift, final ChangeEvent<V> details, int entryLength, int sequenceNumber, int numFields) {
        assert this.hash == keyHash;

        for (int idx = 0, i = 0; i < entries.length; i += entryLength, idx++) {
            if (Objects.equals(entries[i], key)) {
                @SuppressWarnings("unchecked") final V currentVal = numFields == 1 ? null : (V) entries[i + 1];
                if (Objects.equals(currentVal, val)) {
                    details.found(currentVal);
                    return this;
                } else {
                    details.updated(currentVal);
                    if (isAllowedToEdit(mutator)) {
                        entries[i + 1] = val;
                        return this;
                    }
                    final Object[] dst = ArrayHelper.copySet(this.entries, i + 1, val);
                    return ChampTrie.newHashCollisionNode(mutator, this.hash, dst, entryLength);
                }
            }
        }

        // copy entries and add 1 more at the end
        final Object[] entriesNew = ArrayHelper.copyComponentAdd(this.entries, this.entries.length, entryLength);
        entriesNew[this.entries.length] = key;
        if (numFields > 1) {
            entriesNew[this.entries.length + 1] = val;
        }
        if (numFields < entryLength) {
            entriesNew[this.entries.length + entryLength - 1] = sequenceNumber;
        }
        details.modified();
        if (isAllowedToEdit(mutator)) {
            this.entries = entriesNew;
            return this;
        } else {
            return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, entryLength);
        }
    }
}
