package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

public class VertexBackLink<V, C extends Number & Comparable<C>> extends AbstractBackLink<VertexBackLink<V, C>, C> {
    private final @NonNull V vertex;

    public VertexBackLink(@NonNull V node, @Nullable VertexBackLink<V, C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = node;
    }

    public @NonNull V getVertex() {
        return vertex;
    }

}
