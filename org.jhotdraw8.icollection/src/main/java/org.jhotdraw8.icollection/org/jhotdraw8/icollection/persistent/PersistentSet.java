/*
 * @(#)PersistentSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/// This interface provides copy-returning operations for a set.
///
/// A set is a group of distinct elements.
///
/// A copy-returning operation returns a new copy of the set
/// with changes applied to it. The operation does not change the original
/// set.
///
/// @param <E> the element type
public interface PersistentSet<E> extends ReadableSet<E>, PersistentCollection<E> {
    /// Returns an empty set instance that has the specified
    /// element type.
    ///
    /// @return an empty set
    @Override
    PersistentSet<E> cleared();

    /// Returns a copy of this set that contains all elements
    /// of this set and also the specified element.
    ///
    /// @param element an element
    /// @return this set instance if it already contains the element, or
    /// a different set instance with the element added
    PersistentSet<E> adding(E element);

    /// Returns a copy of this set that contains all elements
    /// of this set and also all elements of the specified
    /// collection.
    ///
    /// @param c a collection to be added to this set
    /// @return this set instance if it already contains the elements, or
    /// a different set instance with the elements added
    @SuppressWarnings("unchecked")
    default PersistentSet<E> addingAll(Iterable<? extends E> c) {
        if (c instanceof Collection<?> co && co.isEmpty()
                || c instanceof ReadableCollection<?> rc && rc.isEmpty()) {
            return this;
        }
        if (isEmpty() && c.getClass() == this.getClass()) {
            return (PersistentSet<E>) c;
        }
        var s = this;
        for (var e : c) {
            s = s.adding(e);
        }
        return s;
    }

    /// Returns a copy of this set that contains all elements
    /// of this set except the specified element.
    ///
    /// @param element an element
    /// @return this set instance if it already does not contain the element, or
    /// a different set instance with the element removed
    PersistentSet<E> removing(E element);

    /// Returns a copy of this set that contains all elements
    /// of this set except the elements of the specified
    /// collection.
    ///
    /// @param c a collection with elements to be removed from this set
    /// @return this set instance if it already does not contain the elements, or
    /// a different set instance with the elements removed
    @SuppressWarnings("unchecked")
    default PersistentSet<E> removingAll(Iterable<?> c) {
        if (isEmpty()
                || c instanceof Collection<?> co && co.isEmpty()
                || c instanceof ReadableCollection<?> rc && rc.isEmpty()) {
            return this;
        }
        var s = this;
        for (var e : c) {
            s = s.removing((E) e);
        }
        return s;
    }

    /// Returns a copy of this set that contains only elements
    /// that are in this set and in the specified collection.
    ///
    /// @param c a collection with elements to be retained in this set
    /// @return this set instance if it has not changed, or
    /// a different set instance with elements removed
    @SuppressWarnings("unchecked")
    default PersistentSet<E> retainingAll(Iterable<?> c) {
        if (isEmpty()) {
            return this;
        }
        if (c instanceof Collection<?> co && co.isEmpty()
                || c instanceof ReadableCollection<?> rc && rc.isEmpty()) {
            return cleared();
        }
        var s = this;
        if ((c instanceof ReadableCollection<?>)) {
            var rc = (ReadableCollection<?>) c;
            for (var e : this) {
                if (!rc.contains(e)) {
                    s = s.removing(e);
                }
            }
            return s;
        }
        if (c instanceof Collection<?> co) {
            for (var e : this) {
                if (!co.contains(e)) {
                    s = s.removing(e);
                }
            }
            return s;
        }
        var co = new HashSet<E>();
        c.forEach(e1 -> co.add((E) e1));
        for (var e : this) {
            if (!co.contains(e)) {
                s = s.removing(e);
            }
        }
        return s;
    }


    /// Returns a mutable copy of this set.
    ///
    /// @return a mutable copy.
    Set<E> toMutable();

}
