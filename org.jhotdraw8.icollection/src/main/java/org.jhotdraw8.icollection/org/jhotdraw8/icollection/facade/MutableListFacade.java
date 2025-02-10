/*
 * @(#)MutableListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.impl.iteration.MutableListIterator;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jhotdraw8.icollection.sequenced.ReversedListView;
import org.jspecify.annotations.Nullable;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * Provides a {@link List} facade to a set of {@code PersistentList} functions.
 *
 * @param <E> the element type
 */
public class MutableListFacade<E> extends AbstractList<E> implements ReadableList<E>, List<E> {
    private PersistentList<E> backingList;
    private int modCount;

    public MutableListFacade(PersistentList<E> backingList) {
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
    public List<E> reversed() {
        return new ReversedListView<>(this, this::getModCount);
    }

    private int getModCount() {
        return modCount;
    }

    @Override
    public ReadableSequencedCollection<E> readOnlyReversed() {
        return new ReadableListFacade<>(
                () -> backingList.size(),
                index -> get(backingList.size() - 1 - index),
                () -> this);
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        PersistentList<E> oldList = backingList;
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
        PersistentList<E> oldList = backingList;
        backingList = backingList.empty();
        if (oldList != backingList) {
            modCount++;
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        return new FailFastSpliterator<>(backingList.spliterator(), () -> this.modCount, null);
    }

    @Override
    public ReadableList<E> readOnlySubList(int fromIndex, int toIndex) {
        int length = size();
        Objects.checkFromToIndex(fromIndex, toIndex, length);
        return new ReadableListFacade<>(
                () -> toIndex - fromIndex,
                i -> get(i - fromIndex));
    }


    @Override
    public Stream<E> stream() {
        return backingList.stream();
    }

    @Override
    public Iterator<E> iterator() {
        Iterator<E> it = new MyIterator<>(backingList.iterator());
        return new FailFastIterator<>(it, () -> this.modCount);
    }


    public Iterator<E> reverseIterator() {
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

    class MyIterator<EE> implements Iterator<EE> {
        final Iterator<EE> b;
        @Nullable EE current;
        boolean canRemove;

        MyIterator(Iterator<EE> b) {
            this.b = b;
        }

        @Override
        public boolean hasNext() {
            return b.hasNext();
        }

        @Override
        public EE next() {
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
        PersistentList<E> oldList = backingList;
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
        return ReadableList.super.getFirst();
    }

    @Override
    public E getLast() {
        return ReadableList.super.getLast();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new MutableListIterator<>(this, index, this::getModCount);
    }
}
