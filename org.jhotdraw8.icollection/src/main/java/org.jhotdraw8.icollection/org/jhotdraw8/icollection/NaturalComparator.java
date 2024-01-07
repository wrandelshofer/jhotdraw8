package org.jhotdraw8.icollection;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares two objects using their natural order.
 * <p>
 * This class is similar to {@link Comparator#naturalOrder()} but it also supports null objects.
 *
 * @param <T> the object type
 */
class NaturalComparator<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final NaturalComparator<?> INSTANCE = new NaturalComparator<>();

    private NaturalComparator() {
    }

    @SuppressWarnings("unchecked")
    static <T> NaturalComparator<T> instance() {
        return (NaturalComparator<T>) INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(T a, T b) {
        if (a == null) {
            return (b == null) ? 0 : -1;
        }
        if (b == null) {
            return 1;
        }
        return ((Comparable<T>) a).compareTo(b);
    }

    /**
     * @see Comparator#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NaturalComparator;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    /**
     * Instance control for object serialization.
     *
     * @return The singleton instance of NaturalComparator.
     * @see java.io.Serializable
     */
    private Object readResolve() {
        return INSTANCE;
    }

}
