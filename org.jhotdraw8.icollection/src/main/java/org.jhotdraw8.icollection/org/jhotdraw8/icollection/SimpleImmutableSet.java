/*
 * @(#)SimpleImmutableSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.immutable.CollectionOps;
import org.jhotdraw8.icollection.immutable.ImmutableSet;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChampIterator;
import org.jhotdraw8.icollection.impl.champ.ChampSpliterator;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.Node;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;


/**
 * Implements the {@link ImmutableSet} interface using a Compressed Hash-Array
 * Mapped Prefix-tree (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>31</sup> - 1 elements</li>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>add: O(log₃₂ N)</li>
 *     <li>remove: O(log₃₂ N</li>
 *     <li>contains: O(log₃₂ N)</li>
 *     <li>toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This set performs read and write operations of single elements in O(log₃₂ N) time,
 * and in O(log₃₂ N) space.
 * <p>
 * The CHAMP trie contains nodes that may be shared with other sets.
 * <p>
 * If a write operation is performed on a node, then this set creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * <p>
 * This set can create a mutable copy of itself in O(1) time and O(1) space
 * using method {@link #toMutable()}. The mutable copy shares its nodes
 * with this set, until it has gradually replaced the nodes with exclusively
 * owned nodes.
 * <p>
 * All operations on this set can be performed concurrently, without a need for
 * synchronisation.
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
public class SimpleImmutableSet<E> implements ImmutableSet<E>, Serializable {
    /**
     * We do not guarantee an iteration order. Make sure that nobody accidentally relies on it.
     */
    static final int SALT = new Random().nextInt();
    private static final @NonNull SimpleImmutableSet<?> EMPTY = new SimpleImmutableSet<>(BitmapIndexedNode.emptyNode(), 0);
    @Serial
    private static final long serialVersionUID = 0L;
    final @NonNull BitmapIndexedNode<E> root;
    final int size;

    /**
     * Creates a new instance with the provided privateData data object.
     * <p>
     * This constructor is intended to be called from a constructor
     * of the subclass, that is called from method {@link #newInstance(PrivateData)}.
     *
     * @param privateData an privateData data object
     */
    @SuppressWarnings("unchecked")
    protected SimpleImmutableSet(@NonNull PrivateData privateData) {
        this(((Map.Entry<BitmapIndexedNode<E>, ?>) privateData.get()).getKey(), ((Map.Entry<?, Integer>) privateData.get()).getValue());
    }

    /**
     * Creates a new instance with the provided privateData object as its internal data structure.
     * <p>
     * Subclasses must override this method, and return a new instance of their subclass!
     *
     * @param privateData the internal data structure needed by this class for creating the instance.
     * @return a new instance of the subclass
     */
    protected @NonNull SimpleImmutableSet<E> newInstance(@NonNull PrivateData privateData) {
        return new SimpleImmutableSet<>(privateData);
    }

    private @NonNull SimpleImmutableSet<E> newInstance(@NonNull BitmapIndexedNode<E> root, int size) {
        return new SimpleImmutableSet<>(new PrivateData(new AbstractMap.SimpleImmutableEntry<>(root, size)));
    }
    SimpleImmutableSet(@NonNull BitmapIndexedNode<E> root, int size) {
        this.root = root;
        this.size = size;
    }


    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param c   an iterable
     * @param <E> the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull SimpleImmutableSet<E> copyOf(@NonNull Iterable<? extends E> c) {
        return SimpleImmutableSet.<E>of().addAll(c);
    }

    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */
    @SuppressWarnings("unchecked")
    public static <E> @NonNull SimpleImmutableSet<E> of() {
        return ((SimpleImmutableSet<E>) SimpleImmutableSet.EMPTY);
    }

    @SuppressWarnings("unchecked")
    public static <T> SimpleImmutableSet<T> ofIterator(Iterator<T> iterator) {
        return SimpleImmutableSet.<T>of().addAll(() -> (Iterator<T>) iterator);
    }
    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> @NonNull SimpleImmutableSet<E> of(@NonNull E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return SimpleImmutableSet.<E>of().addAll(Arrays.asList(elements));
    }

    /**
     * Update function for a set: we always keep the old element.
     *
     * @param oldElement the old element
     * @param newElement the new element
     * @param <E>        the element type
     * @return always returns the old element
     */
    static <E> E updateElement(E oldElement, E newElement) {
        return oldElement;
    }

    static int keyHash(Object e) {
        return SALT ^ Objects.hashCode(e);
    }

    @Override
    public @NonNull SimpleImmutableSet<E> add(@Nullable E element) {
        int keyHash = keyHash(element);
        ChangeEvent<E> details = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = root.put(null, element, keyHash, 0, details, SimpleImmutableSet::updateElement, Objects::equals, SimpleImmutableSet::keyHash);
        if (details.isModified()) {
            return newInstance(newRootNode, size + 1);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull SimpleImmutableSet<E> addAll(@NonNull Iterable<? extends E> c) {
        var m = toMutable();
        return m.addAll(c) ? m.toImmutable() : this;
    }

    @Override
    public SimpleImmutableSet<E> diff(@NonNull ReadOnlyCollection<? super E> that) {
        return retainAll(that);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull SimpleImmutableSet<E> empty() {
        return of();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, TC extends CollectionOps<T, TC>> CollectionOps<T, TC> emptyOp() {
        return (CollectionOps<T, TC>) of();
    }
    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable Object o) {
        return root.find((E) o, keyHash(o), 0, Objects::equals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof SimpleImmutableSet) {
            SimpleImmutableSet<?> that = (SimpleImmutableSet<?>) other;
            return size == that.size && root.equivalent(that.root);
        }
        return ReadOnlySet.setEquals(this, other);
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new ChampIterator<E, E>(root, null);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NonNull SimpleImmutableSet<E> remove(@NonNull E key) {
        int keyHash = keyHash(key);
        ChangeEvent<E> details = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = root.remove(null, key, keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            return size == 1 ? SimpleImmutableSet.of() : newInstance(newRootNode, size - 1);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull SimpleImmutableSet<E> removeAll(@NonNull Iterable<?> c) {
        var m = toMutable();
        return m.removeAll(c) ? m.toImmutable() : this;
    }


    @SuppressWarnings("unchecked")
    @Override
    public @NonNull SimpleImmutableSet<E> retainAll(@NonNull Iterable<?> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toImmutable() : this;
    }

    @Override
    public int size() {
        return size;
    }

    public @NonNull Spliterator<E> spliterator() {
        return new ChampSpliterator<>(root, null, size, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    @Override
    public @NonNull SimpleMutableSet<E> toMutable() {
        return new SimpleMutableSet<>(this);
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
            return SimpleImmutableSet.copyOf(deserializedElements);
        }
    }
}