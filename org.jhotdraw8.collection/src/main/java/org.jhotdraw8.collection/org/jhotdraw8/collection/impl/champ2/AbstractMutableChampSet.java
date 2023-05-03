/*
 * @(#)AbstractMutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ2;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.ReadOnlySet;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Abstract base class for CHAMP sets.
 *
 * @param <E> the element type of the set
 * @param <X> the key type of the CHAMP trie
 */
public abstract class AbstractMutableChampSet<E, X> extends AbstractSet<E> implements Serializable, Cloneable,
        ReadOnlySet<E> {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * The root of this CHAMP trie.
     */
    protected BitmapIndexedNode<X> root;

    /**
     * The number of elements in this set.
     */
    protected int size;

    /**
     * The number of times this set has been structurally modified.
     */
    protected transient int modCount;

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        return addAll((Iterable<? extends E>) c);
    }

    /**
     * Adds all specified elements that are not already in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
     */
    @SuppressWarnings("unchecked")
    public abstract boolean addAll(@NonNull Iterable<? extends E> c);

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof AbstractMutableChampSet<?, ?>) {
            AbstractMutableChampSet<?, ?> that = (AbstractMutableChampSet<?, ?>) o;
            return size == that.size && root.equivalent(that.root);
        }
        return super.equals(o);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return removeAll((Iterable<?>) c);
    }

    /**
     * Removes all specified elements that are in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
     */
    public boolean removeAll(@NonNull Iterable<?> c) {
        if (isEmpty()) {
            return false;
        }
        if (c == this) {
            clear();
            return true;
        }
        boolean modified = false;
        for (Object o : c) {
            modified |= remove(o);
        }
        return modified;
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull AbstractMutableChampSet<E, X> clone() {
        try {
            return (AbstractMutableChampSet<E, X>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
