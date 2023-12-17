/*
 * @(#)MappedReadOnlyList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.mapped;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.facade.ReadOnlySequencedCollectionFacade;
import org.jhotdraw8.icollection.readonly.AbstractReadOnlyList;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * Maps a {@link ReadOnlyList} to a different element type.
 * <p>
 * The underlying List is referenced - not copied.
 *
 * @param <E> the mapped element type
 * @param <F> the original element type
 * @author Werner Randelshofer
 */
public final class MappedReadOnlyList<E, F> extends AbstractReadOnlyList<E> {

    private final @NonNull ReadOnlyList<F> backingList;
    private final @NonNull Function<F, E> mapf;

    public MappedReadOnlyList(@NonNull ReadOnlyList<F> backingList, @NonNull Function<F, E> mapf) {
        this.backingList = backingList;
        this.mapf = mapf;
    }

    @Override
    public boolean contains(Object o) {
        for (F f : backingList) {
            if (mapf.apply(f).equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public E get(int index) {
        return mapf.apply(backingList.get(index));
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new Iterator<>() {
            private final Iterator<F> i = backingList.iterator();

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

    @NonNull Iterator<E> reverseIterator() {
        return new Iterator<>() {
            private int i = size() - 1;

            @Override
            public boolean hasNext() {
                return i >= 0;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return mapf.apply(backingList.get(i--));
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlySequencedCollectionFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull Spliterator<E> spliterator() {
        return new MappedSpliterator<>(backingList.spliterator(), mapf);
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public @NonNull ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        return new MappedReadOnlyList<>(backingList.readOnlySubList(fromIndex, toIndex), mapf);
    }
}
