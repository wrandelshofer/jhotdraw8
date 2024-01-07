/*
 * @(#)ReadOnlySequencedMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedMap;

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
 * Provides a {@link ReadOnlySequencedMap} facade to a set of {@code ReadOnlySequencedMap} functions.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Werner Randelshofer
 */
public class ReadOnlySequencedMapFacade<K, V> extends ReadOnlyMapFacade<K, V>
        implements ReadOnlySequencedMap<K, V> {
    private final @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction;
    private final @NonNull Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction;
    private final int characteristics;
    private final @Nullable Comparator<? super K> comparator;

    public ReadOnlySequencedMapFacade(@NonNull SequencedMap<K, V> target) {
        super(target);
        this.firstEntryFunction = target::firstEntry;
        this.lastEntryFunction = target::lastEntry;
        this.reverseIteratorFunction = () -> target.reversed().sequencedEntrySet().iterator();
        this.characteristics = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.DISTINCT;
        this.comparator = null;
    }

    public ReadOnlySequencedMapFacade(
            @NonNull Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction,
            @NonNull Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction,
            @NonNull IntSupplier sizeFunction,
            @NonNull Predicate<Object> containsKeyFunction,
            @NonNull Function<K, V> getFunction,
            @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction,
            @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction, int characteristics, @Nullable Comparator<? super K> comparator) {
        super(iteratorFunction, sizeFunction, containsKeyFunction, getFunction);
        this.firstEntryFunction = firstEntryFunction;
        this.lastEntryFunction = lastEntryFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
        this.characteristics = characteristics;
        this.comparator = comparator;
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return new ReadOnlySequencedMapFacade<>(
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
