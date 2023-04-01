/*
 * @(#)ImmutableSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySet;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for an immutable set.
 * <p>
 * An immutable set provides methods for creating a new immutable set with
 * added or removed elements, without changing the original immutable set.
 *
 * @param <E> the element type
 */
public interface ImmutableSet<E> extends ReadOnlySet<E>, ImmutableCollection<E> {
    /**
     * Returns a copy of this set that is empty.
     *
     * @return this set instance if it is already empty, or a different set
     * instance that is empty.
     */
    @NonNull ImmutableSet<E> clear();

    /**
     * Returns a copy of this set that contains all elements
     * of this set and also the specified element.
     *
     * @param element an element
     * @return this set instance if it already contains the element, or
     * a different set instance with the element added
     */
    @NonNull ImmutableSet<E> add(E element);

    /**
     * Returns a copy of this set that contains all elements
     * of this set and also all elements of the specified
     * collection.
     *
     * @param c a collection to be added to this set
     * @return this set instance if it already contains the elements, or
     * a different set instance with the elements added
     */
    @NonNull ImmutableSet<E> addAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the specified element.
     *
     * @param element an element
     * @return this set instance if it already does not contain the element, or
     * a different set instance with the element removed
     */
    @NonNull ImmutableSet<E> remove(E element);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the elements of the specified
     * collection.
     *
     * @param c a collection with elements to be removed from this set
     * @return this set instance if it already does not contain the elements, or
     * a different set instance with the elements removed
     */
    @NonNull ImmutableSet<E> removeAll(@NonNull Iterable<?> c);

    /**
     * Returns a copy of this set that contains only elements
     * that are in this set and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this set instance if it has not changed, or
     * a different set instance with elements removed
     */
    @NonNull ImmutableSet<E> retainAll(@NonNull Collection<?> c);

    /**
     * Returns a copy of this set that contains only elements
     * that are in this set and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this set instance if it has not changed, or
     * a different set instance with elements removed
     */
    default @NonNull ImmutableSet<E> retainAll(final @NonNull ReadOnlyCollection<?> c) {
        if (c == this) {
            return this;
        }
        return retainAll(c.asCollection());
    }

    /**
     * Returns a mutable copy of this set.
     *
     * @return a mutable copy.
     */
    @NonNull Set<E> toMutable();

}
