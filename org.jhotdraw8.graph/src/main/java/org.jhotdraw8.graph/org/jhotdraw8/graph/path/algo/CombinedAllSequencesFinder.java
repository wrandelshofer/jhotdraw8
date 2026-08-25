/*
 * @(#)CombinedAllSequencesFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

/// Interface for finding all sequences between a set of source
/// vertices and goal vertices up to a maximal depth in a directed graph.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
public interface CombinedAllSequencesFinder<V, A>
        extends AllArcSequencesFinder<V, A>, AllArrowsSequencesFinder<V, A>, AllVertexSequencesFinder<V, A> {

}
