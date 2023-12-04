/*
 * @(#)MutableSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.immutable_collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.immutable_collection.immutable.ImmutableSet;
import org.jhotdraw8.immutable_collection.impl.iteration.FailFastIterator;
import org.jhotdraw8.immutable_collection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.immutable_collection.readonly.ReadOnlySet;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * Wraps {@link ImmutableSet} functions into the {@link Set} interface.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class MutableSetFacade<E> extends AbstractSet<E> implements ReadOnlySet<E> {
    private @NonNull ImmutableSet<E> backingSet;
    private int modCount;

    public MutableSetFacade(@NonNull ImmutableSet<E> backingSet) {
        this.backingSet = backingSet;
    }


    @Override
    public boolean remove(Object o) {
        ImmutableSet<E> oldSet = backingSet;
        backingSet = backingSet.remove((E) o);
        if (oldSet != backingSet) {
            modCount++;
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        ImmutableSet<E> oldSet = backingSet;
        backingSet = backingSet.clear();
        if (oldSet != backingSet) modCount++;
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(backingSet.spliterator(), () -> this.modCount);
    }

    @Override
    public Stream<E> stream() {
        return backingSet.stream();
    }

    @Override
    public @NonNull Iterator<E> iterator() {
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
        ImmutableSet<E> oldSet = backingSet;
        backingSet = backingSet.add(e);
        if (oldSet != backingSet) {
            modCount++;
            return true;
        }
        return false;
    }
}
