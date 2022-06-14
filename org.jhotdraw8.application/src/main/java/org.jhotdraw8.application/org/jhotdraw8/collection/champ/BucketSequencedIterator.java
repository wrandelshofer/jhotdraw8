package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.util.Preconditions;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Iterates over {@link Sequenced} elements in a CHAMP trie in the
 * order of the sequence numbers.
 * <p>
 * Uses a bucket array for ordering the elements. The size of the
 * array is {@code last - first} sequence number.
 * This approach is fast, if the sequence numbers are dense,
 * that is when {@literal last - first <= size * 4}.
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>new instance: O(N)</li>
 *     <li>iterator.next: O(l1)</li>
 * </ul>
 *
 * @param <E> the type parameter of the  CHAMP trie {@link Node}s
 * @param <X> the type parameter of the {@link Iterator} interface
 */
public class BucketSequencedIterator<E extends Sequenced, X> implements Iterator<X> {
    private int next;
    private int remaining;
    private final E[] buckets;
    private final Function<E, X> mappingFunction;
    private final Consumer<E> removeFunction;

    /**
     * Creates a new instance.
     *
     * @param size            the size of the trie
     * @param first           a sequence number which is smaller or equal the first sequence
     *                        number in the trie
     * @param last            a sequence number which is greater or equal the last sequence
     *                        number in the trie
     * @param rootNode        the root node of the trie
     * @param reversed        whether to iterate in the reversed sequence
     * @param removeFunction  this function is called when {@link Iterator#remove()}
     *                        is called
     * @param mappingFunction mapping function from {@code E} to {@code X}
     * @throws IllegalArgumentException if {@code last - first} is greater than
     *                                  {@link Integer#MAX_VALUE}.
     * @throws IllegalArgumentException if {@code size} is negative or
     *                                  greater than {@code last - first}..
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BucketSequencedIterator(int size, int first, int last, @NonNull Node<? extends E> rootNode,
                                   boolean reversed,
                                   @Nullable Consumer<E> removeFunction,
                                   @NonNull Function<E, X> mappingFunction) {
        int extent = last - first;
        Preconditions.checkArgument(extent >= 0, "first=%s, last=%s", first, last);
        Preconditions.checkArgument(0 <= size && size <= extent, "size=%s", size);
        if (size == 0) {
            buckets = null;
            this.removeFunction = null;
            this.mappingFunction = null;
            return;
        }
        this.removeFunction = removeFunction;
        this.mappingFunction = mappingFunction;
        this.remaining = size;
        buckets = (E[]) new Sequenced[last - first + 1];
        if (reversed) {
            for (Iterator<? extends E> it = new KeyIterator<>(rootNode, null); it.hasNext(); ) {
                E k = it.next();
                buckets[last - k.getSequenceNumber() - first] = k;
            }
        } else {
            for (Iterator<? extends E> it = new KeyIterator<>(rootNode, null); it.hasNext(); ) {
                E k = it.next();
                buckets[k.getSequenceNumber() - first] = k;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return remaining > 0;
    }

    @Override
    public X next() {
        if (remaining == 0) {
            throw new NoSuchElementException();
        }
        E current;
        do {
            current = buckets[next++];
        } while (current == null);
        remaining--;
        return mappingFunction.apply(current);
    }

    @Override
    public void remove() {
        if (removeFunction == null) {
            throw new UnsupportedOperationException();
        }
        if (next < 0 || buckets[next - 1] == null) {
            throw new IllegalStateException();
        }
        E current = buckets[next - 1];
        buckets[next - 1] = null;
        removeFunction.accept(current);
    }

    public static boolean isSuitedForBucketSequencedIterator(int size, int first, int last) {
        int extent = last - first;
        return size >= 0 && extent >= 0 && size <= extent && extent <= size << 2;
    }
}
