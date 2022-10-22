package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.ReadOnlySet;

import java.util.HashSet;
import java.util.Set;

public class HashSetTest extends AbstractSetTest {
    @Override
    protected <E> @NonNull Set<E> newInstance() {
        return new HashSet<>();
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(int numElements, float loadFactor) {
        return new HashSet<>();
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(Set<E> m) {
        return new HashSet<>(m);
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(ReadOnlySet<E> m) {
        return new HashSet<>(m.asSet());
    }


    @SuppressWarnings("unchecked")
    @Override
    protected <E> @NonNull Set<E> toClonedInstance(Set<E> m) {
        return (Set<E>) ((HashSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(Iterable<E> m) {
        HashSet<E> s = new HashSet<>();
        m.iterator().forEachRemaining(s::add);
        return s;

    }
}
