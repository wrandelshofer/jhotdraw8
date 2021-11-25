package org.jhotdraw8.graph.path;

/**
 * Defines an API for finding arc-, arrow- and vertex-sequences
 * up to (inclusive) a maximal cost in a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface CombinedSequenceFinder<V, A, C extends Number & Comparable<C>>
        extends ArrowSequenceFinder<V, A, C>,
        ArcSequenceFinder<V, A, C>,
        VertexSequenceFinder<V, C> {
}
