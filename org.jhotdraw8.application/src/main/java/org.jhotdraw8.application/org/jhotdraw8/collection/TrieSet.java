/*
 * @(#)TrieSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.TrieSetHelper.BitmapIndexedNode;
import org.jhotdraw8.collection.TrieSetHelper.ChangeEvent;
import org.jhotdraw8.collection.TrieSetHelper.TrieIterator;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;

public class TrieSet<E> extends AbstractSet<E> implements Serializable, Cloneable {
    private final static long serialVersionUID = 0L;
    private transient UniqueIdentity mutator;
    private BitmapIndexedNode<E> root;
    private int size;
    private int modCount;

    /**
     * Constructs an empty set.
     */
    public TrieSet() {
        this.root = TrieSetHelper.emptyNode();
    }

    /**
     * Constructs a set containing the elements in the specified iterable.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public TrieSet(Iterable<? extends E> c) {
        if (c instanceof TrieSet<?>) {
            c = ((TrieSet<? extends E>) c).toPersistent();
        }
        if (c instanceof PersistentTrieSet<?>) {
            PersistentTrieSet<E> that = (PersistentTrieSet<E>) c;
            this.root = that;
            this.size = that.size;
        } else {
            this.root = TrieSetHelper.emptyNode();
            addAll(c);
        }
    }

    /**
     * Adds the specified element if it is not already in this set.
     *
     * @param e an element
     * @returns {@code true} if this set changed
     */
    public boolean add(final @Nullable E e) {
        final ChangeEvent changeEvent = new ChangeEvent();
        final BitmapIndexedNode<E> newRoot = root.updated(getOrCreateMutator(), e, Objects.hashCode(e), 0, changeEvent);
        if (changeEvent.isModified) {
            root = newRoot;
            size++;
            modCount++;
            return true;
        }
        return false;
    }

    /**
     * Adds all specified elements that are not already in this set.
     *
     * @param c a collection of elements
     * @returns {@code true} if this set changed
     */
    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        return addAll((Iterable<? extends E>) c);
    }

    /**
     * Adds all specified elements that are not already in this set.
     *
     * @param c an iterable of elements
     * @returns {@code true} if this set changed
     */
    @SuppressWarnings("unchecked")
    public boolean addAll(@NonNull Iterable<? extends E> c) {
        if (c == this) {
            return false;
        }

        /*
        if (c instanceof TrieSet<?>) {
            c = ((TrieSet<? extends E>) c).toPersistent();
        }
        if (c instanceof PersistentTrieSet<?>) {
            PersistentTrieSet<E> that = (PersistentTrieSet<E>) c;
            BulkChangeEvent bulkChange = new BulkChangeEvent();
            bulkChange.sizeChange=that.size;
            root = root.copyAddAll(that, 0, bulkChange, getOrCreateMutator());
            if (bulkChange.sizeChange != 0) {
                this.size += bulkChange.sizeChange;
                modCount++;
                return true;
            } else {
                return false;
            }
        } else {*/
            boolean modified = false;
            for (E e : c) {
                modified |= add(e);
            }
            return modified;
        //}
    }

    /**
     * Removes all elements from this set.
     */
    @Override
    public void clear() {
        this.root = TrieSetHelper.emptyNode();
        this.size = 0;
    }

    /**
     * Returns a shallow copy of this set.
     */
    @Override
    public TrieSet<E> clone() {
        try {
            @SuppressWarnings("unchecked") final TrieSet<E> that = (TrieSet<E>) super.clone();
            that.mutator = null;
            this.mutator = null;
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return root.contains(key, Objects.hashCode(key), 0);
    }

    private @NonNull UniqueIdentity getOrCreateMutator() {
        if (mutator == null) {
            mutator = new UniqueIdentity();
        }
        return mutator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns an iterator over the elements of this set.
     */
    @Override
    public Iterator<E> iterator() {
        return new MutableTrieIterator<>(this);
    }

    /**
     * Removes the specified element if it is in this set.
     *
     * @param o an element
     * @returns {@code true} if this set changed
     */
    public boolean remove(final Object o) {
        @SuppressWarnings("unchecked")
        E key = (E) o;
        final ChangeEvent changeEvent = new ChangeEvent();
        final BitmapIndexedNode<E> newRoot = root.removed(getOrCreateMutator(), key, Objects.hashCode(key), 0, changeEvent);
        if (changeEvent.isModified) {
            root = newRoot;
            size--;
            modCount++;
            return true;
        }
        return false;
    }

    /**
     * Removes all specified elements that are in this set.
     *
     * @param c a collection of elements
     * @returns {@code true} if this set changed
     */
    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return removeAll((Iterable<?>) c);
    }

    /**
     * Removes all specified elements that are in this set.
     *
     * @param c an iterable of elements
     * @returns {@code true} if this set changed
     */
    public boolean removeAll(@NonNull Iterable<?> c) {
        if (isEmpty()) {
            return false;
        }
        if (c == this) {
            clear();
            return true;
        }
        boolean modified = false;
        for (Object o : c) {
            modified |= remove(o);
        }
        return modified;
    }

    /**
     * {@inheritDoc
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns a persistent copy of this set.
     *
     * @return a persistent trie set
     */
    public PersistentTrieSet<E> toPersistent() {
        mutator = null;
        return size == 0 ? PersistentTrieSet.of() : new PersistentTrieSet<>(root, size);
    }

    static class MutableTrieIterator<E> extends TrieIterator<E> {
        private final @NonNull TrieSet<E> set;
        private int expectedModCount;

        MutableTrieIterator(@NonNull TrieSet<E> set) {
            super(set.root);
            this.set = set;
            this.expectedModCount = set.modCount;
        }

        @Override
        public E next() {
            if (expectedModCount != set.modCount) {
                throw new ConcurrentModificationException();
            }
            return super.next();
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            }
            if (expectedModCount != set.modCount) {
                throw new ConcurrentModificationException();
            }

            E toRemove = current;
            if (hasNext()) {
                E next = next();
                set.remove(toRemove);
                moveTo(next, set.root);
            } else {
                set.remove(toRemove);
            }

            expectedModCount = set.modCount;
            canRemove = false;
            current = null;
        }
    }
}