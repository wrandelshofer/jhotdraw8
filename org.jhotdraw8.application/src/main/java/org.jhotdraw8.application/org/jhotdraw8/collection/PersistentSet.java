package org.jhotdraw8.collection;

import java.util.Collection;

/**
 * Provides methods for creating a new set with an added or removed element.
 */
public interface PersistentSet<E> extends ReadOnlySet<E> {
    /**
     * Returns a persistent set that contains all elements
     * of this set and also the specified element.
     *
     * @param element an element
     * @return the same set if it already contains the element or
     * a different set with the element added
     */
    PersistentSet<E> withAdd(E element);

    /**
     * Returns a persistent set that contains all elements
     * of this set and also all elements of the specified
     * collection.
     *
     * @param c a collection to be added to this set
     * @return the same set if it already contains the elements or
     * a different set with the elements added
     */
    PersistentSet<E> withAddAll(Iterable<? extends E> c);

    /**
     * Returns a persistent set that contains all elements
     * of this set except the specified element.
     *
     * @param element an element
     * @return the same set if it already does not contain the element or
     * a different set with the element removed
     */
    PersistentSet<E> withRemove(E element);

    /**
     * Returns a persistent set that contains all elements
     * of this set except the elements of the specified
     * collection.
     *
     * @param c a collection to removed from this set
     * @return the same set if it already does not contain the elements or
     * a different set with the elements removed
     */
    PersistentSet<E> withRemoveAll(Collection<? extends E> c);

    /**
     * Returns a persistent set that contains only elements
     * that are in this set and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return the same set if it has not changed or
     * a different set with elements removed
     */
    PersistentSet<E> withRetainAll(Collection<? extends E> c);
}
