package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.icollection.readonly.ReadOnlyNavigableSet;

/**
 * An interface to an immutable navigable set; the implementation guarantees that the state of the collection does not change.
 *
 * @param <E> the element type
 */
public interface ImmutableNavigableSet<E> extends ReadOnlyNavigableSet<E>, ImmutableSortedSet<E> {
}
