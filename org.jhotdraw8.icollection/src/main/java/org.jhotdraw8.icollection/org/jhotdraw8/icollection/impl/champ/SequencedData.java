/*
 * @(#)SequencedData.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.vector.BitMappedTrie;

import java.util.Spliterators;
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
            @Nullable IdentityObject owner, int size, int sizeWithTombstones,
            @NonNull BitmapIndexedNode<K> root,
            @NonNull BitMappedTrie<Object> vector,
            @NonNull ToIntFunction<K> hashFunction,
            @NonNull BiPredicate<K, K> equalsFunction,
            @NonNull BiFunction<K, Integer, K> factoryFunction) {
        if (size == 0) {
            new OrderedPair<>(root, vector);
        }
        BitmapIndexedNode<K> renumberedRoot = root;
        VectorList<Object> renumberedVector = VectorList.of();
        ChangeEvent<K> details = new ChangeEvent<>();
        BiFunction<K, K, K> forceUpdate = (oldk, newk) -> newk;
        int seq = 0;
        for (var i = Spliterators.iterator(new TombSkippingVectorSpliterator<K>(vector, o -> (K) o, 0, size, sizeWithTombstones, 0)); i.hasNext(); ) {
            K current = i.next();
            K data = factoryFunction.apply(current, seq++);
            renumberedVector = renumberedVector.add(data);
            renumberedRoot = renumberedRoot.put(owner, data, hashFunction.applyAsInt(current), 0, details, forceUpdate, equalsFunction, hashFunction);
        }

        return new OrderedPair<>(renumberedRoot, renumberedVector);
    }


    static <K extends SequencedData> OrderedPair<VectorList<Object>, Integer> vecRemove(VectorList<Object> vector, K oldElem, int offset) {
        // If the element is the first, we can remove it and its neighboring tombstones from the vector.
        int size = vector.size();
        int index = oldElem.getSequenceNumber() + offset;
        if (index == 0) {
            if (size > 1) {
                Object o = vector.get(1);
                if (o instanceof Tombstone t) {
                    return new OrderedPair<>(vector.removeRange(0, 2 + t.after()), offset - 2 - t.after());
                }
            }
            return new OrderedPair<>(vector.removeFirst(), offset - 1);
        }

        // If the element is the last , we can remove it and its neighboring tombstones from the vector.
        if (index == size - 1) {
            Object o = vector.get(size - 2);
            if (o instanceof Tombstone t) {
                return new OrderedPair<>(vector.removeRange(size - 2 - t.before(), size), offset);
            }
            return new OrderedPair<>(vector.removeLast(), offset);
        }

        // Otherwise, we replace the element with a tombstone, and we update before/after skip counts
        assert index > 0 && index < size - 1;
        Object before = vector.get(index - 1);
        Object after = vector.get(index + 1);
        if (before instanceof Tombstone tb && after instanceof Tombstone ta) {
            vector = vector.set(index - 1 - tb.before(), Tombstone.create(0, 2 + tb.before() + ta.after()));
            vector = vector.set(index, Tombstone.create(0, 0));
            vector = vector.set(index + 1 + ta.after(), Tombstone.create(2 + tb.before() + ta.after(), 0));
        } else if (before instanceof Tombstone tb) {
            vector = vector.set(index - 1 - tb.before(), Tombstone.create(0, 1 + tb.before()));
            vector = vector.set(index, Tombstone.create(1 + tb.before(), 0));
        } else if (after instanceof Tombstone ta) {
            vector = vector.set(index, Tombstone.create(0, 1 + ta.after()));
            vector = vector.set(index + 1 + ta.after(), Tombstone.create(1 + ta.after(), 0));
        } else {
            vector = vector.set(index, Tombstone.create(0, 0));
        }
        assert !(vector.getFirst() instanceof Tombstone) && !(vector.getLast() instanceof Tombstone);
        return new OrderedPair<>(vector, offset);
    }


    /**
     * Gets the sequence number of the data.
     *
     * @return sequence number in the range from {@link Integer#MIN_VALUE}
     * (exclusive) to {@link Integer#MAX_VALUE} (inclusive).
     */
    int getSequenceNumber();


}
