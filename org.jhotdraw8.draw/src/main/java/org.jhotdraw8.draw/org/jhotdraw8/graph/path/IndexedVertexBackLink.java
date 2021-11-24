package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

public class IndexedVertexBackLink<C extends Number & Comparable<C>> extends AbstractBackLink<IndexedVertexBackLink<C>, C> {

    final int vertex;

    public IndexedVertexBackLink(int vertex, @Nullable IndexedVertexBackLink parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = vertex;
    }


    public int getVertex() {
        return vertex;
    }
}
