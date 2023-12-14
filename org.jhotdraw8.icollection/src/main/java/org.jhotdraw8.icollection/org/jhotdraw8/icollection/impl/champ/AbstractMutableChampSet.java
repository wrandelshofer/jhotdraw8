/*
 * @(#)AbstractMutableChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Abstract base class for CHAMP sets.
 *
 * @param <E> the element type of the set
 * @param <D> the data type of the CHAMP trie
 */
public abstract class AbstractMutableChampSet<E, D> extends AbstractSet<E> implements Serializable, Cloneable,
        ReadOnlySet<E> {
    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * The current owner id of this set.
     * <p>
     * All nodes that have the same non-null owner id, are exclusively owned
     * by this set, and therefore can be mutated without affecting other sets.
     * <p>
     * If this owner id is null, then this set does not own any nodes.
     */
    protected @Nullable IdentityObject owner;

    /**
     * The root of this CHAMP trie.
     */
    protected BitmapIndexedNode<D> root;

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
    public boolean addAll(@NonNull Iterable<? extends E> c) {
        boolean added = false;
        for (E e : c) {
            added |= add(e);
        }
        return added;
    }

    /**
     * Retains all specified elements that are in this set.
     *
     * @param c an iterable of elements
     * @return {@code true} if this set changed
     */
    public boolean retainAll(@NonNull Iterable<?> c) {
        if (c == this || isEmpty()) {
            return false;
        }
        if ((c instanceof Collection<?> cc && cc.isEmpty())
                || (c instanceof ReadOnlyCollection<?> rc) && rc.isEmpty()) {
            clear();
            return true;
        }
        Predicate<E> predicate;
        if (c instanceof Collection<?> that) {
            predicate = that::contains;
        } else if (c instanceof ReadOnlyCollection<?> that) {
            predicate = that::contains;
        } else {
            HashSet<Object> that = new HashSet<>();
            c.forEach(that::add);
            predicate = that::contains;
        }
        boolean removed = false;
        for (Iterator<E> i = iterator(); i.hasNext(); ) {
            E e = i.next();
            if (!predicate.test(e)) {
                i.remove();
                removed = true;
            }
        }
        return removed;
    }

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

    /**
     * Returns the current value of the modification counter.
     *
     * @return value of modification counter
     */
    protected int getModCount() {
        return modCount;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Gets the owner id of this set. Creates a new id, if this
     * set has no owner id.
     *
     * @return a new unique id or the existing unique id.
     */
    @NonNull
    protected IdentityObject makeOwner() {
        if (owner == null) {
            owner = new IdentityObject();
        }
        return owner;
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
    public @NonNull AbstractMutableChampSet<E, D> clone() {
        try {
            owner = null;
            return (AbstractMutableChampSet<E, D>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
