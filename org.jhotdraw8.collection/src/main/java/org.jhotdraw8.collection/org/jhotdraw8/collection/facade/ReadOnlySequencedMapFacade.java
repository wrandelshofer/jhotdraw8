/*
 * @(#)ReadOnlySequencedMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedMap;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Map} functions in the {@link ReadOnlySequencedMap} interface.
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

    public ReadOnlySequencedMapFacade(@NonNull SequencedMap<K, V> target) {
        super(target);
        this.firstEntryFunction = target::firstEntry;
        this.lastEntryFunction = target::lastEntry;
        this.reverseIteratorFunction = () -> target._reversed()._sequencedEntrySet().iterator();
    }

    public ReadOnlySequencedMapFacade(
            @NonNull Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction,
            @NonNull Supplier<Iterator<Map.Entry<K, V>>> reverseIteratorFunction,
            @NonNull IntSupplier sizeFunction,
            @NonNull Predicate<Object> containsKeyFunction,
            @NonNull Function<K, V> getFunction,
            @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction,
            @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction) {
        super(iteratorFunction, sizeFunction, containsKeyFunction, getFunction);
        this.firstEntryFunction = firstEntryFunction;
        this.lastEntryFunction = lastEntryFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
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
                firstEntryFunction
        );
    }

    @Override
    public @Nullable Map.Entry<K, V> firstEntry() {
        return firstEntryFunction.get();
    }

    @Override
    public @Nullable Map.Entry<K, V> lastEntry() {
        return lastEntryFunction.get();
    }
}
