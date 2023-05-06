/*
 * @(#)MutableChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.SetFacade;
import org.jhotdraw8.collection.impl.champ.AbstractMutableChampMap;
import org.jhotdraw8.collection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.impl.champ.ChampSpliterator;
import org.jhotdraw8.collection.impl.champ.ChangeEvent;
import org.jhotdraw8.collection.impl.champ.Node;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.io.Serial;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;

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
 * See description at {@link ChampMap}.
 * <p>
 * References:
 * <p>
 * Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class MutableChampMap<K, V> extends AbstractMutableChampMap<K, V, AbstractMap.SimpleImmutableEntry<K, V>> {
    @Serial
    private static final long serialVersionUID = 0L;

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

    /**
     * Removes all entries from this map.
     */
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
                ChampMap.keyHash(o), 0,
                ChampMap::keyEquals) != Node.NO_DATA;
    }

    @Override
    public @NonNull Iterator<Entry<K, V>> iterator() {
        return new FailFastIterator<>(
                new IteratorFacade<>(new ChampSpliterator<>(root,
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue()),
                        Spliterator.SIZED | Spliterator.DISTINCT, size()), this::iteratorRemove),
                this::getModCount
        );
    }

    @Override
    public @NonNull EnumeratorSpliterator<Entry<K, V>> spliterator() {
        return new FailFastSpliterator<>(
                new ChampSpliterator<>(root,
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue()),
                        Spliterator.SIZED | Spliterator.DISTINCT, size()),
                this::getModCount);
    }

    /**
     * Returns a {@link Set} view of the entries contained in this map.
     *
     * @return a view of the entries contained in this map
     */
    @Override
    public @NonNull Set<Entry<K, V>> entrySet() {
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

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no entry for the key.
     *
     * @param o the key whose associated value is to be returned
     * @return the associated value or null
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(Object o) {
        Object result = root.find(new AbstractMap.SimpleImmutableEntry<>((K) o, null),
                ChampMap.keyHash(o), 0, ChampMap::keyEquals);
        return result == Node.NO_DATA || result == null ? null : ((SimpleImmutableEntry<K, V>) result).getValue();
    }

    private void iteratorPutIfPresent(@Nullable K k, @Nullable V v) {
        if (containsKey(k)) {
            put(k, v);
        }
    }

    private void iteratorRemove(Map.Entry<K, V> entry) {
        remove(entry.getKey());
    }

    @Override
    public V put(K key, V value) {
        SimpleImmutableEntry<K, V> oldValue = putEntry(key, value).getOldData();
        return oldValue == null ? null : oldValue.getValue();
    }

    @NonNull
    ChangeEvent<SimpleImmutableEntry<K, V>> putEntry(@Nullable K key, @Nullable V val) {
        int keyHash = ChampMap.keyHash(key);
        ChangeEvent<SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();
        root = root.put(new AbstractMap.SimpleImmutableEntry<>(key, val), keyHash, 0, details,
                ChampMap::updateEntry,
                ChampMap::keyEquals,
                ChampMap::entryKeyHash);
        if (details.isModified() && !details.isReplaced()) {
            size += 1;
            modCount++;
        }
        return details;
    }

    @Override
    public V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        SimpleImmutableEntry<K, V> oldValue = removeKey(key).getOldData();
        return oldValue == null ? null : oldValue.getValue();
    }

    @NonNull
    ChangeEvent<SimpleImmutableEntry<K, V>> removeKey(K key) {
        int keyHash = ChampMap.keyHash(key);
        ChangeEvent<SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();
        root = root.remove(new AbstractMap.SimpleImmutableEntry<>(key, null), keyHash, 0, details,
                ChampMap::keyEquals);
        if (details.isModified()) {
            size = size - 1;
            modCount++;
        }
        return details;
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

    /**
     * Returns an immutable copy of this map.
     *
     * @return an immutable copy
     */
    public @NonNull ChampMap<K, V> toImmutable() {
        return size == 0 ? ChampMap.of() : new ChampMap<>(root, size);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            return new MutableChampMap<>(deserialized);
        }
    }
}