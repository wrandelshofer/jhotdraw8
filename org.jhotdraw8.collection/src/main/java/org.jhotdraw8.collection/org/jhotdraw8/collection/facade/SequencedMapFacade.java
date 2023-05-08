/*
 * @(#)SequencedMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.mapped.MappedIterator;
import org.jhotdraw8.collection.mapped.MappedSpliterator;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedCollection;
import org.jhotdraw8.collection.sequenced.SequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedSet;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Map} functions into the {@link SequencedMap} interface.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Werner Randelshofer
 */
public class SequencedMapFacade<K, V> extends MapFacade<K, V> implements SequencedMap<K, V> {
    private final @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction;
    private final @NonNull BiFunction<K, V, V> putFirstFunction;
    private final @NonNull BiFunction<K, V, V> putLastFunction;
    private final @NonNull Supplier<Iterator<Entry<K, V>>> reverseIteratorFunction;
    private final @NonNull Supplier<Spliterator<Entry<K, V>>> reverseSpliteratorFunction;

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
        this.reverseIteratorFunction = () -> m.readOnlyReversed().iterator();
        this.reverseSpliteratorFunction = () -> m.readOnlyReversed().spliterator();
    }

    public SequencedMapFacade(@NonNull SequencedMap<K, V> m) {
        super(m);
        this.firstEntryFunction = m::firstEntry;
        this.lastEntryFunction = m::lastEntry;
        this.putFirstFunction = m::putFirst;
        this.putLastFunction = m::putLast;
        this.reverseIteratorFunction = () -> m._reversed()._sequencedEntrySet().iterator();
        this.reverseSpliteratorFunction = () -> m._reversed()._sequencedEntrySet().spliterator();
    }

    public SequencedMapFacade(
            @NonNull Supplier<Iterator<Entry<K, V>>> iteratorFunction,
            @NonNull Supplier<Iterator<Entry<K, V>>> reverseIteratorFunction,
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
        this(iteratorFunction,
                () -> Spliterators.spliterator(iteratorFunction.get(), sizeFunction.getAsInt(), Spliterator.DISTINCT),
                reverseIteratorFunction,
                () -> Spliterators.spliterator(reverseIteratorFunction.get(), sizeFunction.getAsInt(), Spliterator.DISTINCT),
                sizeFunction, containsKeyFunction,
                getFunction, clearFunction, removeFunction, firstEntryFunction, lastEntryFunction,
                putFunction, putFirstFunction, putLastFunction);
    }

    public SequencedMapFacade(
            @NonNull Supplier<Iterator<Entry<K, V>>> iteratorFunction,
            @NonNull Supplier<Spliterator<Entry<K, V>>> spliteratorFunction,
            @NonNull Supplier<Iterator<Entry<K, V>>> reverseIteratorFunction,
            @NonNull Supplier<Spliterator<Entry<K, V>>> reverseSpliteratorFunction,
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
        super(iteratorFunction, spliteratorFunction, sizeFunction, containsKeyFunction, getFunction, clearFunction,
                removeFunction, putFunction);
        this.firstEntryFunction = firstEntryFunction;
        this.lastEntryFunction = lastEntryFunction;
        this.putFirstFunction = putFirstFunction == null ? (k, v) -> {
            throw new UnsupportedOperationException();
        } : putFirstFunction;
        this.putLastFunction = putLastFunction == null ? (k, v) -> {
            throw new UnsupportedOperationException();
        } : putLastFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
        this.reverseSpliteratorFunction = reverseSpliteratorFunction;
    }

    @Override
    public @NonNull SequencedSet<Entry<K, V>> _sequencedEntrySet() {
        return new SequencedSetFacade<>(
                iteratorFunction, spliteratorFunction,
                reverseIteratorFunction, reverseSpliteratorFunction,
                sizeFunction,
                this::containsEntry,
                clearFunction,
                this::removeEntry,
                firstEntryFunction,
                lastEntryFunction, null, null, null, null);
    }

    @Override
    public Entry<K, V> firstEntry() {
        return firstEntryFunction.get();
    }

    @Override
    public @NonNull SequencedSet<K> _sequencedKeySet() {
        return new SequencedSetFacade<>(
                () -> new MappedIterator<>(iteratorFunction.get(), Map.Entry::getKey),
                () -> new MappedSpliterator<>(spliteratorFunction.get(), Map.Entry::getKey, Spliterator.DISTINCT | Spliterator.SIZED),
                () -> new MappedIterator<>(reverseIteratorFunction.get(), Map.Entry::getKey),
                () -> new MappedSpliterator<>(spliteratorFunction.get(), Map.Entry::getKey, Spliterator.DISTINCT | Spliterator.SIZED),
                sizeFunction,
                this::containsKey,
                clearFunction,
                this::removeEntry,
                () -> {
                    Entry<K, V> e = firstEntry();
                    if (e == null) throw new NoSuchElementException();
                    return e.getKey();
                },
                () -> {
                    Entry<K, V> e = lastEntry();
                    if (e == null) throw new NoSuchElementException();
                    return e.getKey();
                },
                null, null, null, null);
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
    public @NonNull SequencedMap<K, V> _reversed() {
        return new SequencedMapFacade<>(
                reverseIteratorFunction,
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
    public @NonNull SequencedCollection<V> _sequencedValues() {
        return new SequencedCollectionFacade<>(
                () -> new MappedIterator<>(iteratorFunction.get(), Map.Entry::getValue),
                () -> new MappedIterator<>(reverseIteratorFunction.get(), Map.Entry::getValue),
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
