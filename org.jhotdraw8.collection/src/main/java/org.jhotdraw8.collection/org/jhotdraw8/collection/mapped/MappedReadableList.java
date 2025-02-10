/*
 * @(#)MappedReadableList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.mapped;

import org.jhotdraw8.icollection.facade.ReadableSequencedCollectionFacade;
import org.jhotdraw8.icollection.readable.AbstractReadableList;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * Maps a {@link ReadableList} to a different element type.
 * <p>
 * The underlying List is referenced - not copied.
 *
 * @param <E> the mapped element type
 * @param <F> the original element type
 */
public final class MappedReadableList<E, F> extends AbstractReadableList<E> {

    private final ReadableList<F> backingList;
    private final Function<F, E> mapf;

    public MappedReadableList(ReadableList<F> backingList, Function<F, E> mapf) {
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
    public Iterator<E> iterator() {
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

    Iterator<E> reverseIterator() {
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
    public ReadableSequencedCollection<E> readOnlyReversed() {
        return new ReadableSequencedCollectionFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst,
                Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<E> spliterator() {
        return new MappedSpliterator<>(backingList.spliterator(), mapf);
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public ReadableList<E> readOnlySubList(int fromIndex, int toIndex) {
        return new MappedReadableList<>(backingList.readOnlySubList(fromIndex, toIndex), mapf);
    }
}
