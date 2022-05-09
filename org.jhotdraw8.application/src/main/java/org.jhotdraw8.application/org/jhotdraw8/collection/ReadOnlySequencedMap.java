package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Map;

public interface ReadOnlySequencedMap<K, V> extends ReadOnlyMap<K, V> {
    /**
     * Gets the first entry.
     *
     * @return an entry
     * @throws java.util.NoSuchElementException if the map is empty
     */
    Map.Entry<K, V> firstEntry();

    /**
     * Gets the first key.
     *
     * @return a key
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default K firstKey() {
        return firstEntry().getKey();
    }

    /**
     * Gets the last entry.
     *
     * @return an entry
     * @throws java.util.NoSuchElementException if the map is empty
     */
    Map.Entry<K, V> lastEntry();

    /**
     * Gets the last key.
     *
     * @return a key
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default K lastKey() {
        return lastEntry().getKey();
    }

    @Override
    default @NonNull ReadOnlySequencedSet<Map.Entry<K, V>> readOnlyEntrySet() {
        return new WrappedReadOnlySequencedSet<>(
                this::iterator,
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
                this::size,
                this::containsEntry,
                this::firstKey,
                this::lastKey
        );
    }

    @Override
    default @NonNull ReadOnlySequencedCollection<V> readOnlyValues() {
        return new WrappedReadOnlySequencedSet<>(
                () -> new MappedIterator<>(iterator(), Map.Entry::getValue),
                this::size,
                this::containsEntry,
                () -> firstEntry().getValue(),
                () -> lastEntry().getValue()
        );
    }
}
