/*
 * @(#)ImmutableSequencedSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.ChampImmutableSequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.collection.sequenced.SequencedSet;

import java.util.Collection;

/**
 * Interface for an immutable sequenced set.
 * <p>
 * An immutable sequenced set provides methods for creating a new immutable sequenced set with
 * added or removed elements, without changing the original immutable sequenced set.
 */
public interface ImmutableSequencedSet<E> extends ImmutableSet<E>, ReadOnlySequencedSet<E> {
    @Override
    @NonNull ImmutableSequencedSet<E> add(E element);

    @Override
    @NonNull ImmutableSequencedSet<E> addAll(@NonNull Iterable<? extends E> c);

    @NonNull ChampImmutableSequencedSet<E> addFirst(final @Nullable E key);

    @NonNull ChampImmutableSequencedSet<E> addLast(final @Nullable E key);

    @Override
    @NonNull ImmutableSequencedSet<E> clear();

    @Override
    @NonNull ImmutableSequencedSet<E> remove(E element);

    @Override
    @NonNull ImmutableSequencedSet<E> removeAll(@NonNull Iterable<?> c);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return this set instanceif it is already empty, or
     * a different set instance with the first element removed
     */
    default ImmutableSequencedSet<E> removeFirst() {
        return isEmpty() ? this : remove(getFirst());
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the last.
     *
     * @return this set instance if it is already empty, or
     * a different set instance with the last element removed
     */
    default ImmutableSequencedSet<E> removeLast() {
        return isEmpty() ? this : remove(getLast());
    }

    @Override
    @NonNull ImmutableSequencedSet<E> retainAll(@NonNull Collection<?> c);

    @Override
    @NonNull
    default ImmutableSequencedSet<E> retainAll(final @NonNull ReadOnlyCollection<?> c) {
        return (ImmutableSequencedSet<E>) ImmutableSet.super.retainAll(c);
    }

    @Override
    @NonNull SequencedSet<E> toMutable();
}
