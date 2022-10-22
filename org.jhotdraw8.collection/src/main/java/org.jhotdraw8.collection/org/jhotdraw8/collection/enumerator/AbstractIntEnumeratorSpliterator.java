/*
 * @(#)AbstractIntEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import java.util.Spliterators;

/**
 * Abstract base class for {@link IntEnumeratorSpliterator}s.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractIntEnumeratorSpliterator
        extends Spliterators.AbstractIntSpliterator
        implements IntEnumeratorSpliterator {
    protected int current;

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
    protected AbstractIntEnumeratorSpliterator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /** {@inheritDoc} */
    @Override
    public final int currentAsInt() {
        return current;
    }

}
