/*
 * @(#)ImmutableSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;

import java.util.NoSuchElementException;
import java.util.SequencedSet;

/**
 * An interface to an immutable set with a well-defined iteration order; the
 * implementation guarantees that the state of the collection does not change.
 * <p>
 * An interface to an immutable sequenced set provides methods for creating a new immutable sequenced set with
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
    <T> @NonNull ImmutableSequencedSet<T> empty();

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

    /**
     * Returns a reversed copy of this set.
     * <p>
     * This operation may be implemented in O(N).
     * <p>
     * Use {@link #readOnlyReversed()} if you only
     * need to iterate in the reversed sequence over this set.
     *
     * @return a reversed copy of this set.
     */
    default @NonNull ImmutableSequencedSet<E> reverse() {
        if (size() < 2) {
            return this;
        }
        return this.<E>empty().addAll(readOnlyReversed());
    }
}
