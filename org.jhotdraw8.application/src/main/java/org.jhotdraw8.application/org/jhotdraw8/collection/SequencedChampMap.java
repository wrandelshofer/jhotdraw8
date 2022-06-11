/*
 * @(#)SeqChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.ChampTrie;
import org.jhotdraw8.collection.champ.ChampTrieGraphviz;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.Node;
import org.jhotdraw8.collection.champ.SequencedEntryIterator;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Implements a mutable map using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP), with predictable iteration order.
 * <p>
 * Features:
 * <ul>
 *     <li>allows null keys and null values</li>
 *     <li>is mutable</li>
 *     <li>is not thread-safe</li>
 *     <li>iterates in the order, in which keys were inserted</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>put: O(1) amortized due to renumbering</li>
 *     <li>remove: O(1)</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toImmutable: O(1) + a cost distributed across subsequent updates</li>
 *     <li>clone: O(1) + a cost distributed across subsequent updates</li>
 *     <li>iterator.next(): O(log n)</li>
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
 * Insertion Order:
 * <p>
 * This map uses a counter to keep track of the insertion order.
 * It stores the current value of the counter in the sequence number
 * field of each data entry. If the counter wraps around, it must renumber all
 * sequence numbers.
 * <p>
 * The renumbering is why the {@code copyPut} is O(1) only in an amortized sense.
 * <p>
 * The iterator of the map is a priority queue, that orders the entries by
 * their stored insertion counter value. This is why {@code iterator.next()}
 * is O(log n).
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
public class SequencedChampMap<K, V> extends AbstractSequencedMap<K, V> implements Serializable, Cloneable {
    private final static long serialVersionUID = 0L;
    private final static int ENTRY_LENGTH = 3;
    private transient @Nullable UniqueId mutator;
    private transient @Nullable BitmapIndexedNode<K, V> root;
    private transient int size;
    private transient int modCount;
    /**
     * Counter for the sequence number of the last element. The counter is
     * incremented when a new entry is added to the end of the sequence.
     * <p>
     * The counter is in the range from {@code 0} to
     * {@link Integer#MAX_VALUE} - 1.
     * When the counter reaches {@link Integer#MAX_VALUE}, all
     * sequence numbers are renumbered, and the counter is reset to
     * {@code size}.
     */
    private transient int last = 0;

    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement before a new entry is added to the start of the sequence.
     * <p>
     * The counter is in the range from {@code 0} to
     * {@link Integer#MIN_VALUE}.
     * When the counter is about to wrap over to {@link Integer#MAX_VALUE}, all
     * sequence numbers are renumbered, and the counter is reset to
     * {@code 0}.
     */
    private int first = 0;

    public SequencedChampMap() {
        this.root = BitmapIndexedNode.emptyNode();
    }

    public SequencedChampMap(@NonNull Map<? extends K, ? extends V> m) {
        if (m instanceof SequencedChampMap) {
            @SuppressWarnings("unchecked")
            SequencedChampMap<K, V> that = (SequencedChampMap<K, V>) m;
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

    public SequencedChampMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> m) {
        this.root = BitmapIndexedNode.emptyNode();
        for (Entry<? extends K, ? extends V> e : m) {
            this.put(e.getKey(), e.getValue());
        }

    }

    public SequencedChampMap(@NonNull ReadOnlyMap<? extends K, ? extends V> m) {
        if (m instanceof ImmutableSequencedChampMap) {
            @SuppressWarnings("unchecked")
            ImmutableSequencedChampMap<K, V> that = (ImmutableSequencedChampMap<K, V>) m;
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
        last = 0;
    }

    @Override
    public @NonNull SequencedChampMap<K, V> clone() {
        try {
            @SuppressWarnings("unchecked") final SequencedChampMap<K, V> that = (SequencedChampMap<K, V>) super.clone();
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
        return root.findByKey(key, Objects.hashCode(key), 0, ENTRY_LENGTH, ENTRY_LENGTH - 1) != Node.NO_VALUE;
    }

    /**
     * Dumps the internal structure of this map in the Graphviz DOT Language.
     *
     * @return a dump of the internal structure
     */
    public @NonNull String dump() {
        return new ChampTrieGraphviz<K, V>().dumpTrie(root, ENTRY_LENGTH, true, true);
    }

    @Nullable Iterator<Entry<K, V>> entryIterator(boolean reversed) {
        return new FailFastIterator<>(new SequencedEntryIterator<>(
                size, root, ENTRY_LENGTH, ENTRY_LENGTH - 1, reversed,
                this::persistentRemove, this::persistentPutIfPresent), () -> this.modCount);

    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull SequencedSet<Entry<K, V>> entrySet() {
        return new WrappedSequencedSet<>(
                () -> entryIterator(false),
                this::size,
                this::containsEntry,
                this::clear,
                this::removeEntry,
                this::firstEntry,
                this::lastEntry, null, null
        );
    }

    @Override
    public Entry<K, V> firstEntry() {
        return entryIterator(false).next();
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final @NonNull Object o) {
        final K key = (K) o;
        Object result = root.findByKey(key, Objects.hashCode(key), 0, ENTRY_LENGTH, ENTRY_LENGTH - 1);
        return result == Node.NO_VALUE ? null : (V) result;
    }

    private @NonNull UniqueId getOrCreateMutator() {
        if (mutator == null) {
            mutator = new UniqueId();
        }
        return mutator;
    }

    @Override
    public Entry<K, V> lastEntry() {
        return entryIterator(true).next();
    }

    @Override
    public V put(K key, V value) {
        return putLastWithoutMoveToLast(key, value).getOldValue();
    }

    @Override
    public V putFirst(K key, V value) {
        V oldValue = remove(key);
        putFirstWithoutMoveToFirst(key, value);
        return oldValue;
    }

    private @NonNull ChangeEvent<V> putFirstWithoutMoveToFirst(final K key, final V val) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        final BitmapIndexedNode<K, V> newRootNode =
                root.update(getOrCreateMutator(), key, val, keyHash, 0, details, ENTRY_LENGTH, first,
                        ENTRY_LENGTH - 1);
        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                root = newRootNode;
            } else {
                root = newRootNode;
                size += 1;
                first--;
                if (first == Node.NO_SEQUENCE_NUMBER) {
                    renumber();
                }
                modCount++;
            }
        }
        return details;
    }

    private void persistentPutIfPresent(@NonNull K k, V v) {
        if (containsKey(k)) {
            mutator = null;
            put(k, v);
        }
    }

    private void persistentRemove(K key) {
        mutator = null;
        remove(key);
    }

    @Override
    public V putLast(K key, V value) {
        V oldValue = remove(key);
        putLastWithoutMoveToLast(key, value);
        return oldValue;
    }

    @NonNull ChangeEvent<V> putLastWithoutMoveToLast(final K key, final V val) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        final BitmapIndexedNode<K, V> newRootNode =
                root.update(getOrCreateMutator(), key, val, keyHash, 0, details, ENTRY_LENGTH, last,
                        ENTRY_LENGTH - 1);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                root = newRootNode;
            } else {
                root = newRootNode;
                size += 1;
                last++;
                if (last == Node.NO_SEQUENCE_NUMBER) {
                    renumber();
                }
                modCount++;
            }
        }
        return details;
    }

    @Override
    public V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return removeAndGiveDetails(key).getOldValue();
    }

    @NonNull ChangeEvent<V> removeAndGiveDetails(final K key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        final BitmapIndexedNode<K, V> newRootNode =
                root.remove(getOrCreateMutator(), key, keyHash, 0, details, ENTRY_LENGTH, ENTRY_LENGTH - 1);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            root = newRootNode;
            size = size - 1;
            modCount++;
            if (size == 0) {
                last = Integer.MIN_VALUE;
            }
        }
        return details;
    }

    boolean removeEntry(final @Nullable Object o) {
        if (o instanceof Entry) {
            @SuppressWarnings("unchecked")
            Entry<K, V> entry = (Entry<K, V>) o;
            K key = entry.getKey();
            Object result = root.findByKey(key, Objects.hashCode(key), 0, ENTRY_LENGTH, ENTRY_LENGTH - 1);
            if (Objects.equals(result, entry.getValue())) {
                removeAndGiveDetails(key);
                return true;
            }
        }
        return false;
    }

    private void renumber() {
        root = ChampTrie.renumber(size, root, getOrCreateMutator(), ENTRY_LENGTH);
        last = size;
        first = 0;
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
    public @Nullable ImmutableSequencedChampMap<K, V> toImmutable() {
        if (size == 0) {
            return ImmutableSequencedChampMap.of();
        }
        mutator = null;
        return new ImmutableSequencedChampMap<>(root, size, first, last);
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
            return new SequencedChampMap<>(deserialized);
        }
    }
}