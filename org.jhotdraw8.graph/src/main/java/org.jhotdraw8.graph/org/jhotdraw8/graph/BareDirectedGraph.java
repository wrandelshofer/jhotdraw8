/*
 * @(#)BareDirectedGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jspecify.annotations.Nullable;

/// Provides minimalistic (bare) read-access to a directed graph.
///
/// A directed graph is a tuple `G = (V, A)`
///
/// where `V` is a set of vertices `{v[0], ..., v[n-1]}`
///
/// and `A` is a bag of ordered pairs `{(v[i], v[j]), ...}`.
///
/// This interface supports arbitrary data types {@literal <V>} and {@literal <A>}
/// for the vertices and arrows of the graph.
///
/// The type {@literal <A>} can be used to store data about an arrow.
/// You may define {@literal <A>} as an ordered pair `(v[i], v[j])`,
/// but you are not required to do so, because implementations of this interface
/// do not need to access the content of {@literal <A>}.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
public interface BareDirectedGraph<V, A> extends BareDirectedVertexGraph<V> {


    /// Returns the arrow data associated with the specified vertex and outgoing arrow
    /// index.
    ///
    /// @param v     a vertex
    /// @param index index of outgoing arrow
    /// @return the next arrow data
    /// @see #getNextCount
    @Nullable
    A getNextArrow(V v, int index);

}
