/*
 * @(#)LongEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * An object for enumerating elements of a collection.
 * <p>
 * The protocol for accessing elements via a {@code Enumerator} imposes smaller per-element overhead than
 * {@link Iterator}, and avoids the inherent race involved in having separate methods for
 * {@code hasNext()} and {@code next()}.
 *
 * @author Werner Randelshofer
 */
public interface LongEnumerator extends Enumerator<Long>, Spliterator.OfLong {

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean tryAdvance(@NonNull LongConsumer action) {
        if (moveNext()) {
            action.accept(currentAsLong());
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean tryAdvance(@NonNull Consumer<? super Long> action) {
        return Enumerator.super.tryAdvance(action);
    }

    /**
     * Returns the current value.
     *
     * @return current
     * @see Enumerator#current()
     */
    long currentAsLong();

    /**
     * {@inheritDoc}
     */
    @Override
    default @NonNull Long current() {
        return currentAsLong();
    }

}
