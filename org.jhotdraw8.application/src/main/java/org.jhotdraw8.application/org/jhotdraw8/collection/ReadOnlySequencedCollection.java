/*
 * @(#)ReadOnlySequencedCollection.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

/**
 * Read-only interface for a collection with a well-defined linear ordering of its elements.
 *
 * @param <E> the element type
 */
public interface ReadOnlySequencedCollection<E> extends ReadOnlyCollection<E> {
    /**
     * Gets the first element.
     *
     * @return an element
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    default E getFirst() {
        return iterator().next();
    }

    /**
     * Gets the last element.
     *
     * @return an element
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    E getLast();
}
