/*
 * @(#)ReadableNavigableMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableNavigableMap;
import org.jspecify.annotations.Nullable;

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
 * Provides a {@link ReadableNavigableMap} facade to a set of {@code ReadableNavigableMap} functions.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class ReadableNavigableMapFacade<K, V> extends ReadableMapFacade<K, V>
        implements ReadableNavigableMap<K, V> {
    private final Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final Supplier<Map.Entry<K, V>> lastEntryFunction;
    private final Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction;
    private final int characteristics;
    final Function<K, Map.Entry<K, V>> ceilingFunction;
    final Function<K, Map.Entry<K, V>> floorFunction;
    final Function<K, Map.Entry<K, V>> higherFunction;
    final Function<K, Map.Entry<K, V>> lowerFunction;
    private final Supplier<Comparator<? super K>> comparatorSupplier;

    public ReadableNavigableMapFacade(NavigableMap<K, V> target) {
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

    public ReadableNavigableMapFacade(
            Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction,
            Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction,
            IntSupplier sizeFunction,
            Predicate<Object> containsKeyFunction,
            Function<K, V> getFunction,
            Supplier<Map.Entry<K, V>> firstEntryFunction,
            Supplier<Map.Entry<K, V>> lastEntryFunction,
            final Function<K, Map.Entry<K, V>> ceilingFunction,
            final Function<K, Map.Entry<K, V>> floorFunction,
            final Function<K, Map.Entry<K, V>> higherFunction,
            final Function<K, Map.Entry<K, V>> lowerFunction,
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
    public ReadableNavigableMap<K, V> readOnlyReversed() {
        return new ReadableNavigableMapFacade<>(
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
