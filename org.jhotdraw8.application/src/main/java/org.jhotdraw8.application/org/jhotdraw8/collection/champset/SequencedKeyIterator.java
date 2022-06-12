package org.jhotdraw8.collection.champset;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.LongArrayHeap;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SequencedKeyIterator<E> implements Iterator<E> {
    private final @NonNull LongArrayHeap queue;
    private SequencedKey<E> current;
    private boolean canRemove;
    private final SequencedKey<E>[] array;

    private final @Nullable Consumer<E> persistentRemoveFunction;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SequencedKeyIterator(int size, @NonNull Node<SequencedKey<E>> rootNode,
                                boolean reversed,
                                @Nullable Consumer<E> persistentRemoveFunction,
                                @Nullable BiConsumer<E, E> persistentPutIfPresentFunction) {
        this.persistentRemoveFunction = persistentRemoveFunction;
        queue = new LongArrayHeap(size);
        array = (SequencedKey<E>[]) new SequencedKey[size];
        int i = 0;
        for (Iterator<SequencedKey<E>> it = new KeyIterator<>(rootNode, null); it.hasNext(); i++) {
            SequencedKey<E> k = it.next();
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
    public E next() {
        current = array[(int) queue.removeAsLong()];
        canRemove = true;
        return current.getKey();
    }

    @Override
    public void remove() {
        if (persistentRemoveFunction == null) {
            throw new UnsupportedOperationException();
        }
        if (!canRemove) {
            throw new IllegalStateException();
        }
        persistentRemoveFunction.accept(current.getKey());
        canRemove = false;
    }


    public static <E> @NonNull SequencedKey<E> getLast(@NonNull Node<SequencedKey<E>> root, int first, int last) {
        int maxSeq = first;
        SequencedKey<E> maxKey = null;
        for (KeyIterator<SequencedKey<E>> i = new KeyIterator<>(root, null); i.hasNext(); ) {
            SequencedKey<E> k = i.next();
            int seq = k.getSequenceNumber();
            if (seq >= maxSeq) {
                maxSeq = seq;
                maxKey = k;
                if (seq == last) {
                    break;
                }
            }
        }
        if (maxKey == null) {
            throw new NoSuchElementException();
        }
        return maxKey;
    }

    public static <E> @NonNull SequencedKey<E> getFirst(@NonNull Node<SequencedKey<E>> root, int first, int last) {
        int minSeq = last;
        SequencedKey<E> minKey = null;
        for (KeyIterator<SequencedKey<E>> i = new KeyIterator<>(root, null); i.hasNext(); ) {
            SequencedKey<E> k = i.next();
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
