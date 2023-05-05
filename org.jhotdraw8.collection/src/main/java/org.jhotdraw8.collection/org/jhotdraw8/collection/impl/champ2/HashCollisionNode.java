/*
 * @(#)HashCollisionNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ2;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ListHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
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

    @Override
    protected @NonNull Node<D> putAll(Node<D> otherNode, int shift, @NonNull BulkChangeEvent bulkChange, @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        HashCollisionNode<D> that = (HashCollisionNode<D>) otherNode;
        if (that == this) {
            bulkChange.inBoth += dataArity();
            return this;
        }

        List<Object> list = new ArrayList<>(this.dataArity() + that.dataArity());

        // Step 1: Add all this.data to list
        list.addAll(Arrays.asList(this.data));

        // Step 2: Add all that.keys to list which are not in this.keys
        //         This is quadratic.
        //         If the sets are disjoint, we can do nothing about it.
        //         If the sets intersect, we can mark those which are
        //         equal in a bitset, so that we do not need to check
        //         them over and over again.
        BitSet bs = new BitSet(that.data.length);
        outer:
        for (int j = 0; j < that.data.length; j++) {
            @SuppressWarnings("unchecked")
            D thatKey = (D) that.data[j];
            for (int i = bs.nextClearBit(0); i >= 0 && i < this.data.length; i = bs.nextClearBit(i + 1)) {
                @SuppressWarnings("unchecked")
                D thisKey = (D) this.data[i];
                if (equalsFunction.test(thatKey, thisKey)) {
                    list.set(i, updateFunction.apply(thisKey, thatKey));
                    bs.set(i);
                    bulkChange.inBoth++;
                    continue outer;
                }
            }
            list.add(thatKey);
        }

        if (list.size() > this.data.length) {
            @SuppressWarnings("unchecked")
            HashCollisionNode<D> unchecked = newHashCollisionNode(hash, list.toArray());
            return unchecked;
        }

        return this;
    }

    @Override
    protected @NonNull Node<D> removeAll(Node<D> otherNode, int shift, @NonNull BulkChangeEvent bulkChange, @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        return null;
    }

    @Override
    protected @NonNull Node<D> retainAll(Node<D> otherNode, int shift, @NonNull BulkChangeEvent bulkChange, @NonNull BiFunction<D, D, D> updateFunction, @NonNull BiPredicate<D, D> equalsFunction, @NonNull ToIntFunction<D> hashFunction, @NonNull ChangeEvent<D> details) {
        return null;
    }
}
