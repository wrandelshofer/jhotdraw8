/*
 * @(#)BareDirectedVertexGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;


import java.util.Set;

/// Provides minimalistic (bare) read-access to a directed graph.
///
/// A directed graph is a tuple `G = (V, A)`
///
/// where `V` is a set of vertices `{v[0], ..., v[n-1]}`
///
/// and `A` is a bag of ordered pairs `{(v[i], v[j]), ...}`.
///
/// This interface supports an arbitrary data type {@literal <V>} for the vertices of the graph.
///
/// Use the interface [BareDirectedGraph] if you also need support for
/// the arrow data type {@literal <A>} of a graph.
///
/// @param <V> the vertex data type
public interface BareDirectedVertexGraph<V> {
    /// Returns the next vertex associated with
    /// the specified vertex and outgoing arrow index.
    ///
    /// @param v     a vertex
    /// @param index index of outgoing arrow
    /// @return the next vertex
    /// @see #getNextCount
    V getNext(V v, int index);

    /// Returns the number of next vertices at the specified vertex.
    ///
    /// This number is the same as the number of outgoing arrows at the specified
    /// vertex.
    ///
    /// @param v a vertex
    /// @return the number of next vertices
    int getNextCount(V v);

    /// Returns all vertices.
    ///
    /// @return a set view on all vertices
    Set<V> getVertices();

}
