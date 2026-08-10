/*
 * @(#)HashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.icollection.impl.ArrayHelper;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.ToIntFunction;

/// Represents a hash-collision node in a CHAMP trie.
///
/// @param <K> the key type
/// @param <V> the value type
class HashCollisionNode<K, V> extends Node<K, V> {
    private final int hash;

    HashCollisionNode(int hash, Object[] entries) {
        this.array = entries;
        this.hash = hash;
    }

    @Override
    int dataArity() {
        return array.length / ENTRY_LENGTH;
    }

    @Override
    boolean hasDataArityOne() {
        return false;
    }

    @Override
    boolean equivalent(Object other) {
        if (this == other) {
            return true;
        }
        HashCollisionNode<?, ?> that = (HashCollisionNode<?, ?>) other;
        Object[] thatEntries = that.array;
        if (hash != that.hash
                || thatEntries.length != array.length) {
            return false;
        }

        // Linear scan for each key, because of arbitrary element order.
        Object[] thatEntriesCloned = thatEntries.clone();
        int remainingLength = thatEntriesCloned.length;
        outerLoop:
        for (int i = 0; i < array.length; i += ENTRY_LENGTH) {
            Object key = array[i];
            for (int j = 0; j < remainingLength; j += ENTRY_LENGTH) {
                Object todoKey = thatEntriesCloned[j];
                if (Objects.equals(todoKey, key)) {
                    for (int f = 1; f < ENTRY_LENGTH; f++) {
                        if (!Objects.equals(thatEntriesCloned[j + f], array[i + f])) {
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
    Object findByKey(K key, int keyHash, int shift) {
        for (int i = 0; i < array.length; i += ENTRY_LENGTH) {
            if (Objects.equals(key, array[i])) {
                return array[i + 1];
            }
        }
        return NO_DATA;
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
    Node<K, V> remove(@Nullable IdentityObject mutator, K key,
                      int keyHash, int shift, ChangeEvent<V> details) {
        for (int idx = 0, i = 0; i < array.length; i += ENTRY_LENGTH, idx++) {
            if (Objects.equals(array[i], key)) {
                @SuppressWarnings("unchecked") V currentVal = ENTRY_LENGTH > 1 ? (V) array[i + 1] : null;
                details.updated(currentVal);

                if (array.length == ENTRY_LENGTH) {
                    return BitmapIndexedNode.emptyNode();
                } else if (array.length == ENTRY_LENGTH * 2) {
                    // Create root node with singleton element.
                    // This node will be a) either be the new root
                    // returned, or b) unwrapped and inlined.
                    Object[] theOtherEntry = getDataEntry(idx ^ 1);
                    return ChampTrie.newBitmapIndexedNode(mutator, 0, bitpos(mask(keyHash, 0)), theOtherEntry);
                } else {
                    // copy keys and vals and remove entryLength elements at position idx
                    Object[] entriesNew = ArrayHelper.copyComponentRemove(this.array, idx * ENTRY_LENGTH, ENTRY_LENGTH);
                    if (isAllowedToEdit(mutator)) {
                        this.array = entriesNew;
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
    Node<K, V> put(@Nullable IdentityObject mutator, K key, V val,
                   int keyHash, int shift, ChangeEvent<V> details, ToIntFunction<K> hashFunction) {
        assert this.hash == keyHash;

        for (int idx = 0, i = 0; i < array.length; i += ENTRY_LENGTH, idx++) {
            if (Objects.equals(array[i], key)) {
                @SuppressWarnings("unchecked") V currentVal = (V) array[i + 1];
                if (Objects.equals(currentVal, val)) {
                    details.found(currentVal);
                    return this;
                } else {
                    details.updated(currentVal);
                    if (isAllowedToEdit(mutator)) {
                        array[i + 1] = val;
                        return this;
                    }
                    Object[] dst = ArrayHelper.copySet(this.array, i + 1, val);
                    return ChampTrie.newHashCollisionNode(mutator, this.hash, dst, ENTRY_LENGTH);
                }
            }
        }

        // copy entries and add 1 more at the end
        Object[] entriesNew = ArrayHelper.copyComponentAdd(this.array, this.array.length, ENTRY_LENGTH);
        entriesNew[this.array.length] = key;
        entriesNew[this.array.length + 1] = val;
        details.modified();
        if (isAllowedToEdit(mutator)) {
            this.array = entriesNew;
            return this;
        } else {
            return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, ENTRY_LENGTH);
        }
    }
}
