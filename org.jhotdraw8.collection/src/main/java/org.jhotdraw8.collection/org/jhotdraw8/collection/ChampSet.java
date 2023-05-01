/*
 * @(#)ChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableSet;
import org.jhotdraw8.collection.impl.champ.ChampBitmapIndexedNode;
import org.jhotdraw8.collection.impl.champ.ChampChangeEvent;
import org.jhotdraw8.collection.impl.champ.ChampNode;
import org.jhotdraw8.collection.impl.champ.ChampSpliterator;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.collection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;


/**
 * Implements an immutable set using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>30</sup> entries</li>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>add: O(1)</li>
 *     <li>remove: O(1)</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This set performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP trie contains nodes that may be shared with other sets.
 * <p>
 * If a write operation is performed on a node, then this set creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP trie has a fixed maximal height, the cost is O(1).
 * <p>
 * This set can create a mutable copy of itself in O(1) time and O(1) space
 * using method {@link #toMutable()}}. The mutable copy shares its nodes
 * with this set, until it has gradually replaced the nodes with exclusively
 * owned nodes.
 * <p>
 * The immutable version of this set extends from the non-public class
 * {@code ChampBitmapIndexNode}. This design safes 16 bytes for every instance,
 * and reduces the number of redirections for finding an element in the
 * collection by 1.
 * <p>
 * References:
 * <p>
 * Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a></dd>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
@SuppressWarnings("exports")
public class ChampSet<E> extends ChampBitmapIndexedNode<E> implements ImmutableSet<E>, Serializable {
    private static final @NonNull ChampSet<?> EMPTY = new ChampSet<>(ChampBitmapIndexedNode.emptyNode(), 0);
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * The size of the set.
     */
    final int size;

    ChampSet(@NonNull ChampBitmapIndexedNode<E> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        this.size = size;
    }

    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param iterable an iterable
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull ChampSet<E> copyOf(@NonNull Iterable<? extends E> iterable) {
        return ((ChampSet<E>) ChampSet.EMPTY).addAll(iterable);
    }

    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull ChampSet<E> of() {
        return ((ChampSet<E>) ChampSet.EMPTY);
    }

    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings({"unchecked", "varargs"})
    @SafeVarargs
    public static <E> @NonNull ChampSet<E> of(@NonNull E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ChampSet.<E>of().addAll(Arrays.asList(elements));
    }

    @Override
    public @NonNull ChampSet<E> add(@Nullable E element) {
        int keyHash = Objects.hashCode(element);
        ChampChangeEvent<E> details = new ChampChangeEvent<>();
        ChampBitmapIndexedNode<E> newRootNode = update(null, element, keyHash, 0, details, ChampSet::updateElement, Objects::equals, Objects::hashCode);
        if (details.isModified()) {
            return new ChampSet<>(newRootNode, size + 1);
        }
        return this;
    }

    @Override
    public @NonNull ChampSet<E> addAll(@NonNull Iterable<? extends E> set) {
        var t = toMutable();
        return t.addAll(set) ? t.toImmutable() : this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull ChampSet<E> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return find((E) o, Objects.hashCode(o), 0, Objects::equals) != ChampNode.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof ChampSet) {
            ChampSet<?> that = (ChampSet<?>) other;
            return size == that.size && equivalent(that);
        }
        return ReadOnlySet.setEquals(this, other);
    }

    /**
     * Update function for a set: we always keep the old element.
     *
     * @param oldElement the old element
     * @param newElement the new element
     * @param <E>        the element type
     * @return always returns the old element
     */
    private static <E> E updateElement(E oldElement, E newElement) {
        return oldElement;
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public @NonNull ChampSet<E> remove(@NonNull E key) {
        int keyHash = Objects.hashCode(key);
        ChampChangeEvent<E> details = new ChampChangeEvent<>();
        ChampBitmapIndexedNode<E> newRootNode = remove(null, key, keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            return new ChampSet<>(newRootNode, size - 1);
        }
        return this;
    }

    @Override
    public @NonNull ChampSet<E> removeAll(@NonNull Iterable<?> set) {
        if (isEmpty()
                || (set instanceof Collection<?> c) && c.isEmpty()
                || (set instanceof ReadOnlyCollection<?> rc) && rc.isEmpty()) {
            return this;
        }
        if (set == this) {
            return of();
        }
        var t = toMutable();
        return t.removeAll(set) ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ChampSet<E> retainAll(@NonNull Collection<?> set) {
        if (isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }
        var t = toMutable();
        return t.retainAll(set) ? t.toImmutable() : this;
    }

    @Override
    public int size() {
        return size;
    }

    public @NonNull Spliterator<E> spliterator() {
        return new ChampSpliterator<>(this, null, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT, size);
    }

    @Override
    public @NonNull MutableChampSet<E> toMutable() {
        return new MutableChampSet<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(@NonNull Set<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            return ChampSet.copyOf(deserialized);
        }
    }
}