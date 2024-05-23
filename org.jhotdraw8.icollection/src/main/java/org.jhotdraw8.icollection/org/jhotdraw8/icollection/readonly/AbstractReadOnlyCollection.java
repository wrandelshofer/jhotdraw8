/*
 * @(#)AbstractReadOnlyCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.readonly;


import java.util.Objects;

/**
 * Abstract base class for {@link ReadOnlyCollection}s.
 *
 * @param <E> the element type
 */
public abstract class AbstractReadOnlyCollection<E> implements ReadOnlyCollection<E> {
    /**
     * Sole constructor. (For invocation by subclass constructors, typically implicit.).
     */
    public AbstractReadOnlyCollection() {
    }

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * ({@code "[]"}).  Adjacent elements are separated by the characters
     * {@code ", "} (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @return a string representation of this collection
     */
    public final String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    @Override
    public boolean contains(Object o) {
        for (E e : this) {
            if (Objects.equals(o, e)) {
                return true;
            }
        }
        return false;
    }
}
