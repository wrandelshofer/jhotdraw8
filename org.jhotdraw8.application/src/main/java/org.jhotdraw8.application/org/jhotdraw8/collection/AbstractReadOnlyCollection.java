/*
 * @(#)AbstractReadOnlyCollection.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Iterator;
import java.util.Objects;

public abstract class AbstractReadOnlyCollection<E> implements ReadOnlyCollection<E> {
    public AbstractReadOnlyCollection() {
    }

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @return a string representation of this collection
     */
    public final @NonNull String toString() {
        return iterableToString(this);
    }

    public boolean contains(Object o) {
        for (E e : this) {
            if (Objects.equals(o, e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a string representation of the provided iterable.  The string
     * representation consists of a list of the iterable's elements in the
     * order they are returned, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @param c an iterable
     * @return a string representation of the iterable
     */
    public static <E> @NonNull String iterableToString(final @NonNull Iterable<E> c) {
        Iterator<E> it = c.iterator();
        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            E e = it.next();
            sb.append(e == c ? "(this Collection)" : e);
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }


}
