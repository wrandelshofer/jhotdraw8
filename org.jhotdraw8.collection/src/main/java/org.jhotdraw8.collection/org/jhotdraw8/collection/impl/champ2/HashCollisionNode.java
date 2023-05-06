/*
 * @(#)HashCollisionNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ2;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ListHelper;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.impl.champ2.ChampNodeFactory.newHashCollisionNode;

/**
 * Represents a hash-collision node in a CHAMP trie.
 * <p>
 * XXX hash-collision nodes may become huge performance bottlenecks.
 * If the trie contains keys that implement {@link Comparable} then a hash-collision
 * nodes should be a sorted tree structure (for example a red-black tree).
 * Otherwise, hash-collision node should be a vector (for example a bit mapped trie).
 * <p>
 * References:
 * <p>
 * This class has been derived from 'The Capsule Hash Trie Collections Library'.
 * <dl>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <D> the data type
 */
class HashCollisionNode<D> extends Node<D> {
    private static final HashCollisionNode<?> EMPTY = new HashCollisionNode<>(0, new Object[0]);
    private final int hash;
    @NonNull Object[] data;

    HashCollisionNode(int hash, Object @NonNull [] data) {
        this.data = data;
        this.hash = hash;
    }

    @Override
    int dataArity() {
        return data.length;
    }

