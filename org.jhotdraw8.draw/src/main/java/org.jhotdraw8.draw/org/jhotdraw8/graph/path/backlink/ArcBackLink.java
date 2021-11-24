package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Represents a back link with cost and depth.
 *
 * @param <V> the vertex type
 * @param <A> the arrow type
 * @param <C> the cost number type
 */
public class ArcBackLink<V, A, C extends Number & Comparable<C>> extends AbstractBackLink<ArcBackLink<V, A, C>, C> {
    private final @NonNull V vertex;
    private final @Nullable A arrow;

    public ArcBackLink(@NonNull V node, @Nullable A arrow, @Nullable ArcBackLink<V, A, C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = node;
        this.arrow = arrow;
    }

    public @Nullable A getArrow() {
        return arrow;
    }

    public @NonNull V getVertex() {
        return vertex;
    }

    @Override
    public String toString() {
        return "ArcBackLink{" +
                "depth=" + depth +
                ", vertex=" + vertex +
                ", arrow=" + arrow +
                '}';
    }
}
