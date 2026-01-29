/*
 * @(#)ReadableSequencedCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.readable;

import org.jhotdraw8.icollection.facade.SequencedCollectionFacade;

import java.util.SequencedCollection;

/**
 * A readable interface to a sequenced collection.
 * A sequenced collection has a well-defined encounter order,
 * that supports operations at both ends, and that is reversible.
 *
 * @param <E> the element type
 */
public interface ReadableSequencedCollection<E> extends ReadableCollection<E> {
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
        return readableReversed().iterator().next();
    }


    /**
     * Returns a reversed-order view of this collection.
     * Changes to the underlying collection are visible in the reversed view.
     *
     * @return a reversed-order view of this collection
     */
    ReadableSequencedCollection<E> readableReversed();

    @Override
    default SequencedCollection<E> asCollection() {
        return new SequencedCollectionFacade<>(this);
    }
}
