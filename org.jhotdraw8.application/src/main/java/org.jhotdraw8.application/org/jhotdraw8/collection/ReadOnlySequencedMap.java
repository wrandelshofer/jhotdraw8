package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Map;

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
    default @Nullable Map.Entry<K, V> firstEntry() {
        return isEmpty() ? null : readOnlyEntrySet().iterator().next();
    }

    /**
     * Gets the first key in this map.
     *
     * @return the first key
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default K firstKey() {
        return readOnlyKeySet().iterator().next();
    }

    /**
     * Gets the last entry in this map or {@code null} if this map is empty.
     *
     * @return the last entry or {@code null}
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default @Nullable Map.Entry<K, V> lastEntry() {
        return isEmpty() ? null : readOnlyReversed().readOnlyEntrySet().iterator().next();
    }

    /**
     * Gets the last key in this map.
     *
     * @return the last key
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default K lastKey() {
        return readOnlyReversed().readOnlyKeySet().iterator().next();
    }

    @Override
    default @NonNull ReadOnlySequencedSet<Map.Entry<K, V>> readOnlyEntrySet() {
        return new WrappedReadOnlySequencedSet<>(
                this::iterator,
                () -> readOnlyReversed().readOnlyEntrySet().iterator(),
                this::size,
                this::containsEntry,
                this::firstEntry,
                this::lastEntry
        );
    }

    @Override
    default @NonNull ReadOnlySequencedSet<K> readOnlyKeySet() {
        return new WrappedReadOnlySequencedSet<>(
                () -> new MappedIterator<>(iterator(), Map.Entry::getKey),
                () -> new MappedIterator<>(readOnlyReversed().readOnlyEntrySet().iterator(), Map.Entry::getKey),
                this::size,
                this::containsKey,
                this::firstKey,
                this::lastKey
        );
    }

    @Override
    default @NonNull ReadOnlySequencedCollection<V> readOnlyValues() {
        return new WrappedReadOnlySequencedSet<>(
                () -> new MappedIterator<>(iterator(), Map.Entry::getValue),
                () -> new MappedIterator<>(readOnlyReversed().readOnlyEntrySet().iterator(), Map.Entry::getValue),
                this::size,
                this::containsValue,
                () -> firstEntry().getValue(),
                () -> lastEntry().getValue()
        );
    }
}
