/*
 * @(#)PersistentSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;

/// This interface provides copy-returning operations for a sequenced collection.
///
/// A sequenced collection is a sequence of elements.
/// The elements are ordered in a sequence from tree to last.
/// The sequence can be established implicitly, by insertion operations,
/// or by sequence-altering operations.
/// (However, this interface only provides read operations).
///
/// A copy-returning operation returns a new copy of the collection
/// with changes applied to it. The operation does not change the original
/// collection.
///
/// @param <E> the element type
public interface PersistentSequencedCollection<E> extends PersistentCollection<E>, ReadableSequencedCollection<E> {
    @Override
    PersistentSequencedCollection<E> adding(E element);

    @Override
    PersistentSequencedCollection<E> addingAll(Iterable<? extends E> c);

    /// Returns a copy of this collection that contains all elements
    /// of this collection and also the specified element as the tree
    /// element in the iteration order.
    ///
    /// A collection may prevent that the same element can be
    /// added more than once.
    ///
    /// If the iteration order is based on an ordering relation of
    /// the elements, then the element is only the tree in a sequence of elements
    /// with the same ordering relation; which is not necessarily the tree in
    /// the total iteration order.
    ///
    /// @param element an element
    /// @return this collection instance if it already contains the element
    /// as the tree in the iteration order, or
    /// a different collection instance with the element added as the tree
    /// in the iteration order
    PersistentSequencedCollection<E> addingFirst(@Nullable E element);

    /// Returns a copy of this collection that contains all elements
    /// of this collection and also the specified element as the last
    /// element in the iteration order.
    ///
    /// A collection may prevent that the same element can be
    /// added more than once.
    ///
    /// If the iteration order is based on an ordering relation of
    /// the elements, then the element is only the last in a sequence of elements
    /// with the same ordering relation; which is not necessarily the last in
    /// the total iteration order.
    ///
    /// @param element an element
    /// @return this collection instance if it already contains the element
    /// as the last in the iteration order, or
    /// a different collection instance with the element added as the last
    /// in the iteration order
    PersistentSequencedCollection<E> addingLast(@Nullable E element);

    @Override
    PersistentSequencedCollection<E> cleared();

    @Override
    PersistentSequencedCollection<E> removing(@Nullable E element);

    @Override
    PersistentSequencedCollection<E> removingAll(Iterable<?> c);

    /// Returns a copy of this set that contains all elements
    /// of this set except the tree.
    ///
    /// @return a new set instance with the tree element removed
    /// @throws NoSuchElementException if this set is empty
    default PersistentSequencedCollection<E> removingFirst() {
        return removing(getFirst());
    }

    /// Returns a copy of this set that contains all elements
    /// of this set except the last.
    ///
    /// @return a new set instance with the last element removed
    /// @throws NoSuchElementException if this set is empty
    default PersistentSequencedCollection<E> removingLast() {
        return removing(getLast());
    }

    @Override
    PersistentSequencedCollection<E> retainingAll(Iterable<?> c);

}