    @Override
    boolean hasDataArityOne() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    boolean equivalent(@NonNull Object other) {
        if (this == other) {
            return true;
        }
        HashCollisionNode<?> that = (HashCollisionNode<?>) other;
        @NonNull Object[] thatEntries = that.data;
        if (hash != that.hash || thatEntries.length != data.length) {
            return false;
        }

        // Linear scan for each key, because of arbitrary element order.
        @NonNull Object[] thatEntriesCloned = thatEntries.clone();
        int remainingLength = thatEntriesCloned.length;
        outerLoop:
        for (Object key : data) {
            for (int j = 0; j < remainingLength; j += 1) {
                Object todoKey = thatEntriesCloned[j];
                if (Objects.equals(todoKey, key)) {
                    // We have found an equal entry. We do not need to compare
                    // this entry again. So we replace it with the last entry
                    // from the array and reduce the remaining length.
                    System.arraycopy(thatEntriesCloned, remainingLength - 1, thatEntriesCloned, j, 1);
                    remainingLength -= 1;

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
    Object find(D key, int dataHash, int shift, @NonNull BiPredicate<D, D> equalsFunction) {
        for (Object entry : data) {
            if (equalsFunction.test(key, (D) entry)) {
                return entry;
            }
        }
        return NO_DATA;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    D getData(int index) {
        return (D) data[index];
    }

    @Override
    @NonNull
    Node<D> getNode(int index) {
        throw new IllegalStateException("Is leaf node.");
    }


    @Override
    boolean hasData() {
        return data.length > 0;
    }

    @Override
    boolean hasNodes() {
        return false;
    }

    @Override
    int nodeArity() {
        return 0;
    }


    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    Node<D> remove(D data,
                   int dataHash, int shift, @NonNull ChangeEvent<D> details, @NonNull BiPredicate<D, D> equalsFunction) {
        for (int idx = 0, i = 0; i < this.data.length; i += 1, idx++) {
            if (equalsFunction.test((D) this.data[i], data)) {
                @SuppressWarnings("unchecked") D currentVal = (D) this.data[i];
                details.setRemoved(currentVal);

                if (this.data.length == 1) {
                    return BitmapIndexedNode.emptyNode();
                } else if (this.data.length == 2) {
                    // Create root node with singleton element.
                    // This node will be a) either be the new root
                    // returned, or b) unwrapped and inlined.
                    return ChampNodeFactory.newBitmapIndexedNode(0, bitpos(mask(dataHash, 0)),
                            new Object[]{getData(idx ^ 1)});
                }
                // copy keys and remove 1 element at position idx
                Object[] entriesNew = ListHelper.copyComponentRemove(this.data, idx, 1);
                return newHashCollisionNode(dataHash, entriesNew);
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    Node<D> put(D newData,
                int dataHash, int shift, @NonNull ChangeEvent<D> details,
                @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction,
                @NonNull ToIntFunction<D> hashFunction) {
        assert this.hash == dataHash;

        for (int i = 0; i < this.data.length; i++) {
            D oldData = (D) this.data[i];
            if (equalsFunction.test(oldData, newData)) {
                D updatedData = updateFunction.apply(oldData, newData);
                if (updatedData == oldData) {
                    details.found(oldData);
                    return this;
                }
                details.setReplaced(oldData, updatedData);
                final Object[] newKeys = ListHelper.copySet(this.data, i, updatedData);
                return newHashCollisionNode(dataHash, newKeys);
            }
        }

        // copy entries and add 1 more at the end
        Object[] entriesNew = ListHelper.copyComponentAdd(this.data, this.data.length, 1);
        entriesNew[this.data.length] = newData;
        details.setAdded(newData);
        return newHashCollisionNode(dataHash, entriesNew);
    }


    @Override
    protected int calculateSize() {
        return dataArity();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull Node<D> putAll(Node<D> otherNode, int shift, @NonNull BulkChangeEvent bulkChange, @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        if (otherNode == this) {
            bulkChange.inBoth += dataArity();
            return this;
        }
        HashCollisionNode<D> that = (HashCollisionNode<D>) otherNode;

        // Unfortunately this is a quadratic operation. Hashing is not possible - that's why we are in a hash collision node.
        // The buffer has enough capacity for all data elements from this node and that node.
        // The buffer initially contains all data elements from this node.
        // Every time we find a matching data element in both nodes, we do not need to ever look at that data element again.
        // So, we swap it out with a data element from the end of thisSize, and subtract 1 from thisSize.
        // If that node contains a data element that is not in this node, we add it to the end, and add 1 to bufferSize.
        int thisSize = this.dataArity();
        int thatSize = that.dataArity();
        int bufferSize = thisSize;
        Object[] buffer = Arrays.copyOf(this.data, thisSize + thatSize);
        System.arraycopy(this.data, 0, buffer, 0, this.data.length);
        Object[] thatArray = that.data;
        boolean updated = false;
        outer:
        for (int i = thatSize - 1; i >= 0 && thisSize > 0; i--) {
            D thatData = (D) thatArray[i];
            for (int j = thisSize - 1; j >= 0; j--) {
                D thisData = (D) buffer[j];
                if (equalsFunction.test(thatData, thisData)) {
                    thisSize--;
                    D swap = (D) buffer[thisSize];
                    D updatedData = updateFunction.apply(thisData, thatData);
                    updated |= updatedData != thisData;
                    buffer[thisSize] = updatedData;
                    buffer[j] = swap;
                    bulkChange.inBoth++;
                    continue outer;
                }
            }
            buffer[bufferSize++] = thatData;
        }
        return newCroppedHashCollisionNode(updated | bufferSize != thisSize, buffer, bufferSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull Node<D> removeAll(Node<D> otherNode, int shift, @NonNull BulkChangeEvent bulkChange, @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        if (otherNode == this) {
            bulkChange.removed += dataArity();
            return (Node<D>) EMPTY;
        }
        HashCollisionNode<D> that = (HashCollisionNode<D>) otherNode;

        // Unfortunately this is a quadratic operation. Hashing is not possible - that's why we are in a hash collision node.
        // The buffer initially contains all data elements from this node.
        // Every time we find a matching data element in both nodes, we do not need to ever look at that data element again.
        // So, we replace it with a data element from the end of thisSize, and subtract 1 from thisSize.
        int thisSize = this.dataArity();
        int thatSize = that.dataArity();
        int bufferSize = thisSize;
        Object[] buffer = Arrays.copyOf(this.data, this.data.length);
        Object[] thatArray = that.data;
        outer:
        for (int i = thatSize - 1; i >= 0 && thisSize > 0; i--) {
            D thatData = (D) thatArray[i];
            for (int j = thisSize - 1; j >= 0; j--) {
                D thisData = (D) buffer[j];
                if (equalsFunction.test(thatData, thisData)) {
                    bufferSize--;
                    buffer[j] = buffer[bufferSize];
                    bulkChange.removed++;
                    continue outer;
                }
            }
        }
        return newCroppedHashCollisionNode(thisSize != bufferSize, buffer, bufferSize);
    }

    @NonNull
    private HashCollisionNode<D> newCroppedHashCollisionNode(boolean changed, Object[] buffer, int bufferSize1) {
        if (changed) {
            if (buffer.length != bufferSize1) {
                buffer = Arrays.copyOf(buffer, bufferSize1);
            }
            return newHashCollisionNode(hash, buffer);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull Node<D> retainAll(Node<D> otherNode, int shift, @NonNull BulkChangeEvent bulkChange, @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        if (otherNode == this) {
            bulkChange.removed += dataArity();
            return (Node<D>) EMPTY;
        }
        HashCollisionNode<D> that = (HashCollisionNode<D>) otherNode;

        // Unfortunately this is a quadratic operation. Hashing is not possible - that's why we are in a hash collision node.
        // The buffer is initially empty.
        // Every time we find a matching data element in both nodes, we do not need to ever look at that data element again.
        // So, we remove it from that array, and subtract 1 from thisSize.
        int thisSize = this.dataArity();
        int thatSize = that.dataArity();
        int bufferSize = 0;
        Object[] buffer = new Object[this.data.length];
        Object[] thatArray = Arrays.copyOf(that.data, that.data.length);
        Object[] thisArray = this.data;
        outer:
        for (int i = 0; i < thatSize && thisSize > 0; i++) {
            D thatData = (D) thatArray[i];
            for (int j = 0; j < thisSize; j++) {
                D thisData = (D) thisArray[i];
                if (equalsFunction.test(thatData, thisData)) {
                    thisSize--;
                    thisArray[j] = thisArray[thisSize];
                    buffer[bufferSize++] = thisData;
                    bulkChange.inBoth++;
                    continue outer;
                }
            }
        }
        return newCroppedHashCollisionNode(thisSize != bufferSize, buffer, bufferSize);
    }
}
