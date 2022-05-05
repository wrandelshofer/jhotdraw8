/*
 * @(#)ReadOnlySet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import javafx.collections.ObservableSet;
import org.jhotdraw8.annotation.NonNull;

import java.util.Iterator;
import java.util.Set;

/**
 * Read-only interface for a set. The state of the set may change.
 * <p>
 * Note: To compare a ReadOnlySet to a {@link Set}, you must either
 * wrap the ReadOnlySet into a Set using {@link WrappedSet},
 * or wrap the Set into a ReadOnlySet using {@link WrappedReadOnlySet}.
 * <p>
 * This interface does not guarantee 'read-only', it actually guarantees
 * 'readable'. We use the prefix 'ReadOnly' because this is the naming
 * convention in JavaFX for APIs that provide read methods but no write methods.
 *
 * @param <E> the element type
 */
public interface ReadOnlySet<E> extends ReadOnlyCollection<E> {
    /**
     * Wraps this set in the Set API - without copying.
     *
     * @return the wrapped set
     */
    default @NonNull Set<E> asSet() {
        return new WrappedSet<>(this);
    }

    /**
     * Wraps this set in the ObservableSet API - without copying.
     *
     * @return the wrapped set
     */
    default @NonNull ObservableSet<E> asObservableSet() {
        return new WrappedObservableSet<>(this);
    }

    static <E> boolean setEquals(ReadOnlySet<E> set, Object o) {
        if (o == set) {
            return true;
        }
        if (!(o instanceof ReadOnlySet)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        ReadOnlyCollection<E> that = (ReadOnlyCollection<E>) o;
        if (that.size() != set.size()) {
            return false;
        }
        try {
            return set.containsAll(that);
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }
    /**
     * Returns the hash code of the provided iterator, assuming that
     * the iterator is from a set.
     *
     * @param iterator an iterator over a set
     * @return the sum of the hash codes of the elements in the set
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
}
