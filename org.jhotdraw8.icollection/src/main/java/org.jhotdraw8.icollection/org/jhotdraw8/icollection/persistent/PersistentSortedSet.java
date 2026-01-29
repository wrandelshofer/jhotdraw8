package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableSortedSet;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

/**
 * An interface to an persistent sorted set; the implementation guarantees that the state of the collection does not change.
 *
 * @param <E> the element type
 */
public interface PersistentSortedSet<E> extends ReadableSortedSet<E>, PersistentSet<E> {
    @Override
    PersistentSortedSet<E> add(E element);

    @Override
    default PersistentSortedSet<E> addAll(Iterable<? extends E> c) {
        return (PersistentSortedSet<E>) PersistentSet.super.addAll(c);
    }

    @Override
    <T> PersistentSortedSet<T> empty();

    /**
     * Returns a copy of this collection that is empty, and has the specified
     * type and comparator.
     *
     * @param comparator a comparator for ordering the elements of the set,
     *                   specify {@code null} to use the natural order of the elements
     * @param <T>        the element type of the collection
     * @return an empty collection of the specified type and comparator
     */
    <T> PersistentCollection<T> empty(@Nullable Comparator<T> comparator);

    @Override
    PersistentSortedSet<E> remove(E element);

    @Override
    default PersistentSortedSet<E> removeAll(Iterable<?> c) {
        return (PersistentSortedSet<E>) PersistentSet.super.removeAll(c);
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return a new set instance with the first element removed
     * @throws NoSuchElementException if this set is empty
     */
    default PersistentSortedSet<E> removeFirst() {
        return remove(getFirst());
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the last.
     *
     * @return a new set instance with the last element removed
     * @throws NoSuchElementException if this set is empty
     */
    default PersistentSortedSet<E> removeLast() {
        return remove(getLast());
    }

    @Override
    default PersistentSortedSet<E> retainAll(Iterable<?> c) {
        return (PersistentSortedSet<E>) PersistentSet.super.retainAll(c);
    }

    @Override
    NavigableSet<E> toMutable();

    default PersistentSortedSet<E> reversed() {
        if (size() < 2) {
            return this;
        }
        return this.<E>empty().addAll(readableReversed());
    }    
}
