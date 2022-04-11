package org.jhotdraw8.collection;

import org.jhotdraw8.util.function.AddToIntSet;

/**
 * A collection of integer elements that contains no duplicates.
 */
public interface IntSet extends AddToIntSet {

    /**
     * Removes the specified element from the set.
     *
     * @param e an element
     * @return true if this set contained the element
     */
    boolean removeAsInt(int e);

    /**
     * Checks if this set contains the specified element.
     *
     * @param e an element
     * @return true if this set contains the element.
     */
    boolean containsAsInt(int e);

    /**
     * Clears the set.
     */
    void clear();
}
