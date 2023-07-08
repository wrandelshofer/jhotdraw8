/*
 * @(#)ImmutableMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.pcollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.pcollection.readonly.ReadOnlyMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Interface for an immutable map; the implementation guarantees that the state
 * of the map does not change.
 * <p>
 * An immutable map provides methods for creating a new immutable map with
 * new, updated or deleted entries, without changing the original immutable map.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ImmutableMap<K, V> extends ReadOnlyMap<K, V> {

    /**
     * Returns a copy of this map that is empty.
     *
     * @return this set instance if it is already empty, or a different set
     * instance that is empty.
     */
    @NonNull ImmutableMap<K, V> clear();

    /**
     * Returns a copy of this map that contains all entries
     * of this map with the specified entry added or updated.
     *
     * @param key   the key of the entry
     * @param value the value of the entry
     * @return this map instance if it already contains the same entry, or
     * a different map instance with the entry added or updated
     */
    @NonNull ImmutableMap<K, V> put(@NonNull K key, @Nullable V value);

    /**
     * Returns a copy of this map that contains all entries
     * of this map with entries from the specified map added or updated.
     *
     * @param m another map
     * @return this map instance if it already contains the same entries, or
     * a different map instance with the entries added or updated
     */
    default @NonNull ImmutableMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return putAll(m.entrySet());
    }

    /**
     * Returns a copy of this map that contains all entries
     * of this map with entries from the specified map added or updated.
     *
     * @param c another map
     * @return this map instance if it already contains the same entries, or
     * a different map instance with the entries added or updated
     */
    @SuppressWarnings("unchecked")
    default @NonNull ImmutableMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        if (c instanceof Collection<?> co && co.isEmpty()
                || c instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            return this;
        }
        if (isEmpty() && c.getClass() == this.getClass()) {
            return (ImmutableMap<K, V>) c;
        }
        var s = this;
        for (var e : c) {
            s = s.put(e.getKey(), e.getValue());
        }
        return s;
    }

    /**
     * Returns a copy of this map that contains all entries
     * of this map with entries from the specified map added or updated.
     *
     * @param kv a list of alternating keys and values
     * @return this map instance if it already contains the same entries, or
     * a different map instance with the entries added or updated
     */
    @SuppressWarnings("unchecked")
    default @NonNull ImmutableMap<K, V> putKeyValues(@NonNull Object @NonNull ... kv) {
        ImmutableMap<K, V> that = this;
        for (int i = 0; i < kv.length; i += 2) {
            that = that.put((K) kv[i], (V) kv[i + 1]);
        }
        return that;
    }


    /**
     * Returns a copy of this map that contains all entries
     * of this map with the specified entry removed.
     *
     * @param key the key of the entry
     * @return this map instance if it already does not contain the entry, or
     * a different map instance with the entry removed
     */
    @NonNull ImmutableMap<K, V> remove(@NonNull K key);

    /**
     * Returns a copy of this map that contains all entries
     * of this map except the entries of the specified
     * collection.
     *
     * @param c a collection with keys of entries to be removed from this map
     * @return this map instance if it already does not contain the entries, or
     * a different map instance with the entries removed
     */
    default @NonNull ImmutableMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        if (isEmpty()
                || c instanceof Collection<?> co && co.isEmpty()
                || c instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            return this;
        }
        var s = this;
        for (var k : c) {
            s = s.remove(k);
        }
        return s;
    }

    /**
     * Returns a copy of this map that contains only entries
     * that are in this map and in the specified collection.
     *
     * @param c a collection with keys of entries to be retained in this map
     * @return this map instance if it has not changed, or
     * a different map instance with entries removed
     */
    @SuppressWarnings("unchecked")
    default @NonNull ImmutableMap<K, V> retainAll(@NonNull Iterable<? extends K> c) {
        if (isEmpty()
                || c instanceof Collection<?> co && co.isEmpty()
                || c instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            return this;
        }
        if (c instanceof ReadOnlyCollection<?> co) {
            if (co.isEmpty()) {
                return clear();
            }
            var s = this;
            for (var e : this) {
                if (!co.contains(e)) {
                    s = s.remove((K) e);
                }
            }
            return s;
        }
        if (!(c instanceof Collection<?>)) {
            HashSet<K> hm = new HashSet<>();
            c.forEach(hm::add);
            c = hm;
        }
        var cc = (Collection<?>) c;
        if (cc.isEmpty()) {
            return clear();
        }
        var s = this;
        for (var e : this) {
            if (!cc.contains(e)) {
                s = s.remove((K) e);
            }
        }
        return s;
    }

    /**
     * Returns a copy of this map that contains only entries
     * that are in this map and in the specified collection.
     *
     * @param c a collection with keys of entries to be retained in this map
     * @return this map instance if it has not changed, or
     * a different map instance with entries removed
     */
    default @NonNull ImmutableMap<K, V> retainAll(@NonNull ReadOnlyCollection<? extends K> c) {
        return retainAll(c.asCollection());
    }

    /**
     * Returns a mutable copy of this map.
     *
     * @return a mutable copy.
     */
    @NonNull Map<K, V> toMutable();

}
