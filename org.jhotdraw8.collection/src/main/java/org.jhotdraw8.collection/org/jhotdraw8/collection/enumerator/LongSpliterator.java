/*
 * @(#)LongEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import org.jhotdraw8.annotation.NonNull;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * An object for enumerating primitive long-valued elements of a collection.
 * <p>
 * The protocol for accessing elements via a {@code Enumerator} imposes smaller per-element overhead than
 * {@link Iterator}, and avoids the inherent race involved in having separate methods for
 * {@code hasNext()} and {@code next()}.
 *
 * @author Werner Randelshofer
 */
public interface LongSpliterator extends EnumeratorSpliterator<Long>, Spliterator.OfLong {

    /**
     * {@inheritDoc}
     */
    @Override
    default @NonNull Long current() {
        return currentAsLong();
    }

    /**
     * Returns the current value.
     *
     * @return current
     * @see EnumeratorSpliterator#current()
     */
    long currentAsLong();

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
        return EnumeratorSpliterator.super.tryAdvance(action);
    }

}
