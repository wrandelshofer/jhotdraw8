package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableNavigableSet;

import java.util.NavigableSet;
import java.util.NoSuchElementException;

/// This interface provides copy-returning operations for a navigable set.
///
/// A navigable set is an ordered group of elements.
/// The elements are ordered by height from a floor element to a ceiling element.
/// The interface allows to navigate from an element to a higher or a lower element.
///
/// A copy-returning operation returns a new copy of the set
/// with changes applied to it. The operation does not change the original
/// set.
///
/// @param <E> the element type
public interface PersistentNavigableSet<E> extends ReadableNavigableSet<E>, PersistentSortedSet<E> {
    @Override
    PersistentNavigableSet<E> adding(E element);

    @Override
    default PersistentNavigableSet<E> addingAll(Iterable<? extends E> c) {
        return (PersistentNavigableSet<E>) PersistentSortedSet.super.addingAll(c);
    }

    @Override
    PersistentNavigableSet<E> cleared();

    @Override
    PersistentNavigableSet<E> removing(E element);

    @Override
    default PersistentNavigableSet<E> removingAll(Iterable<?> c) {
        return (PersistentNavigableSet<E>) PersistentSortedSet.super.removingAll(c);
    }

    /// Returns a copy of this set that contains all elements
    /// of this set except the first.
    ///
    /// @return a new set instance with the first element removed
    /// @throws NoSuchElementException if this set is empty
    @Override
    default PersistentNavigableSet<E> removingFirst() {
        return this.removing(getFirst());
    }

    /// Returns a copy of this set that contains all elements
    /// of this set except the last.
    ///
    /// @return a new set instance with the last element removed
    /// @throws NoSuchElementException if this set is empty
    @Override
    default PersistentNavigableSet<E> removingLast() {
        return this.removing(getLast());
    }

    @Override
    default PersistentNavigableSet<E> retainingAll(Iterable<?> c) {
        return (PersistentNavigableSet<E>) PersistentSortedSet.super.retainingAll(c);
    }

    @Override
    NavigableSet<E> toMutable();

    default PersistentNavigableSet<E> reversed() {
        if (size() < 2) {
            return this;
        }
        return this.<E>cleared().addingAll(readableReversed());
    }

}
