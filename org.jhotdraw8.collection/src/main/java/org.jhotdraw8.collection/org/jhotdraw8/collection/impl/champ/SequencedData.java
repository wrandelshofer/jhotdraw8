/*
 * @(#)SequencedData.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.VectorList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.impl.champ.BitmapIndexedNode.emptyNode;

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

    static <K extends SequencedData> BitmapIndexedNode<K> buildSequencedTrie(@NonNull BitmapIndexedNode<K> root, @NonNull IdentityObject mutator) {
        BitmapIndexedNode<K> seqRoot = emptyNode();
        ChangeEvent<K> details = new ChangeEvent<>();
        for (ChampIterator<K> i = new ChampIterator<>(root, null); i.hasNext(); ) {
            K elem = i.next();
            seqRoot = seqRoot.update(mutator, elem, seqHash(elem.getSequenceNumber()),
                    0, details, (oldK, newK) -> oldK, SequencedData::seqEquals, SequencedData::seqHash);
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

    static <K extends SequencedData> VectorList<Object> vecBuildSequencedTrie(@NonNull BitmapIndexedNode<K> root, IdentityObject mutator, int size) {
        ArrayList<K> list = new ArrayList<>(size);
        for (var i = new ChampSpliterator<K, K>(root, Function.identity(), 0, Long.MAX_VALUE); i.moveNext(); ) {
            list.add(i.current());
        }
        list.sort(Comparator.comparing(SequencedData::getSequenceNumber));
        return VectorList.copyOf(list);
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
     * @param size            the size of the trie
     * @param root            the root of the trie
     * @param sequenceRoot    the sequence root of the trie
     * @param mutator         the mutator that will own the renumbered trie
     * @param hashFunction    the hash function for data elements
     * @param equalsFunction  the equals function for data elements
     * @param factoryFunction the factory function for data elements
     * @param <K>
     * @return a new renumbered root
     */
    static <K extends SequencedData> BitmapIndexedNode<K> renumber(int size,
                                                                   @NonNull BitmapIndexedNode<K> root,
                                                                   @NonNull BitmapIndexedNode<K> sequenceRoot,
                                                                   @NonNull IdentityObject mutator,
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
            newRoot = newRoot.update(mutator,
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
     * Afterwards the sequence number for the next inserted entry must be
     * set to the value {@code size};
     *
     * @param sequenceRoot    the sequence root of the trie
     * @param <K>
     * @param size            the size of the trie
     * @param root            the root of the trie
     * @param vector
     * @param mutator         the mutator that will own the renumbered trie
     * @param hashFunction    the hash function for data elements
     * @param equalsFunction  the equals function for data elements
     * @param factoryFunction the factory function for data elements
     * @return a new renumbered root
     */
    @SuppressWarnings("unchecked")
    static <K extends SequencedData> BitmapIndexedNode<K> vecRenumber(int size,
                                                                      @NonNull BitmapIndexedNode<K> root,
                                                                      VectorList<Object> vector, @NonNull IdentityObject mutator,
                                                                      @NonNull ToIntFunction<K> hashFunction,
                                                                      @NonNull BiPredicate<K, K> equalsFunction,
                                                                      @NonNull BiFunction<K, Integer, K> factoryFunction) {
        if (size == 0) {
            return root;
        }
        BitmapIndexedNode<K> newRoot = root;
        ChangeEvent<K> details = new ChangeEvent<>();
        int seq = 0;

        for (var i = new VectorSpliterator<K>(vector, o -> (K) o, 0, 0); i.moveNext(); ) {
            K e = i.current();
            K newElement = factoryFunction.apply(e, seq);
            newRoot = newRoot.update(mutator,
                    newElement,
                    Objects.hashCode(e), 0, details,
                    (oldk, newk) -> oldk.getSequenceNumber() == newk.getSequenceNumber() ? oldk : newk,
                    equalsFunction, hashFunction);
            seq++;
        }
        return newRoot;
    }

    static <K extends SequencedData> boolean seqEquals(@NonNull K a, @NonNull K b) {
        return a.getSequenceNumber() == b.getSequenceNumber();
    }

    static <K extends SequencedData> int seqHash(K e) {
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

    static <K extends SequencedData> BitmapIndexedNode<K> seqRemove(@NonNull BitmapIndexedNode<K> seqRoot, @Nullable IdentityObject mutator,
                                                                    @Nullable K key, @NonNull ChangeEvent<K> details) {
        return seqRoot.remove(mutator,
                key, seqHash(key.getSequenceNumber()), 0, details,
                SequencedData::seqEquals);
    }

    static <K extends SequencedData> BitmapIndexedNode<K> seqUpdate(@NonNull BitmapIndexedNode<K> seqRoot, @Nullable IdentityObject mutator,
                                                                    @Nullable K key, @NonNull ChangeEvent<K> details,
                                                                    @NonNull BiFunction<K, K, K> replaceFunction) {
        return seqRoot.update(mutator,
                key, seqHash(key.getSequenceNumber()), 0, details,
                replaceFunction,
                SequencedData::seqEquals, SequencedData::seqHash);
    }

    final static Tombstone TOMB_ZERO_ZERO = new Tombstone(0, 0);

    static <K extends SequencedData> VectorList<Object> vecRemove(VectorList<Object> vector, IdentityObject mutator, K oldElem, ChangeEvent<K> details, int offset) {
        // If the element is the first, we can remove it and its neighboring tombstones from the vector.
        int size = vector.size();
        int index = oldElem.getSequenceNumber() + offset;
        if (index == 0) {
            if (size > 1) {
                Object o = vector.get(1);
                if (o instanceof Tombstone t) {
                    return vector.removeRange(0, 2 + t.after());
                }
            }
            return vector.removeFirst();
        }

        // If the element is the last , we can remove it and its neighboring tombstones from the vector.
        if (index == size - 1) {
            Object o = vector.get(size - 2);
            if (o instanceof Tombstone t) {
                return vector.removeRange(size - 2 - t.before(), size);
            }
            return vector.removeLast();
        }

        // Otherwise, we replace the element with a tombstone, and we update before/after skip counts
        assert index > 0 && index < size - 1;
        Object before = vector.get(index - 1);
        Object after = vector.get(index + 1);
        if (before instanceof Tombstone tb && after instanceof Tombstone ta) {
            vector = vector.set(index - 1 - tb.before(), new Tombstone(0, 2 + tb.before() + ta.after()));
            vector = vector.set(index, TOMB_ZERO_ZERO);
            vector = vector.set(index + 1 + ta.after(), new Tombstone(2 + tb.before() + ta.after(), 0));
        } else if (before instanceof Tombstone tb) {
            vector = vector.set(index - 1 - tb.before(), new Tombstone(0, 1 + tb.before()));
            vector = vector.set(index, new Tombstone(1 + tb.before(), 0));
        } else if (after instanceof Tombstone ta) {
            vector = vector.set(index, new Tombstone(0, 1 + ta.after()));
            vector = vector.set(index + 1 + ta.after(), new Tombstone(1 + ta.after(), 0));
        } else {
            vector = vector.set(index, TOMB_ZERO_ZERO);
        }
        return vector;
    }

    static <K extends SequencedData> VectorList<Object> vecUpdate(VectorList<Object> newSeqRoot, IdentityObject mutator, K newElem, ChangeEvent<K> details,
                                                                  @NonNull BiFunction<K, K, K> replaceFunction) {
        return newSeqRoot;
    }

    /**
     * Gets the sequence number of the data.
     *
     * @return sequence number in the range from {@link Integer#MIN_VALUE}
     * (exclusive) to {@link Integer#MAX_VALUE} (inclusive).
     */
    int getSequenceNumber();


}
