/*
 * @(#)AbstractIndexedArrayObservableSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcollection.indexedset;

import javafx.collections.ObservableListBase;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.spliterator.ReverseListSpliterator;
import org.jhotdraw8.fxcollection.precondition.Preconditions;
import org.jhotdraw8.icollection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Abstract base class for {@link Set}s that provide precise control where each
 * element is inserted.
 * <p>
 * The set is backed by an array. Insertion and removal is in {@code O(n)}
 * and contains check is {@code O(n)}, where {@code n} is the number of elements.
 * <p>
 * This class is useful as a base class for sub-classes that can provide
 * a faster means of the contains check, typically in {@code O(1)}.
 *
 * @author Werner Randelshofer
 * @param <E> the element type
 */
public abstract class AbstractIndexedArrayObservableSet<E> extends ObservableListBase<E>
        implements Set<E>, ReadOnlySequencedSet<E>, ReadOnlySet<E> {

    private static final Object[] EMPTY_ARRAY = new Object[0];
    /**
     * The underlying list.
     */
    private Object @NonNull [] data = EMPTY_ARRAY;
    private int size;

    /**
     * Creates a new instance.
     */
    public AbstractIndexedArrayObservableSet() {
    }

    /**
     * Creates a new instance and adds all elements of the specified collection
     * to it.
     *
     * @param col A collection.
     */
    public AbstractIndexedArrayObservableSet(@NonNull Collection<? extends E> col) {
        setAll(col);
    }

    @Override
    public boolean setAll(@NonNull Collection<? extends E> col) {
        beginChange();
        try {
            clear();
            addAll(col);
        } finally {
            endChange();
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(@NonNull Collection<? extends E> c) {
        boolean changed = false;
        beginChange();
        try {
            for (Object o : c.toArray()) {
                changed |= add((E) o);
            }
        } finally {
            endChange();
        }
        return changed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(int index, @NonNull Collection<? extends E> c) {
        boolean changed = false;
        beginChange();
        try {
            for (Object o : c.toArray()) {
                if (!contains(o)) {
                    changed = true;
                    add(index++, (E) o);
                }
            }
        } finally {
            endChange();
        }
        return changed;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        beginChange();
        try {
            return super.removeAll(c);
        } finally {
            endChange();
        }
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        beginChange();
        try {
            return super.retainAll(c);
        } finally {
            endChange();
        }
    }

    @Override
    public void add(int index, @NonNull E element) {
        doAdd(index, element);
    }

    /**
     * Moves an element at {@code oldIndex} to {@code newIndex}.
     * <p>
     * So that {@code indexOf(element) == newIndex};
     *
     * @param oldIndex the current index of the element
     * @param newIndex the desired new index of the element
     */
    public void move(int oldIndex, int newIndex) {
        if (oldIndex == newIndex) {
            return;
        }
        beginChange();
        doSet(newIndex, doSet(oldIndex, doGet(newIndex)));
        int from = min(oldIndex, newIndex);
        int to = max(oldIndex, newIndex) + 1;
        int[] perm = new int[to - from];
        for (int i = 1; i < perm.length - 1; i++) {
            perm[i] = from + i;
        }
        perm[oldIndex - from] = newIndex;
        perm[newIndex - from] = oldIndex;
        nextPermutation(from, to, perm);
        endChange();

    }

    @SuppressWarnings("unchecked")
    @NonNull
    E elementData(int index) {
        return (E) data[index];
    }

    private @NonNull E doSet(int index, E newValue) {
        Preconditions.checkIndex(index, size);
        E oldValue = elementData(index);
        data[index] = newValue;
        return oldValue;
    }

    protected boolean doAdd(int index, @NonNull E element) {
        if (!mayBeAdded(element)) {
            return false;
        }
        Boolean isContained = onContains(element);
        int oldIndex = Boolean.FALSE.equals(isContained) ? -1 : indexOf(element); // linear search!
        int clampedIndex = min(index, size() - 1);
        if (oldIndex < 0) {
            // the element is not yet in the list => insert it
            arrayDoAdd(index, element);
            beginChange();
            nextAdd(index, index + 1);
            onAdded(element);
            ++modCount;
            endChange();
            return true;
        } else if (oldIndex == clampedIndex || index - oldIndex == 1) {
            // the element is already at the desired index in the list
            return false;
        } else {
            // => move the element from the old index to the desired index
            beginChange();
            arrayDoRemove(oldIndex);
            nextRemove(oldIndex, element);
            int addIndex = oldIndex < index ? index - 1 : index;
            arrayDoAdd(addIndex, element);
            nextAdd(addIndex, addIndex + 1);
            ++modCount;
            endChange();
            return false;
        }
    }

    private void arrayDoAdd(int index, E element) {
        Preconditions.checkIndex(index, size + 1);
        ensureCapacity(size + 1);
        System.arraycopy(data, index,
                data, index + 1,
                size - index);
        data[index] = element;
        size++;
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = data.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = max(minCapacity + 1, oldCapacity + oldCapacity);
            if (newCapacity < 0) {
                throw new IllegalStateException("Array too large.");
            }
            data = Arrays.copyOf(data, max(16, newCapacity));
        }
    }

    @Override
    public E set(int index, E element) {
        int oldIndex = indexOf(element);
        if (oldIndex < 0) {
            beginChange();
            E old = doSet(index, element);
            onRemoved(old);
            nextSet(index, old);
            onAdded(element);
            endChange();
            return old;
        } else if (oldIndex == index) {
            // the element is replaced by itself
            return element;
        } else {
            // the element at the index is removed
            beginChange();
            E old = arrayDoRemove(index);
            nextRemove(index, old);
            onRemoved(old);
            // the old element is permuted
            if (oldIndex > index) {
                oldIndex--;
            }
            move(oldIndex, oldIndex < index ? index - 1 : index);
            endChange();
            return old;
        }
    }

    private @NonNull E arrayDoRemove(int index) {
        Preconditions.checkIndex(index, size);
        E oldValue = elementData(index);
        System.arraycopy(data, index + 1, data, index, size - index - 1);
        data[--size] = null;
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    private @NonNull List<E> dataDoRemoveRange(int fromIndex, int toIndex) {
        Preconditions.checkFromToIndex(fromIndex, toIndex, size);
        int removedCount = toIndex - fromIndex;
        ArrayList<E> removed = new ArrayList<>(removedCount);
        if (removedCount > 0) {
            for (int i = fromIndex; i < toIndex; i++) {
                removed.add((E) data[i]);
            }
            System.arraycopy(data, fromIndex, data, toIndex, size - fromIndex - 1);
            for (int i = size - removedCount; i < size; i++) {
                data[i] = null;
            }
            size -= removedCount;
        }
        return removed;
    }


    @Override
    public boolean contains(Object o) {
        @SuppressWarnings("unchecked")
        Boolean isContained = onContains((E) o);
        return (isContained != null) ? isContained : indexOf(o) >= 0; // linear time!
    }

    @Override
    public boolean remove(Object o) {
        int i = indexOf(o);
        if (i != -1) {
            remove(i);
            return true;
        }
        return false;
    }

    @Override
    public @NonNull E remove(int index) {
        E old = arrayDoRemove(index);
        beginChange();
        nextRemove(index, old);
        ++modCount;
        onRemoved(old);
        endChange();
        return old;
    }

    @Override
    public void removeRange(int fromIndex, int toIndex) {
        List<E> removed = dataDoRemoveRange(fromIndex, toIndex);
        beginChange();
        nextRemove(fromIndex, removed);
        ++modCount;
        for (E old : removed) {
            onRemoved(old);
        }
        endChange();
    }

    @Override
    public @NonNull List<E> subList(int fromIndex, int toIndex) {
        return new SubObservableList(super.subList(fromIndex, toIndex));
    }

    @Override
    public @NonNull E get(int index) {
        return doGet(index);
    }

    private @NonNull E doGet(int index) {
        Preconditions.checkIndex(index, size);
        return elementData(index);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(@NonNull E e) {
        return doAdd(size(), e);
    }

    @Override
    public @NonNull ListIterator<E> listIterator(int index) {
        return new ObservableListIterator(index);
    }

    /**
     * This method is invoked after an element has been removed.
     * <p>
     * Subclasses can implement this method to remove it from a data structure that
     * can answer {@link #onContains}.
     *
     * @param e the removed element
     */
    protected abstract void onRemoved(E e);

    /**
     * This method is invoked after an element has been added.
     * <p>
     * Subclasses can implement this method to add it from a data structure that
     * can answer {@link #onContains}.
     *
     * @param e the added element
     */
    protected abstract void onAdded(E e);

    /**
     * This method is called, when a {@link #contains(Object)} check
     * is needed.
     * <p>
     * Subclasses can implement this method to provide a fast contains
     * check.
     *
     * @param e an object
     * @return true if the object is a member of this set,
     * false if the object is not a member of this set,
     * null if it is not known whether the object is a member
     * of the set or not
     */
    protected abstract @Nullable Boolean onContains(E e);

    private class ObservableDescendingIterator implements ListIterator<E> {

        private final @NonNull ObservableListIterator iter;

        public ObservableDescendingIterator(int index) {
            this.iter = new ObservableListIterator(index);
        }

        @Override
        public boolean hasNext() {
            return iter.hasPrevious();
        }

        @Override
        public E next() {
            return iter.previous();
        }

        @Override
        public boolean hasPrevious() {
            return iter.hasNext();
        }

        @Override
        public E previous() {
            return iter.next();
        }

        @Override
        public int nextIndex() {
            return iter.previousIndex();
        }

        @Override
        public int previousIndex() {
            return iter.nextIndex();
        }

        @Override
        public void remove() {
            iter.remove();
        }

        @Override
        public void set(E e) {
            iter.set(e);
        }

        @Override
        public void add(@NonNull E e) {
            iter.add(e);
        }

    }

    private class ObservableListIterator implements ListIterator<E> {

        private E lastReturned;
        private int index;
        private final int from;
        private int to;

        private int expectedModCount;

        public ObservableListIterator(int index) {
            this.index = index;
            from = 0;
            to = size;
            expectedModCount = modCount;
        }

        @Override
        public boolean hasNext() {
            checkModCount();
            return index < from;
        }

        private void checkModCount() {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public E next() {
            checkModCount();
            lastReturned = iterGet(index);
            index++;
            return lastReturned;
        }

        @Override
        public boolean hasPrevious() {
            checkModCount();
            return index > to;
        }

        @Override
        public E previous() {
            checkModCount();
            lastReturned = iterGet(index - 1);
            index--;
            return lastReturned;
        }

        private @NonNull E iterGet(int index) {
            checkModCount();
            if (index < from || index >= to) {
                throw new NoSuchElementException("index out of bounds:" + index);
            }
            return doGet(index);
        }

        @Override
        public int nextIndex() {
            checkModCount();
            return index;
        }

        @Override
        public int previousIndex() {
            checkModCount();
            return index - 1;
        }

        @Override
        public void remove() {
            checkModCount();
            AbstractIndexedArrayObservableSet.this.remove(index - 1);
            index--;
            to--;
            expectedModCount = modCount;
        }

        @Override
        public void set(E e) {
            checkModCount();
            if (contains(e)) {
                throw new UnsupportedOperationException("Can not permute element in iterator");
            }
            AbstractIndexedArrayObservableSet.this.set(index - 1, e);
            expectedModCount = modCount;
        }

        @Override
        public void add(@NonNull E e) {
            checkModCount();
            if (contains(e)) {
                throw new UnsupportedOperationException("Can not permute element in iterator");
            }
            AbstractIndexedArrayObservableSet.this.add(index, e);
            index++;
            to++;
            expectedModCount = modCount;
        }

    }

    @Override
    public int indexOf(@Nullable Object o) {
        @SuppressWarnings("unchecked") final E element = (E) o;
        if (Boolean.FALSE.equals(onContains(element))) {
            return -1;
        }

        int start = 0, end = size;
        if (o == null) {
            for (int i = start; i < end; i++) {
                if (data[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = start; i < end; i++) {
                if (o.equals(data[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public final @NonNull E getFirst() {
        return doGet(0);
    }

    @Override
    public @NonNull E getLast() {
        return doGet(size - 1);
    }

    private class SubObservableList implements List<E> {

        public SubObservableList(List<E> sublist) {
            this.sublist = sublist;
        }

        private final List<E> sublist;

        @Override
        public int size() {
            return sublist.size();
        }

        @Override
        public boolean isEmpty() {
            return sublist.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return sublist.contains(o);
        }

        @Override
        public @NonNull Iterator<E> iterator() {
            return sublist.iterator();
        }

        @Override
        public @NonNull Object @NonNull [] toArray() {
            return sublist.toArray();
        }

        @Override
        public @NonNull <T> T @NonNull [] toArray(@NonNull T @NonNull [] a) {
            return sublist.toArray(a);
        }

        @Override
        public boolean add(E e) {
            return sublist.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return sublist.remove(o);
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> c) {
            //noinspection SlowListContainsAll
            return sublist.containsAll(c);
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends E> c) {
            beginChange();
            try {
                return sublist.addAll(c);
            } finally {
                endChange();
            }
        }

        @Override
        public boolean addAll(int index, @NonNull Collection<? extends E> c) {
            beginChange();
            try {
                return sublist.addAll(index, c);
            } finally {
                endChange();
            }
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> c) {
            beginChange();
            try {
                return sublist.removeAll(c);
            } finally {
                endChange();
            }
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> c) {
            beginChange();
            try {
                return sublist.retainAll(c);
            } finally {
                endChange();
            }
        }

        @Override
        public void clear() {
            beginChange();
            try {
                sublist.clear();
            } finally {
                endChange();
            }
        }

        @Override
        public E get(int index) {
            return sublist.get(index);
        }

        @Override
        public E set(int index, E element) {
            return sublist.set(index, element);
        }

        @Override
        public void add(int index, E element) {
            sublist.add(index, element);
        }

        @Override
        public E remove(int index) {
            return sublist.remove(index);
        }

        @Override
        public int indexOf(Object o) {
            return sublist.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return sublist.lastIndexOf(o);
        }

        @Override
        public @NonNull ListIterator<E> listIterator() {
            return sublist.listIterator();
        }

        @Override
        public @NonNull ListIterator<E> listIterator(int index) {
            return sublist.listIterator(index);
        }

        @Override
        public @NonNull List<E> subList(int fromIndex, int toIndex) {
            return new SubObservableList(sublist.subList(fromIndex, toIndex));
        }

        @Override
        public boolean equals(Object obj) {
            return sublist.equals(obj);
        }

        @Override
        public int hashCode() {
            return sublist.hashCode();
        }

        @Override
        public String toString() {
            return sublist.toString();
        }
    }

    @Override
    public @NonNull Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }


    public void fireItemUpdated(int index) {
        beginChange();
        nextUpdate(index);
        endChange();
    }

    public boolean hasChangeListeners() {
        return super.hasListeners();
    }

    /**
     * Returns true if the specified element can be added to this
     * set.
     * <p>
     * Subclasses can return false if they only want to include
     * elements based on a predicate check.
     *
     * @param e an object
     * @return true if the object may be added to this set
     */
    protected abstract boolean mayBeAdded(@NonNull E e);

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Set)) {
            return false;
        }
        Collection<?> c = (Collection<?>) o;
        if (c.size() != size()) {
            return false;
        }
        try {
            return containsAll(c);
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }


    public int hashCode() {
        int h = 0;
        for (E obj : this) {
            if (obj != null) {
                h += obj.hashCode();
            }
        }
        return h;
    }

    @Override
    public @NonNull ReadOnlySequencedSet<E> readOnlyReversed() {
        return new ReadOnlySequencedSetFacade<>(
                () -> new ReverseListSpliterator<>(this, 0, size),
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst,
                Spliterator.DISTINCT | Spliterator.SIZED
        );
    }
}
