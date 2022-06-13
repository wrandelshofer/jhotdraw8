/*
 * @(#)SeqChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.Node;
import org.jhotdraw8.collection.champ.Sequenced;
import org.jhotdraw8.collection.champ.SequencedEntry;
import org.jhotdraw8.collection.champ.SequencedIterator;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

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
    private transient @NonNull BitmapIndexedNode<SequencedEntry<K, V>> root;
    private transient int size;
    private transient int modCount;
    /**
     * Counter for the sequence number of the last element. The counter is
     * incremented after a new entry is added to the end of the sequence.
     */
    private transient int last = 0;

    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement before a new entry is added to the start of the sequence.
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
        first = last = 0;
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
        return root.findByKey(new SequencedEntry<>(key, null, 0),
                Objects.hashCode(key), 0,
                getEqualsFunction()) != Node.NO_VALUE;
    }


    @NonNull Iterator<Entry<K, V>> entryIterator(boolean reversed) {
        return new FailFastIterator<>(new SequencedIterator<SequencedEntry<K, V>, Entry<K, V>>(
                size, root, reversed,
                this::persistentRemove,
                e -> new MutableMapEntry<>(this::persistentPutIfPresent, e.getKey(), e.getValue())),
                () -> this.modCount);

    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull SequencedSet<Entry<K, V>> entrySet() {
        return new WrappedSequencedSet<>(
                () -> entryIterator(false),
                () -> entryIterator(true),
                this::size,
                this::containsEntry,
                this::clear,
                this::removeEntry,
                this::firstEntry,
                this::lastEntry, null, null
        );
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return new WrappedReadOnlySequencedMap<>(
                () -> entryIterator(true),
                () -> entryIterator(false),
                this::size,
                this::containsKey,
                this::get,
                this::lastEntry,
                this::firstEntry
        );
    }

    @Override
    public @NonNull SequencedMap<K, V> reversed() {
        return new WrappedSequencedMap<>(
                () -> entryIterator(true),
                () -> entryIterator(false),
                this::size,
                this::containsKey,
                this::get,
                this::clear,
                this::remove,
                this::lastEntry,
                this::firstEntry,
                this::put,
                this::putLast,
                this::putFirst
        );
    }

    @Override
    public Entry<K, V> firstEntry() {
        return entryIterator(false).next();
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final @NonNull Object o) {
        Object result = root.findByKey(
                new SequencedEntry<>((K) o),
                Objects.hashCode(o), 0, getEqualsFunction());
        return (result instanceof SequencedEntry<?, ?>) ? ((SequencedEntry<K, V>) result).getValue() : null;
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
        SequencedEntry<K, V> oldValue = this.putLast(key, value, getUpdateFunction()).getOldValue();
        return oldValue == null ? null : oldValue.getValue();
    }

    @Override
    public V putFirst(K key, V value) {
        SequencedEntry<K, V> oldValue = putFirst(key, value, (oldk, newk) -> newk).getOldValue();
        return oldValue == null ? null : oldValue.getValue();
    }

    private @NonNull ChangeEvent<SequencedEntry<K, V>> putFirst(final K key, final V val,
                                                                @NonNull BiFunction<SequencedEntry<K, V>, SequencedEntry<K, V>, SequencedEntry<K, V>> updateFunction) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        final BitmapIndexedNode<SequencedEntry<K, V>> newRootNode =
                root.update(getOrCreateMutator(),
                        new SequencedEntry<>(key, val, first), keyHash, 0, details,
                        updateFunction, getEqualsFunction(), getHashFunction());
        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                root = newRootNode;
            } else {
                root = newRootNode;
                size += 1;
                first--;
                if (first == Sequenced.NO_SEQUENCE_NUMBER) {
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

    private void persistentRemove(SequencedEntry<K, V> entry) {
        mutator = null;
        remove(entry.getKey());
    }

    @Override
    public V putLast(K key, V value) {
        SequencedEntry<K, V> oldValue = putLast(key, value, (oldk, newk) -> newk).getOldValue();
        return oldValue == null ? null : oldValue.getValue();
    }

    @NonNull ChangeEvent<SequencedEntry<K, V>> putLast(
            final K key, final V val,
            @NonNull BiFunction<SequencedEntry<K, V>, SequencedEntry<K, V>, SequencedEntry<K, V>> updateFunction) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        final BitmapIndexedNode<SequencedEntry<K, V>> newRootNode =
                root.update(getOrCreateMutator(),
                        new SequencedEntry<>(key, val, last), keyHash, 0, details,
                        updateFunction, getEqualsFunction(), getHashFunction());

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                root = newRootNode;
            } else {
                root = newRootNode;
                size += 1;
                last++;
                if (last == Sequenced.NO_SEQUENCE_NUMBER) {
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
        SequencedEntry<K, V> oldValue = removeAndGiveDetails(key).getOldValue();
        return oldValue == null ? null : oldValue.getValue();
    }

    @NonNull ChangeEvent<SequencedEntry<K, V>> removeAndGiveDetails(final K key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        final BitmapIndexedNode<SequencedEntry<K, V>> newRootNode =
                root.remove(getOrCreateMutator(),
                        new SequencedEntry<>(key), keyHash, 0, details,
                        getEqualsFunction());
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
            Object result = root.findByKey(
                    new SequencedEntry<>(key), Objects.hashCode(key), 0,
                    getEqualsFunction());
            if ((result instanceof SequencedEntry<?, ?>)
                    && Objects.equals(((SequencedEntry<?, ?>) result).getValue(), entry.getValue())) {
                removeAndGiveDetails(key);
                return true;
            }
        }
        return false;
    }

    private void renumber() {
        root = SequencedEntry.renumber(size, root, getOrCreateMutator(),
                getHashFunction(), getEqualsFunction());
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
    public @NonNull ImmutableSequencedChampMap<K, V> toImmutable() {
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

    @NonNull
    private ToIntFunction<SequencedEntry<K, V>> getHashFunction() {
        return (a) -> Objects.hashCode(a.getKey());
    }

    @NonNull
    private BiPredicate<SequencedEntry<K, V>, SequencedEntry<K, V>> getEqualsFunction() {
        return (a, b) -> Objects.equals(a.getKey(), b.getKey());
    }

    @NonNull
    private BiFunction<SequencedEntry<K, V>, SequencedEntry<K, V>, SequencedEntry<K, V>> getUpdateFunction() {
        return (oldv, newv) -> Objects.equals(oldv.getValue(), newv.getValue()) ? oldv : newv;
    }
}