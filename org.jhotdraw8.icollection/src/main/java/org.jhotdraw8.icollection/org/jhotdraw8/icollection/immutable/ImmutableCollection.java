/*
 * @(#)ImmutableCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;

import java.util.Collection;

/**
 * An interface to an immutable collection; the implementation
 * guarantees that the state of the collection does not change.
 *
 * @param <E> the element type
 */
public interface ImmutableCollection<E> extends ReadOnlyCollection<E> {
    /**
     * Returns a copy of this collection that contains all elements
     * of this collection and also the specified element.
     * <p>
     * A collection may prevent that the same element can be
     * added more than once.
     *
     * @param element an element
     * @return this collection instance if it already contains the element, or
     * a different collection instance with the element added
     */
    @NonNull ImmutableCollection<E> add(E element);

    /**
     * Returns a copy of this collection that contains all elements
     * of this collection and also all elements of the specified
     * collection.
     * <p>
     * A collection may prevent that the same element can be
     * added more than once.
     *
     * @param c a collection to be added to this collection
     * @return this collection instance if it already contains the elements, or
     * a different collection instance with the elements added
     */
    @SuppressWarnings("unchecked")
    @NonNull ImmutableCollection<E> addAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns an empty collection instance that has the specified
     * element type.
     *
     * @param <T> the element type of the returned collection
     * @return an empty collection of the specified element type.
     */
    @NonNull <T> ImmutableCollection<T> empty();

    /*
     * Retains all elements in this collection that satisfy the specified predicate.
     *
     * @param p a predicate
     * @return a collection that only contains elements that satisfy the predicate
     *
     * /
    default ImmutableCollection<E> filter(@NonNull Predicate<E> p) {
        ImmutableCollection<E> result = this.<E>empty();
        for(E e:this){
            if (p.test(e))result=result.add(e);
        }
        return result;
    }*/

    /**
     * Returns the maximal number of elements that this collection type can
     * hold
     *
     * @return the maximal size
     */
    int maxSize();

    /**
     * Returns a copy of this collection that contains all elements
     * of this collection except the specified element.
     *
     * @param element an element
     * @return this collection instance if it already does not contain the element, or
     * a different collection instance with the element removed
     */
    @NonNull ImmutableCollection<E> remove(E element);

    /**
     * Returns a copy of this collection that contains all elements
     * of this collection except the elements of the specified
     * collection.
     *
     * @param c a collection with elements to be removed from this collection
     * @return this collection instance if it already does not contain the elements, or
     * a different collection instance with the elements removed
     */
    @SuppressWarnings("unchecked")
    @NonNull ImmutableCollection<E> removeAll(@NonNull Iterable<?> c);

    /**
     * Returns a copy of this collection that contains only elements
     * that are in this collection and in the specified collection.
     *
     * @param c a collection with elements to be retained in this collection
     * @return this collection instance if it has not changed, or
     * a different collection instance with elements removed
     */
    @SuppressWarnings("unchecked")
    @NonNull ImmutableCollection<E> retainAll(@NonNull Iterable<?> c);

    /**
     * Returns a mutable copy of this collection.
     *
     * @return a mutable copy.
     */
    @NonNull Collection<E> toMutable();
}
