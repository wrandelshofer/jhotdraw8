
package org.jhotdraw8.collection;

import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Interface for a {@link Deque} with a primitive long data elements.
 */
public interface LongDeque extends Deque<Long> {
    @Override
    default boolean add(Long integer) {
        addLastAsLong(integer);
        return true;
    }

    @Override
    default void addFirst(Long integer) {
        addFirstAsLong(integer);
    }

    /**
     * @see Deque#addFirst(Object)
     */
    void addFirstAsLong(long e);

    @Override
    default void addLast(Long integer) {
        addLastAsLong(integer);
    }

    /**
     * @see Deque#addLast(Object)
     */
    void addLastAsLong(long e);

    @Override
    default boolean contains(Object o) {
        if (o instanceof Long) {
            return firstIndexOfAsLong((int) o) != -1;
        }
        return false;
    }

    @Override
    default Long element() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return getFirstAsLong();
    }

    /**
     * Returns the first index of the specified element
     * or -1 if this deque does not contain the element.
     */
    int firstIndexOfAsLong(long o);

    @Override
    default Long getFirst() {
        return getFirstAsLong();
    }

    /**
     * @see Deque#getFirst()
     */
    long getFirstAsLong();

    @Override
    default Long getLast() {
        return getLastAsLong();
    }

    /**
     * @see Deque#getLast()
     */
    long getLastAsLong();

    /**
     * Returns the last index of the specified element
     * or -1 if this deque does not contain the element.
     */
    int lastIndexOfAsLong(long o);

    @Override
    default boolean offer(Long integer) {
        addLastAsLong(integer);
        return true;
    }

    @Override
    default boolean offerFirst(Long integer) {
        addFirstAsLong(integer);
        return true;
    }

    @Override
    default boolean offerLast(Long integer) {
        addLastAsLong(integer);
        return true;
    }

    @Override
    default Long peek() {
        if (isEmpty()) {
            return null;
        }
        return getFirstAsLong();
    }

    @Override
    default Long peekFirst() {
        if (isEmpty()) {
            return null;
        }
        return getFirstAsLong();
    }

    @Override
    default Long peekLast() {
        if (isEmpty()) {
            return null;
        }
        return getLastAsLong();
    }

    @Override
    default Long poll() {
        if (isEmpty()) {
            return null;
        }
        return removeFirstAsLong();
    }

    @Override
    default Long pollFirst() {
        if (isEmpty()) {
            return null;
        }
        return removeFirstAsLong();
    }

    @Override
    default Long pollLast() {
        if (isEmpty()) {
            return null;
        }
        return removeLastAsLong();
    }

    @Override
    default Long pop() {
        return removeFirstAsLong();
    }

    /**
     * @see Deque#pop()
     */
    default long popAsLong() {
        return removeFirstAsLong();
    }

    @Override
    default void push(Long integer) {
        addFirstAsLong(integer);
    }

    /**
     * @see Deque#push(Object)
     */
    default void pushAsLong(long e) {
        addFirstAsLong(e);
    }

    @Override
    default boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    default Long remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return removeFirstAsLong();
    }

    @Override
    default Long removeFirst() {
        return removeFirstAsLong();
    }

    /**
     * @see Deque#removeFirst()
     */
    long removeFirstAsLong();

    @Override
    default boolean removeFirstOccurrence(Object o) {
        if (o instanceof Long) {
            return removeFirstOccurrenceAsLong((int) o);
        }
        return false;
    }

    /**
     * @see Deque#removeFirstOccurrence(Object)
     */
    boolean removeFirstOccurrenceAsLong(long o);

    @Override
    default Long removeLast() {
        return removeLastAsLong();
    }

    /**
     * @see Deque#removeLast()
     */
    long removeLastAsLong();


    @Override
    default boolean removeLastOccurrence(Object o) {
        if (o instanceof Long) {
            return removeLastOccurrenceAsLong((long) o);
        }
        return false;
    }

    boolean removeLastOccurrenceAsLong(long o);
}
