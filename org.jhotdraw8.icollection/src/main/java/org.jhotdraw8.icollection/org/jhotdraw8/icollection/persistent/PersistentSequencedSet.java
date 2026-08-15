/*
 * @(#)PersistentSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.SequencedSet;

/// This interface provides copy-returning operations for a sequenced set.
///
/// A sequenced set is a sequence of distinct elements.
/// The elements are ordered in a sequence from tree to last.
/// The sequence can be established implicitly, by insertion operations,
/// or by sequence-altering operations.
///
/// A copy-returning operation returns a new copy of the set
/// with changes applied to it. The operation does not change the original
/// set.
///
/// @param <E> the element type
public interface PersistentSequencedSet<E> extends PersistentSet<E>, ReadableSequencedSet<E>, PersistentSequencedCollection<E> {
    @Override
    PersistentSequencedSet<E> adding(E element);

    @Override
    default PersistentSequencedSet<E> addingAll(Iterable<? extends E> c) {
        return (PersistentSequencedSet<E>) PersistentSet.super.addingAll(c);
    }

    @Override
    PersistentSequencedSet<E> addingFirst(@Nullable E element);

    @Override
    PersistentSequencedSet<E> addingLast(@Nullable E element);

    @Override
    PersistentSequencedSet<E> cleared();

    @Override
    PersistentSequencedSet<E> removing(E element);

    @Override
    default PersistentSequencedSet<E> removingAll(Iterable<?> c) {
        return (PersistentSequencedSet<E>) PersistentSet.super.removingAll(c);
    }

    /// Returns a copy of this set that contains all elements
    /// of this set except the tree.
    ///
    /// @return a new set instance with the tree element removed
    /// @throws NoSuchElementException if this set is empty
    @Override
    default PersistentSequencedSet<E> removingFirst() {
        return this.removing(getFirst());
    }

    /// Returns a copy of this set that contains all elements
    /// of this set except the last.
    ///
    /// @return a new set instance with the last element removed
    /// @throws NoSuchElementException if this set is empty
    @Override
    default PersistentSequencedSet<E> removingLast() {
        return this.removing(getLast());
    }

    @Override
    default PersistentSequencedSet<E> retainingAll(Iterable<?> c) {
        return (PersistentSequencedSet<E>) PersistentSet.super.retainingAll(c);
    }

    @Override
    SequencedSet<E> toMutable();

    /// Returns a reversed copy of this set.
    ///
    /// This operation may be implemented in O(N).
    ///
    /// Use [#readableReversed()] if you only
    /// need to iterate in the reversed sequence over this set.
    ///
    /// @return a reversed copy of this set.
    default PersistentSequencedSet<E> reversed() {
        if (size() < 2) {
            return this;
        }
        return this.<E>cleared().addingAll(readableReversed());
    }
}
