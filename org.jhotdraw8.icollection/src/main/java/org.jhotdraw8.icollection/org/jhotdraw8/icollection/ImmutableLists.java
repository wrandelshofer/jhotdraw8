package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.immutable.ImmutableList;

/**
 * Provides factory methods for immutable lists.
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
        return VectorList.of();
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
        return VectorList.of(elements);
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
        return elements instanceof ImmutableList<?> ? (ImmutableList<E>) elements : VectorList.<E>of().addAll(elements);
    }
}