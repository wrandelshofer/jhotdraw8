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
    int dataArity(int DATA_LENGTH) {
        return array.length / DATA_LENGTH;
    }

    @Override
    boolean hasDataArityOne() {
        return false;
    }

    @Override
    protected int calculateSize(int DATA_LENGTH) {
        return dataArity(DATA_LENGTH);
    }

    @Override
    boolean equivalent(Object other, int DATA_LENGTH) {
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
        for (int i = 0; i < array.length; i += DATA_LENGTH) {
            Object key = array[i];
            for (int j = 0; j < remainingLength; j += DATA_LENGTH) {
                Object todoKey = thatEntriesCloned[j];
                if (Objects.equals(todoKey, key)) {
                    for (int f = 1; f < DATA_LENGTH; f++) {
                        if (!Objects.equals(thatEntriesCloned[j + f], array[i + f])) {
                            return false;
                        }
                    }
                    // We have found an equal entry. We do not need to compare
                    // this entry again. So we replace it with the last entry
                    // from the array and reduce the remaining length.
                    System.arraycopy(thatEntriesCloned, remainingLength - DATA_LENGTH, thatEntriesCloned, j, DATA_LENGTH);
                    remainingLength -= DATA_LENGTH;

                    continue outerLoop;
                }
            }
            return false;
        }

        return true;
    }

    @Override
    @Nullable
    Object findData(Object key, int keyHash, int shift, int DATA_LENGTH) {
        for (int i = 0; i < array.length; i += DATA_LENGTH) {
            if (Objects.equals(key, array[i])) {
                return Arrays.copyOfRange(array, i, i + DATA_LENGTH);
            }
        }
        return NO_DATA;
    }

    @Override
    @Nullable
    Object findValue(Object key, int keyHash, int shift, int DATA_LENGTH, int VALUE_INDEX) {
        for (int i = 0; i < array.length; i += DATA_LENGTH) {
            if (Objects.equals(key, array[i])) {
                return array[i + VALUE_INDEX];
            }
        }
        return NO_DATA;
    }

    @Override
    boolean contains(Object key, int keyHash, int shift, int DATA_LENGTH) {
        for (int i = 0; i < array.length; i += DATA_LENGTH) {
            if (Objects.equals(key, array[i])) {
                return true;
            }
        }
        return false;
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
                int keyHash, int shift, ChangeEvent details, int DATA_LENGTH) {
        for (int idx = 0, i = 0; i < array.length; i += DATA_LENGTH, idx++) {
            if (Objects.equals(array[i], key)) {
                @SuppressWarnings("unchecked") Object[] currentEntry = Arrays.copyOfRange(array, i, i + DATA_LENGTH);
                details.setRemoved(currentEntry);

                if (array.length == DATA_LENGTH) {
                    return BitmapIndexedNode.emptyNode();
                } else if (array.length == DATA_LENGTH * 2) {
                    // Create root node with singleton element.
                    // This node will be a) either be the new root
                    // returned, or b) unwrapped and inlined.
                    Object[] theOtherEntry = getDataEntry(idx ^ 1, DATA_LENGTH);
                    return ChampTrie.newBitmapIndexedNode(mutator, 0, bitpos(mask(keyHash, 0)), theOtherEntry, DATA_LENGTH);
                } else {
                    // copy keys and vals and remove entryLength elements at position idx
                    Object[] entriesNew = ArrayHelper.copyComponentRemove(this.array, idx * DATA_LENGTH, DATA_LENGTH);
                    if (isAllowedToUpdate(mutator)) {
                        this.array = entriesNew;
                        return this;
                    }
                    return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, DATA_LENGTH);
                }
            }
        }
        return this;
    }

    @Override
    public Node put(@Nullable IdentityObject mutator, Object key, Object[] newData,
                    int keyHash, int shift, ChangeEvent details,
                    BiFunction<Object[], Object[], Object[]> updateFunction,
                    ToIntFunction<Object> hashFunction, int DATA_LENGTH) {
        assert this.hash == keyHash;
        for (int i = 0; i < array.length; i += DATA_LENGTH) {
            if (Objects.equals(array[i], key)) {
                Object[] currentEntry = Arrays.copyOfRange(array, i, i + DATA_LENGTH);
                Object[] updatedEntry = updateFunction.apply(currentEntry, newData);
                if (updatedEntry == currentEntry) {
                    details.setFound(currentEntry);
                    return this;
                }
                details.setReplaced(currentEntry, updatedEntry);
                if (isAllowedToUpdate(mutator)) {
                    System.arraycopy(updatedEntry, 0, array, i, DATA_LENGTH);
                    return this;
                }
                Object[] dst = ArrayHelper.copySet(this.array, i, updatedEntry, DATA_LENGTH);
                return ChampTrie.newHashCollisionNode(mutator, this.hash, dst, DATA_LENGTH);
            }
        }

        // copy entries and add 1 more at the end
        Object[] entriesNew = ArrayHelper.copyComponentAdd(this.array, this.array.length, DATA_LENGTH);
        System.arraycopy(newData, 0, entriesNew, this.array.length, DATA_LENGTH);
        details.setAdded(entriesNew);
        if (isAllowedToUpdate(mutator)) {
            this.array = entriesNew;
            return this;
        }
        return ChampTrie.newHashCollisionNode(mutator, keyHash, entriesNew, DATA_LENGTH);
    }

    @Override
    protected Node removeIf(@Nullable IdentityObject owner, Predicate<Object> predicate, int shift, BulkChangeEvent bulkChange, int DATA_LENGTH) {

        int resultSize = 0;
        Object[] buffer = new Object[this.array.length];
        Object[] thisArray = this.array;
        for (int i = 0; i < thisArray.length; i += DATA_LENGTH) {
            Object thisKey = thisArray[i];
            if (!predicate.test(thisKey)) {
                System.arraycopy(thisArray, i, buffer, resultSize * DATA_LENGTH, DATA_LENGTH);
                resultSize++;
            } else {
                bulkChange.removed++;
            }
        }
        return newCroppedHashCollisionNode(thisArray.length != resultSize * DATA_LENGTH, buffer, resultSize, DATA_LENGTH);
    }

    private HashCollisionNode newCroppedHashCollisionNode(boolean changed, Object[] buffer, int size, int DATA_LENGTH) {
        if (changed) {
            if (buffer.length != size) {
                buffer = Arrays.copyOf(buffer, size * DATA_LENGTH);
            }
            return new HashCollisionNode(hash, buffer);
        }
        return this;
    }

    @Override
    protected Node putAll(@Nullable IdentityObject owner, Node otherNode, int shift, BulkChangeEvent bulkChange, ToIntFunction<Object> hashFunction, ChangeEvent details, int DATA_LENGTH) {
        if (otherNode == this) {
            bulkChange.inBoth += dataArity(DATA_LENGTH);
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
        int thisSize = this.dataArity(DATA_LENGTH);
        int thatSize = that.dataArity(DATA_LENGTH);
        Object[] buffer = Arrays.copyOf(this.array, this.array.length + that.array.length);
        System.arraycopy(this.array, 0, buffer, 0, this.array.length);
        Object[] thatArray = that.array;
        int resultSize = thisSize;
        int unprocessedSize = thisSize;

        outer:
        for (int i = 0; i < that.array.length; i += DATA_LENGTH) {
            Object thatKey = thatArray[i];
            for (int j = 0; j < unprocessedSize * DATA_LENGTH; j += DATA_LENGTH) {
                Object thisKey = buffer[j];
                if (Objects.equals(thatKey, thisKey)) {
                    --unprocessedSize;
                    System.arraycopy(buffer, j, buffer, unprocessedSize * DATA_LENGTH, DATA_LENGTH);
                    System.arraycopy(this.array, unprocessedSize * DATA_LENGTH, buffer, j, DATA_LENGTH);
                    bulkChange.inBoth++;
                    continue outer;
                }
            }
            System.arraycopy(thatArray, i, buffer, resultSize * DATA_LENGTH, DATA_LENGTH);
            resultSize++;
        }
        return newCroppedHashCollisionNode(resultSize != thisSize, buffer, resultSize, DATA_LENGTH);
    }
}
