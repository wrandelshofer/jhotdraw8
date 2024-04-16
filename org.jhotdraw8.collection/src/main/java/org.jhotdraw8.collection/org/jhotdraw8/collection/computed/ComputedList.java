
package org.jhotdraw8.collection.computed;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.exception.SizeLimitExceededException;
import org.jhotdraw8.icollection.readonly.AbstractReadOnlyList;

import java.util.function.LongFunction;

import static java.util.Objects.checkFromToIndex;
import static java.util.Objects.checkIndex;

/**
 * A read-only list over values that are computed from the indices of the list.
 */
public class ComputedList<E> extends AbstractReadOnlyList<E> {
    private final boolean descending;
    private final long size;
    private final long from;
    private final long to;
    private final @NonNull LongFunction<E> function;

    /**
     * Constructs a new instance.
     *
     * @param size     the size of the list
     * @param function the function that computes an element for a given index
     */
    public ComputedList(long size, @NonNull LongFunction<E> function) {
        this(size, function, false);
    }

    /**
     * Constructs a new instance.
     *
     * @param size       the size of the list
     * @param function   the function that computes an element for a given index
     * @param descending whether to list should contain the elements in descending order
     */
    public ComputedList(long size, LongFunction<E> function, boolean descending) {
        this(0, size, function, descending);
    }

    /**
     * Constructs a new instance.
     *
     * @param from     the start of the range
     * @param to       the end of the range (exclusive)
     * @param function the function that computes an element for a given index
     */
    public ComputedList(long from, long to, LongFunction<E> function) {
        this(from, to, function, false);
    }

    /**
     * Constructs a new instance.
     *
     * @param from       the start of the range
     * @param to         the end of the range (exclusive)
     * @param function   the function that computes an element for a given index
     * @param descending whether to list should contain the elements in descending order
     */
    public ComputedList(long from, long to, LongFunction<E> function, boolean descending) {
        this.function = function;
        this.descending = descending;
        this.from = from;
        this.to = to;
        try {
            this.size = Math.abs(Math.subtractExact(from, to));
        } catch (ArithmeticException e) {
            throw new SizeLimitExceededException("from=" + from + " to=" + to, e);
        }

    }

    @Override
    public int size() {
        return size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
    }

    //@Override
    public long sizeAsLong() {
        return size;
    }

    @Override
    public E get(int index) {
        checkIndex(index, size());
        return function.apply(offset(index));
    }

    private long offset(int index) {
        return descending ? to - index - 1 : from + index;
    }


    @Override
    public @NonNull ComputedList<E> readOnlySubList(int fromIndex, int toIndex) {
        checkFromToIndex(fromIndex, toIndex, size);
        return new ComputedList<>(offset(fromIndex), offset(toIndex), function, descending);
    }

    @Override
    public @NonNull ComputedList<E> readOnlyReversed() {
        return new ComputedList<>(from, to, function, !descending);
    }
}
