package org.jhotdraw8.icollection.readable;

import org.jspecify.annotations.Nullable;

/// This interface provides read operations for a navigable set.
///
/// A navigable set is an ordered group of elements.
/// The elements are ordered by height from a floor element to a ceiling element.
/// The interface allows to navigate from an element to a higher or a lower element.
///
/// A read operation returns data about the set.
/// The operation does not change the original set.
///
/// @param <E> the element type
public interface ReadableNavigableSet<E> extends ReadableSortedSet<E> {
    /// Returns the least element in this set greater than or equal to the given element,
    /// or null if there is no such element.
    ///
    /// @param e the given element
    /// @return ceiling element or null
    @Nullable E ceiling(E e);

    /// Returns the greatest element in this set less than or equal to the given element,
    /// or null if there is no such element.
    ///
    /// @param e the given element
    /// @return floor element or null
    @Nullable E floor(E e);

    /// Returns the least element in this set greater than the given element,
    /// or null if there is no such element.
    ///
    /// @param e the given element
    /// @return higher element or null
    @Nullable E higher(E e);

    /// Returns the greatest element in this set less than the given element,
    /// or null if there is no such element.
    ///
    /// @param e the given element
    /// @return lower element or null
    @Nullable E lower(E e);
}
