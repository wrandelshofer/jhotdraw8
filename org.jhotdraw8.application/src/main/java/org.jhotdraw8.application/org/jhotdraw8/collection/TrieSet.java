package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractSet;
import java.util.Collection;
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
public class TrieSet<E> extends AbstractSet<E> {
    private PersistentTrieHelper.Nonce bulkEdit;
    PersistentTrieSet.BitmapIndexedNode<E> root;
    int hashCode;
    int size;

    /**
     * Constructs an empty set.
     */
    public TrieSet() {
        this.bulkEdit = new PersistentTrieHelper.Nonce();
        this.root = PersistentTrieSet.emptyNode();
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
        this.bulkEdit = new PersistentTrieHelper.Nonce();
    }

    public boolean add(final @Nullable E key) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieSet.ChangeEvent changeEvent = new PersistentTrieSet.ChangeEvent();
        final PersistentTrieSet.BitmapIndexedNode<E> newRootNode = (PersistentTrieSet.BitmapIndexedNode<E>) root.updated(this.bulkEdit, key, keyHash, 0, changeEvent);
        if (changeEvent.isModified) {
            root = newRootNode;
            hashCode += keyHash;
            size += 1;
            return true;
        }
        return false;
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present.
     *
     * @param c a collection
     * @return true if this set has changed
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c instanceof TrieSet) {
            return addAll((Iterable<? extends E>) c);
        }
        return super.addAll(c);
    }

    @SuppressWarnings("unchecked")
    public boolean addAll(Iterable<? extends E> c) {
        if (c == this) {
            return false;
        }

        if ((c instanceof TrieSet) || (c instanceof PersistentTrieSet)) {
            PersistentTrieSet.Node<E> root;
            PersistentTrieSet.BulkChangeEvent bulkChange = new PersistentTrieSet.BulkChangeEvent();
            if (c instanceof TrieSet) {
                TrieSet<? extends E> trieSet = (TrieSet<? extends E>) c;
                root = (PersistentTrieSet.Node<E>) trieSet.root;
                bulkChange.hashChange = trieSet.hashCode;
                bulkChange.sizeChange = trieSet.size;
            } else {
                PersistentTrieSet<? extends E> trieSet = (PersistentTrieSet<? extends E>) c;
                bulkChange.hashChange = trieSet.hashCode;
                bulkChange.sizeChange = trieSet.size;
                root = (PersistentTrieSet.Node<E>) trieSet.root;
            }
            PersistentTrieSet.BitmapIndexedNode<E> newNode = this.root.copyAddAll(root, 0, bulkChange);
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
        this.root = PersistentTrieSet.<E>emptyNode();
        this.size = this.hashCode = 0;
    }

    public boolean remove(final Object o) {
        @SuppressWarnings("unchecked")
        E key = (E) o;
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieSet.ChangeEvent changeEvent = new PersistentTrieSet.ChangeEvent();
        final PersistentTrieSet.BitmapIndexedNode<E> newRootNode = (PersistentTrieSet.BitmapIndexedNode<E>) root.removed(this.bulkEdit, key, keyHash, 0, changeEvent);
        if (changeEvent.isModified) {
            root = newRootNode;
            hashCode = hashCode - keyHash;
            size = size - 1;
            return true;
        }
        return false;
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
        bulkEdit = new PersistentTrieHelper.Nonce();
        return size == 0 ? PersistentTrieSet.of() : new PersistentTrieSet<>(root, hashCode, size);
    }

    @Override
    public Iterator<E> iterator() {
        return new PersistentTrieSet.TrieIterator<>(root);
    }

    @Override
    public int size() {
        return size;
    }
}
