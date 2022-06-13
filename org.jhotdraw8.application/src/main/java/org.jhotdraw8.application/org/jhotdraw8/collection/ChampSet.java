/*
 * @(#)ChampSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.KeyIterator;
import org.jhotdraw8.collection.champ.Node;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

/**
 * Implements a mutable set using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>allows null elements</li>
 *     <li>is mutable</li>
 *     <li>is not thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>add: O(1)</li>
 *     <li>remove: O(1)</li>
 *     <li>contains: O(1)</li>
 *     <li>toImmutable: O(1) + a cost distributed across subsequent updates in
 *     this set</li>
 *     <li>clone: O(1) + a cost distributed across subsequent updates in this
 *     set and in the clone</li>
 *     <li>iterator.next: O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This set performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP tree contains nodes that may be shared with other sets, and nodes
 * that are exclusively owned by this set.
 * <p>
 * If a write operation is performed on an exclusively owned node, then this
 * set is allowed to mutate the node (mutate-on-write).
 * If a write operation is performed on a potentially shared node, then this
 * set is forced to create an exclusive copy of the node and of all not (yet)
 * exclusively owned parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP tree has a fixed maximal height, the cost is O(1) in either
 * case.
 * <p>
 * This set can create an immutable copy of itself in O(1) time and O(0) space
 * using method {@link #toImmutable()}. This set loses exclusive ownership of
 * all its tree nodes.
 * Thus, creating an immutable copy increases the constant cost of
 * subsequent writes, until all shared nodes have been gradually replaced by
 * exclusively owned nodes again.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access this set concurrently, and at least
 * one of the threads modifies the set, it <em>must</em> be synchronized
 * externally.  This is typically accomplished by synchronizing on some
 * object that naturally encapsulates the set.
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. BSD-2-Clause License</dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <E> the element type
 */
public class ChampSet<E> extends AbstractSet<E> implements Serializable, Cloneable {
    private final static long serialVersionUID = 0L;
    private transient @Nullable UniqueId mutator;
    private transient @NonNull BitmapIndexedNode<E> root;
    private transient int size;
    private transient int modCount;

    /**
     * Constructs an empty set.
     */
    public ChampSet() {
        this.root = BitmapIndexedNode.emptyNode();
    }

    /**
     * Constructs a set containing the elements in the specified iterable.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public ChampSet(@NonNull Iterable<? extends E> c) {
        if (c instanceof ChampSet<?>) {
            c = ((ChampSet<? extends E>) c).toImmutable();
        }
        if (c instanceof ImmutableChampSet<?>) {
            ImmutableChampSet<E> that = (ImmutableChampSet<E>) c;
            this.root = that;
            this.size = that.size;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            addAll(c);
        }
    }

    @Override
    public boolean add(final @Nullable E e) {
        ChangeEvent<E> changeEvent = new ChangeEvent<>();
        BitmapIndexedNode<E> newRoot = root.update(getOrCreateMutator(),
                e, Objects.hashCode(e), 0, changeEvent,
                getUpdateFunction(),
                getEqualsFunction(),
                getHashFunction());
        if (changeEvent.isModified) {
            root = newRoot;
            size++;
            modCount++;
        }
        return changeEvent.isModified;
    }

    @NonNull
    private ToIntFunction<E> getHashFunction() {
        return Objects::hashCode;
    }

    @NonNull
    private BiPredicate<E, E> getEqualsFunction() {
        return Objects::equals;
    }

    @NonNull
    private BiFunction<E, E, E> getUpdateFunction() {
        return (oldk, newk) -> oldk;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        return addAll((Iterable<? extends E>) c);
    }

    /**
     * Adds all specified elements that are not already in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
     */
    public boolean addAll(@NonNull Iterable<? extends E> c) {
        if (c == this) {
            return false;
        }
        boolean modified = false;
        for (E e : c) {
            modified |= add(e);
        }
        return modified;
    }

    @Override
    public void clear() {
        root = BitmapIndexedNode.emptyNode();
        size = 0;
        modCount++;
    }

    /**
     * Returns a shallow copy of this set.
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ChampSet<E> clone() {
        try {
            mutator = null;
            return (ChampSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable final Object o) {
        return root.findByKey((E) o, Objects.hashCode(o), 0,
                getEqualsFunction()) != Node.NO_VALUE;
    }

    private @NonNull UniqueId getOrCreateMutator() {
        if (mutator == null) {
            mutator = new UniqueId();
        }
        return mutator;
    }

    /**
     * Returns an iterator over the elements of this set.
     */
    @Override
    public @NonNull Iterator<E> iterator() {
        return new FailFastIterator<>(
                new KeyIterator<>(root, this::immutableRemove),
                () -> this.modCount);
    }

    private void immutableRemove(E e) {
        mutator = null;
        remove(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        ChangeEvent<E> changeEvent = new ChangeEvent<>();
        BitmapIndexedNode<E> newRoot = root.remove(
                getOrCreateMutator(), (E) o, Objects.hashCode(o), 0, changeEvent,
                getEqualsFunction());
        if (changeEvent.isModified) {
            root = newRoot;
            size--;
            modCount++;
        }
        return changeEvent.isModified;
    }

    /**
     * Removes all specified elements that are in this set.
     *
     * @param c a collection of elements
     * @return {@code true} if this set changed
     */
    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return removeAll((Iterable<?>) c);
    }

    /**
     * Removes all specified elements that are in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
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

    @Override
    public int size() {
        return size;
    }

    /**
     * Returns an immutable copy of this set.
     *
     * @return an immutable copy
     */
    public @NonNull ImmutableChampSet<E> toImmutable() {
        mutator = null;
        return size == 0 ? ImmutableChampSet.of() : new ImmutableChampSet<>(root, size);
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(@NonNull Set<E> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return new ChampSet<>(deserialized);
        }
    }
}