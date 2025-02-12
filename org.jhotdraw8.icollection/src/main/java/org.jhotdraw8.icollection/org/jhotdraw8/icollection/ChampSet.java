/*
 * @(#)ChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChampIterator;
import org.jhotdraw8.icollection.impl.champ.ChampSpliterator;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.Node;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

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
 * Implements the {@link PersistentSet} interface using a Compressed Hash-Array
 * Mapped Prefix-tree (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>31</sup> - 1 elements</li>
 *     <li>allows null elements</li>
 *     <li>is persistent</li>
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
 *      Efficient Persistent Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-persistent-collections">michael.steindorfer.name</a></dd>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
@SuppressWarnings("exports")
public class ChampSet<E> implements PersistentSet<E>, Serializable {
    /**
     * We do not guarantee an iteration order. Make sure that nobody accidentally relies on it.
     */
    static final int SALT = new Random().nextInt();
    private static final ChampSet<?> EMPTY = new ChampSet<>(BitmapIndexedNode.emptyNode(), 0);
    @Serial
    private static final long serialVersionUID = 0L;
    final transient BitmapIndexedNode<E> root;
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
    protected ChampSet(PrivateData privateData) {
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
    protected ChampSet<E> newInstance(PrivateData privateData) {
        return new ChampSet<>(privateData);
    }

    private ChampSet<E> newInstance(BitmapIndexedNode<E> root, int size) {
        return new ChampSet<>(new PrivateData(new AbstractMap.SimpleImmutableEntry<>(root, size)));
    }

    ChampSet(BitmapIndexedNode<E> root, int size) {
        this.root = root;
        this.size = size;
    }


    /**
     * Returns an persistent set that contains the provided elements.
     *
     * @param c   an iterable
     * @param <E> the element type
     * @return an persistent set of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <E> ChampSet<E> copyOf(Iterable<? extends E> c) {
        return ChampSet.<E>of().addAll(c);
    }

    /**
     * Returns an empty persistent set.
     *
     * @param <E> the element type
     * @return an empty persistent set
     */
    @SuppressWarnings("unchecked")
    public static <E> ChampSet<E> of() {
        return ((ChampSet<E>) ChampSet.EMPTY);
    }

    @SuppressWarnings("unchecked")
    public static <T> ChampSet<T> ofIterator(Iterator<T> iterator) {
        return ChampSet.<T>of().addAll(() -> iterator);
    }
    /**
     * Returns an persistent set that contains the provided elements.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an persistent set of the provided elements
     */
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <E> ChampSet<E> of(E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ChampSet.<E>of().addAll(Arrays.asList(elements));
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
    public ChampSet<E> add(@Nullable E element) {
        int keyHash = keyHash(element);
        ChangeEvent<E> details = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = root.put(null, element, keyHash, 0, details, ChampSet::updateElement, Objects::equals, ChampSet::keyHash);
        if (details.isModified()) {
            return newInstance(newRootNode, size + 1);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChampSet<E> addAll(Iterable<? extends E> c) {
        if(isEmpty()&&c instanceof ChampSet<? extends E> s){
            return (ChampSet<E>) s;
        }
        var m = toMutable();
        return m.addAll(c) ? m.toPersistent() : this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> ChampSet<T> empty() {
        return of();
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
        if (other instanceof ChampSet<?> that) {
            return size == that.size && root.equivalent(that.root);
        }
        return ReadableSet.setEquals(this, other);
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(iterator());
    }

    @Override
    public Iterator<E> iterator() {
        return new ChampIterator<>(root, null);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ChampSet<E> remove(E key) {
        int keyHash = keyHash(key);
        ChangeEvent<E> details = new ChangeEvent<>();
        BitmapIndexedNode<E> newRootNode = root.remove(null, key, keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            return size == 1 ? ChampSet.of() : newInstance(newRootNode, size - 1);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ChampSet<E> removeAll(Iterable<?> c) {
        var m = toMutable();
        return m.removeAll(c) ? m.toPersistent() : this;
    }


    @SuppressWarnings("unchecked")
    @Override
    public ChampSet<E> retainAll(Iterable<?> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toPersistent() : this;
    }

    @Override
    public int size() {
        return size;
    }

    public Spliterator<E> spliterator() {
        return new ChampSpliterator<>(root, null, size, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    @Override
    public MutableChampSet<E> toMutable() {
        return new MutableChampSet<>(this);
    }

    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
    }

    @Serial
    private Object writeReplace() {
        return new SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Set<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            return ChampSet.copyOf(deserializedElements);
        }
    }
}