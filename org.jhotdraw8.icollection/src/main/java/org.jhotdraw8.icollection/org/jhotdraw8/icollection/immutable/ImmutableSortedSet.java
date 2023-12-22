package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.icollection.readonly.ReadOnlySortedSet;

/**
 * An interface to an immutable sorted set; the implementation guarantees that the state of the collection does not change.
 *
 * @param <E> the element type
 */
public interface ImmutableSortedSet<E> extends ReadOnlySortedSet<E>, ImmutableSet<E> {
}
