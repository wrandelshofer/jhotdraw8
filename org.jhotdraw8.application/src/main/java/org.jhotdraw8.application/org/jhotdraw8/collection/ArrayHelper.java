package org.jhotdraw8.collection;

import org.jhotdraw8.util.Preconditions;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Provides helper methods for arrays that are not available in Java SE 8.
 */
class ArrayHelper {
    /**
     * Don't let anyone instantiate this class.
     */
    private ArrayHelper() {
    }

    /**
     * Checks if the elements in two sub-arrays are equal to one another
     * in the same order.
     *
     * @param a     array a
     * @param aFrom from-index
     * @param aTo   to-index
     * @param b     array b (can be the same as array a)
     * @param bFrom from-index
     * @param bTo   to-index
     * @return true if the two sub-arrays have the same length and
     * if the elements are equal to one another in the same order
     */
    public static <T> boolean equals(T[] a, int aFrom, int aTo,
                                     T[] b, int bFrom, int bTo) {
        return equals(a, aFrom, aTo, b, bFrom, bTo, Objects::equals);
    }

    /**
     * Checks if the elements in two sub-arrays are equal to one another
     * in the same order.
     *
     * @param a     array a
     * @param aFrom from-index
     * @param aTo   to-index
     * @param b     array b (can be the same as array a)
     * @param bFrom from-index
     * @param bTo   to-index
     * @param cmp   the predicate that checks if two elements are equal
     * @return true if the two sub-arrays have the same length and
     * if the elements are equal to one another in the same order
     */
    public static <T> boolean equals(T[] a, int aFrom, int aTo,
                                     T[] b, int bFrom, int bTo,
                                     BiPredicate<T, T> cmp) {
        Preconditions.checkFromToIndex(aFrom, aTo, a.length);
        Preconditions.checkFromToIndex(bFrom, bTo, b.length);
        int aLength = aTo - aFrom;
        int bLength = bTo - bFrom;
        if (aLength != bLength) {
            return false;
        }

        for (int i = 0; i < aLength; i++) {
            if (!cmp.test(a[aFrom++], b[bFrom++])) {
                return false;
            }
        }

        return true;
    }

}
