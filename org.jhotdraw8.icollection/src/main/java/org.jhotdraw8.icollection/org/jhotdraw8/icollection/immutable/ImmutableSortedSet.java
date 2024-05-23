package org.jhotdraw8.icollection.immutable;

import org.jspecify.annotations.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlySortedSet;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

/**
 * An interface to an immutable sorted set; the implementation guarantees that the state of the collection does not change.
 *
 * @param <E> the element type
 */
public interface ImmutableSortedSet<E> extends ReadOnlySortedSet<E>, ImmutableSet<E> {
    @Override
    ImmutableSortedSet<E> add(E element);

    @Override
    default ImmutableSortedSet<E> addAll(Iterable<? extends E> c) {
        return (ImmutableSortedSet<E>) ImmutableSet.super.addAll(c);
    }

    @Override
    <T> ImmutableSortedSet<T> empty();

    /**
     * Returns a copy of this collection that is empty, and has the specified
     * type and comparator.
     *
     * @param comparator a comparator for ordering the elements of the set,
     *                   specify {@code null} to use the natural order of the elements
     * @param <T>        the element type of the collection
     * @return an empty collection of the specified type and comparator
     */
    <T> ImmutableCollection<T> empty(@Nullable Comparator<T> comparator);

    @Override
    ImmutableSortedSet<E> remove(E element);

    @Override
    default ImmutableSortedSet<E> removeAll(Iterable<?> c) {
        return (ImmutableSortedSet<E>) ImmutableSet.super.removeAll(c);
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return a new set instance with the first element removed
     * @throws NoSuchElementException if this set is empty
     */
    default ImmutableSortedSet<E> removeFirst() {
        return remove(getFirst());
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the last.
     *
     * @return a new set instance with the last element removed
     * @throws NoSuchElementException if this set is empty
     */
    default ImmutableSortedSet<E> removeLast() {
        return remove(getLast());
    }

    @Override
    default ImmutableSortedSet<E> retainAll(Iterable<?> c) {
        return (ImmutableSortedSet<E>) ImmutableSet.super.retainAll(c);
    }

    @Override
    NavigableSet<E> toMutable();

    default ImmutableSortedSet<E> reversed() {
        if (size() < 2) {
            return this;
        }
        return this.<E>empty().addAll(readOnlyReversed());
    }    
}
