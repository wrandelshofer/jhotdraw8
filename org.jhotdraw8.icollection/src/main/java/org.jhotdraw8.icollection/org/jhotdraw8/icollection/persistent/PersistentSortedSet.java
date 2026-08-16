package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableSortedSet;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

/// This interface provides copy-returning operations for a sorted set.
///
/// A sorted set is a sequence of distinct elements.
/// The elements are sorted from first to last.
///
/// A copy-returning operation returns a new copy of the set
/// with changes applied to it. The operation does not change the original
/// set.
///
/// @param <E> the element type
public interface PersistentSortedSet<E> extends ReadableSortedSet<E>, PersistentSet<E> {
    @Override
    PersistentSortedSet<E> adding(E element);

    @Override
    default PersistentSortedSet<E> addingAll(Iterable<? extends E> c) {
        return (PersistentSortedSet<E>) PersistentSet.super.addingAll(c);
    }

    @Override
    <T> PersistentSortedSet<T> cleared();

    /// Returns a copy of this collection that is empty, and has the specified
    /// type and comparator.
    ///
    /// @param comparator a comparator for ordering the elements of the set,
    ///                   specify `null` to use the natural order of the elements
    /// @param <T>        the element type of the collection
    /// @return an empty collection of the specified type and comparator
    <T> PersistentCollection<T> cleared(@Nullable Comparator<T> comparator);

    @Override
    PersistentSortedSet<E> removing(E element);

    @Override
    default PersistentSortedSet<E> removingAll(Iterable<?> c) {
        return (PersistentSortedSet<E>) PersistentSet.super.removingAll(c);
    }

    /// Returns a copy of this set that contains all elements
    /// of this set except the first.
    ///
    /// @return a new set instance with the first element removed
    /// @throws NoSuchElementException if this set is empty
    default PersistentSortedSet<E> removingFirst() {
        return this.removing(getFirst());
    }

    /// Returns a copy of this set that contains all elements
    /// of this set except the last.
    ///
    /// @return a new set instance with the last element removed
    /// @throws NoSuchElementException if this set is empty
    default PersistentSortedSet<E> removingLast() {
        return this.removing(getLast());
    }

    @Override
    default PersistentSortedSet<E> retainingAll(Iterable<?> c) {
        return (PersistentSortedSet<E>) PersistentSet.super.retainingAll(c);
    }

    @Override
    NavigableSet<E> toMutable();

    default PersistentSortedSet<E> reversed() {
        if (size() < 2) {
            return this;
        }
        return this.<E>cleared().addingAll(readableReversed());
    }
}
