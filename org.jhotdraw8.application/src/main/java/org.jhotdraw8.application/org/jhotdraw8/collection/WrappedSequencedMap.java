package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Iterator;
import java.util.Map;
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
public class WrappedSequencedMap<K, V> extends WrappedMap<K, V> implements SequencedMap<K, V> {
    private final @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction;
    private final @NonNull BiFunction<K, V, V> putFirstFunction;
    private final @NonNull BiFunction<K, V, V> putLastFunction;

    public WrappedSequencedMap(@NonNull ReadOnlySequencedMap<K, V> m) {
        super(m);
        this.firstEntryFunction = m::firstEntry;
        this.lastEntryFunction = m::lastEntry;
        this.putFirstFunction = (k, v) -> {
            throw new UnsupportedOperationException();
        };
        this.putLastFunction = (k, v) -> {
            throw new UnsupportedOperationException();
        };
    }

    public WrappedSequencedMap(@NonNull SequencedMap<K, V> m) {
        super(m);
        this.firstEntryFunction = m::firstEntry;
        this.lastEntryFunction = m::lastEntry;
        this.putFirstFunction = m::putFirst;
        this.putLastFunction = m::putLast;
    }

    public WrappedSequencedMap(
            @NonNull Supplier<Iterator<Entry<K, V>>> iteratorFunction,
            @NonNull IntSupplier sizeFunction,
            @NonNull Predicate<Object> containsKeyFunction,
            @NonNull Function<K, V> getFunction,
            @Nullable Runnable clearFunction,
            @Nullable Predicate<Object> removeFunction,
            @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction,
            @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction,
            @Nullable BiFunction<K, V, V> putFunction,
            @Nullable BiFunction<K, V, V> putFirstFunction,
            @Nullable BiFunction<K, V, V> putLastFunction) {
        super(iteratorFunction, sizeFunction, containsKeyFunction, getFunction, clearFunction, removeFunction, putFunction);
        this.firstEntryFunction = firstEntryFunction;
        this.lastEntryFunction = lastEntryFunction;
        this.putFirstFunction = putFirstFunction == null ? (k, v) -> {
            throw new UnsupportedOperationException();
        } : putFirstFunction;
        this.putLastFunction = putLastFunction == null ? (k, v) -> {
            throw new UnsupportedOperationException();
        } : putLastFunction;
    }

    @Override
    public @NonNull SequencedSet<Entry<K, V>> entrySet() {
        return new WrappedSequencedSet<>(
                iteratorFunction,
                sizeFunction,
                this::containsEntry,
                clearFunction,
                this::removeEntry,
                firstEntryFunction,
                lastEntryFunction, null, null
        );
    }

    @Override
    public Entry<K, V> firstEntry() {
        return firstEntryFunction.get();
    }

    @Override
    public @NonNull Iterator<Entry<K, V>> iterator() {
        return iteratorFunction.get();
    }

    @Override
    public @NonNull SequencedSet<K> keySet() {
        return new WrappedSequencedSet<>(
                () -> new MappedIterator<>(iteratorFunction.get(), Map.Entry::getKey),
                sizeFunction,
                this::containsKey,
                clearFunction,
                this::removeEntry,
                this::firstKey,
                this::lastKey, null, null
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
    public @NonNull SequencedCollection<V> values() {
        return new WrappedSequencedCollection<>(
                () -> new MappedIterator<>(iteratorFunction.get(), Map.Entry::getValue),
                sizeFunction,
                this::containsKey,
                clearFunction,
                this::removeEntry,
                () -> firstEntry().getValue(),
                () -> lastEntry().getValue(), null, null
        );
    }
}
