/*
 * @(#)AbstractIntSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import java.util.Spliterators;

/**
 * Abstract base class for {@link Enumerator.OfDouble}s.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractDoubleEnumerator
        extends Spliterators.AbstractDoubleSpliterator
        implements Enumerator.OfDouble {
    /**
     * The current element of the enumerator.
     */
    protected double current;

    /**
     * Creates a spliterator reporting the given estimated size and
     * additionalCharacteristics.
     *
     * @param est                       the estimated size of this spliterator if known, otherwise
     *                                  {@code Double.MAX_VALUE}.
     * @param additionalCharacteristics properties of this spliterator's
     *                                  source or elements.  If {@code SIZED} is reported then this
     *                                  spliterator will additionally report {@code SUBSIZED}.
     */
    protected AbstractDoubleEnumerator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final double currentAsDouble() {
        return current;
    }

}
