/*
 * @(#)WrappedSequencedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.SequencedCollection;
import org.jhotdraw8.collection.SequencedMap;
import org.jhotdraw8.collection.SequencedSet;
import org.jhotdraw8.collection.mapped.MappedIterator;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps map functions into the {@link SequencedMap} interface.
 *
 * @author Werner Randelshofer
 */
public class SequencedMapFacade<K, V> extends MapFacade<K, V> implements SequencedMap<K, V> {
    private final @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction;
    private final @NonNull BiFunction<K, V, V> putFirstFunction;
    private final @NonNull BiFunction<K, V, V> putLastFunction;
    private final @NonNull Supplier<Iterator<Entry<K, V>>> reversedIteratorFunction;

    public SequencedMapFacade(@NonNull ReadOnlySequencedMap<K, V> m) {
        super(m);
        this.firstEntryFunction = m::firstEntry;
        this.lastEntryFunction = m::lastEntry;
        this.putFirstFunction = (k, v) -> {
            throw new UnsupportedOperationException();
        };
        this.putLastFunction = (k, v) -> {
            throw new UnsupportedOperationException();
        };
        this.reversedIteratorFunction = () -> m.readOnlyReversed().iterator();
    }

    public SequencedMapFacade(@NonNull SequencedMap<K, V> m) {
        super(m);
        this.firstEntryFunction = m::firstEntry;
        this.lastEntryFunction = m::lastEntry;
        this.putFirstFunction = m::putFirst;
        this.putLastFunction = m::putLast;
        this.reversedIteratorFunction = () -> m.reversed().entrySet().iterator();
    }

    public SequencedMapFacade(
            @NonNull Supplier<Iterator<Entry<K, V>>> iteratorFunction,
            @NonNull Supplier<Iterator<Entry<K, V>>> reversedIteratorFunction,
            @NonNull IntSupplier sizeFunction,
            @NonNull Predicate<Object> containsKeyFunction,
            @NonNull Function<K, V> getFunction,
            @Nullable Runnable clearFunction,
            @Nullable Function<Object, V> removeFunction,
            @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction,
            @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction,
            @Nullable BiFunction<K, V, V> putFunction,
            @Nullable BiFunction<K, V, V> putFirstFunction,
            @Nullable BiFunction<K, V, V> putLastFunction) {
        super(iteratorFunction, sizeFunction, containsKeyFunction, getFunction, clearFunction,
                removeFunction, putFunction);
        this.firstEntryFunction = firstEntryFunction;
        this.lastEntryFunction = lastEntryFunction;
        this.putFirstFunction = putFirstFunction == null ? (k, v) -> {
            throw new UnsupportedOperationException();
        } : putFirstFunction;
        this.putLastFunction = putLastFunction == null ? (k, v) -> {
            throw new UnsupportedOperationException();
        } : putLastFunction;
        this.reversedIteratorFunction = reversedIteratorFunction;
    }

    @Override
    public @NonNull SequencedSet<Entry<K, V>> entrySet() {
        return new SequencedSetFacadeFacade<>(
                iteratorFunction,
                reversedIteratorFunction,
                sizeFunction,
                this::containsEntry,
                clearFunction,
                this::removeEntry,
                firstEntryFunction,
                lastEntryFunction, null, null, null, null
        );
    }

    @Override
    public Entry<K, V> firstEntry() {
        return firstEntryFunction.get();
    }

    @Override
    public @NonNull SequencedSet<K> keySet() {
        return new SequencedSetFacadeFacade<>(
                () -> new MappedIterator<>(iteratorFunction.get(), Map.Entry::getKey),
                () -> new MappedIterator<>(reversedIteratorFunction.get(), Map.Entry::getKey),
                sizeFunction,
                this::containsKey,
                clearFunction,
                this::removeEntry,
                this::firstKey,
                this::lastKey, null, null, null, null
        );
    }

    @Override
    public Entry<K, V> lastEntry() {
        return lastEntryFunction.get();
    }

    @Override
    public V putFirst(K k, V v) {
        return putFirstFunction.apply(k, v);
    }

    @Override
    public V putLast(K k, V v) {
        return putLastFunction.apply(k, v);
    }

    @Override
    public @NonNull SequencedMap<K, V> reversed() {
        return new SequencedMapFacade<>(
                reversedIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsKeyFunction,
                getFunction,
                clearFunction,
                removeFunction,
                lastEntryFunction,
                firstEntryFunction,
                putFunction,
                putLastFunction,
                putFirstFunction
        );
    }

    @Override
    public @NonNull SequencedCollection<V> values() {
        return new SequencedCollectionFacade<>(
                () -> new MappedIterator<>(iteratorFunction.get(), Map.Entry::getValue),
                () -> new MappedIterator<>(reversedIteratorFunction.get(), Map.Entry::getValue),
                sizeFunction,
                this::containsKey,
                clearFunction,
                this::removeEntry,
                () -> {
                    Entry<K, V> entry = firstEntry();
                    if (entry == null) {
                        throw new NoSuchElementException();
                    }
                    return entry.getValue();
                },
                () -> {
                    Entry<K, V> entry = lastEntry();
                    if (entry == null) {
                        throw new NoSuchElementException();
                    }
                    return entry.getValue();
                }, null, null
        );
    }
}
