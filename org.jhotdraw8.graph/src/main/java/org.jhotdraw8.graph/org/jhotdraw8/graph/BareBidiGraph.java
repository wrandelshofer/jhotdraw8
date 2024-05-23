/*
 * @(#)BareBidiGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jspecify.annotations.Nullable;

/**
 * Adds methods to the interface defined in {@link BareDirectedGraph}
 * that allow to follow arrows in backward direction.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 */
public interface BareBidiGraph<V, A> extends BareDirectedGraph<V, A> {
    /**
     * Returns the previous vertex associated with the specified vertex and
     * incoming arrow index.
     *
     * @param vertex a vertex
     * @param index  index of incoming arrow
     * @return the previous vertex
     * @see #getPrevCount
     */
    V getPrev(V vertex, int index);

    /**
     * Returns the arrow data associated with the specified vertex and
     * the specified incoming arrow index.
     *
     * @param vertex a vertex
     * @param index  index of incoming arrow
     * @return the arrow
     * @see #getPrevCount
     */
    @Nullable
    A getPrevArrow(V vertex, int index);

    /**
     * Returns the number of previous vertices at the specified vertex.
     * <p>
     * This number is the same as the number of incoming arrows at the
     * specified vertex.
     *
     * @param vertex a vertex
     * @return the number of previous vertices
     */
    int getPrevCount(V vertex);
}
