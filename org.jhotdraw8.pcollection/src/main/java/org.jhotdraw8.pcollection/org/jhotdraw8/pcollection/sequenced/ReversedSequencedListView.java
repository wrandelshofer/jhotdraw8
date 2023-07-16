package org.jhotdraw8.pcollection.sequenced;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.impl.iteration.MutableListIterator;
import org.jhotdraw8.pcollection.impl.iteration.ReverseListSpliterator;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.IntSupplier;

public class ReversedSequencedListView<E> extends AbstractList<E> implements SequencedList<E> {
    private final @NonNull SequencedList<E> src;
    private final @NonNull IntSupplier modCount;

    /**
     * Constructs a new instance.
     *
     * @param src the source set
     */
    public ReversedSequencedListView(@NonNull SequencedList<E> src, @NonNull IntSupplier modCount) {
        this.src = src;
        this.modCount = modCount;
    }

    @Override
    public boolean isEmpty() {
        return src.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return src.contains(o);
    }

    @Override
    public void clear() {
        src.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new MutableListIterator<>(this, index, modCount);

        /*

        return new ListIterator<E>() {
            ListIterator<E> i = src.listIterator(src.size() - index);

            @Override
            public boolean hasNext() {
                return i.hasPrevious();
            }

            @Override
            public E next() {
                return i.previous();
            }

            @Override
            public boolean hasPrevious() {
                return i.hasNext();
            }

            @Override
            public E previous() {
                return i.next();
            }

            @Override
            public int nextIndex() {
                return src.size() - 1 - i.previousIndex();
            }

            @Override
            public int previousIndex() {
                return src.size() - 1 - i.nextIndex();
            }

            @Override
            public void remove() {
                i.remove();
            }

            @Override
            public void set(E e) {
                i.set(e);
            }

            @Override
            public void add(E e) {
                i.add(e);
            }
        };*/
    }

    @Override
    public Spliterator<E> spliterator() {
        return new ReverseListSpliterator<>(src, 0, src.size());
    }


    @Override
    public int size() {
        return src.size();
    }

    @Override
    public @NonNull SequencedList<E> _reversed() {
        return src;
    }

    @Override
    public boolean add(E e) {
        src.addFirst(e);
        return true;
    }

    @Override
    public E set(int index, E element) {
        return src.set(src.size() - 1 - index, element);
    }

    @Override
    public void add(int index, E element) {
        src.add(src.size() - index, element);
    }

    @Override
    public E remove(int index) {
        return src.remove(src.size() - 1 - index);
    }

    @Override
    public int indexOf(Object o) {
        int i = src.lastIndexOf(o);
        return i < 0 ? i : src.size() - 1 - i;
    }

    @Override
    public int lastIndexOf(Object o) {
        int i = src.indexOf(o);
        return i < 0 ? i : src.size() - 1 - i;
    }


    @Override
    public boolean remove(Object o) {
        int i = src.lastIndexOf(o);
        if (i != -1) {
            src.remove(i);
            return true;
        }
        return false;
    }

    @Override
    public void addFirst(E e) {
        src.addLast(e);
    }

    @Override
    public void addLast(E e) {
        src.addFirst(e);
    }

    @Override
    public E getFirst() {
        return src.getLast();
    }

    @Override
    public E getLast() {
        return src.getFirst();
    }

    @Override
    public E removeFirst() {
        return src.removeLast();
    }

    @Override
    public E removeLast() {
        return src.removeFirst();
    }

    @Override
    public E get(int index) {
        return src.get(src.size() - 1 - index);
    }
}
