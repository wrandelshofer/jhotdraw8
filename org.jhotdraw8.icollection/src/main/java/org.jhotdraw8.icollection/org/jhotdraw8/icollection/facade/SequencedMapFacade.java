/*
 * @(#)SequencedMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.impl.iteration.MappedSpliterator;
import org.jhotdraw8.icollection.readable.ReadableSequencedMap;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link SequencedMap} facade to a set of {@code Map} functions.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class SequencedMapFacade<K, V> extends MapFacade<K, V> implements SequencedMap<K, V> {
    private final Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final Supplier<Map.Entry<K, V>> lastEntryFunction;
    private final BiFunction<K, V, V> putFirstFunction;
    private final BiFunction<K, V, V> putLastFunction;
    private final Supplier<Iterator<Entry<K, V>>> reverseIteratorFunction;
    private final Supplier<Spliterator<Entry<K, V>>> reverseSpliteratorFunction;

    public SequencedMapFacade(ReadableSequencedMap<K, V> m) {
        super(m);
        this.firstEntryFunction = m::firstEntry;
        this.lastEntryFunction = m::lastEntry;
        this.putFirstFunction = (k, v) -> {
            throw new UnsupportedOperationException();
        };
        this.putLastFunction = (k, v) -> {
            throw new UnsupportedOperationException();
        };
        this.reverseIteratorFunction = () -> m.readableReversed().iterator();
        this.reverseSpliteratorFunction = () -> m.readableReversed().spliterator();
    }

    public SequencedMapFacade(SequencedMap<K, V> m) {
        super(m);
        this.firstEntryFunction = m::firstEntry;
        this.lastEntryFunction = m::lastEntry;
        this.putFirstFunction = m::putFirst;
        this.putLastFunction = m::putLast;
        this.reverseIteratorFunction = () -> m.reversed().sequencedEntrySet().iterator();
        this.reverseSpliteratorFunction = () -> m.reversed().sequencedEntrySet().spliterator();
    }

    public SequencedMapFacade(
            Supplier<Iterator<Entry<K, V>>> iteratorFunction,
            Supplier<Iterator<Entry<K, V>>> reverseIteratorFunction,
            IntSupplier sizeFunction,
            Predicate<Object> containsKeyFunction,
            Function<K, V> getFunction,
            @Nullable Runnable clearFunction,
            @Nullable Function<Object, V> removeFunction,
            Supplier<Map.Entry<K, V>> firstEntryFunction,
            Supplier<Map.Entry<K, V>> lastEntryFunction,
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
            Supplier<Iterator<Entry<K, V>>> iteratorFunction,
            Supplier<Spliterator<Entry<K, V>>> spliteratorFunction,
            Supplier<Iterator<Entry<K, V>>> reverseIteratorFunction,
            Supplier<Spliterator<Entry<K, V>>> reverseSpliteratorFunction,
            IntSupplier sizeFunction,
            Predicate<Object> containsKeyFunction,
            Function<K, V> getFunction,
            @Nullable Runnable clearFunction,
            @Nullable Function<Object, V> removeFunction,
            Supplier<Map.Entry<K, V>> firstEntryFunction,
            Supplier<Map.Entry<K, V>> lastEntryFunction,
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

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public static <K, V> SequencedSet<K> createKeySet(SequencedMap<K, V> m) {
        return new SequencedSetFacade<>(
                () -> new MappedIterator<>(m.sequencedEntrySet().iterator(), Entry::getKey),
                () -> new MappedSpliterator<>(m.sequencedEntrySet().spliterator(), Entry::getKey,
                        Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.ORDERED, null),
                () -> new MappedIterator<>(m.reversed().sequencedEntrySet().iterator(), Entry::getKey),
                () -> new MappedSpliterator<>(m.reversed().sequencedEntrySet().spliterator(), Entry::getKey,
                        Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.ORDERED, null),
                m::size,
                m::containsKey,
                m::clear,
                o -> {
                    if (m.containsKey(o)) {
                        m.remove(o);
                        return true;
                    }
                    return false;
                },
                () -> {
                    Entry<K, V> e = m.firstEntry();
                    if (e == null) {
                        throw new NoSuchElementException();
                    }
                    return e.getKey();
                },
                () -> {
                    Entry<K, V> e = m.lastEntry();
                    if (e == null) {
                        throw new NoSuchElementException();
                    }
                    return e.getKey();
                },
                null, null, null, null);
    }

    public static <K, V> SequencedCollection<V> createValues(SequencedMap<K, V> m) {
        return new SequencedCollectionFacade<>(
                () -> new MappedIterator<>(m.sequencedEntrySet().iterator(), Entry::getValue),
                () -> new MappedIterator<>(m.reversed().sequencedEntrySet().iterator(), Entry::getValue),
                m::size,
                m::containsValue,
                m::clear,
                (o) -> {
                    for (Entry<K, V> entry : m.sequencedEntrySet()) {
                        if (Objects.equals(entry.getValue(), o)) {
                            m.remove(entry.getKey());
                            return true;
                        }
                    }
                    return false;
                },
                () -> {
                    Entry<K, V> entry = m.firstEntry();
                    if (entry == null) {
                        throw new NoSuchElementException();
                    }
                    return entry.getValue();
                },
                () -> {
                    Entry<K, V> entry = m.lastEntry();
                    if (entry == null) {
                        throw new NoSuchElementException();
                    }
                    return entry.getValue();
                },
                null, null,
                null);
    }

    @Override
    public SequencedSet<Entry<K, V>> sequencedEntrySet() {
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
    public SequencedSet<K> sequencedKeySet() {
        return new SequencedSetFacade<>(
                () -> new MappedIterator<>(iteratorFunction.get(), Map.Entry::getKey),
                () -> new MappedSpliterator<>(spliteratorFunction.get(), Map.Entry::getKey, Spliterator.DISTINCT | Spliterator.SIZED, null),
                () -> new MappedIterator<>(reverseIteratorFunction.get(), Map.Entry::getKey),
                () -> new MappedSpliterator<>(spliteratorFunction.get(), Map.Entry::getKey, Spliterator.DISTINCT | Spliterator.SIZED, null),
                sizeFunction,
                this::containsKey,
                clearFunction,
                this::removeEntry,
                () -> {
                    Entry<K, V> e = lastEntryFunction.get();
                    if (e == null) {
                        throw new NoSuchElementException();
                    }
                    return e.getKey();
                },
                () -> {
                    Entry<K, V> e = firstEntryFunction.get();
                    if (e == null) {
                        throw new NoSuchElementException();
                    }
                    return e.getKey();
                },
                null, null, null, null);
    }

    @Override
    public Entry<K, V> lastEntry() {
        return lastEntryFunction.get();
    }

    @Override
    public @Nullable V putFirst(K k, V v) {
        return putFirstFunction.apply(k, v);
    }

    @Override
    public @Nullable V putLast(K k, V v) {
        return putLastFunction.apply(k, v);
    }

    @Override
    public SequencedMap<K, V> reversed() {
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
    public SequencedCollection<V> sequencedValues() {
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
                }, null, null,
                null);
    }
}
