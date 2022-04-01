/*
 * @(#)PersistentTrieMap.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Implements the {@link PersistentMap} interface with a
 * Compressed Hash-Array Mapped Prefix-trie (CHAMP).
 * <p>
 * Creating a new copy with a single element added, removed or updated
 * is performed in {@code O(1)} time and space.
 * <p>
 * References:
 * <dl>
 *     <dt>This class has been derived from "The Capsule Hash Trie Collections Library".</dt>
 *     <dd>Copyright (c) Michael Steindorfer, Centrum Wiskunde & Informatica, and Contributors.
 *         BSD 2-Clause License.
 *         <a href="https://github.com/usethesource/capsule">github.com</a>.</dd>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class PersistentTrieMap<K, V> extends PersistentTrieMapHelper.BitmapIndexedNode<K, V> implements PersistentMap<K, V>, ImmutableMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;

    private static final PersistentTrieMap<?, ?> EMPTY_MAP = new PersistentTrieMap<>(PersistentTrieMapHelper.EMPTY_NODE, 0);

    final int size;

    PersistentTrieMap(@NonNull PersistentTrieMapHelper.BitmapIndexedNode<K, V> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.nodes);
        this.size = size;
    }

    public static <K, V> PersistentTrieMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        if (map instanceof PersistentTrieMap) {
            @SuppressWarnings("unchecked")
            PersistentTrieMap<K, V> unchecked = (PersistentTrieMap<K, V>) map;
            return unchecked;
        }
        TrieMap<K, V> tr = new TrieMap<>(of());
        for (final Map.Entry<? extends K, ? extends V> entry : map.readOnlyEntrySet()) {
            tr.putAndGiveDetails(entry.getKey(), entry.getValue());
        }
        return tr.toPersistent();
    }

    public static <K, V> PersistentTrieMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ofEntries(map.entrySet());
    }

    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull PersistentTrieMap<K, V> of() {
        return (PersistentTrieMap<K, V>) PersistentTrieMap.EMPTY_MAP;
    }

    @SafeVarargs
    public static <K, V> @NonNull PersistentTrieMap<K, V> ofEntries(@NonNull Map.Entry<K, V>... entries) {
        TrieMap<K, V> result = PersistentTrieMap.<K, V>of().asTransient();
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            result.putAndGiveDetails(entry.getKey(), entry.getValue());
        }
        return result.toPersistent();
    }

    public static <K, V> @NonNull PersistentTrieMap<K, V> ofEntries(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        TrieMap<K, V> result = PersistentTrieMap.<K, V>of().asTransient();
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            result.putAndGiveDetails(entry.getKey(), entry.getValue());
        }
        return result.toPersistent();
    }

    private TrieMap<K, V> asTransient() {
        return new TrieMap<>(this);
    }

    @Override
    public boolean containsKey(final @Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return findByKey(key, Objects.hashCode(key), 0).keyExists();
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> entries() {
        return entryIterator();
    }

    public Iterator<Map.Entry<K, V>> entryIterator() {
        return new PersistentTrieMapHelper.MapEntryIterator<>(this);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (other instanceof PersistentTrieMap) {
            PersistentTrieMap<?, ?> that = (PersistentTrieMap<?, ?>) other;
            if (this.size != that.size) {
                return false;
            }
            return this.equivalent(that);
        } else if (other instanceof Map) {
            Map<?, ?> that = (Map<?, ?>) other;
            if (this.size() != that.size()) {
                return false;
            }
            for (Map.Entry<?, ?> entry : that.entrySet()) {
                @SuppressWarnings("unchecked") final K key = (K) entry.getKey();
                final PersistentTrieMapHelper.SearchResult<V> result = findByKey(key, Objects.hashCode(key), 0);

                if (!result.keyExists()) {
                    return false;
                } else {
                    @SuppressWarnings("unchecked") final V val = (V) entry.getValue();
                    if (!Objects.equals(result.get(), val)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public V get(final @NonNull Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        final PersistentTrieMapHelper.SearchResult<V> result = findByKey(key, Objects.hashCode(key), 0);
        return result.orElse(null);
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iterableToHashCode(entries());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    public Iterator<K> keyIterator() {
        return new PersistentTrieMapHelper.MapKeyIterator<>(this);
    }

    @Override
    public @NonNull Iterator<K> keys() {
        return keyIterator();
    }

    @Override
    public int size() {
        return size;
    }

    public @NonNull PersistentTrieMap<K, V> copyPut(@NonNull K key, @Nullable V value) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieMapHelper.ChangeEvent<V> details = new PersistentTrieMapHelper.ChangeEvent<>();

        final PersistentTrieMapHelper.BitmapIndexedNode<K, V> newRootNode = updated(null, key, value,
                keyHash, 0, details);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                return new PersistentTrieMap<>(newRootNode,
                        size);
            }

            final int valHash = Objects.hashCode(value);
            return new PersistentTrieMap<>(newRootNode,
                    size + 1);
        }

        return this;
    }

    public @NonNull PersistentTrieMap<K, V> copyPutAll(@NonNull Map<? extends K, ? extends V> map) {
        final TrieMap<K, V> t = this.asTransient();
        boolean modified = false;
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            PersistentTrieMapHelper.ChangeEvent<V> details = t.putAndGiveDetails(entry.getKey(), entry.getValue());
            modified |= details.isModified;
        }
        return modified ? t.toPersistent() : this;
    }

    public @NonNull PersistentTrieMap<K, V> copyRemove(@NonNull K key) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieMapHelper.ChangeEvent<V> details = new PersistentTrieMapHelper.ChangeEvent<>();
        final PersistentTrieMapHelper.BitmapIndexedNode<K, V> newRootNode = (PersistentTrieMapHelper.BitmapIndexedNode<K, V>)
                removed(null, key, keyHash, 0, details);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            final int valHash = Objects.hashCode(details.getOldValue());
            return new PersistentTrieMap<>(newRootNode,
                    size - 1);
        }
        return this;
    }

    @Override
    public @NonNull PersistentTrieMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c) {
        final TrieMap<K, V> t = this.asTransient();
        boolean modified = false;
        for (K key : c) {
            PersistentTrieMapHelper.ChangeEvent<V> details = t.removeAndGiveDetails(key);
            modified |= details.isModified;
        }
        return modified ? t.toPersistent() : this;
    }

    @Override
    public @NonNull PersistentTrieMap<K, V> copyRetainAll(@NonNull Collection<? extends K> c) {
        final TrieMap<K, V> t = this.asTransient();
        boolean modified = false;
        for (K key : this.readOnlyKeySet()) {
            if (!c.contains(key)) {
                t.removeAndGiveDetails(key);
                modified = true;
            }
        }
        return modified ? t.toPersistent() : this;
    }

    @Override
    public String toString() {
        return ReadOnlyMap.mapToString(this);
    }
}
