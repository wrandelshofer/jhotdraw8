package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * Implements the {@link Set} interface with a
 * Compressed Hash-Array Mapped Prefix-trie (CHAMP).
 * <p>
 * Creating a persistent copy is performed in O(1).
 * <p>
 * References:
 * <dl>
 *     <dt>This class has been derived from "The Capsule Hash Trie Collections Library".</dt>
 *     <dd>Copyright (c) Michael Steindorfer, Centrum Wiskunde & Informatica, and Contributors.
 *         BSD 2-Clause License.
 *         <a href="https://github.com/usethesource/capsule">github.com</a>.</dd>
 * </dl>
 *
 * @param <E> the element type
 */
public class TrieSet<E> extends AbstractSet<E> implements Serializable {
    private final static long serialVersionUID = 0L;
    private PersistentTrieHelper.UniqueIdentity mutator;
    private PersistentTrieSetHelper.BitmapIndexedNode<E> root;
    private int hashCode;
    private int size;
    private int modCount;

    /**
     * Constructs an empty set.
     */
    public TrieSet() {
        this.mutator = new PersistentTrieHelper.UniqueIdentity();
        this.root = PersistentTrieSetHelper.emptyNode();
    }

    /**
     * Constructs a new set containing the elements in the specified
     * iterable.
     *
     * @param c an iterable
     */
    public TrieSet(Iterable<? extends E> c) {
        this();
        addAll(c);
    }

    TrieSet(PersistentTrieSet<E> trieSet) {
        this.root = trieSet.root;
        this.hashCode = trieSet.hashCode;
        this.size = trieSet.size;
        this.mutator = new PersistentTrieHelper.UniqueIdentity();
    }

    public boolean add(final @Nullable E key) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieSetHelper.ChangeEvent changeEvent = new PersistentTrieSetHelper.ChangeEvent();
        final PersistentTrieSetHelper.BitmapIndexedNode<E> newRootNode =
                (PersistentTrieSetHelper.BitmapIndexedNode<E>) root.updated(this.mutator, key, keyHash, 0, changeEvent);
        if (changeEvent.isModified) {
            root = newRootNode;
            hashCode += keyHash;
            size += 1;
            modCount++;
            return true;
        }
        return false;
    }

    /**
     * Adds all elements in the specified collection to this set if
     * they're not already present.
     *
     * @param c a collection
     * @return true if this set has changed
     */
    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        return addAll((Iterable<? extends E>) c);
    }

    @SuppressWarnings("unchecked")
    public boolean addAll(@NonNull Iterable<? extends E> c) {
        if (c == this) {
            return false;
        }

        if ((c instanceof TrieSet) || (c instanceof PersistentTrieSet)) {
            PersistentTrieSetHelper.Node<E> root;
            PersistentTrieSetHelper.BulkChangeEvent bulkChange = new PersistentTrieSetHelper.BulkChangeEvent();
            if (c instanceof TrieSet) {
                TrieSet<? extends E> trieSet = (TrieSet<? extends E>) c;
                root = (PersistentTrieSetHelper.Node<E>) trieSet.root;
                bulkChange.hashChange = trieSet.hashCode;
                bulkChange.sizeChange = trieSet.size;
            } else {
                PersistentTrieSet<? extends E> trieSet = (PersistentTrieSet<? extends E>) c;
                bulkChange.hashChange = trieSet.hashCode;
                bulkChange.sizeChange = trieSet.size;
                root = (PersistentTrieSetHelper.Node<E>) trieSet.root;
            }
            PersistentTrieSetHelper.BitmapIndexedNode<E> newNode = this.root.copyAddAll(root, 0, bulkChange);
            if (newNode == this.root) {
                return false;
            }
            this.root = newNode;
            this.size += bulkChange.sizeChange;
            this.hashCode += bulkChange.hashChange;
            return true;
        }

        boolean modified = false;
        for (E e : c) {
            modified |= add(e);
        }
        return modified;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        this.root = PersistentTrieSetHelper.emptyNode();
        this.size = this.hashCode = 0;
    }

    public boolean remove(final Object o) {
        @SuppressWarnings("unchecked")
        E key = (E) o;
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieSetHelper.ChangeEvent changeEvent = new PersistentTrieSetHelper.ChangeEvent();
        final PersistentTrieSetHelper.BitmapIndexedNode<E> newRootNode =
                (PersistentTrieSetHelper.BitmapIndexedNode<E>) root.removed(this.mutator, key, keyHash, 0, changeEvent);
        if (changeEvent.isModified) {
            root = newRootNode;
            hashCode = hashCode - keyHash;
            size = size - 1;
            modCount++;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return removeAll((Iterable<?>) c);
    }

    public boolean removeAll(@NonNull Iterable<?> c) {
        boolean modified = false;
        for (Object o : c) {
            modified |= remove(o);
        }
        return modified;
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return root.contains(key, Objects.hashCode(key), 0);
    }

    /**
     * Returns a copy of this set that is persistent.
     * <p>
     * This operation is performed in O(1).
     *
     * @return a persistent trie set
     */
    public PersistentTrieSet<E> toPersistent() {
        mutator = new PersistentTrieHelper.UniqueIdentity();
        return size == 0 ? PersistentTrieSet.of() : new PersistentTrieSet<>(root, hashCode, size);
    }

    @Override
    public Iterator<E> iterator() {
        return new TransientTrieIterator<>(this);
    }

    @Override
    public int size() {
        return size;
    }

    static class TransientTrieIterator<E> extends PersistentTrieSetHelper.TrieIterator<E> {
        private final @NonNull TrieSet<E> set;
        private int expectedModCount;

        TransientTrieIterator(@NonNull TrieSet<E> set) {
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
            if (current == null) {
                throw new IllegalStateException();
            }
            if (expectedModCount != set.modCount) {
                throw new ConcurrentModificationException();
            }

            E toRemove = current;
            if (hasNext()) {
                E next = next();
                set.remove(toRemove);
                expectedModCount = set.modCount;
                moveTo(next, set.root);
            } else {
                set.remove(toRemove);
                expectedModCount = set.modCount;
            }

            current = null;
        }
    }
}
