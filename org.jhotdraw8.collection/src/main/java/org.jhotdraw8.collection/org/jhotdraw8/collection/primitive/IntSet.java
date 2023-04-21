/*
 * @(#)IntSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

/**
 * Interface for a collection of int-valued elements that contains no duplicates.
 */
public interface IntSet {
    /**
     * Adds the specified element to the set if it is not already present.
     *
     * @param e element to be added to the set
     * @return {@code true} if this set did not already contain the specified
     * element
     */
    boolean addAsInt(int e);

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
