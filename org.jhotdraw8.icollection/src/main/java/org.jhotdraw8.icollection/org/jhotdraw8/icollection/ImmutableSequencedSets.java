package org.jhotdraw8.icollection;


import org.jhotdraw8.icollection.immutable.ImmutableSequencedSet;

/**
 * Provides factory methods for {@link ImmutableSequencedSet}s.
 */
public class ImmutableSequencedSets {
    /**
     * Don't let anyone instantiate this class.
     */
    private ImmutableSequencedSets() {
    }

    /**
     * Returns an empty immutable sequenced set.
     *
     * @param <E> the element type
     * @return empty set
     */
    public static <E> ImmutableSequencedSet<E> of() {
        return VectorSet.of();
    }

    /**
     * Returns a immutable sequenced set with the specified elements.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return immutable sequenced set of the specified elements
     */
    @SafeVarargs
    public static <E> ImmutableSequencedSet<E> of(E... elements) {
        return VectorSet.of(elements);
    }

    /**
     * Returns an immutable sequenced set with the specified elements.
     * <p>
     * If the provided iterable can be cast to {@link ImmutableSequencedSet} it will be cast,
     * otherwise a new set will be created.
     *
     * @param elements the specified elements
     * @param <E>      the element type
     * @return immutable sequenced set of the specified elements
     */
    public static <E> ImmutableSequencedSet<E> copyOf(Iterable<E> elements) {
        return elements instanceof ImmutableSequencedSet<?>
                ? (ImmutableSequencedSet<E>) elements :
                VectorSet.<E>of().addAll(elements);
    }

}
