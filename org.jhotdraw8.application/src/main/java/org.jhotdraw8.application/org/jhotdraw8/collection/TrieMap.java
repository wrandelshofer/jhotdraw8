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

public class TrieMap<K, V> extends AbstractMap<K, V> implements Serializable {
    private final static long serialVersionUID = 0L;
    private PersistentTrieHelper.UniqueKey bulkEdit;
    private PersistentTrieMapHelper.Node<K, V> root;
    private int hashCode;
    private int size;
    private int modCount;

    public TrieMap() {
        this.bulkEdit = new PersistentTrieHelper.UniqueKey();
        this.root = PersistentTrieMapHelper.emptyNode();
    }

    public TrieMap(@NonNull Map<? extends K, ? extends V> m) {
        this.bulkEdit = new PersistentTrieHelper.UniqueKey();
        this.root = PersistentTrieMapHelper.emptyNode();
        this.putAll(m);
    }

    TrieMap(@NonNull PersistentTrieMap<K, V> trieMap) {
        this.bulkEdit = new PersistentTrieHelper.UniqueKey();
        this.root = trieMap.root;
        this.hashCode = trieMap.hashCode;
        this.size = trieMap.size;
    }

    @Override
    public void clear() {
        this.root = PersistentTrieMapHelper.emptyNode();
        this.size = 0;
        this.hashCode = 0;
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
                root.updated(bulkEdit, kvEntry.getKey(), kvEntry.getValue(), Objects.hashCode(kvEntry.getKey()), 0, details);
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

        final PersistentTrieMapHelper.Node<K, V> newRootNode = root.updated(bulkEdit, key, val, keyHash, 0, details);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                final V old = details.getOldValue();
                final int valHashOld = Objects.hashCode(old);
                final int valHashNew = Objects.hashCode(val);
                root = newRootNode;
                hashCode = hashCode + (keyHash ^ valHashNew) - (keyHash ^ valHashOld);
            } else {
                final int valHashNew = Objects.hashCode(val);
                root = newRootNode;
                hashCode += (keyHash ^ valHashNew);
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
        final PersistentTrieMapHelper.Node<K, V> newRootNode = root.removed(bulkEdit, key, keyHash, 0, details);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            final int valHash = Objects.hashCode(details.getOldValue());
            root = newRootNode;
            hashCode = hashCode - (keyHash ^ valHash);
            size = size - 1;
            modCount++;
        }
        return details;
    }

    public PersistentTrieMap<K, V> toPersistent() {
        bulkEdit = new PersistentTrieHelper.UniqueKey();
        return size == 0 ? PersistentTrieMap.of() : new PersistentTrieMap<>(root, hashCode, size);
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
}


