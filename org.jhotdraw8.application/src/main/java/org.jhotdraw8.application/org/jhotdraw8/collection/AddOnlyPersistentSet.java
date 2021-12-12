package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

/**
 * Provides an API for a persistent set that supports the addition
 * of an element.
 * <p>
 * Implementations are not required to implement equals and hashCode methods
 * for comparing sets.
 *
 * @param <E> the element type
 */
public interface AddOnlyPersistentSet<E> {
    /**
     * Returns a persistent set that contains all elements
     * of this set and also the specified element.
     *
     * @param element an element
     * @return the same set if it already contains the element, or
     * a different set with the element added
     */
    @NonNull AddOnlyPersistentSet<E> copyAdd(@NonNull E element);

}
