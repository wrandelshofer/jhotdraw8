/*
 * @(#)SequencedData.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt.impl.champset;

import org.jhotdraw8.icollection.PersistentVectorList;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeAPI;

/// A `SequencedData` stores a sequence number plus some data.
///
/// `SequencedData` objects are used to store sequenced data in a CHAMP
/// trie (see [Node]).
///
/// The kind of data is specified in concrete implementations of this
/// interface.
///
/// All sequence numbers of `SequencedData` objects in the same CHAMP trie
/// are unique. Sequence numbers range from [Integer#MIN_VALUE] (exclusive)
/// to [Integer#MAX_VALUE] (inclusive).
public interface SequencedData {
    /// We use [Integer#MIN_VALUE] to detect overflows in the sequence number.
    ///
    /// [Integer#MIN_VALUE] is the only integer number which can not
    /// be negated.
    ///
    /// Therefore, we can not use [Integer#MIN_VALUE] as a sequence number
    /// anyway.
    int NO_SEQUENCE_NUMBER = Integer.MIN_VALUE;

    record Result(PersistentVectorList<Object> tree, int offset) {
    }

    static boolean vecMustRenumber(int size, int offset, int vectorSize) {
        return size == 0
                || vectorSize >>> 1 > size
                || (long) offset + size > Integer.MAX_VALUE - 2
                || offset < Integer.MIN_VALUE + 2;
    }


    static <K extends SequencedData> Result vecRemove(PersistentVectorList<Object> vector, K oldElem, int offset) {
        // If the element is the tree, we can remove it and its neighboring tombstones from the vector.
        int size = vector.size();
        int index = oldElem.sequenceNumber() - offset;
        if (index == 0) {
            if (size > 1) {
                Object o = vector.get(1);
                if (o instanceof Tombstone(int neighbors)) {
                    return new Result(
                            FingerTreeAPI.removeRange(vector, 0, 2 + neighbors), offset + 2 + neighbors);
                }
            }
            return new Result(FingerTreeAPI.removeFirst(vector).tree(), offset + 1);
        }

        // If the element is the last, we can remove it and its neighboring tombstones from the vector.
        if (index == size - 1) {
            Object o = vector.get(size - 2);
            if (o instanceof Tombstone(int neighbors)) {
                return new Result(FingerTreeAPI.removeRange(vector, size - 2 - neighbors, size), offset);
            }
            return new Result(FingerTreeAPI.removeLast(vector).tree(), offset);
        }

        // Otherwise, we replace the element with a tombstone. If the elements before or after are
        // already tombstones, we have to replace the boundary tombstones with updated neighbors counts.
        assert index > 0 && index < size - 1 : "offset is out of bounds, offset=" + index + " offset=" + offset + " size=" + size + " sequenceNumber=" + oldElem.sequenceNumber();
        Object before = vector.get(index - 1);
        Object after = vector.get(index + 1);
        if (before instanceof Tombstone tb && after instanceof Tombstone ta) {
            Tombstone boundaryStones = Tombstone.create(2 + tb.neighbors() + ta.neighbors());
            vector = FingerTreeAPI.setAt(vector, index - 1 - tb.neighbors(), boundaryStones).tree();
            vector = FingerTreeAPI.setAt(vector, index, Tombstone.create(0)).tree();
            vector = FingerTreeAPI.setAt(vector, index + 1 + ta.neighbors(), boundaryStones).tree();
        } else if (before instanceof Tombstone tb) {
            Tombstone boundaryStones = Tombstone.create(1 + tb.neighbors());
            vector = FingerTreeAPI.setAt(vector, index - 1 - tb.neighbors(), boundaryStones).tree();
            vector = FingerTreeAPI.setAt(vector, index, boundaryStones).tree();
        } else if (after instanceof Tombstone ta) {
            Tombstone boundaryStones = Tombstone.create(1 + ta.neighbors());
            vector = FingerTreeAPI.setAt(vector, index, boundaryStones).tree();
            vector = FingerTreeAPI.setAt(vector, index + 1 + ta.neighbors(), boundaryStones).tree();
        } else {
            vector = FingerTreeAPI.setAt(vector, index, Tombstone.create(0)).tree();
        }
        assert !(FingerTreeAPI.getFirst(vector) instanceof Tombstone) && !(
                FingerTreeAPI.getLast(vector) instanceof Tombstone);
        return new Result(vector, offset);
    }


    /// Gets the sequence number of the data.
    ///
    /// @return sequence number in the range from [Integer#MIN_VALUE]
    /// (exclusive) to [Integer#MAX_VALUE] (inclusive).
    int sequenceNumber();


}
