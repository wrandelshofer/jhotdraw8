package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.immutable.ImmutableNavigableMap;
import org.jhotdraw8.icollection.impl.KeyComparator;
import org.jhotdraw8.icollection.impl.redblack.RedBlackTree;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedMap;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class RedBlackMap<K, V> implements ImmutableNavigableMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 0L;
    final @NonNull RedBlackTree<AbstractMap.SimpleImmutableEntry<K, V>> root;
    final @NonNull KeyComparator<K, V> keyComparator;

    RedBlackMap(@NonNull KeyComparator<K, V> keyComparator, @NonNull RedBlackTree<AbstractMap.SimpleImmutableEntry<K, V>> root) {
        this.root = root;
        this.keyComparator = keyComparator;
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
    public static <K, V> @NonNull RedBlackMap<K, V> copyOf(@NonNull Comparator<K> comparator, @NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        KeyComparator<K, V> keyComparator = new KeyComparator<>(comparator);
        if (c instanceof RedBlackMap<?, ?> r && r.keyComparator.equals(keyComparator)) {
            return (RedBlackMap<K, V>) r;
        }
        if (c instanceof MutableRedBlackMap<?, ?> r && r.keyComparator.equals(keyComparator)) {
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
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull RedBlackMap<K, V> copyOf(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return RedBlackMap.<K, V>copyOf(NaturalComparator.instance(), c);
    }

    /**
     * Returns an empty immutable map, sorted according to the
     * specified comparator.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull RedBlackMap<K, V> sortedOf(@NonNull Comparator<K> comparator) {
        KeyComparator<K, V> keyComparator = new KeyComparator<>(comparator);
        return ((RedBlackMap<K, V>) new RedBlackMap<>(keyComparator, RedBlackTree.of(keyComparator, RedBlackMap::updateEntry)));
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
    public static <K, V> @NonNull RedBlackMap<K, V> sortedOf(@NonNull Comparator<K> comparator, Map.@NonNull Entry<K, V> @Nullable ... elements) {
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
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull RedBlackMap<K, V> of() {
        return ((RedBlackMap<K, V>) new RedBlackMap<>(new KeyComparator<>(NaturalComparator.instance()),
                RedBlackTree.<AbstractMap.SimpleImmutableEntry<K, V>>of(NaturalComparator.instance(), RedBlackMap::updateEntry)));
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
        return null;
    }

    @Override
    public @Nullable Comparator<? super K> comparator() {
        return keyComparator.getComparator();
    }

    @Override
    public Map.@Nullable Entry<K, V> floorEntry(K k) {
        return null;
    }

    @Override
    public Map.@Nullable Entry<K, V> higherEntry(K k) {
        return null;
    }

    @Override
    public Map.@Nullable Entry<K, V> lowerEntry(K k) {
        return null;
    }

    @Override
    public @NonNull RedBlackMap<K, V> clear() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public @Nullable V get(Object key) {
        return null;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return false;
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return null;
    }

    @Override
    public @NonNull RedBlackMap<K, V> put(@NonNull K key, @Nullable V value) {
        return null;
    }

    @Override
    public @NonNull RedBlackMap<K, V> remove(@NonNull K key) {
        return null;
    }

    @Override
    public @NonNull MutableRedBlackMap<K, V> toMutable() {
        return null;
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }


    /**
     * Update function for a map: we only keep the old entry if it has the same
     * value as the new entry.
     *
     * @param oldv the old entry
     * @param newv the new entry
     * @param <K>  the key type
     * @param <V>  the value type
     * @return the old or the new entry
     */
    static <K, V> AbstractMap.@Nullable SimpleImmutableEntry<K, V> updateEntry(AbstractMap.@Nullable SimpleImmutableEntry<K, V> oldv, AbstractMap.@Nullable SimpleImmutableEntry<K, V> newv) {
        return Objects.equals(oldv.getValue(), newv.getValue()) ? oldv : newv;
    }
}
