/*
 * @(#)ImmutableChampSet.java
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;


/**
 * Implements an immutable set using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>copyAdd: O(1)</li>
 *     <li>copyRemove: O(1)</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(1) + a cost distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This set performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP tree contains nodes that may be shared with other sets.
 * <p>
 * If a write operation is performed on a node, then this set creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP tree has a fixed maximal height, the cost is O(1).
 * <p>
 * This set can create a mutable copy of itself in O(1) time and O(1) space
 * using method {@link #toMutable()}}. The mutable copy shares its nodes
 * with this set, until it has gradually replaced the nodes with exclusively
 * owned nodes.
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
@SuppressWarnings("exports")
public class ImmutableChampSet<E> extends BitmapIndexedNode<E> implements ImmutableSet<E>, Serializable {
    private final static long serialVersionUID = 0L;
    private static final ImmutableChampSet<?> EMPTY = new ImmutableChampSet<>(BitmapIndexedNode.emptyNode(), 0);
    final int size;

    ImmutableChampSet(@NonNull BitmapIndexedNode<E> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        this.size = size;
    }

    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull ImmutableChampSet<E> of() {
        return ((ImmutableChampSet<E>) ImmutableChampSet.EMPTY);
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
    public static <E> @NonNull ImmutableChampSet<E> of(E... elements) {
        return ((ImmutableChampSet<E>) ImmutableChampSet.EMPTY).copyAddAll(Arrays.asList(elements));
    }

    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param iterable an iterable
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull ImmutableChampSet<E> copyOf(@NonNull Iterable<? extends E> iterable) {
        return ((ImmutableChampSet<E>) ImmutableChampSet.EMPTY).copyAddAll(iterable);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return findByKey((E) o, Objects.hashCode(o), 0, getEqualsFunction()) != Node.NO_VALUE;
    }

    @Override
    public @NonNull ImmutableChampSet<E> copyAdd(@NonNull E key) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<E> changeEvent = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = update(null, key, keyHash, 0, changeEvent, getUpdateFunction(), getEqualsFunction(), getHashFunction());
        if (changeEvent.modified) {
            return new ImmutableChampSet<>(newRootNode, size + 1);
        }
        return this;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public @NonNull ImmutableChampSet<E> copyAddAll(@NonNull Iterable<? extends E> set) {
        if (set == this || isEmpty() && (set instanceof ImmutableChampSet<?>)) {
            return (ImmutableChampSet<E>) set;
        }
        if (isEmpty() && (set instanceof ChampSet)) {
            return ((ChampSet<E>) set).toImmutable();
        }
        ChampSet<E> t = toMutable();
        boolean modified = false;
        for (E key : set) {
            modified |= t.add(key);
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ImmutableSet<E> copyClear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull ImmutableChampSet<E> copyRemove(@NonNull E key) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<E> changeEvent = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = remove(null, key, keyHash, 0, changeEvent, getEqualsFunction());
        if (changeEvent.modified) {
            return new ImmutableChampSet<>(newRootNode, size - 1);
        }
        return this;
    }

    @Override
    public @NonNull ImmutableChampSet<E> copyRemoveAll(@NonNull Iterable<?> set) {
        if (isEmpty()
                || (set instanceof Collection<?>) && ((Collection<?>) set).isEmpty()
                || (set instanceof ReadOnlyCollection<?>) && ((ReadOnlyCollection<?>) set).isEmpty()) {
            return this;
        }
        if (set == this) {
            return of();
        }
        final ChampSet<E> t = toMutable();
        boolean modified = false;
        for (final Object key : set) {
            //noinspection SuspiciousMethodCalls
            if (t.remove(key)) {
                modified = true;
                if (t.isEmpty()) {
                    break;
                }
            }
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ImmutableChampSet<E> copyRetainAll(final @NonNull Collection<?> set) {
        if (isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }
        final ChampSet<E> t = this.toMutable();
        boolean modified = false;
        for (Object key : this) {
            if (!set.contains(key)) {
                //noinspection SuspiciousMethodCalls
                t.remove(key);
                modified = true;
                if (t.isEmpty()) {
                    break;
                }
            }
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof ImmutableChampSet) {
            ImmutableChampSet<?> that = (ImmutableChampSet<?>) other;
            return size == that.size && equivalent(that);
        }
        return ReadOnlySet.setEquals(this, other);
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new KeyIterator<E>(this, null);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public @NonNull ChampSet<E> toMutable() {
        return new ChampSet<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(@NonNull Set<E> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return ImmutableChampSet.copyOf(deserialized);
        }
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<E>(this.toMutable());
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
}