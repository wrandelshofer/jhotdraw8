/*
 * @(#)ImmutableSequencedSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Collection;

public interface ImmutableSequencedSet<E> extends ImmutableSet<E>, ReadOnlySequencedSet<E> {
    @Override
    @NonNull ImmutableSequencedSet<E> copyAdd(E element);

    @Override
    @NonNull ImmutableSequencedSet<E> copyAddAll(@NonNull Iterable<? extends E> c);

    @NonNull ImmutableSequencedChampSet<E> copyAddFirst(final @Nullable E key);

    @NonNull ImmutableSequencedChampSet<E> copyAddLast(final @Nullable E key);

    @Override
    @NonNull ImmutableSequencedSet<E> copyClear();

    @Override
    @NonNull ImmutableSequencedSet<E> copyRemove(E element);

    @Override
    @NonNull ImmutableSequencedSet<E> copyRemoveAll(@NonNull Iterable<?> c);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return this set instanceif it is already empty, or
     * a different set instance with the first element removed
     */
    default ImmutableSequencedSet<E> copyRemoveFirst() {
        return isEmpty() ? this : copyRemove(getFirst());
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the last.
     *
     * @return this set instance if it is already empty, or
     * a different set instance with the last element removed
     */
    default ImmutableSequencedSet<E> copyRemoveLast() {
        return isEmpty() ? this : copyRemove(getLast());
    }

    @Override
    @NonNull ImmutableSequencedSet<E> copyRetainAll(@NonNull Collection<?> c);

    @Override
    @NonNull
    default ImmutableSequencedSet<E> copyRetainAll(final @NonNull ReadOnlyCollection<?> c) {
        return (ImmutableSequencedSet<E>) ImmutableSet.super.copyRetainAll(c);
    }

    @Override
    @NonNull SequencedSet<E> toMutable();
}
