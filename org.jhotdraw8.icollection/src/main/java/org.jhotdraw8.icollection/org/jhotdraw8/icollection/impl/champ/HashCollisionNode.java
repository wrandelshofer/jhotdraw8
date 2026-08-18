/*
 * @(#)HashCollisionNode.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.impl.ArrayHelper;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Represents a hash-collision node in a CHAMP trie.
///
/// @param <K> the key type
/// @param <V> the value type
class HashCollisionNode extends Node {
    private final int hash;

    HashCollisionNode(int hash, Object[] entries) {
        this.array = entries;
        this.hash = hash;
    }

    @Override
    int dataArity(int ENTRY_LENGTH) {
        return array.length / ENTRY_LENGTH;
    }

    @Override
    boolean hasDataArityOne() {
        return false;
    }

    @Override
    protected int calculateSize(int ENTRY_LENGTH) {
        return dataArity(ENTRY_LENGTH);
    }

    @Override
    boolean equivalent(Object other, int ENTRY_LENGTH) {
        if (this == other) {
            return true;
        }
        HashCollisionNode that = (HashCollisionNode) other;
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

    @Override
    @Nullable
    Object findEntry(Object key, int keyHash, int shift, int ENTRY_LENGTH) {
        for (int i = 0; i < array.length; i += ENTRY_LENGTH) {
            if (Objects.equals(key, array[i])) {
                return Arrays.copyOfRange(array, i, i + ENTRY_LENGTH);
            }
        }
        return NO_DATA;
    }

    @Override
    @Nullable
    Object findData(Object key, int keyHash, int shift, int ENTRY_LENGTH, int DATA_INDEX) {
        for (int i = 0; i < array.length; i += ENTRY_LENGTH) {
            if (Objects.equals(key, array[i])) {
                return array[i + DATA_INDEX];
            }
        }
        return NO_DATA;
    }

    @Override
    boolean hasData() {
        return array.length > 0;
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
    Node remove(@Nullable IdentityObject mutator, Object key,
                int keyHash, int shift, ChangeEvent details, int ENTRY_LENGTH) {
        for (int idx = 0, i = 0; i < array.length; i += ENTRY_LENGTH, idx++) {
            if (Objects.equals(array[i], key)) {
                @SuppressWarnings("unchecked") Object[] currentEntry = Arrays.copyOfRange(array, i, i + ENTRY_LENGTH);
                details.setRemoved(currentEntry);

                if (array.length == ENTRY_LENGTH) {
                    return BitmapIndexedNode.emptyNode();
                } else if (array.length == ENTRY_LENGTH * 2) {
                    // Create root node with singleton element.
                    // This node will be a) either be the new root
                    // returned, or b) unwrapped and inlined.
                    Object[] theOtherEntry = getDataEntry(idx ^ 1, ENTRY_LENGTH);
                    return ChampTrie.newBitmapIndexedNode(mutator, 0, bitpos(mask(keyHash, 0)), theOtherEntry, ENTRY_LENGTH);
                } else {
                    // copy keys and vals and remove entryLength elements at position idx
                    Object[] entriesNew = ArrayHelper.copyComponentRemove(this.array, idx * ENTRY_LENGTH, ENTRY_LENGTH);
                    if (isAllowedToUpdate(mutator)) {
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
    public Node put(@Nullable IdentityObject mutator, Object key, Object[] newEntry,
                    int keyHash, int shift, ChangeEvent details,
                    BiFunction<Object[], Object[], Object[]> updateFunction,
                    ToIntFunction<Object> hashFunction, int ENTRY_LENGTH) {
        assert this.hash == keyHash;
        for (int i = 0; i < array.length; i += ENTRY_LENGTH) {
            if (Objects.equals(array[i], key)) {
                Object[] currentEntry = Arrays.copyOfRange(array, i, i + ENTRY_LENGTH);
                Object[] updatedEntry = updateFunction.apply(currentEntry, newEntry);
                if (updatedEntry == currentEntry) {
                    details.setFound(currentEntry);
                    return this;
                }
                details.setReplaced(currentEntry, updatedEntry);
                if (isAllowedToUpdate(mutator)) {
                    System.arraycopy(updatedEntry, 0, array, i, ENTRY_LENGTH);
                    return this;
                }
                Object[] dst = ArrayHelper.copySet(this.array, i, updatedEntry, ENTRY_LENGTH);
                return ChampTrie.newHashCollisionNode(mutator, this.hash, dst, ENTRY_LENGTH);
            }
        }

        // copy entries and add 1 more at the end
        Object[] entriesNew = ArrayHelper.copyComponentAdd(this.array, this.array.length, ENTRY_LENGTH);
        System.arraycopy(newEntry, 0, entriesNew, this.array.length, ENTRY_LENGTH);
        details.setAdded(entriesNew);
        if (isAllowedToUpdate(mutator)) {
            this.array = entriesNew;
            return this;
        }
        return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, ENTRY_LENGTH);
    }

    @Override
    protected Node removeIf(@Nullable IdentityObject owner, Predicate<Object> predicate, int shift, BulkChangeEvent bulkChange, int ENTRY_LENGTH) {

        int resultSize = 0;
        Object[] buffer = new Object[this.array.length];
        Object[] thisArray = this.array;
        for (int i = 0; i < thisArray.length; i += ENTRY_LENGTH) {
            Object thisKey = thisArray[i];
            if (!predicate.test(thisKey)) {
                System.arraycopy(thisArray, i, buffer, resultSize * ENTRY_LENGTH, ENTRY_LENGTH);
                resultSize++;
            } else {
                bulkChange.removed++;
            }
        }
        return newCroppedHashCollisionNode(thisArray.length != resultSize * ENTRY_LENGTH, buffer, resultSize, ENTRY_LENGTH);
    }

    private HashCollisionNode newCroppedHashCollisionNode(boolean changed, Object[] buffer, int size, int ENTRY_LENGTH) {
        if (changed) {
            if (buffer.length != size) {
                buffer = Arrays.copyOf(buffer, size * ENTRY_LENGTH);
            }
            return new HashCollisionNode(hash, buffer);
        }
        return this;
    }

    @Override
    protected Node putAll(@Nullable IdentityObject owner, Node otherNode, int shift, BulkChangeEvent bulkChange, ToIntFunction<Object> hashFunction, ChangeEvent details, int ENTRY_LENGTH) {
        if (otherNode == this) {
            bulkChange.inBoth += dataArity(ENTRY_LENGTH);
            // FIXME also count children!
            return this;
        }
        HashCollisionNode that = (HashCollisionNode) otherNode;

        // The buffer initially contains all data elements from this node.
        // Every time we find a matching data element in both nodes, we do not need to ever look at that data element again.
        // So, we swap it out with a data element from the end of unprocessed data elements, and subtract 1 from unprocessedSize.
        // If that node contains a data element that is not in this node, we add it to the end, and add 1 to bufferSize.
        // Buffer content:
        // 0..unprocessedSize-1 = unprocessed data elements from this node
        // unprocessedSize..resultSize-1 = data elements that we have updated from that node, or that we have added from that node.
        int thisSize = this.dataArity(ENTRY_LENGTH);
        int thatSize = that.dataArity(ENTRY_LENGTH);
        Object[] buffer = Arrays.copyOf(this.array, this.array.length + that.array.length);
        System.arraycopy(this.array, 0, buffer, 0, this.array.length);
        Object[] thatArray = that.array;
        int resultSize = thisSize;
        int unprocessedSize = thisSize;

        outer:
        for (int i = 0; i < that.array.length; i += ENTRY_LENGTH) {
            Object thatKey = thatArray[i];
            for (int j = 0; j < unprocessedSize * ENTRY_LENGTH; j += ENTRY_LENGTH) {
                Object thisKey = buffer[j];
                if (Objects.equals(thatKey, thisKey)) {
                    --unprocessedSize;
                    System.arraycopy(buffer, j, buffer, unprocessedSize * ENTRY_LENGTH, ENTRY_LENGTH);
                    System.arraycopy(this.array, unprocessedSize * ENTRY_LENGTH, buffer, j, ENTRY_LENGTH);
                    bulkChange.inBoth++;
                    continue outer;
                }
            }
            buffer[resultSize] = thatKey;
            System.arraycopy(thatArray, i, buffer, resultSize * ENTRY_LENGTH, ENTRY_LENGTH);
            resultSize++;
        }
        return newCroppedHashCollisionNode(resultSize != thisSize, buffer, resultSize, ENTRY_LENGTH);
    }
}
