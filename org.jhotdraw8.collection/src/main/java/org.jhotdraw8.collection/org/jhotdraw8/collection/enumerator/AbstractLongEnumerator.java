/*
 * @(#)AbstractIntSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import java.util.Spliterators;

/// Abstract base class for [Enumerator.OfLong]s.
public abstract class AbstractLongEnumerator
        extends Spliterators.AbstractLongSpliterator
        implements Enumerator.OfLong {
    /// The current element of the enumerator.
    protected long current;

    /// Creates a spliterator reporting the given estimated size and
    /// additionalCharacteristics.
    ///
    /// @param est                       the estimated size of this spliterator if known, otherwise
    ///                                  `Long.MAX_VALUE`.
    /// @param additionalCharacteristics properties of this spliterator's
    ///                                  source or elements.  If `SIZED` is reported then this
    ///                                  spliterator will additionally report `SUBSIZED`.
    protected AbstractLongEnumerator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /// {@inheritDoc}
    @Override
    public final long currentAsLong() {
        return current;
    }

}
