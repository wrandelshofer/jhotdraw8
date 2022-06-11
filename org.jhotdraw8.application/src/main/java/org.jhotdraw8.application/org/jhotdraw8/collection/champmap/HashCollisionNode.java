/*
 * @(#)HashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champmap;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ArrayHelper;
import org.jhotdraw8.collection.UniqueId;

import java.util.Objects;

/**
 * Represents a hash-collision node in a CHAMP trie.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
class HashCollisionNode<K, V> extends Node<K, V> {
    private final int hash;
    @NonNull Object[] entries;

    HashCollisionNode(final int hash, final Object @NonNull [] entries) {
        this.entries = entries;
        this.hash = hash;
    }

    @Override
    int dataArity() {
        return entries.length / ENTRY_LENGTH;
    }

    @Override
    boolean hasDataArityOne() {
        return false;
    }

    @Override
    int dataIndex(K key, final int keyHash, final int shift) {
        if (this.hash != keyHash) {
            return -1;
        }
        for (int i = 0, index = 0; i < entries.length; i += ENTRY_LENGTH, index++) {
            Object k = entries[i];
            if (Objects.equals(k, key)) {
                return index;
            }
        }
        return -1;
    }

    @Override
    boolean equivalent(@NonNull Object other) {
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
        for (int i = 0; i < entries.length; i += ENTRY_LENGTH) {
            final Object key = entries[i];
            for (int j = 0; j < remainingLength; j += ENTRY_LENGTH) {
                final Object todoKey = thatEntriesCloned[j];
                if (Objects.equals(todoKey, key)) {
                    for (int f = 1; f < ENTRY_LENGTH; f++) {
                        if (!Objects.equals(thatEntriesCloned[j + f], entries[i + f])) {
                            return false;
                        }
                    }
                    // We have found an equal entry. We do not need to compare
                    // this entry again. So we replace it with the last entry
                    // from the array and reduce the remaining length.
                    System.arraycopy(thatEntriesCloned, remainingLength - ENTRY_LENGTH, thatEntriesCloned, j, ENTRY_LENGTH);
                    remainingLength -= ENTRY_LENGTH;

                    continue outerLoop;
                }
            }
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    Object findByKey(final K key, final int keyHash, final int shift) {
        for (int i = 0; i < entries.length; i += ENTRY_LENGTH) {
            if (Objects.equals(key, entries[i])) {
                return ENTRY_LENGTH > 1 ? entries[i + 1] : null;
            }
        }
        return NO_VALUE;
    }

    @Override
    Object @NonNull [] getDataEntry(int index) {
        Object[] entry = new Object[ENTRY_LENGTH];
        System.arraycopy(entries, ENTRY_LENGTH * index, entry, 0, ENTRY_LENGTH);
        return entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public K getKey(final int index) {
        return (K) entries[index * ENTRY_LENGTH];
    }


    public long getSequenceNumber(final int index, int entryLength, int numFields) {
        return numFields < entryLength ? (Long) entries[index * entryLength] : 0L;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    SequencedMapEntry<K, V> getKeyValueSeqEntry(final int index) {
        return new SequencedMapEntry<K, V>(
                (K) entries[index * ENTRY_LENGTH],
                (V) entries[index * ENTRY_LENGTH + 1],
                0
        );
    }

    @Override
    @NonNull
    Node<K, V> getNode(int index) {
        throw new IllegalStateException("Is leaf node.");
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public V getValue(final int index) {
        return (V) entries[index * ENTRY_LENGTH + 1];
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
    @Nullable
    Node<K, V> remove(final @Nullable UniqueId mutator, final K key,
                      final int keyHash, final int shift, final @NonNull ChangeEvent<V> details) {
        for (int idx = 0, i = 0; i < entries.length; i += ENTRY_LENGTH, idx++) {
            if (Objects.equals(entries[i], key)) {
                @SuppressWarnings("unchecked") final V currentVal = ENTRY_LENGTH > 1 ? (V) entries[i + 1] : null;
                details.updated(currentVal);

                if (entries.length == ENTRY_LENGTH) {
                    return BitmapIndexedNode.emptyNode();
                } else if (entries.length == ENTRY_LENGTH * 2) {
                    // Create root node with singleton element.
                    // This node will be a) either be the new root
                    // returned, or b) unwrapped and inlined.
                    final Object[] theOtherEntry = getDataEntry(idx ^ 1);
                    return ChampTrie.newBitmapIndexedNode(mutator, 0, bitpos(mask(keyHash, 0)), theOtherEntry);
                } else {
                    // copy keys and vals and remove entryLength elements at position idx
                    final Object[] entriesNew = ArrayHelper.copyComponentRemove(this.entries, idx * ENTRY_LENGTH, ENTRY_LENGTH);
                    if (isAllowedToEdit(mutator)) {
                        this.entries = entriesNew;
                        return this;
                    }
                    return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, ENTRY_LENGTH);
                }
            }
        }
        return this;
    }

    @Override
    @Nullable
    Node<K, V> update(final @Nullable UniqueId mutator, final K key, final V val,
                      final int keyHash, final int shift, final @NonNull ChangeEvent<V> details) {
        assert this.hash == keyHash;

        for (int idx = 0, i = 0; i < entries.length; i += ENTRY_LENGTH, idx++) {
            if (Objects.equals(entries[i], key)) {
                @SuppressWarnings("unchecked") final V currentVal = (V) entries[i + 1];
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
                    return ChampTrie.newHashCollisionNode(mutator, this.hash, dst, ENTRY_LENGTH);
                }
            }
        }

        // copy entries and add 1 more at the end
        final Object[] entriesNew = ArrayHelper.copyComponentAdd(this.entries, this.entries.length, ENTRY_LENGTH);
        entriesNew[this.entries.length] = key;
        entriesNew[this.entries.length + 1] = val;
        details.modified();
        if (isAllowedToEdit(mutator)) {
            this.entries = entriesNew;
            return this;
        } else {
            return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, ENTRY_LENGTH);
        }
    }
}
