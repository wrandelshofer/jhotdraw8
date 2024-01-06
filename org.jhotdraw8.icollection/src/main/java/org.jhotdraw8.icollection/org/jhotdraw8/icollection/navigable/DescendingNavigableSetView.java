package org.jhotdraw8.icollection.navigable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.function.IntSupplier;

public class DescendingNavigableSetView<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final @NonNull NavigableSet<E> src;
    private final @NonNull IntSupplier modCount;

    private final Comparator<? super E> reverseComparator;

    /**
     * Constructs a new instance.
     *
     * @param src the source set
     */
    public DescendingNavigableSetView(@NonNull NavigableSet<E> src, @NonNull IntSupplier modCount) {
        this.src = src;
        this.modCount = modCount;
        this.reverseComparator = Collections.reverseOrder(src.comparator());
    }

    @Nullable
    @Override
    public E lower(E e) {
        return src.higher(e);
    }

    @Nullable
    @Override
    public E floor(E e) {
        return src.ceiling(e);
    }

    @Nullable
    @Override
    public E ceiling(E e) {
        return src.floor(e);
    }

    @Nullable
    @Override
    public E higher(E e) {
        return src.lower(e);
    }

    @Nullable
    @Override
    public E pollFirst() {
        return src.pollLast();
    }

    @Nullable
    @Override
    public E pollLast() {
        return src.pollFirst();
    }

    @Override
    public int size() {
        return src.size();
    }

    @Override
    public boolean isEmpty() {
        return src.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return src.contains(o);
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return src.descendingIterator();
    }


    @Override
    public boolean add(E e) {
        return src.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return src.remove(o);
    }


    @Override
    public void clear() {
        src.clear();
    }

    @NonNull
    @Override
    public NavigableSet<E> descendingSet() {
        return src;
    }

    @NonNull
    @Override
    public Iterator<E> descendingIterator() {
        return src.iterator();
    }

    @NonNull
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return null;
    }

    @NonNull
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return null;
    }

    @NonNull
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return null;
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return reverseComparator;
    }

    @NonNull
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return null;
    }

    @NonNull
    @Override
    public SortedSet<E> headSet(E toElement) {
        return null;
    }

    @NonNull
    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return null;
    }

    @Override
    public E first() {
        return src.last();
    }

    @Override
    public E last() {
        return src.first();
    }
}
