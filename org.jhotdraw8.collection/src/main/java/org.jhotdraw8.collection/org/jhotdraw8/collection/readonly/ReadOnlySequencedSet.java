/*
 * @(#)ReadOnlySequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.readonly;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.facade.SequencedSetFacade;
import org.jhotdraw8.collection.sequenced.SequencedSet;

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
