/*
 * @(#)HeapSequencedIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.precondition.Preconditions;
import org.jhotdraw8.collection.primitive.LongArrayHeap;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Iterates over {@link SequencedData} elements in a CHAMP trie in the
 * order of the sequence numbers.
 * <p>
 * Uses a {@link LongArrayHeap} and a data array for
 * ordering the elements. This approach uses more memory than
 * a {@link java.util.PriorityQueue} but is about twice as fast.
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>new instance: O(N)</li>
 *     <li>iterator.next: O(log N)</li>
 * </ul>
 *
 * @param <E> the type parameter of the  CHAMP trie {@link Node}s
 * @param <X> the type parameter of the {@link Iterator} interface
 */
class HeapSequencedIterator<E extends SequencedData, X> implements Iterator<X> {
    private final @NonNull LongArrayHeap queue;
    private E current;
    private boolean canRemove;
    private final E[] array;
    private final @NonNull Function<E, X> mappingFunction;
    private final @Nullable Consumer<E> removeFunction;

    /**
     * Constructs a new instance.
     *
     * @param size            the size of the trie
     * @param rootNode        the root node of the trie
     * @param reversed        whether to iterate in the reversed sequence
     * @param removeFunction  this function is called when {@link Iterator#remove()}
     *                        is called
     * @param mappingFunction mapping function from {@code E} to {@code X}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public HeapSequencedIterator(int size, @NonNull Node<? extends E> rootNode,
                                 boolean reversed,
                                 @Nullable Consumer<E> removeFunction,
                                 @NonNull Function<E, X> mappingFunction) {
        Preconditions.checkArgument(size >= 0, "size=%s", size);

        this.removeFunction = removeFunction;
        this.mappingFunction = mappingFunction;
        queue = new LongArrayHeap(size);
        array = (E[]) new SequencedData[size];
        int i = 0;
        for (Iterator<? extends E> it = new KeyIterator<>(rootNode, null); it.hasNext(); i++) {
            E k = it.next();
            array[i] = k;
            int sequenceNumber = k.getSequenceNumber();
            queue.addAsLong(((long) (reversed ? -sequenceNumber : sequenceNumber) << 32) | i);
        }
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public X next() {
        current = array[(int) queue.removeAsLong()];
        canRemove = true;
        return mappingFunction.apply(current);
    }

    @Override
    public void remove() {
        if (removeFunction == null) {
            throw new UnsupportedOperationException();
        }
        if (!canRemove) {
            throw new IllegalStateException();
        }
        removeFunction.accept(current);
        canRemove = false;
    }


    public static <E extends SequencedData> @NonNull E getLast(@NonNull Node<? extends E> root, int first, int last) {
        int maxSeq = first;
        E maxKey = null;
        for (KeyIterator<? extends E> i = new KeyIterator<>(root, null); i.hasNext(); ) {
            E k = i.next();
            int seq = k.getSequenceNumber();
            if (seq >= maxSeq) {
                maxSeq = seq;
                maxKey = k;
                if (seq == last - 1) {
                    break;
                }
            }
        }
        if (maxKey == null) {
            throw new NoSuchElementException();
        }
        return maxKey;
    }

    public static <E extends SequencedData> @NonNull E getFirst(@NonNull Node<? extends E> root, int first, int last) {
        int minSeq = last;
        E minKey = null;
        for (KeyIterator<? extends E> i = new KeyIterator<>(root, null); i.hasNext(); ) {
            E k = i.next();
            int seq = k.getSequenceNumber();
            if (seq <= minSeq) {
                minSeq = seq;
                minKey = k;
                if (seq == first) {
                    break;
                }
            }
        }
        if (minKey == null) {
            throw new NoSuchElementException();
        }
        return minKey;
    }
}
