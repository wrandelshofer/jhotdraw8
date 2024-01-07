package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.icollection.immutable.ImmutableNavigableMap;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.icollection.readonly.ReadOnlySortedMap;
import org.jhotdraw8.icollection.serialization.SortedMapSerializationProxy;

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

public class RedBlackMap<K, V> implements ImmutableNavigableMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 0L;
    final @NonNull RedBlackTree<K, V> root;
    final @NonNull Comparator<? super K> comparator;

    RedBlackMap(@NonNull Comparator<? super K> comparator, @NonNull RedBlackTree<K, V> root) {
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
    public static <K, V> @NonNull RedBlackMap<K, V> copyOf(@NonNull Comparator<? super K> comparator, @NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        if (c instanceof RedBlackMap<?, ?> r && r.comparator.equals(comparator)) {
            return (RedBlackMap<K, V>) r;
        }
        if (c instanceof MutableRedBlackMap<?, ?> r && r.comparator.equals(comparator)) {
            return (RedBlackMap<K, V>) r.toImmutable();
        }
        return RedBlackMap.<K, V>sortedOf(comparator).putAll(c);
    }

    @Override
    public @NonNull RedBlackMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.putAll(m);
    }


    @Override
    public @NonNull RedBlackMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.putAll(c);
    }

    @Override
    public @NonNull RedBlackMap<K, V> putKeyValues(@NonNull Object @NonNull ... kv) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.putKeyValues(kv);
    }

    @Override
    public @NonNull RedBlackMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.removeAll(c);
    }

    @Override
    public @NonNull RedBlackMap<K, V> retainAll(@NonNull Iterable<? extends K> c) {
        return (RedBlackMap<K, V>) ImmutableNavigableMap.super.retainAll(c);
    }

    @Override
    public @NonNull RedBlackMap<K, V> retainAll(@NonNull ReadOnlyCollection<? extends K> c) {
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
    public static <K, V> @NonNull RedBlackMap<K, V> copyOf(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
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
    public static <K, V> @NonNull RedBlackMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
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
    public static <K, V> @NonNull RedBlackMap<K, V> sortedOf(@Nullable Comparator<? super K> comparator) {
        comparator = comparator == null ? NaturalComparator.instance() : comparator;
        return new RedBlackMap<>(comparator, RedBlackTree.of(comparator));
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
    public static <K, V> @NonNull RedBlackMap<K, V> sortedOf(@Nullable Comparator<? super K> comparator, Map.@NonNull Entry<K, V> @Nullable ... elements) {
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
    public static <K, V> @NonNull RedBlackMap<K, V> of() {
        return new RedBlackMap<>(NaturalComparator.instance(),
                RedBlackTree.of(NaturalComparator.instance()));
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
    public static <K, V> @NonNull RedBlackMap<K, V> of(Map.@NonNull Entry<K, V> @Nullable ... entries) {
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
    public @NonNull RedBlackMap<K, V> clear() {
        return isEmpty() ? this : sortedOf(comparator);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
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
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return (Iterator<Map.Entry<K, V>>) (Iterator<?>) root.iterator();
    }

    @SuppressWarnings("unchecked")
    @NonNull Iterator<Map.Entry<K, V>> reverseIterator() {
        return (Iterator<Map.Entry<K, V>>) (Iterator<?>) root.reverseIterator();
    }

    @Override
    public @NonNull RedBlackMap<K, V> put(@NonNull K key, @Nullable V value) {
        RedBlackTree<K, V> newRoot = root.insert(key, value, comparator);
        return newRoot == root ? this : new RedBlackMap<>(comparator, newRoot);
    }

    @Override
    public @NonNull RedBlackMap<K, V> remove(@NonNull K key) {
        RedBlackTree<K, V> newRoot = root.delete(key, comparator);
        return newRoot == root ? this : new RedBlackMap<>(comparator, newRoot);
    }

    @Override
    public @NonNull MutableRedBlackMap<K, V> toMutable() {
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
    private @NonNull Object writeReplace() throws ObjectStreamException {
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
        protected @NonNull Object readResolve() {
            return RedBlackMap.sortedOf(deserializedComparator).putAll(deserializedEntries);
        }
    }


}
