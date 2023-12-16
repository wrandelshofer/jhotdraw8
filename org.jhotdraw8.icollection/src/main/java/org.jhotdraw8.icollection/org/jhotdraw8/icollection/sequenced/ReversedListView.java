package org.jhotdraw8.icollection.sequenced;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.impl.iteration.ReverseListSpliterator;
import org.jhotdraw8.icollection.impl.iteration.ReverseMutableListIterator;

import java.util.*;
import java.util.function.IntSupplier;

public class ReversedListView<E> extends AbstractList<E> implements List<E> {
    private final @NonNull List<E> src;
    private final @NonNull IntSupplier modCount;

    /**
     * Constructs a new instance.
     *
     * @param src the source set
     */
    public ReversedListView(@NonNull List<E> src, @NonNull IntSupplier modCount) {
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
        return new ReverseMutableListIterator<>(src, index, modCount);
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
    public @NonNull List<E> reversed() {
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
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        boolean modified = false;
        int reverseIndex = src.size() - index;
        for (E e : c) {
            src.add(reverseIndex, e);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        return super.addAll(c);
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

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }
    }

}
