package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

public interface MutableBidiGraph<V, A> extends BidiGraph<V, A>, MutableDirectedGraph<V, A> {
    /**
     * Adds an arrow from 'va' to 'vb' and an arrow from 'vb' to 'va'.
     *
     * @param va    vertex a
     * @param vb    vertex b
     * @param arrow the arrow
     */
    @SuppressWarnings("unused")
    default void addBidiArrow(@NonNull V va, @NonNull V vb, A arrow) {
        addArrow(va, vb, arrow);
        addArrow(vb, va, arrow);
    }

}
