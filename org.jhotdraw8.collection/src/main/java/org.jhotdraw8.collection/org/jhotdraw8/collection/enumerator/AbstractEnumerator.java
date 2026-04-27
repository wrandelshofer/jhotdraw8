/*
 * @(#)AbstractSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import java.util.Spliterator;
import java.util.Spliterators;

/// Abstract base classes for [Enumerator]s.
///
/// Subclasses should only implement the [Enumerator#moveNext()]
/// method and (optionally) the [Spliterator#trySplit()] method:
/// <pre>
///     public boolean moveNext() {
///         if (...end not reached...) {
///             current = ...;
///             return true;
///         }
///         return false;
///     }
/// </pre>
///
/// @param <E> the element type
public abstract class AbstractEnumerator<E> extends Spliterators.AbstractSpliterator<E>
        implements Enumerator<E> {
    /// The current element of the enumerator.
    protected E current;

    /// Creates a spliterator reporting the given estimated size and
    /// additionalCharacteristics.
    ///
    /// @param est                       the estimated size of this spliterator if known, otherwise
    ///                                  `Long.MAX_VALUE`.
    /// @param additionalCharacteristics properties of this spliterator's
    ///                                  source or elements.  If `SIZED` is reported then this
    ///                                  spliterator will additionally report `SUBSIZED`.
    protected AbstractEnumerator(long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
    }


    /// {@inheritDoc}
    @Override
    public E current() {
        return current;
    }
}
