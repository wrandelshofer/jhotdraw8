/*
 * @(#)ReadOnlyNavigableMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlyNavigableMap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadOnlyNavigableMap} facade to a set of {@code ReadOnlyNavigableMap} functions.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Werner Randelshofer
 */
public class ReadOnlyNavigableMapFacade<K, V> extends ReadOnlyMapFacade<K, V>
        implements ReadOnlyNavigableMap<K, V> {
    private final @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction;
    private final @NonNull Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction;
    private final int characteristics;
    final @NonNull Function<K, Map.Entry<K, V>> ceilingFunction;
    final @NonNull Function<K, Map.Entry<K, V>> floorFunction;
    final @NonNull Function<K, Map.Entry<K, V>> higherFunction;
    final @NonNull Function<K, Map.Entry<K, V>> lowerFunction;
    private final @NonNull Supplier<Comparator<? super K>> comparatorSupplier;

    public ReadOnlyNavigableMapFacade(@NonNull NavigableMap<K, V> target) {
        super(target);
        this.firstEntryFunction = target::firstEntry;
        this.lastEntryFunction = target::lastEntry;
        this.reverseIteratorFunction = () -> target.reversed().sequencedEntrySet().iterator();
        this.characteristics = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.DISTINCT;
        this.comparatorSupplier = target::comparator;
        this.ceilingFunction = target::ceilingEntry;
        this.floorFunction = target::floorEntry;
        this.higherFunction = target::higherEntry;
        this.lowerFunction = target::lowerEntry;
    }

    public ReadOnlyNavigableMapFacade(
            @NonNull Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction,
            @NonNull Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction,
            @NonNull IntSupplier sizeFunction,
            @NonNull Predicate<Object> containsKeyFunction,
            @NonNull Function<K, V> getFunction,
            @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction,
            @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction,
            final @NonNull Function<K, Map.Entry<K, V>> ceilingFunction,
            final @NonNull Function<K, Map.Entry<K, V>> floorFunction,
            final @NonNull Function<K, Map.Entry<K, V>> higherFunction,
            final @NonNull Function<K, Map.Entry<K, V>> lowerFunction,
            int characteristics, @Nullable Supplier<Comparator<? super K>> comparator) {
        super(iteratorFunction, sizeFunction, containsKeyFunction, getFunction);
        this.firstEntryFunction = firstEntryFunction;
        this.lastEntryFunction = lastEntryFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
        this.characteristics = characteristics;
        this.comparatorSupplier = comparator;
        this.ceilingFunction = ceilingFunction;
        this.floorFunction = floorFunction;
        this.higherFunction = higherFunction;
        this.lowerFunction = lowerFunction;
    }

    @Override
    public Map.@Nullable Entry<K, V> ceilingEntry(K k) {
        return ceilingFunction.apply(k);
    }

    @Override
    public Map.@Nullable Entry<K, V> floorEntry(K k) {
        return floorFunction.apply(k);
    }

    @Override
    public Map.@Nullable Entry<K, V> higherEntry(K k) {
        return higherFunction.apply(k);
    }

    @Override
    public Map.@Nullable Entry<K, V> lowerEntry(K k) {
        return lowerFunction.apply(k);
    }

    @Override
    public @NonNull ReadOnlyNavigableMap<K, V> readOnlyReversed() {
        return new ReadOnlyNavigableMapFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsKeyFunction,
                getFunction,
                lastEntryFunction,
                firstEntryFunction,
                floorFunction,
                ceilingFunction,
                lowerFunction,
                higherFunction,
                characteristics, () -> comparatorSupplier.get().reversed());
    }

    @Override
    public Map.@Nullable Entry<K, V> firstEntry() {
        return firstEntryFunction.get();
    }

    @Override
    public Map.@Nullable Entry<K, V> lastEntry() {
        return lastEntryFunction.get();
    }

    @Override
    public @Nullable Comparator<? super K> comparator() {
        return comparatorSupplier.get();
    }

    @Override
    public int characteristics() {
        return characteristics;
    }
}
