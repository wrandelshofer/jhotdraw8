package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.icollection.immutable.ImmutableNavigableMap;
import org.jhotdraw8.icollection.immutable.ImmutableNavigableSet;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.icollection.readonly.ReadOnlySortedMap;
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

/**
 * Implements the {@link ImmutableNavigableSet} interface using a Red-Black tree.
 * <p>
 * References:
 * <p>
 * For a similar design, see 'TreeMap.java' in vavr. The internal data structure of
 * this class is licensed from vavr.
 * <dl>
 *     <dt>TreeMap.java. Copyright 2023 (c) vavr. <a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE">MIT License</a>.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/TreeMap.java">github.com</a></dd>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */

public class RedBlackMap<K, V> implements ImmutableNavigableMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 0L;
    final transient RedBlackTree<K, V> root;
    final transient Comparator<? super K> comparator;

    /**
     * Creates a new instance with the provided privateData data object.
     * <p>
     * This constructor is intended to be called from a constructor
     * of the subclass, that is called from method {@link #newInstance(PrivateData)}.
     *
     * @param privateData an privateData data object
     */
    @SuppressWarnings("unchecked")
    protected RedBlackMap(PrivateData privateData) {
        this(((Map.Entry<?, RedBlackTree<K, V>>) privateData.get()).getValue(), ((Map.Entry<Comparator<? super K>, ?>) privateData.get()).getKey());
    }

    /**
     * Creates a new instance with the provided privateData object as its internal data structure.
     * <p>
     * Subclasses must override this method, and return a new instance of their subclass!
     *
     * @param privateData the internal data structure needed by this class for creating the instance.
     * @return a new instance of the subclass
     */
    protected RedBlackMap<K, V> newInstance(PrivateData privateData) {
        return new RedBlackMap<>(privateData);
    }

    private RedBlackMap<K, V> newInstance(Comparator<? super K> comparator, RedBlackTree<K, V> root) {
        return newInstance(new PrivateData(new AbstractMap.SimpleImmutableEntry<>(comparator, root)));
    }

    RedBlackMap(RedBlackTree<K, V> root, Comparator<? super K> comparator) {
        this.root = root;
        this.comparator = comparator;
    }

    /**
     * Returns an immutable map that contains the provided entries, sorted according to the
     * specified comparator.
     *
     * @param c   an iterable
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable map of the provided elements
     */
    @SuppressWarnings("unchecked")
    public static <K, V> RedBlackMap<K, V> copyOf(Comparator<? super K> comparator, Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        if (c instanceof RedBlackMap<?, ?> r && r.comparator.equals(comparator)) {
            return (RedBlackMap<K, V>) r;
        }
        if (c instanceof MutableRedBlackMap<?, ?> r && r.comparator.equals(comparator)) {
            return (RedBlackMap<K, V>) r.toImmutable();
        }
        return RedBlackMap.<K, V>sortedOf(comparator).putAll(c);
    }

    @Override
    public RedBlackMap<K, V> putAll(Map<? extends K, ? extends V> m) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.putAll(m);
    }


    @Override
    public RedBlackMap<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.putAll(c);
    }

    @Override
    public RedBlackMap<K, V> putKeyValues(Object... kv) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.putKeyValues(kv);
    }

    @Override
    public RedBlackMap<K, V> removeAll(Iterable<? extends K> c) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.removeAll(c);
    }

    @Override
    public RedBlackMap<K, V> retainAll(Iterable<? extends K> c) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.retainAll(c);
    }

    @Override
    public RedBlackMap<K, V> retainAll(ReadOnlyCollection<? extends K> c) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.retainAll(c);
    }

    /**
     * Returns an immutable map that contains the provided elements sorted according to the
     * <i>natural ordering</i> of its elements.
     *
     * @param c   an iterable
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable map of the provided elements
     */
    public static <K, V> RedBlackMap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return RedBlackMap.copyOf(NaturalComparator.instance(), c);
    }

    /**
     * Returns an immutable copy of the provided map that contains the provided elements sorted according to the
     * <i>natural ordering</i> of its elements.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    public static <K, V> RedBlackMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return RedBlackMap.<K, V>of().putAll(map);
    }

    /**
     * Returns an empty immutable map, sorted according to the
     * specified comparator.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    public static <K, V> RedBlackMap<K, V> sortedOf(@Nullable Comparator<? super K> comparator) {
        comparator = comparator == null ? NaturalComparator.instance() : comparator;
        return new RedBlackMap<>(RedBlackTree.of(comparator), comparator);
    }

    /**
     * Returns an immutable map that contains the provided elements, sorted according to the
     * specified comparator.
     *
     * @param elements elements
     * @param <K>      the key type
     * @param <V>      the value type
     * @return an immutable map of the provided elements
     */
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <K, V> RedBlackMap<K, V> sortedOf(@Nullable Comparator<? super K> comparator, Map.Entry<K, V> @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return RedBlackMap.<K, V>sortedOf(comparator).putAll(Arrays.asList(elements));
    }

    /**
     * Returns an empty immutable map, sorted according to the
     * <i>natural ordering</i> of its entries.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    public static <K, V> RedBlackMap<K, V> of() {
        return new RedBlackMap<>(RedBlackTree.of(NaturalComparator.instance()), NaturalComparator.instance()
        );
    }

    /**
     * Returns an immutable map that contains the provided entries, sorted according to the
     * <i>natural ordering</i> of its entries.
     *
     * @param entries entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return an immutable map of the provided entries
     */
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <K, V> RedBlackMap<K, V> of(Map.Entry<K, V> @Nullable ... entries) {
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
    public RedBlackMap<K, V> clear() {
        return isEmpty() ? this : sortedOf(comparator);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return new ReadOnlySequencedMapFacade<>(
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
    public RedBlackMap<K, V> put(K key, @Nullable V value) {
        RedBlackTree<K, V> newRoot = root.insert(key, value, comparator);
        return newRoot == root ? this : newInstance(comparator, newRoot);
    }

    @Override
    public RedBlackMap<K, V> remove(K key) {
        RedBlackTree<K, V> newRoot = root.delete(key, comparator);
        return newRoot == root ? this : newInstance(comparator, newRoot);
    }

    @Override
    public MutableRedBlackMap<K, V> toMutable() {
        return new MutableRedBlackMap<>(root, comparator);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        return ReadOnlySortedMap.sortedMapEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iteratorToHashCode(iterator());
    }

    @Override
    public String toString() {
        return ReadOnlyMap.mapToString(this);
    }

    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new RedBlackMap.SerializationProxy<>(this.toMutable());
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
            return RedBlackMap.sortedOf(deserializedComparator).putAll(deserializedEntries);
        }
    }


}
