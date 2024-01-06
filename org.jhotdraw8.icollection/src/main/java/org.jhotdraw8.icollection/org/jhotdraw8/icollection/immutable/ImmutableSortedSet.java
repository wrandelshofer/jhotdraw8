package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.readonly.ReadOnlySortedSet;

import java.util.NavigableSet;
import java.util.NoSuchElementException;

/**
 * An interface to an immutable sorted set; the implementation guarantees that the state of the collection does not change.
 *
 * @param <E> the element type
 */
public interface ImmutableSortedSet<E> extends ReadOnlySortedSet<E>, ImmutableSet<E> {
    @Override
    @NonNull ImmutableSortedSet<E> add(E element);

    @Override
    default @NonNull ImmutableSortedSet<E> addAll(@NonNull Iterable<? extends E> c) {
        return (ImmutableSortedSet<E>) ImmutableSet.super.addAll(c);
    }

    @Override
    @NonNull ImmutableSortedSet<E> clear();

    @Override
    @NonNull ImmutableSortedSet<E> remove(E element);

    @Override
    default @NonNull ImmutableSortedSet<E> removeAll(@NonNull Iterable<?> c) {
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
    default @NonNull ImmutableSortedSet<E> retainAll(@NonNull Iterable<?> c) {
        return (ImmutableSortedSet<E>) ImmutableSet.super.retainAll(c);
    }

    @Override
    @NonNull NavigableSet<E> toMutable();

    default @NonNull ImmutableSortedSet<E> reversed() {
        if (size() < 2) return this;
        return clear().addAll(readOnlyReversed());
    }    
}
