package org.jhotdraw8.collection.pair;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * An ordered pair.
 * <p>
 * This is a value-type.
 *
 * @param <U> the type of the first element of the pair
 * @param <V> the type of the second element of the pair
 */
public interface OrderedPair<U, V> {
    U first();

    V second();

    /**
     * Checks if a given ordered pair is equal to a given object.
     *
     * @param pair an ordered pair
     * @param obj  an object
     * @param <U>  the type of the first element of the pair
     * @param <V>  the type of the second element of the pair
     * @return true if equal
     */
    static <U, V> boolean orderedPairEquals(OrderedPair<U, V> pair, @Nullable Object obj) {
        if (pair == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (pair.getClass() != obj.getClass()) {
            return false;
        }
        final OrderedPair<?, ?> other = (OrderedPair<?, ?>) obj;
        if (!Objects.equals(pair.first(), other.first())) {
            return false;
        }
        return Objects.equals(pair.second(), other.second());
    }

    /**
     * Computes a hash code for an ordered pair.
     * The hash code is guaranteed to be non-zero.
     *
     * @param pair an ordered pair
     * @param <U>  the type of the first element of the pair
     * @param <V>  the type of the second element of the pair
     * @return the hash code
     */
    static <U, V> int orderedPairHashCode(OrderedPair<U, V> pair) {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(pair.first());
        hash = 59 * hash + Objects.hashCode(pair.second());
        return hash == 0 ? -89 : hash;
    }
}
