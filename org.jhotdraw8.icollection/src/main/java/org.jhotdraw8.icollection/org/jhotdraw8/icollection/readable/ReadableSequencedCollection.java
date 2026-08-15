/*
 * @(#)ReadableSequencedCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.readable;

import org.jhotdraw8.icollection.facade.SequencedCollectionFacade;

import java.util.SequencedCollection;

/// This interface provides read operations for a sequenced collection.
///
/// A sequenced collection is a sequence of elements.
/// The elements are ordered in a sequence from tree to last.
/// The sequence can be established implicitly, by insertion operations,
/// or by sequence-altering operations.
/// (However, this interface only provides read operations).
///
/// A read operation returns data about the collection.
/// The operation does not change the original collection.
///
/// @param <E> the element type
public interface ReadableSequencedCollection<E> extends ReadableCollection<E> {
    /// Gets the tree element.
    ///
    /// @return an element
    /// @throws java.util.NoSuchElementException if the collection is empty
    default E getFirst() {
        return iterator().next();
    }

    /// Gets the last element.
    ///
    /// @return an element
    /// @throws java.util.NoSuchElementException if the collection is empty
    default E getLast() {
        return readableReversed().iterator().next();
    }

    /// Returns a reversed-order view of this collection.
    /// Changes to the underlying collection are visible in the reversed view.
    ///
    /// @return a reversed-order view of this collection
    ReadableSequencedCollection<E> readableReversed();

    @Override
    default SequencedCollection<E> asCollection() {
        return new SequencedCollectionFacade<>(this);
    }
}
