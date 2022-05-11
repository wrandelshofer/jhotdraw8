/*
 * @(#)BareDirectedGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * A minimalistic read-only interface for a directed graph.
 * <p>
 * A directed graph is a tuple {@code G = (V, A)} where {@code V} is a set of
 * vertices and {@code A} is a set or bag of arrows.
 * <p>
 * This facade supports a data object for each arrow. The type of the arrow data
 * object is provided by the type parameter {@literal <A>}.
 * <p>
 * Users of this interface may define {@literal <A>} as a tuple
 * {@code (v_i, v_j)} but are not required to do so, because this interface
 * provides methods for accessing the next vertex of a given vertex without
 * having to deal with the arrow object.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 */
public interface BareDirectedGraph<V, A> extends BareDirectedVertexGraph<V> {


    /**
     * Returns the arrow data associated with the specified vertex and outgoing arrow
     * index.
     *
     * @param v     a vertex
     * @param index index of outgoing arrow
     * @return the next arrow data
     * @see #getNextCount
     */
    @Nullable
    A getNextArrow(@NonNull V v, int index);

}
