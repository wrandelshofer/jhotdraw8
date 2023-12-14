/*
 * @(#)MutableListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.impl.iteration.MutableListIterator;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.icollection.sequenced.ReversedSequencedListView;
import org.jhotdraw8.icollection.sequenced.SequencedList;

import java.util.*;
import java.util.stream.Stream;

/**
 * Wraps {@link ImmutableList} functions into the {@link List} interface.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class MutableListFacade<E> extends AbstractList<E> implements ReadOnlyList<E>, SequencedList<E> {
    private @NonNull ImmutableList<E> backingList;
    private int modCount;

    public MutableListFacade(@NonNull ImmutableList<E> backingList) {
        this.backingList = backingList;
    }

    @Override
    public void addFirst(E e) {
        backingList = backingList.add(0, e);
        modCount++;
    }

    @Override
    public void addLast(E e) {
        backingList = backingList.add(e);
        modCount++;
    }

    @Override
    public @NonNull SequencedList<E> _reversed() {
        return new ReversedSequencedListView<>(this, this::getModCount);
    }

    private int getModCount() {
        return modCount;
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlyListFacade<>(
                () -> backingList.size(),
                index -> get(backingList.size() - 1 - index),
                () -> this);
    }


    @Override
    public boolean remove(Object o) {
        ImmutableList<E> oldList = backingList;
        backingList = backingList.remove((E) o);
        modCount++;
        return oldList != backingList;
    }

    @Override
    public E remove(int index) {
        E removed = backingList.get(index);
        backingList = backingList.removeAt(index);
        modCount++;
        return removed;
    }


    @Override
    public void clear() {
        ImmutableList<E> oldList = backingList;
        backingList = backingList.clear();
        if (oldList != backingList) modCount++;
    }

    @Override
    public @NonNull Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(backingList.spliterator(), () -> this.modCount);
    }

    @Override
    public @NonNull ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        return null;
    }

    @Override
    public Stream<E> stream() {
        return backingList.stream();
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        Iterator<E> it = new MyIterator<>(backingList.iterator());
        return new FailFastIterator<>(it, () -> this.modCount);
    }


    public @NonNull Iterator<E> reverseIterator() {
        Iterator<E> it = new MyIterator<>(backingList.readOnlyReversed().iterator());
        return new FailFastIterator<>(it, () -> this.modCount);
    }


    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public boolean contains(Object o) {
        return backingList.contains(o);
    }

    class MyIterator<E> implements Iterator<E> {
        final @NonNull Iterator<E> b;
        @Nullable E current;
        boolean canRemove;

        MyIterator(@NonNull Iterator<E> b) {
            this.b = b;
        }

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
                MutableListFacade.this.remove(current);
                canRemove = false;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public boolean add(E e) {
        ImmutableList<E> oldList = backingList;
        backingList = backingList.add(e);
        if (oldList != backingList) {
            modCount++;
            return true;
        }
        return false;
    }

    @Override
    public void add(int index, E e) {
        backingList = backingList.add(index, e);
        modCount++;
    }

    @Override
    public E set(int index, E e) {
        E oldValue = backingList.get(index);
        backingList = backingList.set(index, e);
        return oldValue;
    }

    @Override
    public E get(int index) {
        return backingList.get(index);
    }

    @Override
    public E getFirst() {
        return ReadOnlyList.super.getFirst();
    }

    @Override
    public E getLast() {
        return ReadOnlyList.super.getLast();
    }

    @Override
    public @NonNull ListIterator<E> listIterator(int index) {
        return new MutableListIterator<>(this, index, this::getModCount);
    }

    @Override
    public E removeFirst() {
        return SequencedList.super.removeFirst();
    }

    @Override
    public E removeLast() {
        return SequencedList.super.removeLast();
    }
}
