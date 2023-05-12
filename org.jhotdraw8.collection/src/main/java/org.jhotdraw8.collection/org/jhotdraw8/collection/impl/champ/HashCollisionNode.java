/*
 * @(#)HashCollisionNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.ListHelper;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.impl.champ.NodeFactory.newHashCollisionNode;

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
    @NonNull
    Object getNodeRaw(int index) {
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
    Node<D> remove(@Nullable IdentityObject owner, D data,
                   int dataHash, int shift, @NonNull ChangeEvent<D> details, @NonNull BiPredicate<D, D> equalsFunction) {
        for (int idx = 0, i = 0; i < this.data.length; i += 1, idx++) {
            if (equalsFunction.test((D) this.data[i], data)) {
                @SuppressWarnings("unchecked") D currentVal = (D) this.data[i];
                details.setRemoved(currentVal);

                if (this.data.length == 1) {
                    return BitmapIndexedNode.emptyNode();
                } else if (this.data.length == 2) {
                    // Create root node with singleton element.
                    // This node will either be the new root
                    // returned, or be unwrapped and inlined.
                    return NodeFactory.newBitmapIndexedNode(owner, 0, bitpos(mask(dataHash, 0)),
                            new Object[]{getData(idx ^ 1)});
                }
                // copy keys and remove 1 element at position idx
                Object[] entriesNew = ListHelper.copyComponentRemove(this.data, idx, 1);
                if (isAllowedToUpdate(owner)) {
                    this.data = entriesNew;
                    return this;
                }
                return newHashCollisionNode(owner, dataHash, entriesNew);
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    Node<D> put(@Nullable IdentityObject owner, D newData,
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
                if (isAllowedToUpdate(owner)) {
                    this.data[i] = updatedData;
                    return this;
                }
                final Object[] newKeys = ListHelper.copySet(this.data, i, updatedData);
                return newHashCollisionNode(owner, dataHash, newKeys);
            }
        }

        // copy entries and add 1 more at the end
        Object[] entriesNew = ListHelper.copyComponentAdd(this.data, this.data.length, 1);
        entriesNew[this.data.length] = newData;
        details.setAdded(newData);
        if (isAllowedToUpdate(owner)) {
            this.data = entriesNew;
            return this;
        }
        return newHashCollisionNode(owner, dataHash, entriesNew);
    }

    @Override
    protected int calculateSize() {
        return dataArity();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull Node<D> putAll(@Nullable IdentityObject owner, Node<D> otherNode, int shift, @NonNull BulkChangeEvent bulkChange, @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        if (otherNode == this) {
            bulkChange.inBoth += dataArity();
            return this;
        }
        HashCollisionNode<D> that = (HashCollisionNode<D>) otherNode;

        // The buffer initially contains all data elements from this node.
        // Every time we find a matching data element in both nodes, we do not need to ever look at that data element again.
        // So, we swap it out with a data element from the end of unprocessed data elements, and subtract 1 from unprocessedSize.
        // If that node contains a data element that is not in this node, we add it to the end, and add 1 to bufferSize.
        // Buffer content:
        // 0..unprocessedSize-1 = unprocessed data elements from this node
        // unprocessedSize..resultSize-1 = data elements that we have updated from that node, or that we have added from that node.
        final int thisSize = this.dataArity();
        final int thatSize = that.dataArity();
        Object[] buffer = Arrays.copyOf(this.data, thisSize + thatSize);
        System.arraycopy(this.data, 0, buffer, 0, this.data.length);
        Object[] thatArray = that.data;
        int resultSize = thisSize;
        int unprocessedSize = thisSize;
        boolean updated = false;
        outer:
        for (int i = 0; i < thatSize; i++) {
            D thatData = (D) thatArray[i];
            for (int j = 0; j < unprocessedSize; j++) {
                D thisData = (D) buffer[j];
                if (equalsFunction.test(thatData, thisData)) {
                    D swap = (D) buffer[--unprocessedSize];
                    D updatedData = updateFunction.apply(thisData, thatData);
                    updated |= updatedData != thisData;
                    buffer[unprocessedSize] = updatedData;
                    buffer[j] = swap;
                    bulkChange.inBoth++;
                    continue outer;
                }
            }
            buffer[resultSize++] = thatData;
        }
        return newCroppedHashCollisionNode(updated | resultSize != thisSize, buffer, resultSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull Node<D> removeAll(@Nullable IdentityObject owner, @NonNull Node<D> otherNode, int shift, @NonNull BulkChangeEvent bulkChange, @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        if (otherNode == this) {
            bulkChange.removed += dataArity();
            return (Node<D>) EMPTY;
        }
        HashCollisionNode<D> that = (HashCollisionNode<D>) otherNode;

        // The buffer initially contains all data elements from this node.
        // Every time we find a data element that must be removed, we replace it with the last element from the
        // result part of the buffer, and reduce resultSize by 1.
        // Buffer content:
        // 0..resultSize-1 = data elements from this node that have not been removed
        final int thisSize = this.dataArity();
        final int thatSize = that.dataArity();
        int resultSize = thisSize;
        Object[] buffer = this.data.clone();
        Object[] thatArray = that.data;
        outer:
        for (int i = 0; i < thatSize && resultSize > 0; i++) {
            D thatData = (D) thatArray[i];
            for (int j = 0; j < resultSize; j++) {
                D thisData = (D) buffer[j];
                if (equalsFunction.test(thatData, thisData)) {
                    buffer[j] = buffer[--resultSize];
                    bulkChange.removed++;
                    continue outer;
                }
            }
        }
        return newCroppedHashCollisionNode(thisSize != resultSize, buffer, resultSize);
    }

    @NonNull
    private HashCollisionNode<D> newCroppedHashCollisionNode(boolean changed, Object[] buffer, int size) {
        if (changed) {
            if (buffer.length != size) {
                buffer = Arrays.copyOf(buffer, size);
            }
            return new HashCollisionNode<>(hash, buffer);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull Node<D> retainAll(IdentityObject owner, Node<D> otherNode, int shift, @NonNull BulkChangeEvent bulkChange, @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        if (otherNode == this) {
            bulkChange.removed += dataArity();
            return (Node<D>) EMPTY;
        }
        HashCollisionNode<D> that = (HashCollisionNode<D>) otherNode;

        // The buffer initially contains all data elements from this node.
        // Every time we find a data element that must be retained, we swap it into the result-part of the buffer.
        // 0..resultSize-1 = data elements from this node that must be retained
        // resultSize..thisSize-1 = data elements that might need to be retained
        final int thisSize = this.dataArity();
        final int thatSize = that.dataArity();
        int resultSize = 0;
        Object[] buffer = this.data.clone();
        Object[] thatArray = that.data;
        outer:
        for (int i = 0; i < thatSize && thisSize != resultSize; i++) {
            D thatData = (D) thatArray[i];
            for (int j = resultSize; j < thisSize; j++) {
                D thisData = (D) buffer[j];
                if (equalsFunction.test(thatData, thisData)) {
                    D swap = (D) buffer[resultSize];
                    buffer[resultSize++] = thisData;
                    buffer[j] = swap;
                    continue outer;
                }
            }
            bulkChange.removed++;
        }
        return newCroppedHashCollisionNode(thisSize != resultSize, buffer, resultSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull Node<D> filterAll(@Nullable IdentityObject owner, Predicate<? super D> predicate, int shift, @NonNull BulkChangeEvent bulkChange) {
        final int thisSize = this.dataArity();
        int resultSize = 0;
        Object[] buffer = new Object[thisSize];
        Object[] thisArray = this.data;
        outer:
        for (int i = 0; i < thisSize; i++) {
            D thisData = (D) thisArray[i];
            if (predicate.test(thisData)) {
                buffer[resultSize++] = thisData;
            } else {
                bulkChange.removed++;
            }
        }
        return newCroppedHashCollisionNode(thisSize != resultSize, buffer, resultSize);
    }
}
