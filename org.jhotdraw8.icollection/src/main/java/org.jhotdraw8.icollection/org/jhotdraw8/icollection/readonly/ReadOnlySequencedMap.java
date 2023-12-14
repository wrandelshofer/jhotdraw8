/*
 * @(#)ReadOnlySequencedMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.readonly;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.icollection.facade.SequencedMapFacade;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.sequenced.SequencedMap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;

/**
 * Read-only interface for a map with a well-defined iteration order.
 * The state of the map may change.
 * <p>
 * References:
 * <dl>
 *     <dt>JEP draft: Sequenced Collections</dt>
 *     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReadOnlySequencedMap<K, V> extends ReadOnlyMap<K, V> {
    /**
     * Returns a reversed-order view of this map.
     * <p>
     * Changes to the underlying map are visible in the reversed view.
     *
     * @return a reversed-order view of this map
     */
    @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed();

    /**
     * Gets the first entry in this map or {@code null} if this map is empty.
     *
     * @return the first entry or {@code null}
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default Map.@Nullable Entry<K, V> firstEntry() {
        return isEmpty() ? null : readOnlyEntrySet().iterator().next();
    }

    /**
     * Gets the last entry in this map or {@code null} if this map is empty.
     *
     * @return the last entry or {@code null}
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default Map.@Nullable Entry<K, V> lastEntry() {
        return isEmpty() ? null : readOnlyReversed().readOnlyEntrySet().iterator().next();
    }

    @Override
    @NonNull
    default ReadOnlySet<Map.Entry<K, V>> readOnlyEntrySet() {
        return readOnlySequencedEntrySet();
    }

    @Override
    @NonNull
    default ReadOnlySet<K> readOnlyKeySet() {
        return readOnlySequencedKeySet();
    }

    @Override
    @NonNull
    default ReadOnlyCollection<V> readOnlyValues() {
        return readOnlySequencedValues();
    }

    /**
     * Returns a {@link ReadOnlySequencedSet} view of the entries contained in this map.
     *
     * @return a {@link ReadOnlySequencedSet} view of the entries
     */
    default @NonNull ReadOnlySequencedSet<Map.Entry<K, V>> readOnlySequencedEntrySet() {
        return new ReadOnlySequencedSetFacade<>(
                this::iterator,
                () -> readOnlyReversed().readOnlyEntrySet().iterator(),
                this::size,
                this::containsEntry,
                this::firstEntry,
                this::lastEntry,
                Spliterator.NONNULL | Spliterator.ORDERED);
    }

    /**
     * Returns a {@link ReadOnlySequencedSet} view of the keys contained in this map.
     *
     * @return a {@link ReadOnlySequencedSet} view of the keys
     */
    default @NonNull ReadOnlySequencedSet<K> readOnlySequencedKeySet() {
        return new ReadOnlySequencedSetFacade<>(
                () -> new MappedIterator<>(iterator(), Map.Entry::getKey),
                () -> new MappedIterator<>(readOnlyReversed().readOnlyEntrySet().iterator(), Map.Entry::getKey),
                this::size,
                this::containsKey,
                () -> {
                    Map.Entry<K, V> e = firstEntry();
                    if (e == null) throw new NoSuchElementException();
                    return e.getKey();
                },
                () -> {
                    Map.Entry<K, V> e = lastEntry();
                    if (e == null) throw new NoSuchElementException();
                    return e.getKey();
                },
                Spliterator.ORDERED);
    }

    /**
     * Returns a {@link ReadOnlySequencedCollection} view of the values contained in
     * this map.
     *
     * @return a {@link ReadOnlySequencedCollection} view of the values
     */
    default @NonNull ReadOnlySequencedCollection<V> readOnlySequencedValues() {
        return new ReadOnlySequencedSetFacade<>(
                () -> new MappedIterator<>(iterator(), Map.Entry::getValue),
                () -> new MappedIterator<>(readOnlyReversed().readOnlyEntrySet().iterator(), Map.Entry::getValue),
                this::size,
                this::containsValue,
                () -> {
                    Map.Entry<K, V> e = firstEntry();
                    if (e == null) throw new NoSuchElementException();
                    return e.getValue();
                },
                () -> {
                    Map.Entry<K, V> e = lastEntry();
                    if (e == null) throw new NoSuchElementException();
                    return e.getValue();
                },
                Spliterator.ORDERED);
    }

    @Override
    @NonNull
    default SequencedMap<K, V> asMap() {
        return new SequencedMapFacade<>(this);
    }
}
