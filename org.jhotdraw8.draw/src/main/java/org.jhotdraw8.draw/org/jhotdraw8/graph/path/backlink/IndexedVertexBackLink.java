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

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex index
     * @param parent the parent back link
     * @param cost   the cumulated cost of this back link. Must be zero if parent is null.
     */
    public IndexedVertexBackLink(int vertex, @Nullable IndexedVertexBackLink<C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = vertex;
    }


    public int getVertex() {
        return vertex;
    }

}
