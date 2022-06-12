/*
 * @(#)ReadOnlySequencedCollection.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

/**
 * Read-only interface for a collection with a well-defined iteration order.
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
    default E getLast() {
        return readOnlyReversed().iterator().next();
    }


    /**
     * Returns a reversed-order view of this collection.
     * Changes to the underlying collection are visible in the reversed view.
     *
     * @return a reversed-order view of this collection
     */
    @NonNull ReadOnlySequencedCollection<E> readOnlyReversed();
}
