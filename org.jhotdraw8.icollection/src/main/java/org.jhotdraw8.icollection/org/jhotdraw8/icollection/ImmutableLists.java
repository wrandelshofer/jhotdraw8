package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.impl.iteration.IntRangeIterator;

/**
 * Provides static factory methods for {@link ImmutableList}s.
 */
public class ImmutableLists {
    /**
     * Don't let anyone instantiate this class.
     */
    private ImmutableLists() {
    }

    /**
     * Returns an empty immutable list.
     *
     * @param <E> the element type
     * @return empty immutable list
     */
    public static <E> ImmutableList<E> of() {
        return SimpleImmutableList.of();
    }

    /**
     * Returns a new immutable list with the specified elements.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return immutable list of the specified elements
     */
    @SafeVarargs
    public static <E> ImmutableList<E> of(E... elements) {
        return SimpleImmutableList.of(elements);
    }

    /**
     * Returns an immutable list with the specified elements.
     * <p>
     * If the provided list can be cast to {@link ImmutableList} it will be cast,
     * otherwise a new list will be created.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return immutable list of the specified elements
     */
    public static <E> ImmutableList<E> copyOf(Iterable<E> elements) {
        return elements instanceof ImmutableList<?> ? (ImmutableList<E>) elements : SimpleImmutableList.<E>of().addAll(elements);
    }

    /**
     * Creates a List of int numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * ImmutableLists.range(0, 0)  // = ImmutableList()
     * ImmutableLists.range(2, 0)  // = ImmutableList()
     * ImmutableLists.range(-2, 2) // = ImmutableList(-2, -1, 0, 1)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of int values as specified or the empty range if {@code from >= toExclusive}
     */
    public static ImmutableList<Integer> range(int from, int toExclusive) {
        return copyOf(() -> new IntRangeIterator(from, toExclusive - 1));
    }

    /**
     * Creates a List of int numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * ImmutableLists.rangeClosed(0, 0)  // = ImmutableList(0)
     * ImmutableLists.rangeClosed(2, 0)  // = ImmutableList()
     * ImmutableLists.rangeClosed(-2, 2) // = ImmutableList(-2, -1, 0, 1, 2)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number + 1
     * @return a range of int values as specified or the empty range if {@code from >= toExclusive}
     */
    public static ImmutableList<Integer> rangeClosed(int from, int toInclusive) {
        return copyOf(() -> new IntRangeIterator(from, toInclusive));
    }

    /**
     * Creates a Stream of int numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Stream.rangeBy(1, 3, 1)  // = Stream(1, 2)
     * Stream.rangeBy(1, 4, 2)  // = Stream(1, 3)
     * Stream.rangeBy(4, 1, -2) // = Stream(4, 2)
     * Stream.rangeBy(4, 1, 2)  // = Stream()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @param step        the step
     * @return a range of long values as specified or {@code Nil} if<br>
     * {@code from >= toInclusive} and {@code step > 0} or<br>
     * {@code from <= toInclusive} and {@code step < 0}
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static ImmutableList<Integer> rangeBy(int from, int toExclusive, int step) {
        return copyOf(() -> new IntRangeIterator(from, toExclusive - 1, step));
    }

}
