package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.champ.ChampSet;
import org.jhotdraw8.collection.readonly.ReadOnlySet;

import java.util.Set;

public class ChampSetTest extends AbstractSetTest {
    @Override
    protected <E> @NonNull Set<E> newInstance() {
        return new ChampSet<>();
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(int numElements, float loadFactor) {
        return new ChampSet<>();
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(Set<E> m) {
        return new ChampSet<>(m);
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(ReadOnlySet<E> m) {
        return new ChampSet<>(m);
    }

    @Override
    protected <E> @NonNull Set<E> toClonedInstance(Set<E> m) {
        return ((ChampSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(Iterable<E> m) {
        return new ChampSet<>(m);
    }
}
