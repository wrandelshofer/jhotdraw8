/*
 * @(#)MutableChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.facade.SetFacade;
import org.jhotdraw8.icollection.impl.champmap.AbstractMutableChampMap;
import org.jhotdraw8.icollection.impl.champmap.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champmap.ChangeEvent;
import org.jhotdraw8.icollection.impl.champmap.EntryIterator;
import org.jhotdraw8.icollection.impl.champmap.Node;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;
import org.jhotdraw8.icollection.serialization.MapSerializationProxy;

import java.io.Serial;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;

/**
 * Implements the {@link Map} interface using a Compressed Hash-Array Mapped
 * Prefix-tree (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>31</sup> - 1 entries</li>
 *     <li>allows null keys and null values</li>
 *     <li>is mutable</li>
 *     <li>is not thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>put: O(log₃₂ N)</li>
 *     <li>remove: O(log₃₂ N)</li>
 *     <li>containsKey: O(log₃₂ N)</li>
 *     <li>toImmutable: O(1) + O(log₃₂ N) distributed across subsequent updates in
 *     this map</li>
 *     <li>clone: O(1) + O(log₃₂ N) distributed across subsequent updates in this
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
public class MutableChampMap<K, V> extends AbstractMutableChampMap<K, V> {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * Constructs a new empty map.
     */
    public MutableChampMap() {
        root = BitmapIndexedNode.emptyNode();
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Map}.
     *
     * @param m a map
     */
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

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Iterable}.
     *
     * @param m an iterable
     */
    public MutableChampMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> m) {
        if (m instanceof ChampMap) {
            @SuppressWarnings("unchecked")
            ChampMap<K, V> that = (ChampMap<K, V>) m;
            this.root = that.root;
            this.size = that.size;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            for (Entry<? extends K, ? extends V> e : m) {
                this.put(e.getKey(), e.getValue());
            }
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
        return root.findByKey((K) o,
                ChampMap.keyHash(o), 0) != Node.NO_DATA;
    }

    @Override
    public @NonNull Iterator<Entry<K, V>> iterator() {
        return new FailFastIterator<>(
                new EntryIterator<K, V>(root,
                        this::iteratorRemoveKey, this::iteratorPutIfPresent), this::getModCount
        );
    }

    @Override
    public @NonNull Spliterator<Entry<K, V>> spliterator() {
        return new IteratorSpliterator<>(iterator(), size(), Spliterator.NONNULL | characteristics(), null);
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
        Object result = root.findByKey((K) o,
                ChampMap.keyHash(o), 0);
        return result == Node.NO_DATA ? null : (V) result;
    }

    private void iteratorPutIfPresent(@Nullable K k, @Nullable V v) {
        if (containsKey(k)) {
            owner = null;
            put(k, v);
        }
    }



    @Override
    public V put(K key, V value) {
        return putEntry(key, value).getOldValue();
    }

    /*
    @Override
    @SuppressWarnings("unchecked")
    public boolean putAll(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> c) {
        if (c instanceof MutableChampMap<?, ?> m) {
            c = (Iterable<? extends Entry<? extends K, ? extends V>>) m.toImmutable();
        }
        if (isEmpty() && c instanceof ChampMap<?, ?> that) {
            if (that.isEmpty()) {
                return false;
            }
            root = (BitmapIndexedNode<K, V>) (BitmapIndexedNode<?>) that.root;
            size = that.size;
            modCount++;
            return true;
        }
        if (c instanceof ChampMap<?, ?> that) {
            var bulkChange = new BulkChangeEvent();
            var newRootNode = root.putAll(getOrCreateOwner(), (Node<SimpleImmutableEntry<K, V>>) (Node<?>) that.root, 0, bulkChange, ChampMap::updateEntry, ChampMap::entryKeyEquals,
                    ChampMap::entryKeyHash, new ChangeEvent<>());
            if (bulkChange.inBoth == that.size() && !bulkChange.replaced) {
                return false;
            }
            root = newRootNode;
            size += that.size - bulkChange.inBoth;
            modCount++;
            return true;
        }
        return super.putAll(c);
    }*/

    @NonNull
    ChangeEvent<V> putEntry(@Nullable K key, @Nullable V val) {
        int keyHash = ChampMap.keyHash(key);
        ChangeEvent<V> details = new ChangeEvent<>();
        root = root.put(getOrCreateOwner(), key, val, keyHash, 0, details, ChampMap::keyHash);
        if (details.isModified() && !details.isReplaced()) {
            size += 1;
            modCount++;
        }
        return details;
    }

    @Override
    public V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return removeKey(key).getOldValue();
    }

    @Override
    public boolean removeAll(@NonNull Iterable<?> c) {
        return super.removeAll(c);
    }

    /*
    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(@NonNull Iterable<?> c) {
        if (isEmpty()) {
            return false;
        }
        if ((c instanceof Collection<?> cc && cc.isEmpty())
                || (c instanceof ReadOnlyCollection<?> rc) && rc.isEmpty()) {
            clear();
            return true;
        }
        BulkChangeEvent bulkChange = new BulkChangeEvent();
        BitmapIndexedNode<K, V> newRootNode;
        if (c instanceof Collection<?> that) {
            newRootNode = root.filterAll(getOrCreateOwner(), e -> that.contains(e.getKey()), 0, bulkChange);
        } else if (c instanceof ReadOnlyCollection<?> that) {
            newRootNode = root.filterAll(getOrCreateOwner(), e -> that.contains(e.getKey()), 0, bulkChange);
        } else {
            HashSet<Object> that = new HashSet<>();
            c.forEach(that::add);
            newRootNode = root.filterAll(getOrCreateOwner(), that::contains, 0, bulkChange);
        }
        if (bulkChange.removed == 0) {
            return false;
        }
        root = newRootNode;
        size -= bulkChange.removed;
        modCount++;
        return true;
    }*/

    @NonNull
    ChangeEvent<V> removeKey(K key) {
        int keyHash = ChampMap.keyHash(key);
        ChangeEvent<V> details = new ChangeEvent<>();
        root = root.remove(getOrCreateOwner(), key, keyHash, 0, details);
        if (details.isModified()) {
            size = size - 1;
            modCount++;
        }
        return details;
    }

    void iteratorRemoveKey(K key) {
        // Note: mutator must be null, because we must not change the structure of the trie, while iterating over it.
        int keyHash = ChampMap.keyHash(key);
        ChangeEvent<V> details = new ChangeEvent<>();
        root = root.remove(null, key, keyHash, 0, details);
        if (details.isModified()) {
            size = size - 1;
            modCount++;
        }
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
        owner = null;
        return isEmpty() ? ChampMap.of()
                : new ChampMap<>(root, size);
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
            return new MutableChampMap<>(deserializedEntries);
        }
    }
}