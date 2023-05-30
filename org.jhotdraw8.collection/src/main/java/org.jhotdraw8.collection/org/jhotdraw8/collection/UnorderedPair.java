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

    static <V> boolean unorderedPairEquals(@NonNull UnorderedPair<V> self, @Nullable Object obj) {
        if (self == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (self.getClass() != obj.getClass()) {
            return false;
        }
        final UnorderedPair<?> other = (UnorderedPair<?>) obj;
        if (Objects.equals(self.either(), other.either()) && Objects.equals(self.other(), other.other())) {
            return true;
        }
        return Objects.equals(self.other(), other.either()) && Objects.equals(self.either(), other.other());
    }

    static <V> int unorderedPairHashCode(@NonNull UnorderedPair<V> self) {
        final int hash = 7 + Objects.hashCode(self.either()) + Objects.hashCode(self.other());
        return hash == 0 ? -61 : hash;
    }
}
