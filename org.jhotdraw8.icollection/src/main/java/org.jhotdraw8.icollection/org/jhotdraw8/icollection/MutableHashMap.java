/*
 * @(#)MutableHashMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;


import org.jhotdraw8.icollection.alt.impl.champmap.EditableMapEntry;
import org.jhotdraw8.icollection.facade.SetFacade;
import org.jhotdraw8.icollection.impl.champmap.DeltaCounter;
import org.jhotdraw8.icollection.impl.champmap.EntryIterator;
import org.jhotdraw8.icollection.impl.champmap.TrieBuilder;
import org.jhotdraw8.icollection.impl.champmap.TrieNode;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;
import org.jhotdraw8.icollection.serialization.MapSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

/// Implements the [Map] interface using a Compressed Hash-Array Mapped
/// Prefix-tree (CHAMP).
///
/// Features:
///
///   - supports up to 2<sup>31</sup> - 1 entries
///   - allows null keys and null values
///   - is mutable
///   - is not thread-safe
///   - does not guarantee a specific iteration order
///
///
/// Performance characteristics:
///
///   - put: O(log₃₂ N)
///   - remove: O(log₃₂ N)
///   - containsKey: O(log₃₂ N)
///   - toPersistent: O(1) + O(log₃₂ N) distributed across subsequent updates in
///     this map
///   - clone: O(1) + O(log₃₂ N) distributed across subsequent updates in this
///     map and in the clone
///   - iterator.next: O(1)
///
///
/// Implementation details:
///
/// See description at [PersistentHashMap].
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
public class MutableHashMap<K, V> extends AbstractMap<K, V> implements Cloneable, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings("TransientFieldNotInitialized")
    transient TrieNode<K, V> node;
    int size;
    int modCount;
    private TrieBuilder<K, V> mutator = new TrieBuilder<>();

    /// Constructs a new empty map.
    public MutableHashMap() {
        node = TrieNode.empty();
    }

    /// Constructs a map containing the same entries as in the specified
    /// [Map].
    ///
    /// @param m a map
    @SuppressWarnings("this-escape")
    public MutableHashMap(Map<? extends K, ? extends V> m) {
        this();
        putAll(m);
    }

    @SuppressWarnings("this-escape")
    MutableHashMap(TrieNode<K, V> node, int size) {
        this();
        this.node = node;
        this.size = size;
    }

    /// Removes all entries from this map.
    @Override
    public void clear() {
        node = TrieNode.empty();
        size = 0;
        modCount++;
    }

    /// Returns a shallow copy of this map.
    @SuppressWarnings("unchecked")
    @Override
    public MutableHashMap<K, V> clone() {
        MutableHashMap<K, V> that = null;
        try {
            that = (MutableHashMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        that.mutator = new TrieBuilder<>();
        return that;
    }


    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") K key = (K) o;
        return node.containsKey(Objects.hashCode(key), key, 0);
    }

    public boolean containsEntry(@Nullable Object o) {
        if (o instanceof Entry<?, ?> entry) {
            @SuppressWarnings("unchecked") K key = (K) entry.getKey();
            V v = node.getOrDefault(Objects.hashCode(key), key, 0, node.noDataValue());
            return Objects.equals(entry.getValue(), v);
        }
        return false;
    }


    private Iterator<Entry<K, V>> iterator() {
        return new FailFastIterator<>(
                new EntryIterator<>(node, (k, v) -> new EditableMapEntry<>(k, v,
                        this::iteratorPutIfPresent)), this::iteratorRemoveEntry, this::getModCount);
        //  return new FailFastIterator<>(
        //                this::iteratorRemoveKey, this::iteratorPutIfPresent), this::getModCount
        // );
    }

    int getModCount() {
        return modCount;
    }

    private Spliterator<Entry<K, V>> spliterator() {
        return new IteratorSpliterator<>(iterator(), size(), Spliterator.NONNULL | Spliterator.DISTINCT | Spliterator.SIZED, null);
    }

    /// Returns a [Set] view of the entries contained in this map.
    ///
    /// @return a view of the entries contained in this map
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new SetFacade<>(
                this::iterator,
                this::spliterator,
                this::size,
                this::containsEntry,
                this::clear,
                null,
                this::removeEntry
        );
    }

    /// Returns the value to which the specified key is mapped,
    /// or `null` if this map contains no entry for the key.
    ///
    /// @param o the key whose associated value is to be returned
    /// @return the associated value or null
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(Object o) {
        return node.getOrDefault(Objects.hashCode(o), (K) o, 0, null);
    }

    private void iteratorPutIfPresent(@Nullable K k, @Nullable V v) {
        if (containsKey(k)) {
            mutator = new TrieBuilder<>();
            put(k, v);
        }
    }

    @Override
    public V replace(K key, V value) {
        return super.replace(key, value);
    }

    @Override
    public @Nullable V put(K key, V value) {
        var newNode = node.mutablePut(Objects.hashCode(key), key, value, 0, mutator.reset());
        if (!mutator.isModified()) {
            return value;
        }
        if (mutator.size != 0) modCount++;
        this.node = newNode;
        size += mutator.size;
        return mutator.getAndClearOperationResult();// must clear result to prevent memory leak
    }


    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m == this || m.isEmpty()) return;
        if (m instanceof MutableHashMap<? extends K, ? extends V> pm) {
            putAllEntries(pm.toPersistent());
        } else {
            super.putAll(m);
        }
    }

    public boolean putAllEntries(Iterable<? extends Map.Entry<? extends K, ? extends V>> m) {
        if (m instanceof MutableHashMap<?, ?> mh) {
            m = (Iterable<? extends Entry<? extends K, ? extends V>>) mh.toPersistent();
        }
        if (m instanceof PersistentHashMap<?, ?> pm) {
            var newNode = node;
            var deltaCounter = new DeltaCounter();
            newNode = newNode.mutablePutAll((TrieNode<K, V>) pm.node, 0, deltaCounter, mutator);
            var newSize = size + pm.size - deltaCounter.count;
            if (newSize != size || mutator.isModified()) {
                size = newSize;
                this.node = newNode;
                modCount++;
                return true;
            }
            return false;
        }
        boolean changed = false;
        for (var entry : m) {
            var newNode = node.mutablePut(Objects.hashCode(entry.getKey()), entry.getKey(), entry.getValue(), 0, mutator.reset());
            if (!mutator.isModified()) {
                continue;
            }
            changed = true;
            if (mutator.size != 0) modCount++;
            this.node = newNode;
            size += mutator.size;
        }
        mutator.getAndClearOperationResult();// must clear result to prevent memory leak
        return changed;
    }


    @Override
    public V remove(Object o) {
        var newNode = node.mutableRemove(Objects.hashCode(o), (K) o, 0, mutator.reset());
        if (mutator.isModified()) {
            this.size--;
            this.node = newNode;
            modCount++;
        }
        return mutator.getAndClearOperationResult();// must clear result to prevent memory leak
    }

    void iteratorRemoveKey(K key) {
        // Note: mutator must be recreated, because we must not change the structure of the trie, while iterating over it.
        this.mutator = new TrieBuilder<>();
        remove(key);
    }

    void iteratorRemoveEntry(Map.Entry<K, V> entry) {
        iteratorRemoveKey(entry.getKey());
    }

    @SuppressWarnings("unchecked")
    protected boolean removeEntry(@Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            @SuppressWarnings("unchecked") Entry<K, V> entry = (Entry<K, V>) o;
            remove(entry.getKey());
            return true;
        }
        return false;
    }

    /// Returns a persistent copy of this map.
    ///
    /// @return a persistent copy
    public PersistentHashMap<K, V> toPersistent() {
        mutator = new TrieBuilder<>();
        return isEmpty() ? PersistentHashMap.of()
                : new PersistentHashMap<>(node, size);
    }

    @Serial
    private Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    @Override
    public int size() {
        return size;
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            MutableHashMap<Object, Object> map = new MutableHashMap<>();
            map.putAllEntries(deserializedEntries);
            return map;
        }
    }
}