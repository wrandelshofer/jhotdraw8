/*
 * @(#)ImmutableSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for an immutable set.
 * <p>
 * A persistent set provides methods for creating a new persistent set with
 * added or removed elements, without changing the original persistent set.
 * <p>
 * Implementations are expected to only require time and space that is
 * proportional to the differences between the newly created persistent set to
 * the original persistent set.
 */
public interface ImmutableSet<E> extends ReadOnlySet<E>, ImmutableCollection<E> {
    /**
     * Returns a copy of this set that is empty.
     *
     * @return this set instance if it is already empty, or a different set
     * instance that is empty.
     */
    @NonNull ImmutableSet<E> copyClear();

    /**
     * Returns a copy of this set that contains all elements
     * of this set and also the specified element.
     *
     * @param element an element
     * @return this set instance if it already contains the element, or
     * a different set instance with the element added
     */
    @NonNull ImmutableSet<E> copyAdd(E element);

    /**
     * Returns a copy of this set that contains all elements
     * of this set and also all elements of the specified
     * collection.
     *
     * @param c a collection to be added to this set
     * @return this set instance if it already contains the elements, or
     * a different set instance with the elements added
     */
    @NonNull ImmutableSet<E> copyAddAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the specified element.
     *
     * @param element an element
     * @return this set instance if it already does not contain the element, or
     * a different set instance with the element removed
     */
    @NonNull ImmutableSet<E> copyRemove(E element);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the elements of the specified
     * collection.
     *
     * @param c a collection with elements to be removed from this set
     * @return this set instance if it already does not contain the elements, or
     * a different set instance with the elements removed
     */
    @NonNull ImmutableSet<E> copyRemoveAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns a copy of this set that contains only elements
     * that are in this set and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this set instance if it has not changed, or
     * a different set instance with elements removed
     */
    @NonNull ImmutableSet<E> copyRetainAll(@NonNull Collection<? extends E> c);

    /**
     * Returns a copy of this set that contains only elements
     * that are in this set and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this set instance if it has not changed, or
     * a different set instance with elements removed
     */
    default @NonNull ImmutableSet<E> copyRetainAll(final @NonNull ReadOnlyCollection<? extends E> c) {
        if (c == this) {
            return this;
        }
        return copyRetainAll(c.asCollection());
    }

    /**
     * Returns a mutable copy of this set.
     *
     * @return a mutable copy.
     */
    @NonNull Set<E> toMutable();

}
