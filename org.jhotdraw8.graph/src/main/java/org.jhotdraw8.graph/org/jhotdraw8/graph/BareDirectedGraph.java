/*
 * @(#)BareDirectedGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jspecify.annotations.Nullable;

/**
 * Provides minimalistic (bare) read-access to a directed graph.
 * <p>
 * A directed graph is a tuple {@code G = (V, A)}
 * <br>where {@code V} is a set of vertices {@code { v[0], ..., v[n-1] }}
 * <br>and {@code A} is a bag of ordered pairs {@code { (v[i], v[j]), ... } }.
 * <p>
 * This interface supports arbitrary data types {@literal <V>} and {@literal <A>}
 * for the vertices and arrows of the graph.
 * <p>
 * The type {@literal <A>} can be used to store data about an arrow.
 * You may define {@literal <A>} as an ordered pair {@code (v[i], v[j])},
 * but you are not required to do so, because implementations of this interface
 * do not need to access the content of {@literal <A>}.
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
    A getNextArrow(V v, int index);

}
