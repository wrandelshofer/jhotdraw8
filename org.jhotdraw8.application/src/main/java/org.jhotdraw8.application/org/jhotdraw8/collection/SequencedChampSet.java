/*
 * @(#)ChampSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champset.BitmapIndexedNode;
import org.jhotdraw8.collection.champset.ChampTrie;
import org.jhotdraw8.collection.champset.ChangeEvent;
import org.jhotdraw8.collection.champset.Node;
import org.jhotdraw8.collection.champset.SequencedKey;
import org.jhotdraw8.collection.champset.SequencedKeyIterator;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

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
 *     <li>toImmutable: O(1) + a cost distributed across subsequent updates</li>
 *     <li>clone: O(1) + a cost distributed across subsequent updates</li>
 *     <li>iterator.next(): O(1)</li>
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
public class SequencedChampSet<E> extends AbstractSet<E> implements Serializable, Cloneable, SequencedSet<E> {
    private final static long serialVersionUID = 0L;
    private transient @Nullable UniqueId mutator;
    private transient @NonNull BitmapIndexedNode<SequencedKey<E>> root;
    private transient int size;
    private transient int modCount;

    /**
     * Counter for the sequence number of the last element. The counter is
     * incremented after a new entry is added to the end of the sequence.
     * <p>
     * The counter is in the range from {@code 0} to
     * {@link Integer#MAX_VALUE} - 1.
     * When the counter reaches {@link Integer#MAX_VALUE}, all
     * sequence numbers are renumbered, and the counter is reset to
     * {@code size}.
     */
    private int last = 0;
    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement before a new entry is added to the start of the sequence.
     * <p>
     * The counter is in the range from {@code 0} to
     * {@link Integer#MIN_VALUE}.
     * When the counter is about to wrap over to {@link Integer#MAX_VALUE}, all
     * sequence numbers are renumbered, and the counter is reset to
     * {@code 0}.
     */
    private int first = 0;

    /**
     * Constructs an empty set.
     */
    public SequencedChampSet() {
        this.root = BitmapIndexedNode.emptyNode();
    }

    /**
     * Constructs a set containing the elements in the specified iterable.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public SequencedChampSet(Iterable<? extends E> c) {
        if (c instanceof SequencedChampSet<?>) {
            c = ((SequencedChampSet<? extends E>) c).toImmutable();
        }
        if (c instanceof ImmutableSequencedChampSet<?>) {
            ImmutableSequencedChampSet<E> that = (ImmutableSequencedChampSet<E>) c;
            this.root = that;
            this.size = that.size;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            addAll(c);
        }
    }

    @Override
    public boolean add(final @Nullable E e) {
        return addLast(e, (oldk, newk) -> oldk);
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
    public void addFirst(@Nullable E e) {
        addFirst(e, (oldk, newk) -> newk);
    }

    private boolean addFirst(@Nullable E e,
                             @NonNull BiFunction<SequencedKey<E>, SequencedKey<E>, SequencedKey<E>> updateFunction) {
        final ChangeEvent<SequencedKey<E>> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<SequencedKey<E>> newRoot = root.update(getOrCreateMutator(), new SequencedKey<>(e, first - 1),
                Objects.hashCode(e), 0, changeEvent, updateFunction);
        if (changeEvent.isModified) {
            root = newRoot;
            size++;
            modCount++;
            first--;
            renumber();
        }
        return changeEvent.isModified;
    }

    @Override
    public void addLast(@Nullable E e) {
        addLast(e, (oldk, newk) -> newk);
    }

    private boolean addLast(final @Nullable E e,
                            @NonNull BiFunction<SequencedKey<E>, SequencedKey<E>, SequencedKey<E>> updateFunction) {
        final ChangeEvent<SequencedKey<E>> changeEvent = new ChangeEvent<>();
        final BitmapIndexedNode<SequencedKey<E>> newRoot = root.update(
                getOrCreateMutator(), new SequencedKey<>(e, last), Objects.hashCode(e), 0,
                changeEvent, updateFunction);
        if (changeEvent.isModified) {
            root = newRoot;
            size++;
            modCount++;
            last++;
            renumber();
        }
        return changeEvent.isModified;
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
    public @NonNull SequencedChampSet<E> clone() {
        try {
            mutator = null;
            return (SequencedChampSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return root.findByKey(new SequencedKey<>(key, SequencedKey.NO_SEQUENCE_NUMBER), Objects.hashCode(key), 0) != Node.NO_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof SequencedChampSet<?>) {
            return root.equivalent(((SequencedChampSet<?>) o).root);
        }
        return super.equals(o);
    }

    @Override
    public E getFirst() {
        return SequencedKeyIterator.getFirst(root, first, last).getKey();
    }

    @Override
    public E getLast() {
        return SequencedKeyIterator.getLast(root, first, last).getKey();
    }

    private @NonNull UniqueId getOrCreateMutator() {
        if (mutator == null) {
            mutator = new UniqueId();
        }
        return mutator;
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return iterator(false);
    }

    /**
     * Returns an iterator over the elements of this set, that optionally
     * iterates in reversed direction.
     *
     * @param reversed whether to iterate in reverse direction
     * @return an iterator
     */
    public @NonNull Iterator<E> iterator(boolean reversed) {
        return new FailFastIterator<>(new SequencedKeyIterator<>(
                size, root, reversed,
                this::persistentRemove, null),
                () -> SequencedChampSet.this.modCount);
    }

    private void persistentRemove(E e) {
        mutator = null;
        remove(e);
    }

    @Override
    public boolean remove(final Object o) {
        final ChangeEvent<SequencedKey<E>> changeEvent = new ChangeEvent<>();
        @SuppressWarnings("unchecked")//
        final BitmapIndexedNode<SequencedKey<E>> newRoot = root.remove(
                getOrCreateMutator(), new SequencedKey<>((E) o, SequencedKey.NO_SEQUENCE_NUMBER),
                Objects.hashCode(o), 0, changeEvent);
        if (changeEvent.isModified) {
            root = newRoot;
            size--;
            modCount++;
        }
        return changeEvent.isModified;
    }

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
    public E removeFirst() {
        SequencedKey<E> k = SequencedKeyIterator.getFirst(root, first, last);
        remove(k.getKey());
        first = k.getSequenceNumber() + 1;
        return k.getKey();
    }

    @Override
    public E removeLast() {
        SequencedKey<E> k = SequencedKeyIterator.getLast(root, first, last);
        remove(k.getKey());
        last = k.getSequenceNumber();
        return k.getKey();
    }

    private void renumber() {
        if (first == SequencedKey.NO_SEQUENCE_NUMBER
                || last == SequencedKey.NO_SEQUENCE_NUMBER) {
            root = ChampTrie.renumber(size, root, getOrCreateMutator());
            last = size;
            first = 0;
        }
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
    public @NonNull ImmutableSequencedChampSet<E> toImmutable() {
        mutator = null;
        return size == 0 ? ImmutableSequencedChampSet.of() : new ImmutableSequencedChampSet<>(root, size, first, last);
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(Set<E> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return new SequencedChampSet<>(deserialized);
        }
    }
}