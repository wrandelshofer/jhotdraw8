/*
 * @(#)MappedReadOnlyList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * Maps a {@link ReadOnlyList} in {@link ReadOnlyList} of a different element type.
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
        return new Iterator<E>() {
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

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull Spliterator<E> spliterator() {
        return (Spliterator<E>) backingList.spliterator();
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public void copyInto(Object[] out, int offset) {
        int i = offset;
        for (E e : this) {
            out[i++] = e;
        }
    }

    @Override
    public @NonNull ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        return new MappedReadOnlyList<>(backingList.readOnlySubList(fromIndex, toIndex), mapf);
    }
}
