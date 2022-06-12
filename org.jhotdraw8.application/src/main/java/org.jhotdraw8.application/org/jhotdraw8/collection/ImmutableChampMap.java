/*
 * @(#)ImmutableChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champset.BitmapIndexedNode;
import org.jhotdraw8.collection.champset.ChangeEvent;
import org.jhotdraw8.collection.champset.KeyIterator;
import org.jhotdraw8.collection.champset.Node;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

/**
 * Implements an immutable map using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>allows null keys and null values</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>copyPut: O(1)</li>
 *     <li>copyRemove: O(1)</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toMutable: O(1) + a cost distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP tree contains nodes that may be shared with other maps.
 * <p>
 * If a write operation is performed on a node, then this map creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP tree has a fixed maximal height, the cost is O(1).
 * <p>
 * This map can create a mutable copy of itself in O(1) time and O(0) space
 * using method {@link #toMutable()}}. The mutable copy shares its nodes
 * with this map, until it has gradually replaced the nodes with exclusively
 * owned nodes.
 * <p>
 * All operations on this set can be performed concurrently, without a need for
 * synchronisation.
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
@SuppressWarnings("exports")
public class ImmutableChampMap<K, V> extends BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>>
        implements ImmutableMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static BiPredicate<AbstractMap.SimpleImmutableEntry<?, ?>, AbstractMap.SimpleImmutableEntry<?, ?>> EQUALS_FUNCTION =
            (a, b) -> Objects.equals(a.getKey(), b.getKey());
    private final static ToIntFunction<AbstractMap.SimpleImmutableEntry<?, ?>> HASH_FUNCTION =
            (a) -> Objects.hashCode(a.getKey());
    public static final @NonNull BiFunction<AbstractMap.SimpleImmutableEntry<?, ?>, AbstractMap.SimpleImmutableEntry<?, ?>, AbstractMap.SimpleImmutableEntry<?, ?>> UPDATE_FUNCTION =
            (oldv, newv) -> Objects.equals(oldv.getValue(), newv.getValue()) ? oldv : newv;
    private static final ImmutableChampMap<?, ?> EMPTY = new ImmutableChampMap<>(BitmapIndexedNode.emptyNode(), 0);
    final transient int size;

    ImmutableChampMap(@NonNull BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        this.size = size;
    }


    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ImmutableChampMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (ImmutableChampMap<K, V>) ((ImmutableChampMap<K, V>) ImmutableChampMap.EMPTY).copyPutAll(map);
    }

    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ImmutableChampMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ((ImmutableChampMap<K, V>) ImmutableChampMap.EMPTY).copyPutAll(map);
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ImmutableChampMap<K, V> of() {
        return (ImmutableChampMap<K, V>) ImmutableChampMap.EMPTY;
    }

    /**
     * Returns an immutable map that contains the provided entries.
     *
     * @param entries map entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return an immutable map of the provided entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ImmutableChampMap<K, V> ofEntries(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        return (ImmutableChampMap<K, V>) of().copyPutAll(entries);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(final @Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return findByKey(new AbstractMap.SimpleImmutableEntry<>(key, null), Objects.hashCode(key), 0,
                (BiPredicate<AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>>) (BiPredicate<?, ?>) EQUALS_FUNCTION) != Node.NO_VALUE;
    }

    @Override
    public @NonNull ImmutableMap<K, V> copyClear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull ImmutableChampMap<K, V> copyPut(@NonNull K key, @Nullable V value) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();

        final BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>> newRootNode = update(null, new AbstractMap.SimpleImmutableEntry<>(key, value),
                keyHash, 0, details,
                getUpdateFunction(),
                getEqualsFunction(),
                getHashFunction());

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                return new ImmutableChampMap<>(newRootNode,
                        size);
            }

            return new ImmutableChampMap<>(newRootNode, size + 1);
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ImmutableChampMap<K, V> copyPutAll(@NonNull Map<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof ChampMap)) {
            return ((ChampMap<K, V>) m).toImmutable();
        }
        return copyPutAll(m.entrySet().iterator());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ImmutableChampMap<K, V> copyPutAll(@NonNull ImmutableMap<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof ImmutableChampMap)) {
            return (ImmutableChampMap<K, V>) m;
        }
        return copyPutAll(m.readOnlyEntrySet().iterator());
    }


    @Override
    public @NonNull ImmutableChampMap<K, V> copyPutAll(@NonNull Iterator<? extends Map.Entry<? extends K, ? extends V>> entries) {
        final ChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        while (entries.hasNext()) {
            Map.Entry<? extends K, ? extends V> entry = entries.next();
            ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details =
                    t.putAndGiveDetails(entry.getKey(), entry.getValue());
            modified |= details.isModified;
        }
        return modified ? t.toImmutable() : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ImmutableChampMap<K, V> copyRemove(@NonNull K key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();
        final BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>> newRootNode =
                remove(null, new AbstractMap.SimpleImmutableEntry<>(key, null), keyHash, 0, details,
                        (BiPredicate<AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>>) (BiPredicate<?, ?>) EQUALS_FUNCTION);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            return new ImmutableChampMap<>(newRootNode, size - 1);
        }
        return this;
    }

    @Override
    public @NonNull ImmutableChampMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }

        final ChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = t.removeAndGiveDetails(key);
            modified |= details.isModified;
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ImmutableChampMap<K, V> copyRetainAll(@NonNull Collection<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        if (c.isEmpty()) {
            return of();
        }
        final ChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : readOnlyKeySet()) {
            if (!c.contains(key)) {
                t.removeAndGiveDetails(key);
                modified = true;
            }
        }
        return modified ? t.toImmutable() : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return (Iterator<Map.Entry<K, V>>) (Iterator<?>) new KeyIterator<>(this, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final @Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (other instanceof ImmutableChampMap) {
            ImmutableChampMap<?, ?> that = (ImmutableChampMap<?, ?>) other;
            if (this.size != that.size) {
                return false;
            }
            return this.equivalent(that);
        } else {
            return ReadOnlyMap.mapEquals(this, other);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final @NonNull Object o) {
        K key = (K) o;
        Object result = findByKey(new AbstractMap.SimpleImmutableEntry<>(key, null), Objects.hashCode(key), 0, getEqualsFunction());
        return result == Node.NO_VALUE || result == null ? null : ((AbstractMap.SimpleImmutableEntry<K, V>) result).getValue();
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iterableToHashCode(iterator());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public @NonNull ChampMap<K, V> toMutable() {
        return new ChampMap<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyMap.mapToString(this);
    }

    private @NonNull Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(this.toMutable());
    }

    static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        private final static long serialVersionUID = 0L;

        SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return ImmutableChampMap.of().copyPutAll(deserialized.iterator());
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private ToIntFunction<AbstractMap.SimpleImmutableEntry<K, V>> getHashFunction() {
        return (ToIntFunction<AbstractMap.SimpleImmutableEntry<K, V>>) (ToIntFunction<?>) HASH_FUNCTION;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private BiPredicate<AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>> getEqualsFunction() {
        return (BiPredicate<AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>>) (BiPredicate<?, ?>) EQUALS_FUNCTION;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private BiFunction<AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>> getUpdateFunction() {
        return (BiFunction<AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>>)
                (BiFunction<?, ?, ?>) UPDATE_FUNCTION;
    }
}