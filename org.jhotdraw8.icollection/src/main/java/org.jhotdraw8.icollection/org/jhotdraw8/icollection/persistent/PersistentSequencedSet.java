/*
 * @(#)PersistentSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.SequencedSet;

/**
 * An interface to an persistent set with a well-defined iteration order; the
 * implementation guarantees that the state of the collection does not change.
 * <p>
 * An interface to an persistent sequenced set provides methods for creating a new persistent sequenced set with
 * added or removed elements, without changing the original persistent sequenced set.
 *
 * @param <E> the element type
 */
public interface PersistentSequencedSet<E> extends PersistentSet<E>, ReadableSequencedSet<E>, PersistentSequencedCollection<E> {
    @Override
    PersistentSequencedSet<E> add(E element);

    @Override
    default PersistentSequencedSet<E> addAll(Iterable<? extends E> c) {
        return (PersistentSequencedSet<E>) PersistentSet.super.addAll(c);
    }

    @Override
    PersistentSequencedSet<E> addFirst(final @Nullable E element);

    @Override
    PersistentSequencedSet<E> addLast(final @Nullable E element);

    @Override
    <T> PersistentSequencedSet<T> empty();

    @Override
    PersistentSequencedSet<E> remove(E element);

    @Override
    default PersistentSequencedSet<E> removeAll(Iterable<?> c) {
        return (PersistentSequencedSet<E>) PersistentSet.super.removeAll(c);
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return a new set instance with the first element removed
     * @throws NoSuchElementException if this set is empty
     */
    @Override
    default PersistentSequencedSet<E> removeFirst() {
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
    default PersistentSequencedSet<E> removeLast() {
        return remove(getLast());
    }

    @Override
    default PersistentSequencedSet<E> retainAll(Iterable<?> c) {
        return (PersistentSequencedSet<E>) PersistentSet.super.retainAll(c);
    }

    @Override
    SequencedSet<E> toMutable();

    /**
     * Returns a reversed copy of this set.
     * <p>
     * This operation may be implemented in O(N).
     * <p>
     * Use {@link #readableReversed()} if you only
     * need to iterate in the reversed sequence over this set.
     *
     * @return a reversed copy of this set.
     */
    default PersistentSequencedSet<E> reverse() {
        if (size() < 2) {
            return this;
        }
        return this.<E>empty().addAll(readableReversed());
    }
}
