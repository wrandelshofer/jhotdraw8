package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.LongArrayHeap;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

public class SequencedIterator<E extends Sequenced, X> implements Iterator<X> {
    private final @NonNull LongArrayHeap queue;
    private E current;
    private boolean canRemove;
    private final E[] array;
    private final @NonNull Function<E, X> mappingFunction;
    private final @Nullable Consumer<E> persistentRemoveFunction;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SequencedIterator(int size, @NonNull Node<? extends E> rootNode,
                             boolean reversed,
                             @Nullable Consumer<E> persistentRemoveFunction,
                             @NonNull Function<E, X> mappingFunction) {
        this.persistentRemoveFunction = persistentRemoveFunction;
        this.mappingFunction = mappingFunction;
        queue = new LongArrayHeap(size);
        array = (E[]) new Sequenced[size];
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
        if (persistentRemoveFunction == null) {
            throw new UnsupportedOperationException();
        }
        if (!canRemove) {
            throw new IllegalStateException();
        }
        persistentRemoveFunction.accept(current);
        canRemove = false;
    }


    public static <E extends Sequenced> @NonNull E getLast(@NonNull Node<? extends E> root, int first, int last) {
        int maxSeq = first;
        E maxKey = null;
        for (KeyIterator<? extends E> i = new KeyIterator<>(root, null); i.hasNext(); ) {
            E k = i.next();
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

    public static <E extends Sequenced> @NonNull E getFirst(@NonNull Node<? extends E> root, int first, int last) {
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
