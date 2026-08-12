package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.readable.ReadableSet;

import java.util.Set;

public class MutableSimpleSetTest extends AbstractSetTest {
    @Override
    protected <E> Set<E> newInstance() {
        return new MutableHashSet<>();
    }

    @Override
    protected <E> Set<E> newInstance(int numElements, float loadFactor) {
        return new MutableHashSet<>();
    }

    @Override
    protected <E> Set<E> newInstance(Set<E> m) {
        return new MutableHashSet<>(m);
    }

    @Override
    protected <E> Set<E> newInstance(ReadableSet<E> m) {
        return new MutableHashSet<>(m);
    }

    @Override
    protected <E> Set<E> toClonedInstance(Set<E> m) {
        return ((MutableHashSet<E>) m).clone();
    }

    @Override
    protected <E> Set<E> newInstance(Iterable<E> m) {
        return new MutableHashSet<>(m);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
