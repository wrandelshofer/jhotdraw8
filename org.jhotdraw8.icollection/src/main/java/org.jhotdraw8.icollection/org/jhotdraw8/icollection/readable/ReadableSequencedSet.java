/*
 * @(#)ReadableSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.readable;

import org.jhotdraw8.icollection.facade.SequencedSetFacade;

import java.util.SequencedSet;

/// This interface provides read operations for a sequenced set.
///
/// A sequenced set is a sequence of distinct elements.
/// The elements are ordered in a sequence from first to last.
/// The sequence can be established implicitly, by insertion operations,
/// or by sequence-altering operations.
/// (However, this interface only provides read operations).
///
/// A read operation returns data about the set.
/// The operation does not change the original set.
///
/// References:
/// <dl>
///     <dt>JEP draft: Sequenced Collections</dt>
///     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
/// </dl>
///
/// @param <E> the element type
public interface ReadableSequencedSet<E> extends ReadableSet<E>, ReadableSequencedCollection<E> {
    /// Returns a reversed-order view of this set.
    /// Changes to the underlying set are visible in the reversed view.
    ///
    /// @return a reversed-order view of this set
    ReadableSequencedSet<E> readableReversed();

    @Override
    default SequencedSet<E> asSet() {
        return new SequencedSetFacade<>(this);
    }
}
