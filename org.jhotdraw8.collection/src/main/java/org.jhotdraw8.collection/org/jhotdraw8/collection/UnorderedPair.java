package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Objects;

/**
 * An unordered pair.
 * <p>
 * This is a value-type.
 *
 * @param <V> the type of the elements that form the pair
 * @author Werner Randelshofer
 */
public interface UnorderedPair<V> {

    V either();

    V other();

    /**
     * Checks if a given ordered pair is equal to a given object.
     *
     * @param pair an ordered pair
     * @param obj  an object
     * @param <V>  the type of the elements that form the pair
     * @return true if equal
     */
    static <V> boolean unorderedPairEquals(@NonNull UnorderedPair<V> pair, @Nullable Object obj) {
        if (pair == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (pair.getClass() != obj.getClass()) {
            return false;
        }
        final UnorderedPair<?> other = (UnorderedPair<?>) obj;
        if (Objects.equals(pair.either(), other.either()) && Objects.equals(pair.other(), other.other())) {
            return true;
        }
        return Objects.equals(pair.other(), other.either()) && Objects.equals(pair.either(), other.other());
    }

    /**
     * Computes a hash code for an ordered pair.
     * The hash code is guaranteed to be non-zero.
     *
     * @param pair an ordered pair
     * @param <V>  the type of the elements that form the pair
     * @return the hash code
     */
    static <V> int unorderedPairHashCode(@NonNull UnorderedPair<V> pair) {
        final int hash = 7 + Objects.hashCode(pair.either()) + Objects.hashCode(pair.other());
        return hash == 0 ? -61 : hash;
    }
}
