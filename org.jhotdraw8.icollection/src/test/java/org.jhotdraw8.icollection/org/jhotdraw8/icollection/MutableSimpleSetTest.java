package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.Set;

public class MutableSimpleSetTest extends AbstractSetTest {
    @Override
    protected <E> @NonNull Set<E> newInstance() {
        return new SimpleMutableSet<>();
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(int numElements, float loadFactor) {
        return new SimpleMutableSet<>();
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(Set<E> m) {
        return new SimpleMutableSet<>(m);
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(ReadOnlySet<E> m) {
        return new SimpleMutableSet<>(m);
    }

    @Override
    protected <E> @NonNull Set<E> toClonedInstance(Set<E> m) {
        return ((SimpleMutableSet<E>) m).clone();
    }

    @Override
    protected <E> @NonNull Set<E> newInstance(Iterable<E> m) {
        return new SimpleMutableSet<>(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
