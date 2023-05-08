/*
 * @(#)AbstractLongEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import java.util.Spliterators;

/**
 * Abstract base class for {@link LongSpliterator}s.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractLongEnumeratorSpliterator extends Spliterators.AbstractLongSpliterator
        implements LongSpliterator {

    protected long current;

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
    protected AbstractLongEnumeratorSpliterator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final long currentAsLong() {
        return current;
    }

}
