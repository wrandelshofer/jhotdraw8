/*
 * @(#)AbstractIntSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import java.util.Spliterators;

/// Abstract base class for [Enumerator.OfInt]s.
public abstract class AbstractIntEnumerator
        extends Spliterators.AbstractIntSpliterator
        implements Enumerator.OfInt {
    /// The current element of the enumerator.
    protected int current;

    /// Creates a spliterator reporting the given estimated size and
    /// additionalCharacteristics.
    ///
    /// @param est                       the estimated size of this spliterator if known, otherwise
    ///                                  `Long.MAX_VALUE`.
    /// @param additionalCharacteristics properties of this spliterator's
    ///                                  source or elements.  If `SIZED` is reported then this
    ///                                  spliterator will additionally report `SUBSIZED`.
    protected AbstractIntEnumerator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /// {@inheritDoc}
    @Override
    public final int currentAsInt() {
        return current;
    }

}
