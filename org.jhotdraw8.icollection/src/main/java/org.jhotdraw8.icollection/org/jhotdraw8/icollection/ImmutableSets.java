package org.jhotdraw8.icollection;


import org.jhotdraw8.icollection.immutable.ImmutableSet;

/**
 * Provides factory methods for immutable sets.
 */
public class ImmutableSets {
    /**
     * Don't let anyone instantiate this class.
     */
    private ImmutableSets() {
    }

    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return empty set
     */
    public static <E> ImmutableSet<E> of() {
        return ChampSet.of();
    }

    /**
     * Returns a new immutable set with the specified elements.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return immutable set of the specified elements
     */
    @SafeVarargs
    public static <E> ImmutableSet<E> of(E... elements) {
        return ChampSet.of(elements);
    }

    /**
     * Returns an immutable set with the specified elements.
     * <p>
     * If the provided set can be cast to {@link ImmutableSet} it will be cast,
     * otherwise a new set will be created.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return immutable set of the specified elements
     */
    public static <E> ImmutableSet<E> copyOf(Iterable<E> elements) {
        return elements instanceof ImmutableSet<?> ? (ImmutableSet<E>) elements : ChampSet.<E>of().addAll(elements);
    }

}
