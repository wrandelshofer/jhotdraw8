package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Collection;
import java.util.Map;

public interface ImmutableSequencedMap<K, V> extends ImmutableMap<K, V>, ReadOnlySequencedMap<K, V> {
    @Override
    @NonNull ImmutableSequencedMap<K, V> copyClear();

    @Override
    @NonNull ImmutableSequencedMap<K, V> copyPut(@NonNull K key, @Nullable V value);

    @Override
    @NonNull
    default ImmutableSequencedMap<K, V> copyPutAll(@NonNull Map<? extends K, ? extends V> m) {
        return (ImmutableSequencedMap<K, V>) ImmutableMap.super.copyPutAll(m);
    }

    @Override
    @NonNull ImmutableSequencedMap<K, V> copyPutAll(@NonNull ImmutableMap<? extends K, ? extends V> m);

    @Override
    @NonNull ImmutableSequencedMap<K, V> copyPutAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> m);

    @Override
    @NonNull
    default ImmutableSequencedMap<K, V> copyPutAll(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (ImmutableSequencedMap<K, V>) ImmutableMap.super.copyPutAll(map);
    }

    @Override
    @NonNull
    default ImmutableSequencedMap<K, V> copyPutKeyValues(@NonNull Object @NonNull ... kv) {
        return (ImmutableSequencedMap<K, V>) ImmutableMap.super.copyPutKeyValues(kv);
    }

    @Override
    @NonNull ImmutableSequencedMap<K, V> copyRemove(@NonNull K key);

    @Override
    @NonNull ImmutableSequencedMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c);

    /**
     * Returns a copy of this map that contains all entries
     * of this map except the first.
     *
     * @return this map instance if it is already empty, or
     * a different map instance with the first entry removed
     */
    default @NonNull ImmutableSequencedMap<K, V> copyRemoveFirst() {
        return isEmpty() ? this : copyRemove(firstKey());
    }

    /**
     * Returns a copy of this map that contains all entries
     * of this map except the last.
     *
     * @return this map instance if it is already empty, or
     * a different map instance with the last entry removed
     */
    default @NonNull ImmutableSequencedMap<K, V> copyRemoveLast() {
        return isEmpty() ? this : copyRemove(lastKey());
    }

    @Override
    @NonNull ImmutableSequencedMap<K, V> copyRetainAll(@NonNull Collection<? extends K> c);

    @Override
    @NonNull
    default ImmutableSequencedMap<K, V> copyRetainAll(@NonNull ReadOnlyCollection<? extends K> c) {
        return (ImmutableSequencedMap<K, V>) ImmutableMap.super.copyRetainAll(c);
    }

    @Override
    @NonNull Map<K, V> toMutable();
}
