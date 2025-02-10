/*
 * @(#)MutableSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableSet;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * Provides a {@link Set} facade to a set of {@code PersistentSet} functions.
 *
 * @param <E> the element type
 */
public class MutableSetFacade<E> extends AbstractSet<E> implements ReadableSet<E> {
    private PersistentSet<E> backingSet;
    private int modCount;

    public MutableSetFacade(PersistentSet<E> backingSet) {
        this.backingSet = backingSet;
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        PersistentSet<E> oldSet = backingSet;
        backingSet = backingSet.remove((E) o);
        if (oldSet != backingSet) {
            modCount++;
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        PersistentSet<E> oldSet = backingSet;
        backingSet = backingSet.empty();
        if (oldSet != backingSet) {
            modCount++;
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(backingSet.spliterator(), () -> this.modCount, null);
    }

    @Override
    public Stream<E> stream() {
        return backingSet.stream();
    }

    @Override
    public Iterator<E> iterator() {
        Iterator<E> it = new Iterator<>() {
            final Iterator<E> b = backingSet.iterator();
            E current;
            boolean canRemove;

            @Override
            public boolean hasNext() {
                return b.hasNext();
            }

            @Override
            public E next() {
                current = b.next();
                canRemove = true;
                return current;
            }

            @Override
            public void remove() {
                if (canRemove) {
                    MutableSetFacade.this.remove(current);
                    canRemove = false;
                } else {
                    throw new IllegalStateException();
                }
            }
        };

        return new FailFastIterator<>(it, () -> this.modCount);
    }


    @Override
    public int size() {
        return backingSet.size();
    }

    @Override
    public boolean contains(Object o) {
        return backingSet.contains(o);
    }

    @Override
    public boolean add(E e) {
        PersistentSet<E> oldSet = backingSet;
        backingSet = backingSet.add(e);
        if (oldSet != backingSet) {
            modCount++;
            return true;
        }
        return false;
    }
}
