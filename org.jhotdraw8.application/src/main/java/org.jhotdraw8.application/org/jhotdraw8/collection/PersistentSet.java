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
 * proportional to the differences between the newly created persistent set to
 * the original persistent set.
 */
public interface PersistentSet<E> extends ImmutableSet<E> {
    /**
     * Returns a copy of this set that is empty.
     *
     * @param element an element
     * @return this set instance if it is already empty, or a different set
     * instance that is empty.
     */
    @NonNull PersistentSet<E> copyClear(@NonNull E element);

    /**
     * Returns a copy of this set that contains all elements
     * of this set and also the specified element.
     *
     * @param element an element
     * @return this set instance if it already contains the element, or
     * a different set instance with the element added
     */
    @NonNull PersistentSet<E> copyAdd(@NonNull E element);

    /**
     * Returns a copy of this set that contains all elements
     * of this set and also all elements of the specified
     * collection.
     *
     * @param c a collection to be added to this set
     * @return this set instance if it already contains the elements, or
     * a different set instance with the elements added
     */
    @NonNull PersistentSet<E> copyAddAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the specified element.
     *
     * @param element an element
     * @return this set instance if it already does not contain the element, or
     * a different set instance with the element removed
     */
    @NonNull PersistentSet<E> copyRemove(@NonNull E element);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the elements of the specified
     * collection.
     *
     * @param c a collection with elements to be removed from this set
     * @return this set instance if it already does not contain the elements, or
     * a different set instance with the elements removed
     */
    @NonNull PersistentSet<E> copyRemoveAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns a copy of this set that contains only elements
     * that are in this set and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this set instance if it has not changed, or
     * a different set instance with elements removed
     */
    @NonNull PersistentSet<E> copyRetainAll(@NonNull Collection<? extends E> c);

    /**
     * Returns a copy of this set that contains only elements
     * that are in this set and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this set instance if it has not changed, or
     * a different set instance with elements removed
     */
    default @NonNull PersistentSet<E> copyRetainAll(final @NonNull ReadOnlyCollection<? extends E> c) {
        if (c == this) {
            return this;
        }
        return copyRetainAll(c.asCollection());
    }


}
