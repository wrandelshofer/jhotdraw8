/*
 * @(#)SequencedData.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ2;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.collection.VectorList;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.impl.champ2.BitmapIndexedNode.emptyNode;

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
public interface ChampSequencedData {
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

    static <K extends ChampSequencedData> BitmapIndexedNode<K> buildSequencedTrie(@NonNull BitmapIndexedNode<K> root) {
        BitmapIndexedNode<K> seqRoot = emptyNode();
        ChangeEvent<K> details = new ChangeEvent<>();
        for (ChampSpliterator<K, K> i = new ChampSpliterator<K, K>(root, null, 0, 0); i.moveNext(); ) {
            K elem = i.current();
            seqRoot = seqRoot.update(elem, seqHash(elem.getSequenceNumber()),
                    0, details, (oldK, newK) -> oldK, ChampSequencedData::seqEquals, ChampSequencedData::seqHash);
        }
        return seqRoot;
    }

    /**
     * Returns true if the sequenced elements must be renumbered because
     * {@code first} or {@code last} are at risk of overflowing.
     * <p>
     * {@code first} and {@code last} are estimates of the first and last
     * sequence numbers in the trie. The estimated extent may be larger
     * than the actual extent, but not smaller.
     *
     * @param size  the size of the trie
     * @param first the estimated first sequence number
     * @param last  the estimated last sequence number
     * @return
     */
    static boolean mustRenumber(int size, int first, int last) {
        return size == 0 && (first != -1 || last != 0)
                || last > Integer.MAX_VALUE - 2
                || first < Integer.MIN_VALUE + 2;
    }

    static boolean vecMustRenumber(int size, int offset, int vectorSize) {
        return size == 0
                || vectorSize >>> 1 > size
                || (long) vectorSize - offset > Integer.MAX_VALUE - 2
                || offset < Integer.MIN_VALUE + 2;
    }

    /**
     * Renumbers the sequence numbers in all nodes from {@code 0} to {@code size}.
     * <p>
     * Afterwards the sequence number for the next inserted entry must be
     * set to the value {@code size};
     *
     * @param <K>
     * @param size            the size of the trie
     * @param root            the root of the trie
     * @param sequenceRoot    the sequence root of the trie
     * @param hashFunction    the hash function for data elements
     * @param equalsFunction  the equals function for data elements
     * @param factoryFunction the factory function for data elements
     * @return a new renumbered root
     */
    static <K extends ChampSequencedData> BitmapIndexedNode<K> renumber(int size,
                                                                        @NonNull BitmapIndexedNode<K> root,
                                                                        @NonNull BitmapIndexedNode<K> sequenceRoot,
                                                                        @NonNull ToIntFunction<K> hashFunction,
                                                                        @NonNull BiPredicate<K, K> equalsFunction,
                                                                        @NonNull BiFunction<K, Integer, K> factoryFunction

    ) {
        if (size == 0) {
            return root;
        }
        BitmapIndexedNode<K> newRoot = root;
        ChangeEvent<K> details = new ChangeEvent<>();
        int seq = 0;

        for (var i = new ChampSpliterator<>(sequenceRoot, Function.identity(), 0, 0); i.moveNext(); ) {
            K e = i.current();
            K newElement = factoryFunction.apply(e, seq);
            newRoot = newRoot.update(
                    newElement,
                    Objects.hashCode(e), 0, details,
                    (oldk, newk) -> oldk.getSequenceNumber() == newk.getSequenceNumber() ? oldk : newk,
                    equalsFunction, hashFunction);
            seq++;
        }
        return newRoot;
    }

    /**
     * Renumbers the sequence numbers in all nodes from {@code 0} to {@code size}.
     * <p>
     * Afterward, the sequence number for the next inserted entry must be
     * set to the value {@code size};
     *
     * @param <K>
     * @param size            the size of the trie
     * @param root            the root of the trie
     * @param vector          the sequence root of the trie
     * @param hashFunction    the hash function for data elements
     * @param equalsFunction  the equals function for data elements
     * @param factoryFunction the factory function for data elements
     * @return a new renumbered root and a new vector with matching entries
     */
    @SuppressWarnings("unchecked")
    static <K extends ChampSequencedData> OrderedPair<BitmapIndexedNode<K>, VectorList<Object>> vecRenumber(
            int size,
            @NonNull BitmapIndexedNode<K> root,
            @NonNull VectorList<Object> vector,
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
        for (var i = new ChampVectorSpliterator<K>(vector, o -> (K) o, Long.MAX_VALUE, 0); i.moveNext(); ) {
            K current = i.current();
            K data = factoryFunction.apply(current, seq++);
            renumberedVector = renumberedVector.add(data);
            renumberedRoot = renumberedRoot.update(data, hashFunction.applyAsInt(current), 0, details, forceUpdate, equalsFunction, hashFunction);
        }

        return new OrderedPair<>(renumberedRoot, renumberedVector);
    }

    static <K extends ChampSequencedData> boolean seqEquals(@NonNull K a, @NonNull K b) {
        return a.getSequenceNumber() == b.getSequenceNumber();
    }

    static <K extends ChampSequencedData> int seqHash(K e) {
        return seqHash(e.getSequenceNumber());
    }

    /**
     * Computes a hash code from the sequence number, so that we can
     * use it for iteration in a CHAMP trie.
     * <p>
     * Convert the sequence number to unsigned 32 by adding Integer.MIN_VALUE.
     * Then reorders its bits from 66666555554444433333222221111100 to
     * 00111112222233333444445555566666.
     *
     * @param sequenceNumber a sequence number
     * @return a hash code
     */
    static int seqHash(int sequenceNumber) {
        int u = sequenceNumber + Integer.MIN_VALUE;
        return (u >>> 27)
                | ((u & 0b00000_11111_00000_00000_00000_00000_00) >>> 17)
                | ((u & 0b00000_00000_11111_00000_00000_00000_00) >>> 7)
                | ((u & 0b00000_00000_00000_11111_00000_00000_00) << 3)
                | ((u & 0b00000_00000_00000_00000_11111_00000_00) << 13)
                | ((u & 0b00000_00000_00000_00000_00000_11111_00) << 23)
                | ((u & 0b00000_00000_00000_00000_00000_00000_11) << 30);
    }

    static <K extends ChampSequencedData> BitmapIndexedNode<K> seqRemove(@NonNull BitmapIndexedNode<K> seqRoot,
                                                                         @Nullable K key, @NonNull ChangeEvent<K> details) {
        return seqRoot.remove(
                key, seqHash(key.getSequenceNumber()), 0, details,
                ChampSequencedData::seqEquals);
    }

    static <K extends ChampSequencedData> BitmapIndexedNode<K> seqUpdate(@NonNull BitmapIndexedNode<K> seqRoot,
                                                                         @Nullable K key, @NonNull ChangeEvent<K> details,
                                                                         @NonNull BiFunction<K, K, K> replaceFunction) {
        return seqRoot.update(
                key, seqHash(key.getSequenceNumber()), 0, details,
                replaceFunction,
                ChampSequencedData::seqEquals, ChampSequencedData::seqHash);
    }

    final static ChampTombstone TOMB_ZERO_ZERO = new ChampTombstone(0, 0);

    static <K extends ChampSequencedData> OrderedPair<VectorList<Object>, Integer> vecRemove(VectorList<Object> vector, K oldElem, ChangeEvent<K> details, int offset) {
        // If the element is the first, we can remove it and its neighboring tombstones from the vector.
        int size = vector.size();
        int index = oldElem.getSequenceNumber() + offset;
        if (index == 0) {
            if (size > 1) {
                Object o = vector.get(1);
                if (o instanceof ChampTombstone t) {
                    return new OrderedPair<>(vector.removeRange(0, 2 + t.after()), offset - 2 - t.after());
                }
            }
            return new OrderedPair<>(vector.removeFirst(), offset - 1);
        }

        // If the element is the last , we can remove it and its neighboring tombstones from the vector.
        if (index == size - 1) {
            Object o = vector.get(size - 2);
            if (o instanceof ChampTombstone t) {
                return new OrderedPair<>(vector.removeRange(size - 2 - t.before(), size), offset);
            }
            return new OrderedPair<>(vector.removeLast(), offset);
        }

        // Otherwise, we replace the element with a tombstone, and we update before/after skip counts
        assert index > 0 && index < size - 1;
        Object before = vector.get(index - 1);
        Object after = vector.get(index + 1);
        if (before instanceof ChampTombstone tb && after instanceof ChampTombstone ta) {
            vector = vector.set(index - 1 - tb.before(), new ChampTombstone(0, 2 + tb.before() + ta.after()));
            vector = vector.set(index, TOMB_ZERO_ZERO);
            vector = vector.set(index + 1 + ta.after(), new ChampTombstone(2 + tb.before() + ta.after(), 0));
        } else if (before instanceof ChampTombstone tb) {
            vector = vector.set(index - 1 - tb.before(), new ChampTombstone(0, 1 + tb.before()));
            vector = vector.set(index, new ChampTombstone(1 + tb.before(), 0));
        } else if (after instanceof ChampTombstone ta) {
            vector = vector.set(index, new ChampTombstone(0, 1 + ta.after()));
            vector = vector.set(index + 1 + ta.after(), new ChampTombstone(1 + ta.after(), 0));
        } else {
            vector = vector.set(index, TOMB_ZERO_ZERO);
        }
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
