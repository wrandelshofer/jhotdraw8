package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Objects;

/**
 * An ordered pair.
 * <p>
 * This is a value-type.
 *
 * @param <U> the type of the first element of the pair
 * @param <V> the type of the second element of the pair
 * @author Werner Randelshofer
 */
public interface OrderedPair<U, V> {
    U first();

    V second();


    static <U, V> boolean orderedPairEquals(@NonNull OrderedPair<U, V> self, @Nullable Object obj) {
        if (self == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (self.getClass() != obj.getClass()) {
            return false;
        }
        final OrderedPair<?, ?> other = (OrderedPair<?, ?>) obj;
        if (!Objects.equals(self.first(), other.first())) {
            return false;
        }
        return Objects.equals(self.second(), other.second());
    }

    static <U, V> int orderedPairHashCode(@NonNull OrderedPair<U, V> self) {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(self.first());
        hash = 59 * hash + Objects.hashCode(self.second());
        return hash == 0 ? -89 : hash;
    }
}
