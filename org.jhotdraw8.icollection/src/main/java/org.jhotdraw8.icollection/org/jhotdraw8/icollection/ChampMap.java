/*
 * @(#)SimplePersistentMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSetFacade;
import org.jhotdraw8.icollection.impl.champmap.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champmap.ChangeEvent;
import org.jhotdraw8.icollection.impl.champmap.EntryIterator;
import org.jhotdraw8.icollection.impl.champmap.Node;
import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jhotdraw8.icollection.serialization.MapSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Spliterator;

/**
 * Implements the {@link PersistentMap} interface using a Compressed Hash-Array
 * Mapped Prefix-tree (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>31</sup> - 1 entries</li>
 *     <li>allows null keys and null values</li>
 *     <li>is persistent</li>
 *     <li>is thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>put: O(log₃₂ N)</li>
 *     <li>remove: O(log₃₂ N)</li>
 *     <li>containsKey: O(log₃₂ N)</li>
 *     <li>toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(log₃₂ N) time,
 * and in O(log₃₂ N) space.
 * <p>
 * The CHAMP trie contains nodes that may be shared with other maps.
 * <p>
 * If a write operation is performed on a node, then this map creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * <p>
 * This map can create a mutable copy of itself in O(1) time and O(1) space
 * using method {@link #toMutable()}. The mutable copy shares its nodes
 * with this map, until it has gradually replaced the nodes with exclusively
 * owned nodes.
 * <p>
 * All operations on this map can be performed concurrently, without a need for
 * synchronisation.
 * <p>
 * References:
 * <p>
 * Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Persistent Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-persistent-collections">michael.steindorfer.name</a>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("exports")
public class ChampMap<K, V>
        implements PersistentMap<K, V>, Serializable {
    private static final ChampMap<?, ?> EMPTY = new ChampMap<>(BitmapIndexedNode.emptyNode(), 0);
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * We do not guarantee an iteration order. Make sure that nobody accidentally relies on it.
     */
    static final int SALT = new Random().nextInt();
    final transient BitmapIndexedNode<K, V> root;
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
    protected ChampMap(PrivateData privateData) {
        this(((Map.Entry<BitmapIndexedNode<K, V>, ?>) privateData.get()).getKey(), ((Map.Entry<?, Integer>) privateData.get()).getValue());
    }

    /**
     * Creates a new instance with the provided privateData object as its internal data structure.
     * <p>
     * Subclasses must override this method, and return a new instance of their subclass!
     *
     * @param privateData the internal data structure needed by this class for creating the instance.
     * @return a new instance of the subclass
     */
    protected ChampMap<K, V> newInstance(PrivateData privateData) {
        return new ChampMap<>(privateData);
    }

    private ChampMap<K, V> newInstance(BitmapIndexedNode<K, V> root, int size) {
        return newInstance(new PrivateData(new AbstractMap.SimpleImmutableEntry<>(root, size)));
    }

    ChampMap(BitmapIndexedNode<K, V> root, int size) {
        this.root = root;
        this.size = size;
    }

    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.DISTINCT;
    }

    /**
     * Returns an persistent copy of the provided map.
     *
     * @param c   a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an persistent copy
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ChampMap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return ChampMap.<K, V>of().putAll(c);
    }

    /**
     * Returns an persistent copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an persistent copy
     */
    public static <K, V> ChampMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return ChampMap.<K, V>of().putAll(map);
    }

    static <V, K> boolean entryKeyEquals(SimpleImmutableEntry<K, V> a, SimpleImmutableEntry<K, V> b) {
        return Objects.equals(a.getKey(), b.getKey());
    }

    static <V, K> int keyHash(Object e) {
        return SALT ^ Objects.hashCode(e);
    }

    static <V, K> int entryKeyHash(SimpleImmutableEntry<K, V> e) {
        return SALT ^ keyHash(e.getKey());
    }

    /**
     * Returns an empty persistent map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty persistent map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ChampMap<K, V> of() {
        return (ChampMap<K, V>) ChampMap.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChampMap<K, V> clear() {
        return isEmpty() ? this : of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return root.findByKey(key, keyHash(key), 0) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ChampMap<?, ?> that) {
            return size == that.size && root.equivalent(that.root);
        }
        return ReadableMap.mapEquals(this, other);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(Object o) {
        Object result = root.findByKey((K) o, keyHash(o), 0);
        return result == Node.NO_DATA ? null : (V) result;
    }

    /**
     * Update function for a map: we keep the old entry if it has the same
     * value as the new entry.
     *
     * @param oldv the old entry
     * @param newv the new entry
     * @param <K>  the key type
     * @param <V>  the value type
     * @return the old or the new entry
     */
    @Nullable
    static <K, V> SimpleImmutableEntry<K, V> updateEntry(@Nullable SimpleImmutableEntry<K, V> oldv, @Nullable SimpleImmutableEntry<K, V> newv) {
        return Objects.equals(oldv.getValue(), newv.getValue()) ? oldv : newv;
    }

    @Override
    public int hashCode() {
        return ReadableMap.iteratorToHashCode(iterator());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new EntryIterator<>(root, null, null);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ChampMap<K, V> put(K key, @Nullable V value) {
        var details = new ChangeEvent<V>();
        var newRootNode = root.put(null, key, value,
                keyHash(key), 0, details, ChampMap::keyHash);
        if (details.isModified()) {
            return newInstance(newRootNode, details.isReplaced() ? size : size + 1);
        }
        return this;
    }

    @Override
    public ChampMap<K, V> putAll(Map<? extends K, ? extends V> m) {
        return (ChampMap<K, V>) PersistentMap.super.putAll(m);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ChampMap<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        var m = toMutable();
        return m.putAll(c) ? m.toPersistent() : this;
    }

    @Override
    public ChampMap<K, V> remove(K key) {
        int keyHash = keyHash(key);
        var details = new ChangeEvent<V>();
        var newRootNode = root.remove(null, key, keyHash, 0, details);
        if (details.isModified()) {
            return size == 1 ? ChampMap.of() : newInstance(newRootNode, size - 1);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ChampMap<K, V> removeAll(Iterable<? extends K> c) {
        var m = toMutable();
        return m.removeAll(c) ? m.toPersistent() : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ChampMap<K, V> retainAll(Iterable<? extends K> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toPersistent() : this;
    }

    @Override
    public ReadableSet<K> readableKeySet() {
        return new ReadableSetFacade<>(
                () -> new MappedIterator<>(new EntryIterator<>(root, null, null), Map.Entry::getKey),
                this::size,
                this::containsKey,
                Spliterator.IMMUTABLE);
    }

    @Override
    public int size() {
        return size;
    }

    public Spliterator<Map.Entry<K, V>> spliterator() {
        return new IteratorSpliterator<>(iterator(), size(), characteristics(), null);
    }

    /**
     * Creates a mutable copy of this map.
     *
     * @return a mutable CHAMP map
     */
    @Override
    public MutableChampMap<K, V> toMutable() {
        return new MutableChampMap<>(this);
    }

    @Override
    public MutableChampMap<K, V> asMap() {
        return new MutableChampMap<>(this);
    }

    /**
     * Returns a string representation of this map.
     * <p>
     * The string representation is consistent with the one produced
     * by {@link AbstractMap#toString()}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return ReadableMap.mapToString(this);
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(this.toMutable());
    }

    static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        @Serial
        private static final long serialVersionUID = 0L;

        SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            return ChampMap.of().putAll(deserializedEntries);
        }
    }
}