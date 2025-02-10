/*
 * @(#)ReadableSequencedMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableSequencedMap;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadableSequencedMap} facade to a set of {@code ReadableSequencedMap} functions.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class ReadableSequencedMapFacade<K, V> extends ReadableMapFacade<K, V>
        implements ReadableSequencedMap<K, V> {
    private final Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final Supplier<Map.Entry<K, V>> lastEntryFunction;
    private final Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction;
    private final int characteristics;
    private final @Nullable Comparator<? super K> comparator;

    public ReadableSequencedMapFacade(SequencedMap<K, V> target) {
        super(target);
        this.firstEntryFunction = target::firstEntry;
        this.lastEntryFunction = target::lastEntry;
        this.reverseIteratorFunction = () -> target.reversed().sequencedEntrySet().iterator();
        this.characteristics = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.DISTINCT;
        this.comparator = null;
    }

    public ReadableSequencedMapFacade(
            Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction,
            Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction,
            IntSupplier sizeFunction,
            Predicate<Object> containsKeyFunction,
            Function<K, V> getFunction,
            Supplier<Map.Entry<K, V>> firstEntryFunction,
            Supplier<Map.Entry<K, V>> lastEntryFunction, int characteristics, @Nullable Comparator<? super K> comparator) {
        super(iteratorFunction, sizeFunction, containsKeyFunction, getFunction);
        this.firstEntryFunction = firstEntryFunction;
        this.lastEntryFunction = lastEntryFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
        this.characteristics = characteristics;
        this.comparator = comparator;
    }

    @Override
    public ReadableSequencedMap<K, V> readOnlyReversed() {
        return new ReadableSequencedMapFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsKeyFunction,
                getFunction,
                lastEntryFunction,
                firstEntryFunction,
                characteristics, comparator);
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
    public int characteristics() {
        return characteristics;
    }
}
