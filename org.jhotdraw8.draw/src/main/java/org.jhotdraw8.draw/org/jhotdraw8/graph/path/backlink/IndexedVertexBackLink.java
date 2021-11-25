package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Represents an indexed vertex back link with cost and depth.
 *
 * @param <C> the cost number type
 */
public class IndexedVertexBackLink<C extends Number & Comparable<C>> extends AbstractBackLink<IndexedVertexBackLink<C>, C> {

    final int vertex;

    public IndexedVertexBackLink(int vertex, @Nullable IndexedVertexBackLink<C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = vertex;
    }


    public int getVertex() {
        return vertex;
    }

}
