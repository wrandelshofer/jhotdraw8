/*
 * @(#)OrderedPair.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
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
public class OrderedPair<U, V> implements Pair<U, V> {

    private final U a;
    private final V b;
    /**
     * Cached hash-value for faster hashing.
     */
    private int hash;

    public OrderedPair(U a, V b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public U first() {
        return a;
    }

    @Override
    public V second() {
        return b;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrderedPair<?, ?> other = (OrderedPair<?, ?>) obj;
        if (!Objects.equals(this.a, other.a)) {
            return false;
        }
        return Objects.equals(this.b, other.b);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = 3;
            hash = 59 * hash + Objects.hashCode(this.a);
            hash = 59 * hash + Objects.hashCode(this.b);
        }
        return hash;
    }

    @Override
    public @NonNull String toString() {
        return "OrderedPair{"
                + a +
                ", " + b +
                '}';
    }
}
