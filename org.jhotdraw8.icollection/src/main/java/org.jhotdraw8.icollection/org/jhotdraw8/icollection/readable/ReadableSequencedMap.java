/*
 * @(#)ReadableSequencedMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.readable;

import org.jhotdraw8.icollection.facade.ReadableSequencedCollectionFacade;
import org.jhotdraw8.icollection.facade.ReadableSequencedSetFacade;
import org.jhotdraw8.icollection.facade.SequencedMapFacade;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SequencedMap;
import java.util.Spliterator;

/**
 * A readable interface to a sequenced map. A sequenced map has a well-defined encounter order, that supports
 * operations at both ends, and that is reversible.
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
public interface ReadableSequencedMap<K, V> extends ReadableMap<K, V> {
    /**
     * Returns a reversed-order view of this map.
     * <p>
     * Changes to the underlying map are visible in the reversed view.
     *
     * @return a reversed-order view of this map
     */
    ReadableSequencedMap<K, V> readableReversed();

    /**
     * Gets the first entry in this map or {@code null} if this map is empty.
     *
     * @return the first entry or {@code null}
     * @throws NoSuchElementException if the map is empty
     */
    default Map.@Nullable Entry<K, V> firstEntry() {
        return isEmpty() ? null : readableEntrySet().iterator().next();
    }

    /**
     * Gets the last entry in this map or {@code null} if this map is empty.
     *
     * @return the last entry or {@code null}
     * @throws NoSuchElementException if the map is empty
     */
    default Map.@Nullable Entry<K, V> lastEntry() {
        return isEmpty() ? null : readableReversed().readableEntrySet().iterator().next();
    }


    /**
     * Returns a {@link ReadableSequencedSet} view of the entries contained in this map.
     *
     * @return a {@link ReadableSequencedSet} view of the entries
     */
    @Override
    default ReadableSequencedSet<Map.Entry<K, V>> readableEntrySet() {
        return new ReadableSequencedSetFacade<>(
                this::iterator,
                () -> readableReversed().readableEntrySet().iterator(),
                this::size,
                this::containsEntry,
                this::firstEntry,
                this::lastEntry,
                characteristics() | Spliterator.NONNULL);
    }

    /**
     * Returns the spliterator characteristics of the key set.
     * This implementation returns {@link Spliterator#SIZED}|{@link Spliterator#DISTINCT}.
     *
     * @return characteristics.
     */
    default int characteristics() {
        return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.DISTINCT;
    }

    /**
     * Returns a {@link ReadableSequencedSet} view of the keys contained in this map.
     *
     * @return a {@link ReadableSequencedSet} view of the keys
     */
    @Override
    default ReadableSequencedSet<K> readableKeySet() {
        return new ReadableSequencedSetFacade<>(
                () -> new MappedIterator<>(iterator(), Map.Entry::getKey),
                () -> new MappedIterator<>(readableReversed().readableEntrySet().iterator(), Map.Entry::getKey),
                this::size,
                this::containsKey,
                () -> {
                    Map.Entry<K, V> e = firstEntry();
                    if (e == null) {
                        throw new NoSuchElementException();
                    }
                    return e.getKey();
                },
                () -> {
                    Map.Entry<K, V> e = lastEntry();
                    if (e == null) {
                        throw new NoSuchElementException();
                    }
                    return e.getKey();
                },
                characteristics());
    }

    /**
     * Returns a {@link ReadableSequencedCollection} view of the values contained in
     * this map.
     *
     * @return a {@link ReadableSequencedCollection} view of the values
     */
    @Override
    default ReadableSequencedCollection<V> readableValues() {
        return new ReadableSequencedCollectionFacade<>(
                () -> new MappedIterator<>(iterator(), Map.Entry::getValue),
                () -> new MappedIterator<>(readableReversed().readableEntrySet().iterator(), Map.Entry::getValue),
                this::size,
                this::containsValue,
                () -> {
                    Map.Entry<K, V> e = firstEntry();
                    if (e == null) {
                        throw new NoSuchElementException();
                    }
                    return e.getValue();
                },
                () -> {
                    Map.Entry<K, V> e = lastEntry();
                    if (e == null) {
                        throw new NoSuchElementException();
                    }
                    return e.getValue();
                },
                characteristics() & ~(Spliterator.DISTINCT | Spliterator.SORTED));
    }

    @Override
    default SequencedMap<K, V> asMap() {
        return new SequencedMapFacade<>(this);
    }
}
