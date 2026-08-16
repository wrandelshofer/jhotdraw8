package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.PersistentVectorList;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeAPI;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeIterator;

import java.util.Iterator;
import java.util.function.Function;

public class TombSkippingVectorIterator<E> implements Iterator<E> {
    private final Function<Object, E> mapper;
    private final FingerTreeIterator<Object> iterator;

    public TombSkippingVectorIterator(PersistentVectorList<Object> vector, Function<Object, E> mapper) {
        this.mapper = mapper;
        this.iterator = FingerTreeAPI.iterator(vector);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public E next() {
        Object next = iterator.next();
        if (next instanceof Tombstone(int neighbors)) {
            iterator.skip(neighbors);
            next = iterator.next();
        }
        return mapper.apply(next);
    }
}
