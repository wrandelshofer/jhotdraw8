/*
 * @(#)MappedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.mapped;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Maps a {@link Set} to a different element type.
 * <p>
 * The underlying Set is referenced - not copied.
 *
 * @param <E> the mapped element type
 * @param <F> the original element type
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
        // FIXME Should throw UnsupportedOperationException!
        return backingSet.contains(o);
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new Iterator<>() {
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
            public boolean tryAdvance(@NonNull Consumer<? super E> action) {
                return i.tryAdvance(f -> action.accept(mapf.apply(f)));
            }

            @Override
            public @Nullable Spliterator<E> trySplit() {
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
