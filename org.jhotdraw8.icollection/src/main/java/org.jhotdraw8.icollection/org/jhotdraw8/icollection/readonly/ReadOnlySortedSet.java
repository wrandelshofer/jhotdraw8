package org.jhotdraw8.icollection.readonly;

import org.jhotdraw8.annotation.Nullable;

import java.util.Comparator;

/**
 * A read-only interface for a sorted set. A sorted set is a set that  provides a total ordering on its elements.
 *
 * @param <E> the element type
 */
public interface ReadOnlySortedSet<E> extends ReadOnlySequencedSet<E> {
    /**
     * Returns the comparator used to order the elements in this set, or null if this set uses
     * the natural ordering of its elements.
     *
     * @return comparator or null
     */
    @Nullable
    Comparator<? super E> comparator();

}
