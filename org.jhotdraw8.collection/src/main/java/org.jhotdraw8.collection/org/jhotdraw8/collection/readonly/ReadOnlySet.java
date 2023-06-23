/*
 * @(#)ReadOnlySet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.readonly;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.facade.ReadOnlySetFacade;
import org.jhotdraw8.collection.facade.SetFacade;

import java.util.Iterator;
import java.util.Set;

/**
 * Read-only interface for a set. The state of the set may change.
 * <p>
 * Note: To compare a ReadOnlySet to a {@link Set}, you must either
 * wrap the ReadOnlySet into a Set using {@link SetFacade},
 * or wrap the Set into a ReadOnlySet using {@link ReadOnlySetFacade}.
 * <p>
 * This interface does not guarantee 'read-only', it actually guarantees
 * 'readable'. We use the prefix 'ReadOnly' because this is the naming
 * convention in JavaFX for interfaces that provide read methods but no write methods.
 *
 * @param <E> the element type
 */
public interface ReadOnlySet<E> extends ReadOnlyCollection<E> {
    /**
     * Returns the sum of the hash codes of all elements in the provided
     * iterator.
     *
     * @param iterator an iterator
     * @return the sum of the hash codes of the elements
     * @see Set#hashCode()
     */
    static <E> int iteratorToHashCode(@NonNull Iterator<E> iterator) {
        int h = 0;
        while (iterator.hasNext()) {
            E e = iterator.next();
            if (e != null) {
                h += e.hashCode();
            }
        }
        return h;
    }

    /**
     * Compares a read-only set with an object for equality.  Returns
     * {@code true} if the given object is also a read-only set and the two sets
     * contain the same elements.
     *
     * @param set a set
     * @param o   an object
     * @param <E> the element type
     * @return {@code true} if the object is equal to the set
     */
    static <E> boolean setEquals(@NonNull ReadOnlySet<E> set, @Nullable Object o) {
        if (o == set) {
            return true;
        }
        if (o instanceof ReadOnlySet) {
            @SuppressWarnings("unchecked")
            ReadOnlySet<E> that = (ReadOnlySet<E>) o;
            return set.size() == that.size() && set.containsAll(that);
        }
        return false;
    }


    /**
     * Wraps this set in the Set interface - without copying.
     *
     * @return the wrapped set
     */
    default @NonNull Set<E> asSet() {
        return new SetFacade<>(this);
    }

    /**
     * Compares the specified object with this set for equality.
     * <p>
     * Returns {@code true} if the given object is also a read-only set and the
     * two sets contain the same elements, ignoring the sequence of the elements.
     * <p>
     * Implementations of this method should use {@link ReadOnlySet#setEquals}.
     *
     * @param o an object
     * @return {@code true} if the object is equal to this map
     */
    boolean equals(@Nullable Object o);

    /**
     * Returns the hash code value for this set. The hash code
     * is the sum of the hash code of its elements.
     * <p>
     * Implementations of this method should use {@link ReadOnlySet#iteratorToHashCode}.
     *
     * @return the hash code value for this set
     * @see Set#hashCode()
     */
    int hashCode();
}
