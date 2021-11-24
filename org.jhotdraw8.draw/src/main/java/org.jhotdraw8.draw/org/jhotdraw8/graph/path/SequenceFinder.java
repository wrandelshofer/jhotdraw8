package org.jhotdraw8.graph.path;

/**
 * Defines an API for finding arc-, arrow- and vertex-sequences associated
 * with a cost through a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface SequenceFinder<V, A, C extends Number & Comparable<C>>
        extends ArrowSequenceFinder<V, A, C>,
        ArcSequenceFinder<V, A, C>,
        VertexSequenceFinder<V, C> {
}
