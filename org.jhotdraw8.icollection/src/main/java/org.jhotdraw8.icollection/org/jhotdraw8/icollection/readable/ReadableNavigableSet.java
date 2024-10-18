package org.jhotdraw8.icollection.readable;

import org.jspecify.annotations.Nullable;

/**
 * A readable interface to a navigable set.
 *
 * @param <E> the element type
 */
public interface ReadableNavigableSet<E> extends ReadableSortedSet<E> {
    /**
     * Returns the least element in this set greater than or equal to the given element,
     * or null if there is no such element.
     *
     * @param e the given element
     * @return ceiling element or null
     */
    @Nullable E ceiling(E e);

    /**
     * Returns the greatest element in this set less than or equal to the given element,
     * or null if there is no such element.
     *
     * @param e the given element
     * @return floor element or null
     */
    @Nullable E floor(E e);

    /**
     * Returns the least element in this set greater than the given element,
     * or null if there is no such element.
     *
     * @param e the given element
     * @return higher element or null
     */
    @Nullable E higher(E e);

    /**
     * Returns the greatest element in this set less than the given element,
     * or null if there is no such element.
     *
     * @param e the given element
     * @return lower element or null
     */
    @Nullable E lower(E e);
}
