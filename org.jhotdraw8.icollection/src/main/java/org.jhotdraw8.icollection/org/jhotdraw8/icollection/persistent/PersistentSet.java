/*
 * @(#)PersistentSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSet;

import java.util.Collection;
import java.util.Set;

/**
 * An interface to an persistent set; the implementation guarantees that the
 * state of the collection does not change.
 * <p>
 * An interface to an persistent set provides methods for creating a new persistent set with
 * added or removed elements, without changing the original persistent set.
 *
 * @param <E> the element type
 */
public interface PersistentSet<E> extends ReadableSet<E>, PersistentCollection<E> {
    /**
     * Returns an empty set instance that has the specified
     * element type.
     *
     * @param <T> the element type of the returned set
     * @return an empty set of the specified element type.
     */
    @Override
    <T> PersistentSet<T> empty();

    /**
     * Returns a copy of this set that contains all elements
     * of this set and also the specified element.
     *
     * @param element an element
     * @return this set instance if it already contains the element, or
     * a different set instance with the element added
     */
    PersistentSet<E> add(E element);

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
    default PersistentSet<E> addAll(Iterable<? extends E> c) {
        if (c instanceof Collection<?> co && co.isEmpty()
            || c instanceof ReadableCollection<?> rc && rc.isEmpty()) {
            return this;
        }
        if (isEmpty() && c.getClass() == this.getClass()) {
            return (PersistentSet<E>) c;
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
    PersistentSet<E> remove(E element);

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
    default PersistentSet<E> removeAll(Iterable<?> c) {
        if (isEmpty()
            || c instanceof Collection<?> co && co.isEmpty()
            || c instanceof ReadableCollection<?> rc && rc.isEmpty()) {
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
    default PersistentSet<E> retainAll(Iterable<?> c) {
        if (isEmpty()) {
            return this;
        }
        if (c instanceof Collection<?> co && co.isEmpty()
            || c instanceof ReadableCollection<?> rc && rc.isEmpty()) {
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
        if (!(c instanceof ReadableCollection<?>)) {
            PersistentSet<Object> clear = empty();
            c = clear.addAll(c);
        }
        var rc = (ReadableCollection<?>) c;
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
