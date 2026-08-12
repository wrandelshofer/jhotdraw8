package org.jhotdraw8.icollection.readable;

import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/// This interface provides read operations for a sorted set.
///
/// A sorted set is a sequence of distinct elements.
/// The elements are sorted from first to last.
///
/// A read operation returns data about the set.
/// The operation does not change the original set.
///
/// @param <E> the element type
public interface ReadableSortedSet<E> extends ReadableSequencedSet<E> {
    /// Returns the comparator used to order the elements in this set, or `null` if this set uses
    /// the natural ordering of its elements.
    ///
    /// @return comparator or null
    @Nullable
    Comparator<? super E> comparator();

}
