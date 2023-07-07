/*
 * @(#)SequencedData.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.impl.IdentityObject;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

/**
 * A {@code SequencedData} stores a sequence number plus some data.
 * <p>
 * {@code SequencedData} objects are used to store sequenced data in a CHAMP
 * trie (see {@link Node}).
 * <p>
 * The kind of data is specified in concrete implementations of this
 * interface.
 * <p>
 * All sequence numbers of {@code SequencedData} objects in the same CHAMP trie
 * are unique. Sequence numbers range from {@link Integer#MIN_VALUE} (exclusive)
 * to {@link Integer#MAX_VALUE} (inclusive).
 */
public interface SequencedData {
    /**
     * We use {@link Integer#MIN_VALUE} to detect overflows in the sequence number.
     * <p>
     * {@link Integer#MIN_VALUE} is the only integer number which can not
     * be negated.
     * <p>
     * Therefore, we can not use {@link Integer#MIN_VALUE} as a sequence number
     * anyway.
     */
    int NO_SEQUENCE_NUMBER = Integer.MIN_VALUE;


    static boolean vecMustRenumber(int size, int offset, int vectorSize) {
        return size == 0
                || vectorSize >>> 1 > size
                || (long) vectorSize - offset > Integer.MAX_VALUE - 2
                || offset < Integer.MIN_VALUE + 2;
    }

    /**
     * Renumbers the sequence numbers in all nodes from {@code 0} to {@code size}.
     * <p>
     * Afterward, the sequence number for the next inserted entry must be
     * set to the value {@code size};
     *
     * @param <K>
     * @param owner
     * @param size            the size of the trie
     * @param root            the root of the trie
     * @param vector          the sequence root of the trie
     * @param hashFunction    the hash function for data elements
     * @param equalsFunction  the equals function for data elements
     * @param factoryFunction the factory function for data elements
     * @return a new renumbered root and a new vector with matching entries
     */
    @SuppressWarnings("unchecked")
    static <K extends SequencedData> OrderedPair<BitmapIndexedNode<K>, VectorList<Object>> vecRenumber(
            @Nullable IdentityObject owner, int size,
            @NonNull BitmapIndexedNode<K> root,
            @NonNull VectorList<Object> vector,
            @NonNull ToIntFunction<K> hashFunction,
            @NonNull BiPredicate<K, K> equalsFunction,
            @NonNull BiFunction<K, Integer, K> factoryFunction) {
        if (size == 0) {
            new SimpleOrderedPair<>(root, vector);
        }
        BitmapIndexedNode<K> renumberedRoot = root;
        VectorList<Object> renumberedVector = VectorList.of();
        ChangeEvent<K> details = new ChangeEvent<>();
        BiFunction<K, K, K> forceUpdate = (oldk, newk) -> newk;
        int seq = 0;
        for (var i = new VectorSpliterator<K>(vector, o -> (K) o, 0, Long.MAX_VALUE, 0); i.moveNext(); ) {
            K current = i.current();
            K data = factoryFunction.apply(current, seq++);
            renumberedVector = renumberedVector.add(data);
            renumberedRoot = renumberedRoot.put(owner, data, hashFunction.applyAsInt(current), 0, details, forceUpdate, equalsFunction, hashFunction);
        }

        return new SimpleOrderedPair<>(renumberedRoot, renumberedVector);
    }


    final static VectorTombstone TOMB_ZERO_ZERO = new VectorTombstone(0, 0);

    static <K extends SequencedData> OrderedPair<VectorList<Object>, Integer> vecRemove(VectorList<Object> vector, K oldElem, int offset) {
        // If the element is the first, we can remove it and its neighboring tombstones from the vector.
        int size = vector.size();
        int index = oldElem.getSequenceNumber() + offset;
        if (index == 0) {
            if (size > 1) {
                Object o = vector.get(1);
                if (o instanceof VectorTombstone t) {
                    return new SimpleOrderedPair<>(vector.removeRange(0, 2 + t.after()), offset - 2 - t.after());
                }
            }
            return new SimpleOrderedPair<>(vector.removeFirst(), offset - 1);
        }

        // If the element is the last , we can remove it and its neighboring tombstones from the vector.
        if (index == size - 1) {
            Object o = vector.get(size - 2);
            if (o instanceof VectorTombstone t) {
                return new SimpleOrderedPair<>(vector.removeRange(size - 2 - t.before(), size), offset);
            }
            return new SimpleOrderedPair<>(vector.removeLast(), offset);
        }

        // Otherwise, we replace the element with a tombstone, and we update before/after skip counts
        assert index > 0 && index < size - 1;
        Object before = vector.get(index - 1);
        Object after = vector.get(index + 1);
        if (before instanceof VectorTombstone tb && after instanceof VectorTombstone ta) {
            vector = vector.set(index - 1 - tb.before(), new VectorTombstone(0, 2 + tb.before() + ta.after()));
            vector = vector.set(index, TOMB_ZERO_ZERO);
            vector = vector.set(index + 1 + ta.after(), new VectorTombstone(2 + tb.before() + ta.after(), 0));
        } else if (before instanceof VectorTombstone tb) {
            vector = vector.set(index - 1 - tb.before(), new VectorTombstone(0, 1 + tb.before()));
            vector = vector.set(index, new VectorTombstone(1 + tb.before(), 0));
        } else if (after instanceof VectorTombstone ta) {
            vector = vector.set(index, new VectorTombstone(0, 1 + ta.after()));
            vector = vector.set(index + 1 + ta.after(), new VectorTombstone(1 + ta.after(), 0));
        } else {
            vector = vector.set(index, TOMB_ZERO_ZERO);
        }
        assert !(vector.getFirst() instanceof VectorTombstone) && !(vector.getLast() instanceof VectorTombstone);
        return new SimpleOrderedPair<>(vector, offset);
    }

    /**
     * Gets the sequence number of the data.
     *
     * @return sequence number in the range from {@link Integer#MIN_VALUE}
     * (exclusive) to {@link Integer#MAX_VALUE} (inclusive).
     */
    int getSequenceNumber();


}
