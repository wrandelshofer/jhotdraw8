package org.jhotdraw8.icollection.navigable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.function.IntSupplier;

public class SubsetNavigableSetView<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final @NonNull NavigableSet<E> src;
    private final @NonNull IntSupplier modCount;

    /**
     * Constructs a new instance.
     *
     * @param src the source set
     */
    public SubsetNavigableSetView(@NonNull NavigableSet<E> src, @NonNull IntSupplier modCount) {
        this.src = src;
        this.modCount = modCount;
    }

    @Nullable
    @Override
    public E lower(E e) {
        return null;
    }

    @Nullable
    @Override
    public E floor(E e) {
        return null;
    }

    @Nullable
    @Override
    public E ceiling(E e) {
        return null;
    }

    @Nullable
    @Override
    public E higher(E e) {
        return null;
    }

    @Nullable
    @Override
    public E pollFirst() {
        return null;
    }

    @Nullable
    @Override
    public E pollLast() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @NonNull
    @Override
    public Object @NonNull [] toArray() {
        return new Object[0];
    }

    @NonNull
    @Override
    public <T> T @NonNull [] toArray(@NonNull T @NonNull [] a) {
        return null;
    }

    @Override
    public boolean add(E e) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @NonNull
    @Override
    public NavigableSet<E> descendingSet() {
        return null;
    }

    @NonNull
    @Override
    public Iterator<E> descendingIterator() {
        return null;
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
        return null;
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
        return null;
    }

    @Override
    public E last() {
        return null;
    }
}
