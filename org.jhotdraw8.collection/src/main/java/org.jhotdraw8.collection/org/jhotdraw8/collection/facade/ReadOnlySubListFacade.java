/*
 * @(#)ReadOnlySubListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.AbstractReadOnlyList;
import org.jhotdraw8.collection.readonly.ReadOnlyList;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedCollection;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class ReadOnlySubListFacade<E> extends AbstractReadOnlyList<E> {

    private final ReadOnlyList<E> root;
    private final ReadOnlySubListFacade<E> parent;
    private final int offset;
    private int size;
    private int modCount;
    private final @NonNull IntSupplier modCountSupplier;

    /**
     * Constructs a sublist of an arbitrary ArrayList.
     */
    public ReadOnlySubListFacade(ReadOnlyList<E> root, int fromIndex, int toIndex, @NonNull IntSupplier modCountSupplier) {
        this.root = root;
        this.modCountSupplier = modCountSupplier;
        this.parent = null;
        this.offset = fromIndex;
        this.size = toIndex - fromIndex;
        this.modCount = modCountSupplier.getAsInt();
    }

    /**
     * Constructs a sublist of another SubList.
     */
    private ReadOnlySubListFacade(ReadOnlySubListFacade<E> parent, int fromIndex, int toIndex) {
        this.root = parent.root;
        this.parent = parent;
        this.offset = parent.offset + fromIndex;
        this.size = toIndex - fromIndex;
        this.modCount = parent.modCount;
        this.modCountSupplier = parent.modCountSupplier;
    }

    public E get(int index) {
        Objects.checkIndex(index, size);
        ensureUnmodified();
        return root.get(offset + index);
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlyListFacade<>(
                () -> size,
                index -> get(size - 1 - index),
                () -> this);
    }

    public int size() {
        ensureUnmodified();
        return size;
    }


    public boolean equals(Object o) {
        ensureUnmodified();
        return super.equals(o);
    }

    public int hashCode() {
        ensureUnmodified();
        return super.hashCode();
    }

    public int indexOf(Object o) {
        ensureUnmodified();
        for (int i = 0; i < size; i++) {
            if (Objects.equals(root.get(i + offset), o)) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        ensureUnmodified();
        for (int i = size - 1; i >= 0; i--) {
            if (Objects.equals(root.get(i + offset), o)) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    public Iterator<E> iterator() {
        return listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        ensureUnmodified();
        Objects.checkIndex(index, size + 1);

        return new ListIterator<E>() {
            int cursor = index;
            int lastRet = -1;
            int expectedModCount = ReadOnlySubListFacade.this.modCount;

            public boolean hasNext() {
                return cursor != ReadOnlySubListFacade.this.size;
            }

            @SuppressWarnings("unchecked")
            public E next() {
                checkForComodification();
                int i = cursor;
                if (i >= ReadOnlySubListFacade.this.size)
                    throw new NoSuchElementException();
                if (offset + i >= root.size())
                    throw new ConcurrentModificationException();
                cursor = i + 1;
                return (E) root.get(offset + (lastRet = i));
            }

            public boolean hasPrevious() {
                return cursor != 0;
            }

            @SuppressWarnings("unchecked")
            public E previous() {
                checkForComodification();
                int i = cursor - 1;
                if (i < 0)
                    throw new NoSuchElementException();
                if (offset + i >= root.size())
                    throw new ConcurrentModificationException();
                cursor = i;
                return (E) root.get(offset + (lastRet = i));
            }

            public void forEachRemaining(Consumer<? super E> action) {
                Objects.requireNonNull(action);
                final int size = ReadOnlySubListFacade.this.size;
                int i = cursor;
                if (i < size) {
                    if (offset + i >= root.size())
                        throw new ConcurrentModificationException();
                    for (; i < size && modCountSupplier.getAsInt() == expectedModCount; i++)
                        action.accept(get(offset + i));
                    cursor = i;
                    lastRet = i - 1;
                }
            }

            public int nextIndex() {
                return cursor;
            }

            public int previousIndex() {
                return cursor - 1;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(E e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(E e) {
                throw new UnsupportedOperationException();
            }

            void checkForComodification() {
                if (modCountSupplier.getAsInt() != expectedModCount)
                    throw new ConcurrentModificationException();
            }
        };
    }

    public ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        Objects.checkIndex(fromIndex, toIndex + 1);
        Objects.checkIndex(toIndex, size + 1);
        return new ReadOnlySubListFacade<>(this, fromIndex, toIndex);
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > this.size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + this.size;
    }

    private void ensureUnmodified() {
        if (modCountSupplier.getAsInt() != modCount)
            throw new ConcurrentModificationException();
    }

}
