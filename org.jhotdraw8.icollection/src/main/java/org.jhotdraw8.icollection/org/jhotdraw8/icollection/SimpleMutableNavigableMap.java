package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.facade.CollectionFacade;
import org.jhotdraw8.icollection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.icollection.facade.SetFacade;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.impl.iteration.MappedSpliterator;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.readonly.ReadOnlyNavigableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.icollection.serialization.SortedMapSerializationProxy;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;

public class SimpleMutableNavigableMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, ReadOnlyNavigableMap<K, V>, Cloneable {
    @Serial
    private static final long serialVersionUID = 0L;
    @NonNull RedBlackTree<K, V> root;
    final @NonNull Comparator<? super K> comparator;
    private int modCount;

    @SuppressWarnings("unchecked")
    @Override
    public SimpleMutableNavigableMap<K, V> clone() {
        try {
            return (SimpleMutableNavigableMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    int getModCount() {
        return modCount;
    }

    /**
     * Constructs a new empty map, using the natural ordering of its
     * keys.
     */
    public SimpleMutableNavigableMap() {
        root = RedBlackTree.empty();
        comparator = NaturalComparator.instance();
    }

    /**
     * Constructs a new empty map, that uses the specified comparator
     * for ordering its keys.
     *
     * @param comparator the comparator that will be used to order this map.
     *                   If null, the natural ordering of the keys is used.
     */
    public SimpleMutableNavigableMap(@Nullable Comparator<? super K> comparator) {
        root = RedBlackTree.empty();
        this.comparator = comparator == null ? NaturalComparator.instance() : comparator;
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Map}, using the natural ordering of its
     * keys.
     *
     * @param m a map
     */
    public SimpleMutableNavigableMap(@NonNull Map<? extends K, ? extends V> m) {
        this.comparator = NaturalComparator.instance();
        if (m instanceof SimpleMutableNavigableMap<?, ?> r && r.comparator() == null) {
            @SuppressWarnings("unchecked")
            SimpleMutableNavigableMap<K, V> that = (SimpleMutableNavigableMap<K, V>) m;
            this.root = that.root;
        } else {
            this.root = RedBlackTree.empty();
            this.putAll(m);
        }
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Map}, using the same ordering as used by the provided map.
     *
     * @param m a map
     */
    @SuppressWarnings("unchecked")
    public SimpleMutableNavigableMap(@NonNull SortedMap<? extends K, ? extends V> m) {
        this.comparator = m.comparator() == null ? NaturalComparator.instance() : (Comparator<? super K>) m.comparator();
        if (m instanceof SimpleMutableNavigableMap<?, ?> r && r.comparator() == null) {
            @SuppressWarnings("unchecked")
            SimpleMutableNavigableMap<K, V> that = (SimpleMutableNavigableMap<K, V>) m;
            this.root = that.root;
        } else {
            this.root = RedBlackTree.empty();
            this.putAll(m);
        }
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Iterable}, using the natural ordering of its
     * keys.
     *
     * @param m an iterable
     */
    public SimpleMutableNavigableMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> m) {
        this.comparator = NaturalComparator.instance();
        if (m instanceof SimpleImmutableNavigableMap) {
            @SuppressWarnings("unchecked")
            SimpleImmutableNavigableMap<K, V> that = (SimpleImmutableNavigableMap<K, V>) m;
            this.root = that.root;
        } else {
            this.root = RedBlackTree.empty();
            for (Entry<? extends K, ? extends V> e : m) {
                this.put(e.getKey(), e.getValue());
            }
        }
    }

    SimpleMutableNavigableMap(@NonNull RedBlackTree<K, V> root, @NonNull Comparator<? super K> comparator) {
        this.root = root;
        this.comparator = comparator;
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return root.lower(key, comparator).entryOrNull();
    }

    @Override
    public K lowerKey(K key) {
        return root.lower(key, comparator).keyOrNull();
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return root.floor(key, comparator).entryOrNull();
    }

    @Override
    public K floorKey(K key) {
        return root.floor(key, comparator).keyOrNull();
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return root.ceiling(key, comparator).entryOrNull();
    }

    @Override
    public K ceilingKey(K key) {
        return root.ceiling(key, comparator).keyOrNull();
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return root.higher(key, comparator).entryOrNull();
    }

    @Override
    public K higherKey(K key) {
        return root.higher(key, comparator).keyOrNull();
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return new ReadOnlySequencedMapFacade<>(
                this::iterator,
                this::reverseIterator,
                this::size,
                this::containsKey,
                this::get,
                this::lastEntry,
                this::firstEntry,
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, null);
    }

    @Override
    public Map.@Nullable Entry<K, V> firstEntry() {
        return root.min().entryOrNull();
    }

    @Override
    public Map.@Nullable Entry<K, V> lastEntry() {
        return root.max().entryOrNull();
    }

    @Override
    public @Nullable Entry<K, V> pollFirstEntry() {
        var min = root.min();
        if (!min.isEmpty()) {
            root = root.delete(min.getKey(), comparator);
        }
        return min.entryOrNull();
    }

    @Override
    public @Nullable Entry<K, V> pollLastEntry() {
        var max = root.max();
        if (max.isEmpty()) {
            root = root.delete(max.getKey(), comparator);
        }
        return max.entryOrNull();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return null;
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return null;
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return null;
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return null;
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return null;
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return null;
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator == NaturalComparator.instance() ? null : comparator;
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return null;
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return null;
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return null;
    }

    @Override
    public K firstKey() {
        Map.Entry<K, V> entry = firstEntry();
        return entry == null ? null : entry.getKey();
    }

    @Override
    public K lastKey() {
        Map.Entry<K, V> entry = lastEntry();
        return entry == null ? null : entry.getKey();
    }

    @Override
    public int size() {
        return root.size();
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        return root.contains((K) key, comparator);
    }

    @Override
    public boolean containsValue(Object value) {
        for (var node : root) {
            if (Objects.equals(value, node.getValue())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        return root.find((K) key, comparator).valueOrNull();
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        var newRoot = root.insert(key, value, comparator);
        if (newRoot != root) {
            V oldValue = newRoot.size() == root.size() ? root.find(key, comparator).getValue() : null;
            root = newRoot;
            return oldValue;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        var newRoot = root.delete((K) key, comparator);
        if (newRoot != root) {
            V oldValue = root.find((K) key, comparator).getValue();
            root = newRoot;
            return oldValue;
        }
        return null;
    }


    @Override
    public void clear() {
        if (!isEmpty()) {
            root = RedBlackTree.empty();
        }
    }

    private void iteratorPutIfPresent(@Nullable K k, @Nullable V v) {
        if (containsKey(k)) {
            put(k, v);
        }
    }

    private void iteratorRemove(Map.Entry<K, V> entry) {
        remove(entry.getKey());
    }


    public SimpleImmutableNavigableMap<K, V> toImmutable() {
        return null;
    }

    public @NonNull Iterator<Entry<K, V>> iterator() {
        return new FailFastIterator<>(
                new MappedIterator<>(root.iterator(),
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue())),
                this::iteratorRemove, this::getModCount
        );
    }

    @NonNull Iterator<Entry<K, V>> reverseIterator() {
        return new FailFastIterator<>(
                new MappedIterator<>(root.reverseIterator(),
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue())),
                this::iteratorRemove, this::getModCount
        );
    }

    public @NonNull Spliterator<Entry<K, V>> spliterator() {
        Spliterator<Entry<K, V>> spliterator = Spliterators.spliterator(root.iterator(), size(),
                Spliterator.NONNULL | characteristics());
        return new FailFastSpliterator<>(
                spliterator,
                this::getModCount, comparator == NaturalComparator.instance() ? null : Entry.comparingByKey(comparator));
    }


    @Override
    public Set<Entry<K, V>> entrySet() {
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


    @Override
    public Set<K> keySet() {
        return new SetFacade<>(
                () -> new MappedIterator<>(iterator(), Entry::getKey),
                () -> new MappedSpliterator<>(spliterator(), Entry::getKey, characteristics(), comparator()),
                this::size,
                this::containsKey,
                this::clear,
                null,
                this::removeKey
        );
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return new CollectionFacade<>(
                () -> new MappedIterator<>(iterator(), Entry::getValue),
                () -> new MappedSpliterator<>(spliterator(),
                        Entry::getValue, characteristics() & ~(Spliterator.DISTINCT | Spliterator.NONNULL), null),
                this::size,
                this::containsKey,
                this::clear,
                null,
                this::removeKey
        );
    }

    /**
     * Removes the specified entry from the map.
     *
     * @param o an entry (should be a {@link Map.Entry}).
     * @return true if the element was contained in the map
     */
    @SuppressWarnings("unchecked")
    protected boolean removeEntry(@Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            remove(((Entry<K, V>) o).getKey());
            return true;
        }
        return false;
    }

    private boolean removeKey(@Nullable Object o) {
        if (containsKey(o)) {
            remove(o);
            return true;
        }
        return false;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    public void putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> m) {
        for (Map.Entry<? extends K, ? extends V> e : m)
            put(e.getKey(), e.getValue());
    }

    @Serial
    private @NonNull Object writeReplace() throws ObjectStreamException {
        return new SimpleMutableNavigableMap.SerializationProxy<>(this);
    }

    private static class SerializationProxy<K, V> extends SortedMapSerializationProxy<K, V> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(SortedMap<K, V> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            SimpleMutableNavigableMap<K, V> m = new SimpleMutableNavigableMap<>(deserializedComparator);
            m.putAll(deserializedEntries);
            return m;
        }
    }
}