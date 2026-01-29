package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableNavigableSet;

import java.util.NavigableSet;
import java.util.NoSuchElementException;

/**
 * An interface to an persistent navigable set; the implementation guarantees that the state of the collection does not change.
 *
 * @param <E> the element type
 */
public interface PersistentNavigableSet<E> extends ReadableNavigableSet<E>, PersistentSortedSet<E> {
    @Override
    PersistentNavigableSet<E> add(E element);

    @Override
    default PersistentNavigableSet<E> addAll(Iterable<? extends E> c) {
        return (PersistentNavigableSet<E>) PersistentSortedSet.super.addAll(c);
    }

    @Override
    <T> PersistentNavigableSet<T> empty();

    @Override
    PersistentNavigableSet<E> remove(E element);

    @Override
    default PersistentNavigableSet<E> removeAll(Iterable<?> c) {
        return (PersistentNavigableSet<E>) PersistentSortedSet.super.removeAll(c);
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return a new set instance with the first element removed
     * @throws NoSuchElementException if this set is empty
     */
    @Override
    default PersistentNavigableSet<E> removeFirst() {
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
    default PersistentNavigableSet<E> removeLast() {
        return remove(getLast());
    }

    @Override
    default PersistentNavigableSet<E> retainAll(Iterable<?> c) {
        return (PersistentNavigableSet<E>) PersistentSortedSet.super.retainAll(c);
    }

    @Override
    NavigableSet<E> toMutable();

    default PersistentNavigableSet<E> reversed() {
        if (size() < 2) {
            return this;
        }
        return this.<E>empty().addAll(readableReversed());
    }
    
}
