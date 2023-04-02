/*
 * @(#)ChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.FailFastIterator;
import org.jhotdraw8.collection.FailFastSpliterator;
import org.jhotdraw8.collection.MutableMapEntry;
import org.jhotdraw8.collection.facade.SetFacade;
import org.jhotdraw8.collection.mapped.MappedIterator;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implements a mutable map using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>allows null keys and null values</li>
 *     <li>is mutable</li>
 *     <li>is not thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>put: O(1)</li>
 *     <li>remove: O(1)</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toImmutable: O(1) + O(log N) distributed across subsequent updates in
 *     this map</li>
 *     <li>clone: O(1) + O(log N) distributed across subsequent updates in this
 *     map and in the clone</li>
 *     <li>iterator.next: O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP trie contains nodes that may be shared with other maps, and nodes
 * that are exclusively owned by this map.
 * <p>
 * If a write operation is performed on an exclusively owned node, then this
 * map is allowed to mutate the node (mutate-on-write).
 * If a write operation is performed on a potentially shared node, then this
 * map is forced to create an exclusive copy of the node and of all not (yet)
 * exclusively owned parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP trie has a fixed maximal height, the cost is O(1) in either
 * case.
 * <p>
 * This map can create an immutable copy of itself in O(1) time and O(1) space
 * using method {@link #toImmutable()}. This map loses exclusive ownership of
 * all its tree nodes.
 * Thus, creating an immutable copy increases the constant cost of
 * subsequent writes, until all shared nodes have been gradually replaced by
 * exclusively owned nodes again.
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
 * @param <K> the key type
 * @param <V> the value type
 */
public class MutableChampMap<K, V> extends AbstractChampMap<K, V, AbstractMap.SimpleImmutableEntry<K, V>> {
    private final static long serialVersionUID = 0L;

    /**
     * Constructs a new empty map.
     */
    public MutableChampMap() {
        root = BitmapIndexedNode.emptyNode();
    }

    public MutableChampMap(@NonNull Map<? extends K, ? extends V> m) {
        if (m instanceof MutableChampMap) {
            @SuppressWarnings("unchecked")
            MutableChampMap<K, V> that = (MutableChampMap<K, V>) m;
            this.mutator = null;
            that.mutator = null;
            this.root = that.root;
            this.size = that.size;
            this.modCount = 0;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            this.putAll(m);
        }
    }

    public MutableChampMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> m) {
        this.root = BitmapIndexedNode.emptyNode();
        for (Entry<? extends K, ? extends V> e : m) {
            this.put(e.getKey(), e.getValue());
        }
    }

    public MutableChampMap(@NonNull ReadOnlyMap<? extends K, ? extends V> m) {
        if (m instanceof ChampMap) {
            @SuppressWarnings("unchecked")
            ChampMap<K, V> that = (ChampMap<K, V>) m;
            this.root = that;
            this.size = that.size();
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            this.putAll(m.asMap());
        }
    }

    @Override
    public void clear() {
        root = BitmapIndexedNode.emptyNode();
        size = 0;
        modCount++;
    }

    /**
     * Returns a shallow copy of this map.
     */
    @Override
    public @NonNull MutableChampMap<K, V> clone() {
        return (MutableChampMap<K, V>) super.clone();
    }


    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(@Nullable Object o) {
        return root.find(new AbstractMap.SimpleImmutableEntry<>((K) o, null),
                Objects.hashCode(o), 0,
                ChampMap::keyEquals) != Node.NO_DATA;
    }

    @Override
    public @NonNull Set<Entry<K, V>> entrySet() {
        return new SetFacade<>(
                () -> new MappedIterator<>(new FailFastIterator<>(new KeyIterator<>(
                        root,
                        this::iteratorRemove),
                        () -> this.modCount),
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue())),
                () -> new FailFastSpliterator<>(
                        new KeySpliterator<>(root, Function.identity(), Spliterator.SIZED | Spliterator.DISTINCT, size()),
                        () -> this.modCount),
                MutableChampMap.this::size,
                MutableChampMap.this::containsEntry,
                MutableChampMap.this::clear,
                null,
                MutableChampMap.this::removeEntry
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(Object o) {
        Object result = root.find(new AbstractMap.SimpleImmutableEntry<>((K) o, null),
                Objects.hashCode(o), 0, ChampMap::keyEquals);
        return result == Node.NO_DATA || result == null ? null : ((SimpleImmutableEntry<K, V>) result).getValue();
    }


    @NonNull
    private BiFunction<AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>> getUpdateFunction() {
        return (oldv, newv) -> Objects.equals(oldv.getValue(), newv.getValue()) ? oldv : newv;
    }

    private void iteratorPutIfPresent(@Nullable K k, @Nullable V v) {
        if (containsKey(k)) {
            mutator = null;
            put(k, v);
        }
    }

    private void iteratorRemove(AbstractMap.SimpleImmutableEntry<K, V> entry) {
        mutator = null;
        remove(entry.getKey());
    }

    @Override
    public V put(K key, V value) {
        SimpleImmutableEntry<K, V> oldValue = putAndGiveDetails(key, value).getData();
        return oldValue == null ? null : oldValue.getValue();
    }

    @NonNull ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> putAndGiveDetails(@Nullable K key, @Nullable V val) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();
        root = root.update(getOrCreateMutator(), new AbstractMap.SimpleImmutableEntry<>(key, val), keyHash, 0, details,
                getUpdateFunction(),
                ChampMap::keyEquals,
                ChampMap::keyHash);
        if (details.isModified() && !details.isReplaced()) {
            size += 1;
            modCount++;
        }
        return details;
    }

    @Override
    public V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        SimpleImmutableEntry<K, V> oldValue = removeAndGiveDetails(key).getData();
        return oldValue == null ? null : oldValue.getValue();
    }

    @NonNull ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> removeAndGiveDetails(K key) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();
        root = root.remove(getOrCreateMutator(), new AbstractMap.SimpleImmutableEntry<>(key, null), keyHash, 0, details,
                ChampMap::keyEquals);
        if (details.isModified()) {
            size = size - 1;
            modCount++;
        }
        return details;
    }

    @SuppressWarnings("unchecked")
    boolean removeEntry(@Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            @SuppressWarnings("unchecked") Entry<K, V> entry = (Entry<K, V>) o;
            remove(entry.getKey());
            return true;
        }
        return false;
    }

    /**
     * Returns an immutable copy of this map.
     *
     * @return an immutable copy
     */
    public @NonNull ChampMap<K, V> toImmutable() {
        mutator = null;
        return size == 0 ? ChampMap.of() : new ChampMap<>(root, size);
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return new MutableChampMap<>(deserialized);
        }
    }
}