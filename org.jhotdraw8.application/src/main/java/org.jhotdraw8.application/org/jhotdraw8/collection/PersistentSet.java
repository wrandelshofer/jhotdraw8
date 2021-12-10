package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;

/**
 * Provides an API for a persistent set.
 * <p>
 * A persistent set provides methods for creating a new persistent set with
 * added or removed elements, without changing the original persistent set.
 * <p>
 * Implementations are expected to only require time and space that is
 * proportional to the differences (also known as 'delta')
 * between the newly created persistent set to the original persistent set.
 */
public interface PersistentSet<E> extends ReadOnlySet<E>, InsertOnlyPersistentSet<E> {
    /**
     * Returns a persistent set that contains all elements
     * of this set and also the specified element.
     *
     * @param element an element
     * @return the same set if it already contains the element, or
     * a different set with the element added
     */
    @NonNull PersistentSet<E> withAdd(@NonNull E element);

    /**
     * Returns a persistent set that contains all elements
     * of this set and also all elements of the specified
     * collection.
     *
     * @param c a collection to be added to this set
     * @return the same set if it already contains the elements, or
     * a different set with the elements added
     */
    @NonNull PersistentSet<E> withAddAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns a persistent set that contains all elements
     * of this set except the specified element.
     *
     * @param element an element
     * @return the same set if it already does not contain the element, or
     * a different set with the element removed
     */
    @NonNull PersistentSet<E> withRemove(@NonNull E element);

    /**
     * Returns a persistent set that contains all elements
     * of this set except the elements of the specified
     * collection.
     *
     * @param c a collection to removed from this set
     * @return the same set if it already does not contain the elements, or
     * a different set with the elements removed
     */
    @NonNull PersistentSet<E> withRemoveAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns a persistent set that contains only elements
     * that are in this set and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return the same set if it has not changed, or
     * a different set with elements removed
     */
    @NonNull PersistentSet<E> withRetainAll(@NonNull Collection<? extends E> c);
}
