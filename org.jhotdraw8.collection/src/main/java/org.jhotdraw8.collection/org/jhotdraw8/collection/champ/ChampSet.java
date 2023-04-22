/*
 * @(#)ChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableSet;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.collection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;


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
public class ChampSet<E> extends BitmapIndexedNode<E> implements ImmutableSet<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private static final @NonNull ChampSet<?> EMPTY = new ChampSet<>(BitmapIndexedNode.emptyNode(), 0);
    final int size;

    ChampSet(@NonNull BitmapIndexedNode<E> root, int size) {
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
     * Returns an immutable set that contains the provided elements.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings({"unchecked", "varargs"})
    @SafeVarargs
    public static <E> @NonNull ChampSet<E> of(E... elements) {
        return ((ChampSet<E>) ChampSet.EMPTY).addAll(Arrays.asList(elements));
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

    @Override
    public @NonNull ChampSet<E> add(@NonNull E key) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<E> details = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = update(null, key, keyHash, 0, details, getUpdateFunction(), Objects::equals, Objects::hashCode);
        if (details.isModified()) {
            return new ChampSet<>(newRootNode, size + 1);
        }
        return this;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public @NonNull ChampSet<E> addAll(@NonNull Iterable<? extends E> set) {
        if (set == this || isEmpty() && (set instanceof ChampSet<?>)) {
            return (ChampSet<E>) set;
        }
        if (isEmpty() && (set instanceof MutableChampSet<?> t)) {
            return (ChampSet<E>) t.toImmutable();
        }
        // XXX if the other set is a ChampSet or a VectorChampSet, we should merge the trees
        // See kotlinx collections:
        // https://github.com/Kotlin/kotlinx.collections.immutable/blob/d7b83a13fed459c032dab1b4665eda20a04c740f/core/commonMain/src/implementations/immutableSet/TrieNode.kt#L338

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
        return find((E) o, Objects.hashCode(o), 0, Objects::equals) != Node.NO_DATA;
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

    @NonNull
    private BiFunction<E, E, E> getUpdateFunction() {
        return (oldk, newk) -> oldk;
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return Spliterators.iterator(spliterator());
    }

    public @NonNull Spliterator<E> spliterator() {
        return new ChampSpliterator<>(this, null, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT, size);
    }

    @Override
    public @NonNull ChampSet<E> remove(@NonNull E key) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<E> details = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = remove(null, key, keyHash, 0, details, Objects::equals);
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