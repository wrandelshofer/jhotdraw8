package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class WrappedImmutableMap<K, V> extends AbstractReadOnlyMap<K, V> implements ImmutableMap<K, V> {

    private final @NonNull Map<K, V> target;
    private final @NonNull Function<Map<K, V>, Map<K, V>> cloneFunction;

    public WrappedImmutableMap(@NonNull Map<K, V> target, @NonNull Function<Map<K, V>, Map<K, V>> cloneFunction) {
        this.target = target;
        this.cloneFunction = cloneFunction;
    }

    @Override
    public @NonNull WrappedImmutableMap<K, V> copyClear() {
        Map<K, V> clone = cloneFunction.apply(target);
        clone.clear();
        return new WrappedImmutableMap<>(clone, cloneFunction);
    }

    @Override
    public @NonNull WrappedImmutableMap<K, V> copyPut(@NonNull K key, @Nullable V value) {
        Map<K, V> clone = cloneFunction.apply(target);
        clone.put(key, value);
        return new WrappedImmutableMap<>(clone, cloneFunction);
    }

    @Override
    public @NonNull WrappedImmutableMap<K, V> copyPutAll(@NonNull ImmutableMap<? extends K, ? extends V> m) {
        Map<K, V> clone = cloneFunction.apply(target);
        clone.putAll(m.asMap());
        return new WrappedImmutableMap<>(clone, cloneFunction);
    }

    @Override
    public @NonNull WrappedImmutableMap<K, V> copyPutAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> m) {
        Map<K, V> clone = cloneFunction.apply(target);
        for (Map.Entry<? extends K, ? extends V> e : m) {
            clone.put(e.getKey(), e.getValue());
        }
        return new WrappedImmutableMap<>(clone, cloneFunction);
    }

    @Override
    public @NonNull WrappedImmutableMap<K, V> copyRemove(@NonNull K key) {
        Map<K, V> clone = cloneFunction.apply(target);
        clone.remove(key);
        return new WrappedImmutableMap<>(clone, cloneFunction);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public @NonNull WrappedImmutableMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c) {
        Map<K, V> clone = cloneFunction.apply(target);
        if (c instanceof Collection<?>) {
            clone.keySet().removeAll((Collection<?>) c);
        } else {
            for (K k : c) {
                clone.remove(k);
            }
        }
        return new WrappedImmutableMap<>(clone, cloneFunction);
    }

    @Override
    public @NonNull WrappedImmutableMap<K, V> copyRetainAll(@NonNull Collection<? extends K> c) {
        Map<K, V> clone = cloneFunction.apply(target);
        clone.keySet().retainAll(c);
        return new WrappedImmutableMap<>(clone, cloneFunction);
    }

    @Override
    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return Iterators.unmodifiableIterator(target.entrySet().iterator());
    }

    @Override
    public int size() {
        return target.size();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public @Nullable V get(Object key) {
        return target.get(key);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean containsKey(@Nullable Object key) {
        return target.containsKey(key);
    }

    @Override
    public @NonNull Map<K, V> toMutable() {
        return cloneFunction.apply(target);
    }
}
