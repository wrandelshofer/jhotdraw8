/*
 * @(#)ReadOnlySequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.readonly;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.facade.SequencedSetFacade;
import org.jhotdraw8.icollection.sequenced.SequencedSet;

/**
 * Read-only interface for a set with a well-defined iteration order.
 * The state of the set may change.
 * <p>
 * References:
 * <dl>
 *     <dt>JEP draft: Sequenced Collections</dt>
 *     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public interface ReadOnlySequencedSet<E> extends ReadOnlySet<E>, ReadOnlySequencedCollection<E> {
    /**
     * Returns a reversed-order view of this set.
     * Changes to the underlying set are visible in the reversed view.
     *
     * @return a reversed-order view of this set
     */
    @NonNull ReadOnlySequencedSet<E> readOnlyReversed();

    @Override
    default @NonNull SequencedSet<E> asSet() {
        return new SequencedSetFacade<>(this);
    }
}
