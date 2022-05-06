package org.jhotdraw8.collection;

import java.util.Set;

/**
 * Interface for a set with a well-defined linear ordering of its elements.
 * <p>
 * References:
 * <dl>
 *     <dt>JEP draft: Sequenced Collections</dt>
 *     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public interface SequencedSet<E> extends Set<E>, SequencedCollection<E> {
    boolean addFirst(E e);

    boolean addLast(E e);

    E removeFirst();

    E removeLast();

    @Override
    default Object[] toArray() {
        return SequencedCollection.super.toArray();
    }

    @Override
    default <T> T[] toArray(T[] a) {
        return SequencedCollection.super.toArray(a);
    }

    @Override
    default boolean isEmpty() {
        return SequencedCollection.super.isEmpty();
    }
}
