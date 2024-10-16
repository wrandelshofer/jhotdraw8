/*
 * @(#)MutableListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.impl.iteration.MutableListIterator;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;
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
 * Provides a {@link List} facade to a set of {@code ImmutableList} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class MutableListFacade<E> extends AbstractList<E> implements ReadOnlyList<E>, List<E> {
    private ImmutableList<E> backingList;
    private int modCount;

    public MutableListFacade(ImmutableList<E> backingList) {
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
    public ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlyListFacade<>(
                () -> backingList.size(),
                index -> get(backingList.size() - 1 - index),
                () -> this);
    }


    @SuppressWarnings("unchecked")
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
    public ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        int length = size();
        Objects.checkFromToIndex(fromIndex, toIndex, length);
        return new ReadOnlyListFacade<>(
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
    public ListIterator<E> listIterator(int index) {
        return new MutableListIterator<>(this, index, this::getModCount);
    }
}
