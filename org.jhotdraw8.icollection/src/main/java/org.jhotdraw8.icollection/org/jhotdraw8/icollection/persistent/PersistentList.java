/*
 * @(#)PersistentList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableList;
import org.jspecify.annotations.Nullable;

import java.util.List;

/// This interface provides copy-returning operations for a list.
///
/// A list is an indexed sequence of elements.
///
/// A copy-returning operation returns a new copy of the list
/// with changes applied to it. The operation does not change the original
/// list.
///
/// @param <E> the element type
public interface PersistentList<E> extends ReadableList<E>, PersistentSequencedCollection<E> {
    /// Returns a copy of this list that is empty.
    ///
    /// @return this list instance if it is already empty, or a different list
    /// instance that is empty.
    @SuppressWarnings("unchecked")
    @Override
    PersistentList<E> cleared();

    @Override
    PersistentList<E> addingFirst(@Nullable E element);

    @Override
    PersistentList<E> addingLast(@Nullable E element);

    @Override
    default PersistentList<E> removingFirst() {
        return (PersistentList<E>) PersistentSequencedCollection.super.removingFirst();
    }

    @Override
    default PersistentList<E> removingLast() {
        return (PersistentList<E>) PersistentSequencedCollection.super.removingLast();
    }

    /// Returns a copy of this list that contains all elements
    /// of this list and the specified element appended to the
    /// end of the list.
    ///
    /// @param element an element
    /// @return a different list instance with the element added
    @Override
    PersistentList<E> adding(E element);

    /// Returns a copy of this list that contains all elements
    /// of this list and the specified element appended to the
    /// end of the list.
    ///
    /// @param index   the insertion index
    /// @param element an element
    /// @return a different list instance with the element added
    PersistentList<E> addingAt(int index, E element);

    /// Returns a copy of this list that contains all elements
    /// of this list and all elements of the specified
    /// collection appended.
    ///
    /// @param c a collection to be added to this list
    /// @return a different list instance with the elements added
    @Override
    PersistentList<E> addingAll(Iterable<? extends E> c);

    /// Returns a copy of this list that contains all elements
    /// of this list and all elements of the specified
    /// collection appended.
    ///
    /// @param index the insertion index
    /// @param c     a collection to be added to this list
    /// @return a different list instance with the elements added
    PersistentList<E> addingAllAt(int index, Iterable<? extends E> c);

    /// Returns a copy of this list that contains all elements
    /// of this list except the specified element.
    ///
    /// @param element an element
    /// @return this list instance if it already does not contain the element, or
    /// a different list instance with the element removed
    @Override
    PersistentList<E> removing(@Nullable E element);

    /// Returns a copy of this list that contains all elements
    /// of this list except the element at the specified index
    ///
    /// @param index an index
    /// @return a different list instance with the element removed
    PersistentList<E> removingAt(int index);

    /// Returns a copy of this list that contains all elements
    /// of this list except the elements in the specified range.
    ///
    /// @param fromIndex from index (inclusive) of the sub-list
    /// @param toIndex   to index (exclusive) of the sub-list
    /// @return a different list instance with the element removed
    PersistentList<E> removingRange(int fromIndex, int toIndex);

    /// Returns a copy of this list that contains all elements
    /// of this list except the elements of the specified
    /// collection.
    ///
    /// @param c a collection with elements to be removed from this set
    /// @return this list instance if it already does not contain the elements, or
    /// a different list instance with the elements removed
    @Override
    PersistentList<E> removingAll(Iterable<?> c);

    /// Returns a copy of this list that contains only elements
    /// that are in this list and in the specified collection.
    ///
    /// @param c a collection with elements to be retained in this set
    /// @return this list instance if it has not changed, or
    /// a different list instance with elements removed
    @Override
    PersistentList<E> retainingAll(Iterable<?> c);

    /// Returns a reversed copy of this list.
    ///
    /// This operation may be implemented in O(N).
    ///
    /// Use [#readableReversed()] if you only
    /// need to iterate in the reversed sequence over this list.
    ///
    /// @return a reversed copy of this list.
    PersistentList<E> reversed();

    /// Returns a copy of this list that contains all elements
    /// of this list and the specified element replaced.
    ///
    /// @param element an element
    /// @return this list instance if it has not changed, or
    /// a different list instance with the element changed
    PersistentList<E> settingAt(int index, E element);

    /// Returns a copy of this list that contains only
    /// the elements in the given index range.
    ///
    /// @param fromIndex from index (inclusive) of the sub-list
    /// @param toIndex   to index (exclusive) of the sub-list
    /// @return this list instance if it has not changed, or
    /// a different list instance with the element changed
    @Override
    PersistentList<E> readableSubList(int fromIndex, int toIndex);

    /// Returns a mutable copy of this list.
    ///
    /// @return a mutable copy.
    List<E> toMutable();
}
