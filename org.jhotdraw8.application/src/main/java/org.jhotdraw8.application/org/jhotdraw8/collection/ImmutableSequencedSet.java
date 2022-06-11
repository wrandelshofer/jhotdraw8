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

    default ImmutableSequencedSet<E> copyRemoveFirst() {
        return copyRemove(getFirst());
    }

    default ImmutableSequencedSet<E> copyRemoveLast() {
        return copyRemove(getLast());
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
