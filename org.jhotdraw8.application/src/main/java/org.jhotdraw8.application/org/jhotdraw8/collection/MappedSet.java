/*
 * @(#)ReadOnlyTransformationSet.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Wraps a {@link Set} in a {@link Set} of a different type.
 * <p>
 * The underlying Set is referenced - not copied.
 *
 * @author Werner Randelshofer
 */
public final class MappedSet<E, F> extends AbstractSet<E> {

    private final Set<F> backingSet;
    private final Function<F, E> mapf;

    public MappedSet(Set<F> backingSet, Function<F, E> mapf) {
        this.backingSet = backingSet;
        this.mapf = mapf;
    }

    @Override
    public boolean contains(Object o) {
        return backingSet.contains(o);
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<F> i = backingSet.iterator();

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public E next() {
                return mapf.apply(i.next());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public @NonNull Spliterator<E> spliterator() {
        class MappingSpliterator implements Spliterator<E> {
            private final Spliterator<F> i;

            public MappingSpliterator(Spliterator<F> i) {
                this.i = i;
            }

            @Override
            public boolean tryAdvance(Consumer<? super E> action) {
                return i.tryAdvance(f -> action.accept(mapf.apply(f)));
            }

            @Override
            public Spliterator<E> trySplit() {
                Spliterator<F> fSpliterator = i.trySplit();
                return fSpliterator == null ? null : new MappingSpliterator(fSpliterator);
            }

            @Override
            public long estimateSize() {
                return i.estimateSize();
            }

            @Override
            public int characteristics() {
                return i.characteristics();
            }
        }
        return new MappingSpliterator(backingSet.spliterator());
    }


    @Override
    public int size() {
        return backingSet.size();
    }

}
