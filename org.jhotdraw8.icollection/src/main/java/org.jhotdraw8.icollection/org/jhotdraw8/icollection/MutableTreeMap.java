package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.CollectionFacade;
import org.jhotdraw8.icollection.facade.NavigableSetFacade;
import org.jhotdraw8.icollection.facade.ReadableSequencedMapFacade;
import org.jhotdraw8.icollection.facade.SetFacade;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastSpliterator;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.impl.iteration.MappedSpliterator;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.navigable.DescendingNavigableMapView;
import org.jhotdraw8.icollection.navigable.SubsetNavigableMapView;
import org.jhotdraw8.icollection.readable.ReadableNavigableMap;
import org.jhotdraw8.icollection.readable.ReadableSequencedMap;
import org.jhotdraw8.icollection.readable.ReadableSortedMap;
import org.jhotdraw8.icollection.serialization.SortedMapSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;

public class MutableTreeMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, ReadableNavigableMap<K, V>, Cloneable, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    transient RedBlackTree<K, V> tree;
    @SuppressWarnings({"serial", "RedundantSuppression"})//Conditionally serializable
    final Comparator<? super K> comparator;
    private transient int modCount;

    @SuppressWarnings("unchecked")
    @Override
    public MutableTreeMap<K, V> clone() {
        try {
            return (MutableTreeMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    int getModCount() {
        return modCount;
    }

    /// Constructs a new empty map, using the natural ordering of its
    /// keys.
    public MutableTreeMap() {
        tree = RedBlackTree.empty();
        comparator = NaturalComparator.instance();
    }

    /// Constructs a new empty map, that uses the specified comparator
    /// for ordering its keys.
    ///
    /// @param comparator the comparator that will be used to order this map.
    ///                   If null, the natural ordering of the keys is used.
    public MutableTreeMap(@Nullable Comparator<? super K> comparator) {
        tree = RedBlackTree.empty();
        this.comparator = comparator == null ? NaturalComparator.instance() : comparator;
    }

    /// Constructs a map containing the same entries as in the specified
    /// [Map], using the natural ordering of its
    /// keys.
    ///
    /// @param m a map
    @SuppressWarnings("this-escape")
    public MutableTreeMap(Map<? extends K, ? extends V> m) {
        this(NaturalComparator.instance());
        putAll(m);
    }

    /// Constructs a map containing the same entries as in the specified
    /// [Map], using the same ordering as used by the provided map.
    ///
    /// @param m a map
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableTreeMap(SortedMap<? extends K, ? extends V> m) {
        this((Comparator<? super K>) m.comparator());
        putAll(m);
    }

    /// Constructs a map containing the same entries as in the specified
    /// [Map], using the same ordering as used by the provided map.
    ///
    /// @param m a map
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableTreeMap(ReadableSortedMap<? extends K, ? extends V> m) {
        this((Comparator<? super K>) m.comparator());
        putAll(m);
    }

    /// Constructs a map containing the same entries as in the specified
    /// [Iterable], using the natural ordering of its
    /// keys.
    ///
    /// @param m an iterable
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableTreeMap(Iterable<? extends Entry<K, V>> m) {
        this(NaturalComparator.instance());
        this.putAll(m);
    }

    MutableTreeMap(RedBlackTree<K, V> tree, Comparator<? super K> comparator) {
        this.tree = tree;
        this.comparator = comparator;
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return tree.lower(key, comparator).entryOrNull();
    }

    @Override
    public K lowerKey(K key) {
        return tree.lower(key, comparator).keyOrNull();
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return tree.floor(key, comparator).entryOrNull();
    }

    @Override
    public K floorKey(K key) {
        return tree.floor(key, comparator).keyOrNull();
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return tree.ceiling(key, comparator).entryOrNull();
    }

    @Override
    public K ceilingKey(K key) {
        return tree.ceiling(key, comparator).keyOrNull();
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return tree.higher(key, comparator).entryOrNull();
    }

    @Override
    public K higherKey(K key) {
        return tree.higher(key, comparator).keyOrNull();
    }

    @Override
    public ReadableSequencedMap<K, V> readableReversed() {
        return new ReadableSequencedMapFacade<>(
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
        return tree.min().entryOrNull();
    }

    @Override
    public Map.@Nullable Entry<K, V> lastEntry() {
        return tree.max().entryOrNull();
    }

    @Override
    public @Nullable Entry<K, V> pollFirstEntry() {
        var min = tree.min();
        if (!min.isEmpty()) {
            tree = tree.delete(min.getKey(), comparator);
        }
        return min.entryOrNull();
    }

    @Override
    public @Nullable Entry<K, V> pollLastEntry() {
        var max = tree.max();
        if (max.isEmpty()) {
            tree = tree.delete(max.getKey(), comparator);
        }
        return max.entryOrNull();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return new DescendingNavigableMapView<>(this, this::getModCount);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return NavigableSetFacade.createKeySet(this);
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return navigableKeySet().reversed();
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return new SubsetNavigableMapView<>(this, this::getModCount,
                false, fromKey, fromInclusive, false, toKey, toInclusive, true);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return new SubsetNavigableMapView<>(this, this::getModCount,
                true, null, true, false, toKey, inclusive, true);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return new SubsetNavigableMapView<>(this, this::getModCount,
                false, fromKey, inclusive, true, null, true, true);
    }

    @Override
    public @Nullable Comparator<? super K> comparator() {
        return comparator == NaturalComparator.instance() ? null : comparator;
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return headMap(toKey, true);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    @Override
    public K firstKey() {
        Map.Entry<K, V> entry = firstEntry();
        if (entry == null) {
            throw new NoSuchElementException();
        }
        return entry.getKey();
    }

    @Override
    public K lastKey() {
        Map.Entry<K, V> entry = lastEntry();
        if (entry == null) {
            throw new NoSuchElementException();
        }
        return entry.getKey();
    }

    @Override
    public int size() {
        return tree.size();
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        return tree.contains((K) key, comparator);
    }

    @Override
    public boolean containsValue(Object value) {
        for (var node : tree) {
            if (Objects.equals(value, node.getValue())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable V get(Object key) {
        return tree.find((K) key, comparator).valueOrNull();
    }


    @Override
    public @Nullable V put(K key, V value) {
        var newRoot = tree.insert(key, value, comparator);
        if (newRoot != tree) {
            if (newRoot.size() != tree.size()) {
                modCount++;
            }
            V oldValue = newRoot.size() == tree.size() ? tree.find(key, comparator).getValue() : null;
            tree = newRoot;
            return oldValue;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V remove(Object key) {
        var newRoot = tree.delete((K) key, comparator);
        if (newRoot != tree) {
            modCount++;
            V oldValue = tree.find((K) key, comparator).getValue();
            tree = newRoot;
            return oldValue;
        }
        return null;
    }


    @Override
    public void clear() {
        if (!isEmpty()) {
            tree = RedBlackTree.empty();
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


    public PersistentTreeMap<K, V> toPersistent() {
        return new PersistentTreeMap<>(comparator, tree);
    }

    public Iterator<Entry<K, V>> iterator() {
        return new FailFastIterator<>(
                new MappedIterator<>(tree.iterator(),
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue())),
                this::iteratorRemove, this::getModCount
        );
    }

    Iterator<Entry<K, V>> reverseIterator() {
        return new FailFastIterator<>(
                new MappedIterator<>(tree.reverseIterator(),
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue())),
                this::iteratorRemove, this::getModCount
        );
    }

    public Spliterator<Entry<K, V>> spliterator() {
        //noinspection MagicConstant
        Spliterator<Entry<K, V>> spliterator = Spliterators.spliterator(tree.iterator(), size(),
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

    /// Removes the specified entry from the map.
    ///
    /// @param o an entry (should be a [Map.Entry]).
    /// @return true if the element was contained in the map
    @SuppressWarnings("unchecked")
    private boolean removeEntry(@Nullable Object o) {
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
        for (Map.Entry<? extends K, ? extends V> e : m) {
            put(e.getKey(), e.getValue());
        }
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new MutableTreeMap.SerializationProxy<>(this);
    }

    private static class SerializationProxy<K, V> extends SortedMapSerializationProxy<K, V> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(SortedMap<K, V> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            MutableTreeMap<K, V> m = new MutableTreeMap<>(deserializedComparator);
            m.putAll(deserializedEntries);
            return m;
        }
    }
}
