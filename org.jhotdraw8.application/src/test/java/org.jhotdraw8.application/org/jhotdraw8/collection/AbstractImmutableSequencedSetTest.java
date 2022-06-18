package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Set;

public abstract class AbstractImmutableSequencedSetTest extends AbstractImmutableSetTest {
    @Override
    protected abstract @NonNull <E> ImmutableSequencedSet<E> newInstance();

    @Override
    protected abstract @NonNull <E> SequencedSet<E> toMutableInstance(ImmutableSet<E> m);

    @Override
    protected abstract @NonNull <E> ImmutableSequencedSet<E> toImmutableInstance(Set<E> m);

    @Override
    protected abstract @NonNull <E> ImmutableSequencedSet<E> toClonedInstance(ImmutableSet<E> m);

    @Override
    protected abstract @NonNull <E> ImmutableSequencedSet<E> newInstance(Iterable<E> m);
}
