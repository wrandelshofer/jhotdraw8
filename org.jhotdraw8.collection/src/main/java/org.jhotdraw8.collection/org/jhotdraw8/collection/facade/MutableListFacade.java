/*
 * @(#)MutableListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.iterator.FailFastIterator;
import org.jhotdraw8.collection.iterator.FailFastListIterator;
import org.jhotdraw8.collection.iterator.FailFastSpliterator;
import org.jhotdraw8.collection.readonly.ReadOnlyList;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.collection.sequenced.SequencedCollection;

import java.util.*;
import java.util.stream.Stream;

/**
 * Wraps {@link ImmutableList} functions into the {@link List} interface.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class MutableListFacade<E> extends AbstractList<E> implements ReadOnlyList<E>, SequencedCollection<E> {
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
    public @NonNull SequencedCollection<E> _reversed() {
        return new SequencedCollectionFacade<E>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::contains
        );
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
        if (oldList != backingList) {
            return true;
        }
        return false;
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
    public @NonNull EnumeratorSpliterator<E> spliterator() {
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

    class MyListIterator implements ListIterator<E> {
        int index;
        @Nullable E current;
        int prevIndex = -1;

        MyListIterator(int index) {
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            prevIndex = index;
            current = get(index++);
            return current;
        }

        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public E previous() {
            if (!hasPrevious()) throw new NoSuchElementException();
            current = get(--index);
            prevIndex = index;
            return current;
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void remove() {
            if (prevIndex >= 0 && prevIndex < size()) {
                MutableListFacade.this.remove(prevIndex);
                index = prevIndex;
                prevIndex = -1;
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void set(E e) {
            if (prevIndex >= 0 && prevIndex < size()) {
                MutableListFacade.this.set(prevIndex, e);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void add(E e) {
            if (index >= 0 && index <= size()) {
                MutableListFacade.this.add(index++, e);
                prevIndex = -1;
            } else {
                throw new IllegalStateException();
            }

        }
    }

    @Override
    public boolean add(E e) {
        ImmutableList<E> oldList = backingList;
        backingList = backingList.add((E) e);
        if (oldList != backingList) {
            modCount++;
            return true;
        }
        return false;
    }

    @Override
    public void add(int index, E e) {
        backingList = backingList.add(index, (E) e);
        modCount++;
    }

    @Override
    public E set(int index, E e) {
        E oldValue = backingList.get(index);
        backingList = backingList.set(index, (E) e);
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
        Objects.checkIndex(index, size() + 1);
        return new FailFastListIterator<>(new MyListIterator(index), () -> this.modCount);
    }

    @Override
    public E removeFirst() {
        return SequencedCollection.super.removeFirst();
    }

    @Override
    public E removeLast() {
        return SequencedCollection.super.removeLast();
    }
}
