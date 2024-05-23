package org.jhotdraw8.icollection.readonly;

import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * A read-only interface to a sorted set. A sorted set is a set that  provides a total ordering on its elements.
 *
 * @param <E> the element type
 */
public interface ReadOnlySortedSet<E> extends ReadOnlySequencedSet<E> {
    /**
     * Returns the comparator used to order the elements in this set, or {@code null} if this set uses
     * the natural ordering of its elements.
     *
     * @return comparator or null
     */
    @Nullable
    Comparator<? super E> comparator();

}
