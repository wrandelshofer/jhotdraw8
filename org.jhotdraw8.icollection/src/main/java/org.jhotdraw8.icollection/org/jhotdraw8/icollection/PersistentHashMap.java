/*
 * @(#)SimplePersistentMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSetFacade;
import org.jhotdraw8.icollection.impl.champmap.EntryIterator;
import org.jhotdraw8.icollection.impl.champmap.TrieBuilder;
import org.jhotdraw8.icollection.impl.champmap.TrieNode;
import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;
import org.jhotdraw8.icollection.persistent.PersistentCollection;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;

/// Implements the [PersistentMap] interface using a Compressed Hash-Array
/// Mapped Prefix-tree (CHAMP).
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 entries
///   - allows null keys and null values
///   - is persistent
///   - is thread-safe
///   - does not guarantee a specific iteration order
///
///
/// Performance characteristics:
///
///   - put: O(log₃₂ N)
///   - remove: O(log₃₂ N)
///   - containsKey: O(log₃₂ N)
///   - toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in the mutable copy
///   - clone: O(1)
///   - iterator.next(): O(1)
///
///
/// Implementation details:
///
/// This map performs read and write operations of single elements in O(log₃₂ N) time,
/// and in O(log₃₂ N) space.
///
/// The CHAMP trie contains nodes that may be shared with other maps.
///
/// If a write operation is performed on a node, then this map creates a
/// copy of the node and of all parent nodes up to the root (copy-path-on-write).
///
/// This map can create a mutable copy of itself in O(1) time and O(1) space
/// using method [#toMutable()]. The mutable copy shares its nodes
/// with this map, until it has gradually replaced the nodes with exclusively
/// owned nodes.
///
/// All operations on this map can be performed concurrently, without a need for
/// synchronisation.
///
/// References:
///
/// Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
/// <dl>
///      <dt>Michael J. Steindorfer (2017).
///      Efficient Persistent Collections.</dt>
///      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-persistent-collections">michael.steindorfer.name</a>
///      <dt>The Capsule Hash Trie Collections Library.
///
/// Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
///      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
/// </dl>
///
/// @param <K> the key type
/// @param <V> the value type
@SuppressWarnings("exports")
public class PersistentHashMap<K, V>
        implements PersistentMap<K, V>, Serializable {
    private static final PersistentHashMap<?, ?> EMPTY = new PersistentHashMap<>(TrieNode.empty(), 0);
    @Serial
    private static final long serialVersionUID = 0L;

    @SuppressWarnings("TransientFieldNotInitialized")
    final transient TrieNode<K, V> node;
    final int size;


    PersistentHashMap(TrieNode<K, V> node, int size) {
        this.node = node;
        this.size = size;
    }

    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.DISTINCT;
    }

    /// Returns a persistent copy of the provided map.
    ///
    /// @param c   a map
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return a persistent copy
    public static <K, V> PersistentHashMap<K, V> copyOf(java.lang.Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return new PersistentHashMapBuilder<K, V>().putEntries(c).build();
    }

    /// Returns a persistent copy of the provided map.
    ///
    /// @param map a map
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return a persistent copy
    public static <K, V> PersistentHashMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return PersistentHashMap.<K, V>of().puttingAll(map);
    }

    /// Returns an empty persistent map.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return an empty persistent map
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentHashMap<K, V> of() {
        return (PersistentHashMap<K, V>) PersistentHashMap.EMPTY;
    }

    /// Returns a builder for a persistent map.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return an empty builder
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentHashMapBuilder<K, V> builder() {
        return new PersistentHashMapBuilder<>();
    }

    /// {@inheritDoc}
    @Override
    public PersistentHashMap<K, V> cleared() {
        return isEmpty() ? this : of();
    }

    /// {@inheritDoc}
    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") K key = (K) o;
        return node.containsKey(Objects.hashCode(key), key, 0);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return ReadableMap.mapEquals(this, other);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(Object o) {
        return node.getOrDefault(Objects.hashCode(o), (K) o, 0, null);
    }

    /// Update function for a map: we keep the old entry if it has the same
    /// value as the new entry.
    ///
    /// @param oldv the old entry
    /// @param newv the new entry
    /// @param <K>  the key type
    /// @param <V>  the value type
    /// @return the old or the new entry
    static @Nullable <K, V> SimpleImmutableEntry<K, V> updateEntry(@Nullable SimpleImmutableEntry<K, V> oldv, @Nullable SimpleImmutableEntry<K, V> newv) {
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
        return new EntryIterator<>(node, (k, v) -> new AbstractMap.SimpleImmutableEntry<>(k, v));
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public PersistentHashMap<K, V> putting(K key, @Nullable V value) {
        var details = node.put(Objects.hashCode(key), key, value, 0);
        return details == null ? this : new PersistentHashMap<>(details.node, size + details.sizeDelta);
    }

    @Override
    public PersistentHashMap<K, V> puttingAll(Map<? extends K, ? extends V> m) {
        var mutable = toMutable();
        return mutable.putAllEntries(m.entrySet()) ? mutable.toPersistent() : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentHashMap<K, V> puttingAll(java.lang.Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        var mutable = toMutable();
        return mutable.putAllEntries(c) ? mutable.toPersistent() : this;
    }

    @Override
    public PersistentHashMap<K, V> removing(K key) {
        var newNode = node.remove(Objects.hashCode(key), key, 0);
        return newNode == node ? this : size == 1 ? of() : new PersistentHashMap<>(newNode, size - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentHashMap<K, V> removingAll(java.lang.Iterable<? extends K> c) {
        if (c instanceof PersistentCollection<?> mhm) {
            c = (java.lang.Iterable<? extends K>) mhm.toMutable();
        }
        if (!(c instanceof Collection<?>)) {
            HashSet<K> hm = new HashSet<>();
            c.forEach(hm::add);
            c = hm;
        }
        var cc = (Collection<K>) c;
        if (cc.isEmpty()) {
            return this;
        }
        var mutator = new TrieBuilder<K, V>();
        var newNode = node;
        for (K key : this.readableKeySet()) {
            if (cc.contains(key)) {
                newNode = newNode.mutableRemove(Objects.hashCode(key), key, 0, mutator);
                if (newNode.isEmpty()) {
                    return of();
                }
            }
        }
        int newSize = size + mutator.size;
        return (newSize != size) ? newSize == 0 ? of() : new PersistentHashMap<>(newNode, newSize) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentHashMap<K, V> retainingAll(java.lang.Iterable<? extends K> c) {
        if (c instanceof PersistentCollection<? extends K>) {
            c = ((PersistentCollection<? extends K>) c).toMutable();
        }
        if (!(c instanceof Collection<?>)) {
            HashSet<K> hm = new HashSet<>();
            c.forEach(hm::add);
            c = hm;
        }
        var cc = (Collection<K>) c;
        if (cc.isEmpty()) {
            return of();
        }
        var mutator = new TrieBuilder<K, V>();
        var newNode = node;
        for (K key : this.readableKeySet()) {
            if (!cc.contains(key)) {
                newNode = newNode.mutableRemove(Objects.hashCode(key), key, 0, mutator);
                if (newNode.isEmpty()) {
                    return of();
                }
            }
        }

        int newSize = size + mutator.size;
        return (newSize != size) ? newSize == 0 ? of() : new PersistentHashMap<>(newNode, newSize) : this;
    }

    @Override
    public ReadableSet<K> readableKeySet() {
        return new ReadableSetFacade<>(
                () -> new EntryIterator<>(node, (k, v) -> k),
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

    /// Creates a mutable copy of this map.
    ///
    /// @return a mutable CHAMP map
    @Override
    public MutableHashMap<K, V> toMutable() {
        return new MutableHashMap<>(this.node, this.size);
    }

    @Override
    public MutableHashMap<K, V> asMap() {
        return new MutableHashMap<>(this.node, this.size);
    }

    /// Returns a string representation of this map.
    ///
    /// The string representation is consistent with the one produced
    /// by [AbstractMap#toString()].
    ///
    /// @return a string representation
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
            return PersistentHashMap.of().puttingAll(deserializedEntries);
        }
    }
}