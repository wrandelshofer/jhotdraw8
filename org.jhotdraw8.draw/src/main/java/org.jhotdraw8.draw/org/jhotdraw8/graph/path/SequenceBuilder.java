package org.jhotdraw8.graph.path;

/**
 * Defines an API for finding arc-, arrow- and vertex-sequences associated
 * with a cost through a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface SequenceBuilder<V, A, C extends Number & Comparable<C>>
        extends ArrowSequenceBuilder<V, A, C>,
        ArcSequenceBuilder<V, A, C>,
        VertexSequenceBuilder<V, C> {
}
