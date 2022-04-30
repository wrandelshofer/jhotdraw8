/*
 * @(#)TrieMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
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
 *     <li>toPersistent: O(log n) distributed across subsequent updates</li>
 *     <li>clone: O(log n) distributed across subsequent updates</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP tree contains nodes that may be shared with other map, and nodes
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
 * This map can create a persistent copy of itself in O(1) time and O(0) space
 * using method {@link #toPersistent()}. This map loses exclusive ownership of
 * all its tree nodes.
 * Thus, creating a persistent copy increases the constant cost of
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
public class TrieMap<K, V> extends AbstractMap<K, V> implements Serializable, Cloneable {
    private final static long serialVersionUID = 0L;
    private transient UniqueIdentity mutator;
    private ChampTrieHelper.BitmapIndexedNode<K, V> root;
    private int size;
    private int modCount;
    private K first, last;
    private final static int TUPLE_LENGTH = 2;
    private final static ToIntFunction<Object> hashFunction = Objects::hashCode;

    public TrieMap() {
        this.root = ChampTrieHelper.emptyNode();
    }

    public TrieMap(@NonNull Map<? extends K, ? extends V> m) {
        this.root = ChampTrieHelper.emptyNode();
        this.putAll(m);
    }

    public TrieMap(@NonNull Collection<? extends Entry<? extends K, ? extends V>> m) {
        this.root = ChampTrieHelper.emptyNode();
        for (Entry<? extends K, ? extends V> e : m) {
            this.put(e.getKey(), e.getValue());
        }

    }

    public TrieMap(@NonNull ReadOnlyMap<? extends K, ? extends V> m) {
        this.root = ChampTrieHelper.emptyNode();
        this.putAll(m.asMap());
    }

    public TrieMap(@NonNull PersistentTrieMap<K, V> trieMap) {
        this.root = trieMap;
        this.size = trieMap.size;
    }

    public TrieMap(@NonNull TrieMap<K, V> trieMap) {
        this.mutator = null;
        trieMap.mutator = null;
        this.root = trieMap.root;
        this.size = trieMap.size;
        this.modCount = 0;
    }

    @Override
    public void clear() {
        root = ChampTrieHelper.emptyNode();
        size = 0;
        modCount++;
    }

    @Override
    public TrieMap<K, V> clone() {
        try {
            @SuppressWarnings("unchecked") final TrieMap<K, V> that = (TrieMap<K, V>) super.clone();
            that.mutator = null;
            this.mutator = null;
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public boolean containsKey(final @NonNull Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return root.findByKey(key, hashFunction.applyAsInt(key), 0, TUPLE_LENGTH).keyExists();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // Type arguments are needed for Java 8!
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public void clear() {
                TrieMap.this.clear();
            }

            @Override
            public boolean contains(Object o) {
                if (o instanceof Entry) {
                    @SuppressWarnings("unchecked")
                    Entry<K, V> entry = (Entry<K, V>) o;
                    K key = entry.getKey();
                    ChampTrieHelper.SearchResult<V> result = root.findByKey(key, hashFunction.applyAsInt(key), 0, TUPLE_LENGTH);
                    return result.keyExists() && Objects.equals(result.get(), entry.getValue());
                }
                return false;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new TrieMap.MutableMapEntryIterator<K, V>(TrieMap.this, TrieMap.this::hash);
            }

            @Override
            public boolean remove(Object o) {
                if (o instanceof Entry) {
                    @SuppressWarnings("unchecked")
                    Entry<K, V> entry = (Entry<K, V>) o;
                    K key = entry.getKey();
                    ChampTrieHelper.SearchResult<V> result = root.findByKey(key, hashFunction.applyAsInt(key), 0, TUPLE_LENGTH);
                    if (result.keyExists() && Objects.equals(result.get(), entry.getValue())) {
                        removeAndGiveDetails(key);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    @Override
    public V get(final @NonNull Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return root.findByKey(key, hashFunction.applyAsInt(key), 0, TUPLE_LENGTH).orElse(null);
    }

    private @NonNull UniqueIdentity getOrCreateMutator() {
        if (mutator == null) {
            mutator = new UniqueIdentity();
        }
        return mutator;
    }

    @Override
    public V put(K key, V value) {
        return putAndGiveDetails(key, value).getOldValue();
    }

    @NonNull ChampTrieHelper.ChangeEvent<V> putAndGiveDetails(final K key, final V val) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChampTrieHelper.ChangeEvent<V> details = new ChampTrieHelper.ChangeEvent<>();

        final ChampTrieHelper.BitmapIndexedNode<K, V> newRootNode =
                root.updated(getOrCreateMutator(), key, val, keyHash, 0, details, TUPLE_LENGTH, this::hash, ChampTrieHelper.TUPLE_VALUE);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                root = newRootNode;
            } else {
                root = newRootNode;
                size += 1;
            }
            modCount++;
        }

        return details;
    }

    @Override
    public V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return removeAndGiveDetails(key).getOldValue();
    }

    @NonNull ChampTrieHelper.ChangeEvent<V> removeAndGiveDetails(final K key) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChampTrieHelper.ChangeEvent<V> details = new ChampTrieHelper.ChangeEvent<>();
        final ChampTrieHelper.BitmapIndexedNode<K, V> newRootNode =
                (ChampTrieHelper.BitmapIndexedNode<K, V>) root.removed(getOrCreateMutator(), key, keyHash, 0, details, TUPLE_LENGTH, this::hash);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            root = newRootNode;
            size = size - 1;
            modCount++;
        }
        return details;
    }

    /**
     * Returns a copy of this map that is persistent.
     * <p>
     * This operation is performed in O(1) because the persistent map shares
     * the underlying trie nodes with this map.
     * <p>
     * This map loses exclusive ownership of all trie nodes. Therefore, the
     * first few updates that it performs, are copy-on-write operations, until
     * it exclusively owns some trie nodes that it can update.
     *
     * @return a persistent trie set
     */
    public PersistentTrieMap<K, V> toPersistent() {
        if (size == 0) {
            return PersistentTrieMap.of();
        }
        mutator = null;
        return new PersistentTrieMap<>(root, size);
    }

    static abstract class AbstractTransientMapEntryIterator<K, V> extends ChampTrieHelper.AbstractTrieIterator<K, V> {
        protected final @NonNull TrieMap<K, V> map;
        protected int expectedModCount;
        private final ToIntFunction<K> hashFunction;

        public AbstractTransientMapEntryIterator(@NonNull TrieMap<K, V> map, ToIntFunction<K> hashFunction) {
            super(map.root, TUPLE_LENGTH, hashFunction);
            this.map = map;
            this.expectedModCount = map.modCount;
            this.hashFunction = hashFunction;
        }

        @Override
        public boolean hasNext() {
            if (expectedModCount != map.modCount) {
                throw new ConcurrentModificationException();
            }
            return super.hasNext();
        }

        @Override
        public Entry<K, V> nextEntry(@NonNull BiFunction<K, V, Entry<K, V>> factory) {
            if (expectedModCount != map.modCount) {
                throw new ConcurrentModificationException();
            }
            return super.nextEntry(factory);
        }


        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            if (expectedModCount != map.modCount) {
                throw new ConcurrentModificationException();
            }
            Entry<K, V> toRemove = current;

            if (hasNext()) {
                Entry<K, V> next = nextEntry(SimpleImmutableEntry::new);
                map.remove(toRemove.getKey());
                expectedModCount = map.modCount;
                moveTo(next.getKey(), map.root, TUPLE_LENGTH, hashFunction);
            } else {
                map.remove(toRemove.getKey());
                expectedModCount = map.modCount;
            }

            current = null;
        }
    }

    private static class MutableMapEntryIterator<K, V> extends TrieMap.AbstractTransientMapEntryIterator<K, V> implements Iterator<Entry<K, V>> {

        public MutableMapEntryIterator(@NonNull TrieMap<K, V> map, final ToIntFunction<K> hashFunction) {
            super(map, hashFunction);
        }

        @Override
        public Entry<K, V> next() {
            Entry<K, V> kvEntry = nextEntry(TrieMap.MutableMapEntry::new);
            ((TrieMap.MutableMapEntry<K, V>) kvEntry).iterator = this;
            return kvEntry;
        }
    }

    private static class MutableMapEntry<K, V> extends SimpleEntry<K, V> {
        private @Nullable TrieMap.MutableMapEntryIterator<K, V> iterator;

        public MutableMapEntry(K key, V value) {
            super(key, value);
        }

        @Override
        public V setValue(V value) {
            V oldValue = super.setValue(value);
            if (iterator != null) {
                iterator.map.put(getKey(), value);
                iterator.expectedModCount = iterator.map.modCount;
            } else {
                throw new UnsupportedOperationException();
            }
            return oldValue;
        }
    }

    /**
     * Computes a hash code for the specified object.
     *
     * @param e an object
     * @return hash code
     */
    private int hash(@Nullable K e) {
        return hashFunction.applyAsInt(e);
    }
}


