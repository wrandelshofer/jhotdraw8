/*
 * @(#)AbstractChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Abstract base class for CHAMP maps.
 *
 * @param <K> the key type of the map
 * @param <V> the value typeof the map
 */
public abstract class AbstractMutableChampMap<K, V> extends AbstractMap<K, V> implements Serializable, Cloneable,
        ReadableMap<K, V> {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * The current owner id of this map.
     * <p>
     * All nodes that have the same non-null owner id, are exclusively owned
     * by this map, and therefore can be mutated without affecting other map.
     * <p>
     * If this owner id is null, then this map does not own any nodes.
     */
    @Nullable
    protected IdentityObject owner;

    /**
     * The root of this CHAMP trie.
     */
    protected transient BitmapIndexedNode<K, V> root;

    /**
     * The number of entries in this map.
     */
    protected int size;

    /**
     * The number of times this map has been structurally modified.
     */
    protected int modCount;

    /**
     * Gets the owner object. If the owner object was set to null, creates a new one.
     *
     * @return the owner object
     */
    protected IdentityObject getOrCreateOwner() {
        if (owner == null) {
            owner = new IdentityObject();
        }
        return owner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractMutableChampMap<K, V> clone() {
        try {
            owner = null;
            return (AbstractMutableChampMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof AbstractMutableChampMap<?, ?> that) {
            return size == that.size && root.equivalent(that.root);
        }
        return super.equals(o);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the current value of the modification counter.
     *
     * @return value of modification counter
     */
    protected int getModCount() {
        return modCount;
    }

    /**
     * Adds all specified elements that are not already in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
     */
    public boolean putAll(Iterable<? extends Entry<? extends K, ? extends V>> c) {
        if (c == this) {
            return false;
        }
        boolean modified = false;
        for (var e : c) {
            var value = e.getValue();
            if (value == null && !containsKey(e.getKey())) {
                modified = true;
            }
            var oldValue = put(e.getKey(), value);
            modified = modified || !Objects.equals(oldValue, value);
        }
        return modified;
    }

    /**
     * Removes all specified entries that are in this set.
     *
     * @param c an iterable of keys
     * @return {@code true} if this map changed
     */
    public boolean removeAll(Iterable<?> c) {
        if (isEmpty()) {
            return false;
        }
        boolean modified = false;
        for (Object o : c) {
            if (containsKey(o)) {
                remove(o);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Retains all specified entries that are in this set.
     *
     * @param c an iterable of keys
     * @return {@code true} if this map changed
     */
    public boolean retainAll(Iterable<?> c) {
        if (isEmpty()) {
            return false;
        }
        if ((c instanceof Collection<?> cc && cc.isEmpty())
                || (c instanceof ReadableCollection<?> rc) && rc.isEmpty()) {
            clear();
            return true;
        }
        Predicate<K> predicate;
        if (c instanceof Collection<?> that) {
            predicate = that::contains;
        } else if (c instanceof ReadableCollection<?> that) {
            predicate = that::contains;
        } else {
            HashSet<Object> that = new HashSet<>();
            c.forEach(that::add);
            predicate = that::contains;
        }
        boolean removed = false;
        for (Iterator<Entry<K, V>> i = iterator(); i.hasNext(); ) {
            var e = i.next();
            if (!predicate.test(e.getKey())) {
                i.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Removes the specified entry from the map.
     *
     * @param o an entry (should be a {@link Entry}).
     * @return true if the element was contained in the map
     */
    @SuppressWarnings("unchecked")
    protected boolean removeEntry(@Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            remove(((Entry<K, V>) o).getKey());
            return true;
        }
        return false;
    }
}
