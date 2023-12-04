/*
 * @(#)ImmutableSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.immutable_collection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.immutable_collection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.immutable_collection.sequenced.SequencedSet;

import java.util.NoSuchElementException;

/**
 * Interface for an immutable set with a well-defined iteration order; the
 * implementation guarantees that the state of the collection does not change.
 * <p>
 * An immutable sequenced set provides methods for creating a new immutable sequenced set with
 * added or removed elements, without changing the original immutable sequenced set.
 *
 * @param <E> the element type
 */
public interface ImmutableSequencedSet<E> extends ImmutableSet<E>, ReadOnlySequencedSet<E>, ImmutableSequencedCollection<E> {
    @Override
    @NonNull ImmutableSequencedSet<E> add(E element);

    @Override
    default @NonNull ImmutableSequencedSet<E> addAll(@NonNull Iterable<? extends E> c) {
        return (ImmutableSequencedSet<E>) ImmutableSet.super.addAll(c);
    }

    @Override
    @NonNull ImmutableSequencedSet<E> addFirst(final @Nullable E element);

    @Override
    @NonNull ImmutableSequencedSet<E> addLast(final @Nullable E element);

    @Override
    @NonNull ImmutableSequencedSet<E> clear();

    @Override
    @NonNull ImmutableSequencedSet<E> remove(E element);

    @Override
    default @NonNull ImmutableSequencedSet<E> removeAll(@NonNull Iterable<?> c) {
        return (ImmutableSequencedSet<E>) ImmutableSet.super.removeAll(c);
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return a new set instance with the first element removed
     * @throws NoSuchElementException if this set is empty
     */
    @Override
    default ImmutableSequencedSet<E> removeFirst() {
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
    default ImmutableSequencedSet<E> removeLast() {
        return remove(getLast());
    }

    @Override
    default @NonNull ImmutableSequencedSet<E> retainAll(@NonNull Iterable<?> c) {
        return (ImmutableSequencedSet<E>) ImmutableSet.super.retainAll(c);
    }

    @Override
    @NonNull SequencedSet<E> toMutable();

    default @NonNull ImmutableSequencedSet<E> reversed() {
        if (size() < 2) return this;
        return clear().addAll(readOnlyReversed());
    }
}
