/*
 * @(#)ReadableSortedMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableSortedMap;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadableSortedMap} facade to a set of {@code ReadableSortedMap} functions.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Werner Randelshofer
 */
public class ReadableSortedMapFacade<K, V> extends ReadableMapFacade<K, V>
        implements ReadableSortedMap<K, V> {
    private final Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final Supplier<Map.Entry<K, V>> lastEntryFunction;
    private final Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction;
    private final int characteristics;
    private final Supplier<Comparator<? super K>> comparatorSupplier;

    public ReadableSortedMapFacade(SortedMap<K, V> target) {
        super(target);
        this.firstEntryFunction = target::firstEntry;
        this.lastEntryFunction = target::lastEntry;
        this.reverseIteratorFunction = () -> target.reversed().sequencedEntrySet().iterator();
        this.characteristics = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.DISTINCT;
        this.comparatorSupplier = target::comparator;
    }

    public ReadableSortedMapFacade(
            Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction,
            Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction,
            IntSupplier sizeFunction,
            Predicate<Object> containsKeyFunction,
            Function<K, V> getFunction,
            Supplier<Map.Entry<K, V>> firstEntryFunction,
            Supplier<Map.Entry<K, V>> lastEntryFunction,
            int characteristics, @Nullable Supplier<Comparator<? super K>> comparator) {
        super(iteratorFunction, sizeFunction, containsKeyFunction, getFunction);
        this.firstEntryFunction = firstEntryFunction;
        this.lastEntryFunction = lastEntryFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
        this.characteristics = characteristics;
        this.comparatorSupplier = comparator;
    }

    @Override
    public ReadableSortedMap<K, V> readOnlyReversed() {
        return new ReadableSortedMapFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsKeyFunction,
                getFunction,
                lastEntryFunction,
                firstEntryFunction,
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
