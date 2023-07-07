/*
 * @(#)OrderedPair.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.pair;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * A simple implementation of the {@link OrderedPair} interface.
 * <p>
 * This is a value-type.
 *
 * @param <U> the type of the first element of the pair
 * @param <V> the type of the second element of the pair
 * @author Werner Randelshofer
 */
public class SimpleOrderedPair<U, V> implements OrderedPair<U, V> {

    private final U a;
    private final V b;
    /**
     * Cached hash-value for faster hashing.
     */
    private int hash;

    public SimpleOrderedPair(U a, V b) {
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
        return OrderedPair.orderedPairEquals(this, obj);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = OrderedPair.orderedPairHashCode(this);
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
