/*
 * @(#)AbstractEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Abstract base classes for {@link Enumerator}s.
 * <p>
 * Subclasses should only implement the {@link Enumerator#moveNext()}
 * method and the {@link Spliterator#trySplit()} method:
 * <pre>
 *     public boolean moveNext() {
 *         if (...end not reached...) {
 *             current = ...;
 *             return true;
 *         }
 *         return false;
 *     }
 * </pre>
 *
 * @param <E> the element type
 */
public abstract class AbstractEnumerator<E> extends Spliterators.AbstractSpliterator<E>
        implements Enumerator<E> {
    protected E current;

    /**
     * Creates a spliterator reporting the given estimated size and
     * additionalCharacteristics.
     *
     * @param est                       the estimated size of this spliterator if known, otherwise
     *                                  {@code Long.MAX_VALUE}.
     * @param additionalCharacteristics properties of this spliterator's
     *                                  source or elements.  If {@code SIZED} is reported then this
     *                                  spliterator will additionally report {@code SUBSIZED}.
     */
    protected AbstractEnumerator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final E current() {
        return current;
    }
}
