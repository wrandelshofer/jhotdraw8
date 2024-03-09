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

    @Override
    public boolean add(E e) {
        return src.add(e);
    }

    @Nullable
    @Override
    public E ceiling(E e) {
        return src.floor(e);
    }

    @Override
    public void clear() {
        src.clear();
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return reverseComparator;
    }

    @Override
    public boolean contains(Object o) {
        return src.contains(o);
    }

    @NonNull
    @Override
    public Iterator<E> descendingIterator() {
        return src.iterator();
    }

    @NonNull
    @Override
    public NavigableSet<E> descendingSet() {
        return src;
    }

    @Override
    public E first() {
        return src.last();
    }

    @Nullable
    @Override
    public E floor(E e) {
        return src.ceiling(e);
    }

    @NonNull
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new SubsetNavigableSetView<>(this, modCount,
                true, null, true, false, toElement, inclusive, true);
    }

    @NonNull
    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Nullable
    @Override
    public E higher(E e) {
        return src.lower(e);
    }

    @Override
    public boolean isEmpty() {
        return src.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return src.descendingIterator();
    }

    @Override
    public E last() {
        return src.first();
    }

    @Nullable
    @Override
    public E lower(E e) {
        return src.higher(e);
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
    public boolean remove(Object o) {
        return src.remove(o);
    }

    @Override
    public int size() {
        return src.size();
    }

    @NonNull
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new SubsetNavigableSetView<>(this, modCount,
                false, fromElement, fromInclusive, false, toElement, toInclusive, true);
    }

    @NonNull
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @NonNull
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new SubsetNavigableSetView<>(this, modCount,
                false, fromElement, inclusive, true, null, true, true);
    }

    @NonNull
    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }
}
