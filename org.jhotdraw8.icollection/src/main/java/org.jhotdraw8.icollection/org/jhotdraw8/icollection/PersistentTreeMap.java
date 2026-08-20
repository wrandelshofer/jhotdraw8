package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSequencedMapFacade;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.persistent.PersistentNavigableMap;
import org.jhotdraw8.icollection.persistent.PersistentNavigableSet;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jhotdraw8.icollection.readable.ReadableSequencedMap;
import org.jhotdraw8.icollection.readable.ReadableSortedMap;
import org.jhotdraw8.icollection.serialization.SortedMapSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.Spliterator;

/// Implements the [PersistentNavigableSet] interface using a Red-Black tree.
///
/// References:
///
/// For a similar design, see 'TreeMap.java' in vavr. The internal data structure of
/// this class is licensed from vavr.
///
/// [vavr TreeMap.java](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/TreeMap.java)
/// [vavr MIT-License](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE)
///
/// @param <K> the key type
/// @param <V> the value type
public class PersistentTreeMap<K, V> implements PersistentNavigableMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings("TransientFieldNotInitialized")
    final transient RedBlackTree<K, V> root;
    @SuppressWarnings("TransientFieldNotInitialized")
    final transient Comparator<? super K> comparator;


    PersistentTreeMap(Comparator<? super K> comparator, RedBlackTree<K, V> root) {
        this.root = root;
        this.comparator = comparator;
    }

    /// Returns a persistent map that contains the provided entries, sorted according to the
    /// specified comparator.
    ///
    /// @param c   an iterable
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return a persistent map of the provided elements
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTreeMap<K, V> copyOf(Comparator<? super K> comparator, java.lang.Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        if (c instanceof PersistentTreeMap<?, ?> r && r.comparator.equals(comparator)) {
            return (PersistentTreeMap<K, V>) r;
        }
        if (c instanceof MutableTreeMap<?, ?> r && r.comparator.equals(comparator)) {
            return (PersistentTreeMap<K, V>) r.toPersistent();
        }
        return PersistentTreeMap.<K, V>sortedOf(comparator).puttingAll(c);
    }

    @Override
    public PersistentTreeMap<K, V> puttingAll(Map<? extends K, ? extends V> m) {
        return (PersistentTreeMap<K, V>) PersistentNavigableMap.super.puttingAll(m);
    }


    @Override
    public PersistentTreeMap<K, V> puttingAll(java.lang.Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (PersistentTreeMap<K, V>) PersistentNavigableMap.super.puttingAll(c);
    }

    @Override
    public PersistentTreeMap<K, V> puttingKeyValues(Object... kv) {
        return (PersistentTreeMap<K, V>) PersistentNavigableMap.super.puttingKeyValues(kv);
    }

    @Override
    public PersistentTreeMap<K, V> removingAll(java.lang.Iterable<? extends K> c) {
        return (PersistentTreeMap<K, V>) PersistentNavigableMap.super.removingAll(c);
    }

    @Override
    public PersistentTreeMap<K, V> retainingAll(java.lang.Iterable<? extends K> c) {
        return (PersistentTreeMap<K, V>) PersistentNavigableMap.super.retainingAll(c);
    }

    @Override
    public PersistentTreeMap<K, V> retainingAll(ReadableCollection<? extends K> c) {
        return (PersistentTreeMap<K, V>) PersistentNavigableMap.super.retainingAll(c);
    }

    /// Returns a persistent map that contains the provided elements sorted according to the
    /// _natural ordering_ of its elements.
    ///
    /// @param c   an iterable
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return a persistent map of the provided elements
    public static <K, V> PersistentTreeMap<K, V> copyOf(java.lang.Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return PersistentTreeMap.copyOf(NaturalComparator.instance(), c);
    }

    /// Returns a persistent copy of the provided map that contains the provided elements sorted according to the
    /// _natural ordering_ of its elements.
    ///
    /// @param map a map
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return a persistent copy
    public static <K, V> PersistentTreeMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return PersistentTreeMap.<K, V>of().puttingAll(map);
    }

    /// Returns an empty persistent map, sorted according to the
    /// specified comparator.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return an empty persistent map
    public static <K, V> PersistentTreeMap<K, V> sortedOf(@Nullable Comparator<? super K> comparator) {
        comparator = comparator == null ? NaturalComparator.instance() : comparator;
        return new PersistentTreeMap<>(comparator, RedBlackTree.of(comparator));
    }

    /// Returns a persistent map that contains the provided elements, sorted according to the
    /// specified comparator.
    ///
    /// @param elements elements
    /// @param <K>      the key type
    /// @param <V>      the value type
    /// @return a persistent map of the provided elements
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <K, V> PersistentTreeMap<K, V> sortedOf(@Nullable Comparator<? super K> comparator, Map.Entry<K, V> @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return PersistentTreeMap.<K, V>sortedOf(comparator).puttingAll(Arrays.asList(elements));
    }

    /// Returns an empty persistent map, sorted according to the
    /// _natural ordering_ of its entries.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return an empty persistent map
    public static <K, V> PersistentTreeMap<K, V> of() {
        return new PersistentTreeMap<>(NaturalComparator.instance(), RedBlackTree.of(NaturalComparator.instance())
        );
    }

    /// Returns a persistent map that contains the provided entries, sorted according to the
    /// _natural ordering_ of its entries.
    ///
    /// @param entries entries
    /// @param <K>     the key type
    /// @param <V>     the value type
    /// @return a persistent map of the provided entries
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <K, V> PersistentTreeMap<K, V> of(Map.Entry<K, V> @Nullable ... entries) {
        return sortedOf(NaturalComparator.instance(), entries);
    }

    @Override
    public Map.@Nullable Entry<K, V> ceilingEntry(K k) {
        return root.ceiling(k, comparator).mapOrNull(AbstractMap.SimpleImmutableEntry::new);
    }

    @Override
    public @Nullable Comparator<? super K> comparator() {
        return comparator == NaturalComparator.instance() ? null : comparator;
    }

    @Override
    public Map.@Nullable Entry<K, V> floorEntry(K k) {
        return root.floor(k, comparator).mapOrNull(AbstractMap.SimpleImmutableEntry::new);
    }

    @Override
    public Map.@Nullable Entry<K, V> higherEntry(K k) {
        return root.higher(k, comparator).mapOrNull(AbstractMap.SimpleImmutableEntry::new);
    }

    @Override
    public Map.@Nullable Entry<K, V> lowerEntry(K k) {
        return root.lower(k, comparator).mapOrNull(AbstractMap.SimpleImmutableEntry::new);
    }

    @Override
    public Map.@Nullable Entry<K, V> firstEntry() {
        return root.min().mapOrNull(AbstractMap.SimpleImmutableEntry::new);
    }

    @Override
    public Map.@Nullable Entry<K, V> lastEntry() {
        return root.max().mapOrNull(AbstractMap.SimpleImmutableEntry::new);
    }

    @Override
    public PersistentTreeMap<K, V> cleared() {
        return isEmpty() ? this : sortedOf(comparator);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public ReadableSequencedMap<K, V> readableReversed() {
        return new ReadableSequencedMapFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::containsKey,
                this::get,
                this::lastEntry,
                this::firstEntry,
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, null);
    }

    @Override
    public int size() {
        return root.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable V get(Object key) {
        return root.find((K) key, comparator).valueOrNull();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(@Nullable Object key) {
        return root.contains((K) key, comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return (Iterator<Map.Entry<K, V>>) (Iterator<?>) root.iterator();
    }

    @SuppressWarnings("unchecked")
    Iterator<Map.Entry<K, V>> reverseIterator() {
        return (Iterator<Map.Entry<K, V>>) (Iterator<?>) root.reverseIterator();
    }

    @Override
    public PersistentTreeMap<K, V> putting(K key, @Nullable V value) {
        RedBlackTree<K, V> newRoot = root.insert(key, value, comparator);
        return newRoot == root ? this : new PersistentTreeMap<>(comparator, newRoot);
    }

    @Override
    public PersistentTreeMap<K, V> removing(K key) {
        RedBlackTree<K, V> newRoot = root.delete(key, comparator);
        return newRoot == root ? this : new PersistentTreeMap<>(comparator, newRoot);
    }

    @Override
    public MutableTreeMap<K, V> toMutable() {
        return new MutableTreeMap<>(root, comparator);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        return ReadableSortedMap.sortedMapEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadableMap.iteratorToHashCode(iterator());
    }

    @Override
    public String toString() {
        return ReadableMap.mapToString(this);
    }

    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new PersistentTreeMap.SerializationProxy<>(this.toMutable());
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
            return PersistentTreeMap.sortedOf(deserializedComparator).puttingAll(deserializedEntries);
        }
    }


}
