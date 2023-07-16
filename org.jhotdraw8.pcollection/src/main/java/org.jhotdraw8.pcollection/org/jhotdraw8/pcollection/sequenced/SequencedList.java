package org.jhotdraw8.pcollection.sequenced;

import org.jhotdraw8.annotation.NonNull;

import java.util.List;

/**
 * Interface for a list with a well-defined iteration order.
 * <p>
 * References:
 * <dl>
 *     <dt>JEP draft: Sequenced Collections</dt>
 *     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public interface SequencedList<E> extends List<E>, SequencedCollection<E> {
    /**
     * Returns a reversed-order view of this list.
     * <p>
     * Modifications write through to the underlying collection.
     * Changes to the underlying collection are visible in the reversed view.
     *
     * @return a reversed-order view of this list
     */
    @NonNull SequencedList<E> _reversed();
}
