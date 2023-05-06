/*
 * @(#)ImmutableSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedCollection;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Interface for an immutable sequenced collection.
 * <p>
 * An immutable sequenced collection provides methods for creating a new immutable sequenced collection with
 * added or removed elements, without changing the original immutable sequenced collection.
 *
 * @param <E> the element type
 */
public interface ImmutableSequencedCollection<E> extends ImmutableCollection<E>, ReadOnlySequencedCollection<E> {
    @NonNull ImmutableSequencedCollection<E> add(E element);

    @NonNull ImmutableSequencedCollection<E> addAll(@NonNull Iterable<? extends E> c);

    @NonNull ImmutableSequencedCollection<E> addFirst(final @Nullable E key);

    @NonNull ImmutableSequencedCollection<E> addLast(final @Nullable E key);

    @NonNull ImmutableSequencedCollection<E> clear();

    @NonNull ImmutableSequencedCollection<E> remove(E element);

    @NonNull ImmutableSequencedCollection<E> removeAll(@NonNull Iterable<?> c);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return a new set instance with the first element removed
     * @throws NoSuchElementException if this set is empty
     */
    default ImmutableSequencedCollection<E> removeFirst() {
        return remove(getFirst());
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the last.
     *
     * @return a new set instance with the last element removed
     * @throws NoSuchElementException if this set is empty
     */
    default ImmutableSequencedCollection<E> removeLast() {
        return remove(getLast());
    }

    @NonNull ImmutableSequencedCollection<E> retainAll(@NonNull Iterable<?> c);

    @NonNull Collection<E> toMutable();
}
