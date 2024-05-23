package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.icollection.readonly.ReadOnlyNavigableSet;

import java.util.NavigableSet;
import java.util.NoSuchElementException;

/**
 * An interface to an immutable navigable set; the implementation guarantees that the state of the collection does not change.
 *
 * @param <E> the element type
 */
public interface ImmutableNavigableSet<E> extends ReadOnlyNavigableSet<E>, ImmutableSortedSet<E> {
    @Override
    ImmutableNavigableSet<E> add(E element);

    @Override
    default ImmutableNavigableSet<E> addAll(Iterable<? extends E> c) {
        return (ImmutableNavigableSet<E>) ImmutableSortedSet.super.addAll(c);
    }

    @Override
    <T> ImmutableNavigableSet<T> empty();

    @Override
    ImmutableNavigableSet<E> remove(E element);

    @Override
    default ImmutableNavigableSet<E> removeAll(Iterable<?> c) {
        return (ImmutableNavigableSet<E>) ImmutableSortedSet.super.removeAll(c);
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return a new set instance with the first element removed
     * @throws NoSuchElementException if this set is empty
     */
    @Override
    default ImmutableNavigableSet<E> removeFirst() {
        return remove(getFirst());
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the last.
     *
     * @return a new set instance with the last element removed
     * @throws NoSuchElementException if this set is empty
     */
    @Override
    default ImmutableNavigableSet<E> removeLast() {
        return remove(getLast());
    }

    @Override
    default ImmutableNavigableSet<E> retainAll(Iterable<?> c) {
        return (ImmutableNavigableSet<E>) ImmutableSortedSet.super.retainAll(c);
    }

    @Override
    NavigableSet<E> toMutable();

    default ImmutableNavigableSet<E> reversed() {
        if (size() < 2) {
            return this;
        }
        return this.<E>empty().addAll(readOnlyReversed());
    }
    
}
