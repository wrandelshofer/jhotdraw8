package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implements the {@link Map} interface with a
 * Compressed Hash-Array Mapped Prefix-trie (CHAMP).
 * <p>
 * Creating a persistent copy is performed in O(1).
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

public class TrieMap<K, V> extends AbstractMap<K, V> implements Serializable, Cloneable {
    private final static long serialVersionUID = 0L;
    private volatile UniqueIdentity mutator;
    private PersistentTrieMapHelper.BitmapIndexedNode<K, V> root;
    private int size;
    private int modCount;

    public TrieMap() {
        this.mutator = new UniqueIdentity();
        this.root = PersistentTrieMapHelper.emptyNode();
    }

    public TrieMap(@NonNull Map<? extends K, ? extends V> m) {
        this.mutator = new UniqueIdentity();
        this.root = PersistentTrieMapHelper.emptyNode();
        this.putAll(m);
    }

    public TrieMap(@NonNull ReadOnlyMap<? extends K, ? extends V> m) {
        this.mutator = new UniqueIdentity();
        this.root = PersistentTrieMapHelper.emptyNode();
        this.putAll(m.asMap());
    }

    public TrieMap(@NonNull PersistentTrieMap<K, V> trieMap) {
        this.mutator = new UniqueIdentity();
        this.root = trieMap;
        this.size = trieMap.size;
    }

    public TrieMap(@NonNull TrieMap<K, V> trieMap) {
        this.mutator = new UniqueIdentity();
        trieMap.mutator = new UniqueIdentity();
        this.root = trieMap.root;
        this.size = trieMap.size;
        this.modCount = 0;
    }

    @Override
    public void clear() {
        this.root = PersistentTrieMapHelper.emptyNode();
        this.size = 0;
    }

    @Override
    public boolean containsKey(final @NonNull Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return root.findByKey(key, Objects.hashCode(key), 0).keyExists();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // Type arguments are needed for Java 8!
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public boolean add(Entry<K, V> kvEntry) {
                PersistentTrieMapHelper.ChangeEvent<V> details = new PersistentTrieMapHelper.ChangeEvent<>();
                root.updated(mutator, kvEntry.getKey(), kvEntry.getValue(), Objects.hashCode(kvEntry.getKey()), 0, details);
                return details.isModified();
            }

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
                    PersistentTrieMapHelper.SearchResult<V> result = root.findByKey(key, Objects.hashCode(key), 0);
                    return result.keyExists() && Objects.equals(result.get(), entry.getValue());
                }
                return false;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new TransientMapEntryIterator<K, V>(TrieMap.this);
            }

            @Override
            public boolean remove(Object o) {
                if (o instanceof Entry) {
                    @SuppressWarnings("unchecked")
                    Entry<K, V> entry = (Entry<K, V>) o;
                    K key = entry.getKey();
                    PersistentTrieMapHelper.SearchResult<V> result = root.findByKey(key, Objects.hashCode(key), 0);
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
        return root.findByKey(key, Objects.hashCode(key), 0).orElse(null);
    }

    @Override
    public V put(K key, V value) {
        return putAndGiveDetails(key, value).getOldValue();
    }

    @NonNull PersistentTrieMapHelper.ChangeEvent<V> putAndGiveDetails(final K key, final V val) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieMapHelper.ChangeEvent<V> details = new PersistentTrieMapHelper.ChangeEvent<>();

        final PersistentTrieMapHelper.BitmapIndexedNode<K, V> newRootNode = root.updated(mutator, key, val, keyHash, 0, details);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                final V old = details.getOldValue();
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

    @NonNull PersistentTrieMapHelper.ChangeEvent<V> removeAndGiveDetails(final K key) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieMapHelper.ChangeEvent<V> details = new PersistentTrieMapHelper.ChangeEvent<>();
        final PersistentTrieMapHelper.BitmapIndexedNode<K, V> newRootNode = (PersistentTrieMapHelper.BitmapIndexedNode<K, V>) root.removed(mutator, key, keyHash, 0, details);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            root = newRootNode;
            size = size - 1;
            modCount++;
        }
        return details;
    }

    public PersistentTrieMap<K, V> toPersistent() {
        if (size == 0) {
            return PersistentTrieMap.of();
        }
        mutator = new UniqueIdentity();
        return new PersistentTrieMap<>(root, size);
    }

    static abstract class AbstractTransientMapEntryIterator<K, V> extends PersistentTrieMapHelper.AbstractMapIterator<K, V> {
        private final @NonNull TrieMap<K, V> map;
        private int expectedModCount;

        public AbstractTransientMapEntryIterator(@NonNull TrieMap<K, V> map) {
            super(map.root);
            this.map = map;
            this.expectedModCount = map.modCount;
        }

        @Override
        public boolean hasNext() {
            if (expectedModCount != map.modCount) {
                throw new ConcurrentModificationException();
            }
            return super.hasNext();
        }

        public Map.Entry<K, V> nextEntry() {
            if (expectedModCount != map.modCount) {
                throw new ConcurrentModificationException();
            }
            return super.nextEntry();
        }


        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            if (expectedModCount != map.modCount) {
                throw new ConcurrentModificationException();
            }
            Map.Entry<K, V> toRemove = current;

            if (hasNext()) {
                Map.Entry<K, V> next = nextEntry();
                map.remove(toRemove.getKey());
                expectedModCount = map.modCount;
                moveTo(next.getKey(), map.root);
            } else {
                map.remove(toRemove.getKey());
                expectedModCount = map.modCount;
            }

            current = null;
        }
    }

    private static class TransientMapEntryIterator<K, V> extends AbstractTransientMapEntryIterator<K, V> implements Iterator<Entry<K, V>> {

        public TransientMapEntryIterator(@NonNull TrieMap<K, V> map) {
            super(map);
        }

        @Override
        public Entry<K, V> next() {
            return nextEntry();
        }
    }

    @Override
    protected TrieMap<K, V> clone() {
        try {
            @SuppressWarnings("unchecked") final TrieMap<K, V> that = (TrieMap<K, V>) super.clone();
            that.mutator = new UniqueIdentity();
            this.mutator = new UniqueIdentity();
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}


