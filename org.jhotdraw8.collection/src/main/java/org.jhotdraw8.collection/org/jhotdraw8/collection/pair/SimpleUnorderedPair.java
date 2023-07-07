/*
 * @(#)UnorderedPair.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.pair;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * A simple implementation of the {@link UnorderedPair} interface.
 * <p>
 * This is a value-type.
 *
 * @param <V> the type of the elements that form the pair
 * @author Werner Randelshofer
 */
public class SimpleUnorderedPair<V> implements UnorderedPair<V> {

    private final V a;
    private final V b;
    /**
     * Cached hash-value for faster hashing.
     */
    private int hash;

    public SimpleUnorderedPair(V a, V b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public V either() {
        return a;
    }

    @Override
    public V other() {
        return b;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return UnorderedPair.unorderedPairEquals(this, obj);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = UnorderedPair.unorderedPairHashCode(this);
        }
        return hash;
    }

    @Override
    public @NonNull String toString() {
        return "UnorderedPair{" + "a=" + a + ", b=" + b + '}';
    }

}
