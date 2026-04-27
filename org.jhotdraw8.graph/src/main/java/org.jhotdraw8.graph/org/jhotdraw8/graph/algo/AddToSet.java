/*
 * @(#)AddToSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.algo;

/// A function that adds an element to a set if not already present.
///
/// The set can be implemented in various ways. For example:
///
///   - The set can be an implementation of one of the collection classes
///     provided by the Java API.
///     <pre>
///         {@literal AddToSet<E> = new HashSet<>()::add;}
///         </pre>
///
///   - The set can be a marker bit on an element object.
///     <pre>
///         class Element {
///             private boolean marked;
///             public boolean mark() {
///                 boolean wasMarked = false;
///                 marked = true;
///                 return wasMarked;
///             }
///         }
///        {@literal AddToSet<Element> = Element::mark;}
///        </pre>
///
/// @param <E> the element type of the set.
@FunctionalInterface
public interface AddToSet<E> {
    /// Adds the specified element to the set if it is not already present.
    ///
    /// @param e element to be added to the set
    /// @return `true` if this set did not already contain the specified
    /// element
    boolean add(E e);
}
