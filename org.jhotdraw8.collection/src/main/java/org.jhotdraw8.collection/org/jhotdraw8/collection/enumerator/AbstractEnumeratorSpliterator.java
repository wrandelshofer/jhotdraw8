/*
 * @(#)AbstractSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Abstract base classes for {@link Spliterator}s.
 * <p>
 * Subclasses should only implement the {@link Spliterator#moveNext()}
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
public abstract class AbstractEnumeratorSpliterator<E> extends Spliterators.AbstractSpliterator<E>
        implements EnumeratorSpliterator<E> {
    /**
     * The current element of the enumerator.
     */
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
    protected AbstractEnumeratorSpliterator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public E current() {
        return current;
    }
}
