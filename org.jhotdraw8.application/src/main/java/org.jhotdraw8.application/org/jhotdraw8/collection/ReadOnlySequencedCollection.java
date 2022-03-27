package org.jhotdraw8.collection;

/**
 * A collection with a well-defined linear ordering of its elements.
 *
 * @param <E> the element type
 */
public interface ReadOnlySequencedCollection<E> extends ReadOnlyCollection<E> {
    /**
     * Gets the first element.
     *
     * @return an element
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    E getFirst();

    /**
     * Gets the last element.
     *
     * @return an element
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    E getLast();
}
