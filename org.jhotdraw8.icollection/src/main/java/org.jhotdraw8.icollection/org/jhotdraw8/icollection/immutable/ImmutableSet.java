/*
 * @(#)ImmutableSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.Collection;
import java.util.Set;

/**
 * An interface to an immutable set; the implementation guarantees that the
 * state of the collection does not change.
 * <p>
 * An interface to an immutable set provides methods for creating a new immutable set with
 * added or removed elements, without changing the original immutable set.
 *
 * @param <E> the element type
 */
public interface ImmutableSet<E> extends ReadOnlySet<E>, ImmutableCollection<E> {
    /**
     * Returns an empty set instance that has the specified
     * element type.
     *
     * @return an empty set of the specified element type.
     * @param <T> the element type of the returned set
     */
    @Override
    <T> ImmutableSet<T> empty();

    /**
     * Returns a copy of this set that contains all elements
     * of this set and also the specified element.
     *
     * @param element an element
     * @return this set instance if it already contains the element, or
     * a different set instance with the element added
     */
    ImmutableSet<E> add(E element);

    /**
     * Returns a copy of this set that contains all elements
     * of this set and also all elements of the specified
     * collection.
     *
     * @param c a collection to be added to this set
     * @return this set instance if it already contains the elements, or
     * a different set instance with the elements added
     */
    @SuppressWarnings("unchecked")
    default ImmutableSet<E> addAll(Iterable<? extends E> c) {
        if (c instanceof Collection<?> co && co.isEmpty()
                || c instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            return this;
        }
        if (isEmpty() && c.getClass() == this.getClass()) {
            return (ImmutableSet<E>) c;
        }
        var s = this;
        for (var e : c) {
            s = s.add(e);
        }
        return s;
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the specified element.
     *
     * @param element an element
     * @return this set instance if it already does not contain the element, or
     * a different set instance with the element removed
     */
    ImmutableSet<E> remove(E element);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the elements of the specified
     * collection.
     *
     * @param c a collection with elements to be removed from this set
     * @return this set instance if it already does not contain the elements, or
     * a different set instance with the elements removed
     */
    @SuppressWarnings("unchecked")
    default ImmutableSet<E> removeAll(Iterable<?> c) {
        if (isEmpty()
                || c instanceof Collection<?> co && co.isEmpty()
                || c instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            return this;
        }
        var s = this;
        for (var e : c) {
            s = s.remove((E) e);
        }
        return s;
    }

    /**
     * Returns a copy of this set that contains only elements
     * that are in this set and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this set instance if it has not changed, or
     * a different set instance with elements removed
     */
    @SuppressWarnings("unchecked")
    default ImmutableSet<E> retainAll(Iterable<?> c) {
        if (isEmpty()) {
            return this;
        }
        if (c instanceof Collection<?> co && co.isEmpty()
                || c instanceof ReadOnlyCollection<?> rc && rc.isEmpty()) {
            return empty();
        }
        if (c instanceof Collection<?> co) {
            var s = this;
            for (var e : this) {
                if (!co.contains(e)) {
                    s = s.remove(e);
                }
            }
            return s;
        }
        if (!(c instanceof ReadOnlyCollection<?>)) {
            ImmutableSet<Object> clear = empty();
            c = clear.addAll(c);
        }
        var rc = (ReadOnlyCollection<?>) c;
        var s = this;
        for (var e : this) {
            if (!rc.contains(e)) {
                s = s.remove(e);
            }
        }
        return s;
    }


    /**
     * Returns a mutable copy of this set.
     *
     * @return a mutable copy.
     */
    Set<E> toMutable();

}
