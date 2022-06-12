/*
 * @(#)ChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champset.BitmapIndexedNode;
import org.jhotdraw8.collection.champset.ChangeEvent;
import org.jhotdraw8.collection.champset.KeyIterator;
import org.jhotdraw8.collection.champset.Node;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

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
 *     <li>toImmutable: O(1) + a cost distributed across subsequent updates</li>
 *     <li>clone: O(1) + a cost distributed across subsequent updates</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP tree contains nodes that may be shared with other maps, and nodes
 * that are exclusively owned by this map.
 * <p>
 * If a write operation is performed on an exclusively owned node, then this
 * map is allowed to mutate the node (mutate-on-write).
 * If a write operation is performed on a potentially shared node, then this
 * map is forced to create an exclusive copy of the node and of all not (yet)
 * exclusively owned parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP tree has a fixed maximal height, the cost is O(1) in either
 * case.
 * <p>
 * This map can create an immutable copy of itself in O(1) time and O(0) space
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
public class ChampMap<K, V> extends AbstractMap<K, V> implements Serializable, Cloneable {
    private final static long serialVersionUID = 0L;
    private final static BiPredicate<AbstractMap.SimpleImmutableEntry<?, ?>, AbstractMap.SimpleImmutableEntry<?, ?>> EQUALS_FUNCTION =
            (a, b) -> Objects.equals(a.getKey(), b.getKey());
    private final static ToIntFunction<SimpleImmutableEntry<?, ?>> HASH_FUNCTION =
            (a) -> Objects.hashCode(a.getKey());
    public static final @NonNull BiFunction<SimpleImmutableEntry<?, ?>, SimpleImmutableEntry<?, ?>, SimpleImmutableEntry<?, ?>> UPDATE_FUNCTION =
            (oldv, newv) -> Objects.equals(oldv.getValue(), newv.getValue()) ? oldv : newv;
    private transient @Nullable UniqueId mutator;
    private transient @NonNull BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>> root;
    private transient int size;
    private transient int modCount;

    public ChampMap() {
        this.root = BitmapIndexedNode.emptyNode();
    }

    public ChampMap(@NonNull Map<? extends K, ? extends V> m) {
        if (m instanceof ChampMap) {
            @SuppressWarnings("unchecked")
            ChampMap<K, V> that = (ChampMap<K, V>) m;
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

    public ChampMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> m) {
        this.root = BitmapIndexedNode.emptyNode();
        for (Entry<? extends K, ? extends V> e : m) {
            this.put(e.getKey(), e.getValue());
        }
    }

    public ChampMap(@NonNull ReadOnlyMap<? extends K, ? extends V> m) {
        if (m instanceof ImmutableChampMap) {
            @SuppressWarnings("unchecked")
            ImmutableChampMap<K, V> that = (ImmutableChampMap<K, V>) m;
            this.root = that;
            this.size = that.size;
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

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ChampMap<K, V> clone() {
        try {
            mutator = null;
            return (ChampMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    boolean containsEntry(final @Nullable Object o) {
        if (o instanceof Entry) {
            @SuppressWarnings("unchecked") Entry<K, V> entry = (Entry<K, V>) o;
            K key = entry.getKey();
            return containsKey(key)
                    && Objects.equals(entry.getValue(), get(key));
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(@NonNull Object o) {
        return root.findByKey(new AbstractMap.SimpleImmutableEntry<>((K) o, null), Objects.hashCode(o), 0,
                getEqualsFunction()) != Node.NO_VALUE;
    }

    @Override
    public @NonNull Set<Entry<K, V>> entrySet() {
        return new WrappedSet<>(
                () -> new MappedIterator<>(new FailFastIterator<>(new KeyIterator<AbstractMap.SimpleImmutableEntry<K, V>>(
                        root,
                        this::persistentRemove/*, this::persistentPutIfPresent*/),
                        () -> this.modCount),
                        e -> new MutableMapEntry<>(this::persistentPutIfPresent, e.getKey(), e.getValue())),
                ChampMap.this::size,
                ChampMap.this::containsEntry,
                ChampMap.this::clear,
                ChampMap.this::removeEntry
        );
    }

    private void persistentRemove(AbstractMap.SimpleImmutableEntry<K, V> entry) {
        mutator = null;
        remove(entry.getKey());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(@NonNull Object o) {
        K key = (K) o;
        Object result = root.findByKey(new AbstractMap.SimpleImmutableEntry<>(key, null), Objects.hashCode(key), 0,
                getEqualsFunction());
        return result == Node.NO_VALUE ? null : ((SimpleImmutableEntry<K, V>) result).getValue();
    }

    private @NonNull UniqueId getOrCreateMutator() {
        if (mutator == null) {
            mutator = new UniqueId();
        }
        return mutator;
    }

    @Override
    public V put(K key, V value) {
        SimpleImmutableEntry<K, V> oldValue = putAndGiveDetails(key, value).getOldValue();
        return oldValue == null ? null : oldValue.getValue();
    }

    @NonNull ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> putAndGiveDetails(@Nullable K key, @Nullable V val) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();
        BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>> newRootNode = root
                .update(getOrCreateMutator(), new AbstractMap.SimpleImmutableEntry<>(key, val), keyHash, 0, details,
                        getUpdateFunction(),
                        getEqualsFunction(),
                        getHashFunction());
        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                root = newRootNode;
            } else {
                root = newRootNode;
                size += 1;
                modCount++;
            }
        }
        return details;
    }

    private void persistentPutIfPresent(@Nullable K k, @Nullable V v) {
        if (containsKey(k)) {
            mutator = null;
            put(k, v);
        }
    }

    @Override
    public V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        SimpleImmutableEntry<K, V> oldValue = removeAndGiveDetails(key).getOldValue();
        return oldValue == null ? null : oldValue.getValue();
    }

    @SuppressWarnings("unchecked")
    @NonNull ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> removeAndGiveDetails(final K key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();
        final BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>> newRootNode =
                root.remove(getOrCreateMutator(), new AbstractMap.SimpleImmutableEntry<>(key, null), keyHash, 0, details,
                        getEqualsFunction());
        if (details.isModified()) {
            assert details.hasReplacedValue();
            root = newRootNode;
            size = size - 1;
            modCount++;
        }
        return details;
    }

    boolean removeEntry(final @Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            @SuppressWarnings("unchecked") Entry<K, V> entry = (Entry<K, V>) o;
            remove(entry.getKey());
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Returns an immutable copy of this map.
     *
     * @return an immutable copy
     */
    public @NonNull ImmutableChampMap<K, V> toImmutable() {
        mutator = null;
        return size == 0 ? ImmutableChampMap.of() : new ImmutableChampMap<>(root, size);
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return new ChampMap<>(deserialized);
        }
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<K, V>(this);
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