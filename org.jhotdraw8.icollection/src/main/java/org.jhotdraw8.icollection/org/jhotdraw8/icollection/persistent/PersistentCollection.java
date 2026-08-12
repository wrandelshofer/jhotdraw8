/*
 * @(#)PersistentCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableCollection;

import java.util.Collection;
import java.util.function.Predicate;

/// This interface provides copy-returning operations for a collection.
///
/// A collection is a group of elements.
///
/// A copy-returning operation returns a new copy of the collection
/// with changes applied to it. The operation does not change the original
/// collection.
///
/// To avoid confusion with operations that change a collection in-place,
/// the method name of a copy-returning operation is an imperative verb with an
///  "ed" or "ing" suffix.
///
/// References:
/// - Kotlin, Proposals, Keep-0459, Naming conventions for copy-returning and in-place-mutating operations.
///   [github.com](https://github.com/Kotlin/KEEP/blob/134d1edf510504efb6f1a8ab3e30f41b6d4588b5/proposals/KEEP-0459-naming-conventions-for-copy-returning-operations.md)
///
/// - Swift, API Design Guidelines, Strive for Fluent Usage.
///  [swift.org](https://www.swift.org/documentation/api-design-guidelines/#strive-for-fluent-usage)
///
/// @param <E> the element type
public interface PersistentCollection<E> extends ReadableCollection<E> {
    /// Returns a copy of this collection that contains all elements
    /// of this collection and also the specified element.
    ///
    /// A collection may prevent that the same element can be
    /// added more than once.
    ///
    /// @param element an element
    /// @return this collection instance if it already contains the element, or
    /// a different collection instance with the element added
    PersistentCollection<E> adding(E element);

    /// Returns a copy of this collection that contains all elements
    /// of this collection and also all elements of the specified
    /// collection.
    ///
    /// A collection may prevent that the same element can be
    /// added more than once.
    ///
    /// @param c a collection to be added to this collection
    /// @return this collection instance if it already contains the elements, or
    /// a different collection instance with the elements added
    @SuppressWarnings("unchecked")
    PersistentCollection<E> addingAll(Iterable<? extends E> c);

    /// Returns an empty collection instance that has the specified
    /// element type.
    ///
    /// @param <T> the element type of the returned collection
    /// @return an empty collection of the specified element type.
    <T> PersistentCollection<T> cleared();

    /// Removes all elements in this collection that satisfy the specified predicate.
    ///
    /// @param p a predicate
    /// @return a collection that only contains elements that do not satisfy the predicate
    default PersistentCollection<E> removingIf(Predicate<E> p) {
        PersistentCollection<E> result = this;
        for (E e : this) {
            if (p.test(e)) {
                result = result.removing(e);
            }
        }
        return result;
    }

    /// Returns the maximal number of elements that this collection type can
    /// hold
    ///
    /// @return the maximal size
    int maxSize();

    /// Returns a copy of this collection that contains all elements
    /// of this collection except the specified element.
    ///
    /// @param element an element
    /// @return this collection instance if it already does not contain the element, or
    /// a different collection instance with the element removed
    PersistentCollection<E> removing(E element);

    /// Returns a copy of this collection that contains all elements
    /// of this collection except the elements of the specified
    /// collection.
    ///
    /// @param c a collection with elements to be removed from this collection
    /// @return this collection instance if it already does not contain the elements, or
    /// a different collection instance with the elements removed
    @SuppressWarnings("unchecked")
    PersistentCollection<E> removingAll(Iterable<?> c);

    /// Returns a copy of this collection that contains only elements
    /// that are in this collection and in the specified collection.
    ///
    /// @param c a collection with elements to be retained in this collection
    /// @return this collection instance if it has not changed, or
    /// a different collection instance with elements removed
    @SuppressWarnings("unchecked")
    PersistentCollection<E> retainingAll(Iterable<?> c);

    /// Returns a mutable copy of this collection.
    ///
    /// @return a mutable copy.
    Collection<E> toMutable();
}
