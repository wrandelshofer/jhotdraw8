package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.readable.ReadableSet;

import java.util.HashSet;
import java.util.Set;

public class HashSetTest extends AbstractSetTest {
    @Override
    protected <E> Set<E> newInstance() {
        return new HashSet<>();
    }

    @Override
    protected <E> Set<E> newInstance(int numElements, float loadFactor) {
        return new HashSet<>();
    }

    @Override
    protected <E> Set<E> newInstance(Set<E> m) {
        return new HashSet<>(m);
    }

    @Override
    protected <E> Set<E> newInstance(ReadableSet<E> m) {
        return new HashSet<>(m.asSet());
    }


    @SuppressWarnings("unchecked")
    @Override
    protected <E> Set<E> toClonedInstance(Set<E> m) {
        return (Set<E>) ((HashSet<E>) m).clone();
    }

    @Override
    protected <E> Set<E> newInstance(Iterable<E> m) {
        HashSet<E> s = new HashSet<>();
        m.iterator().forEachRemaining(s::add);
        return s;

    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
