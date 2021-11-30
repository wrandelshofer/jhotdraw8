package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.Arc;

import java.util.function.Predicate;

/**
 * Defines an API for finding all paths up to a maximal cost
 * in a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface AllPathsFinder<V, A, C extends Number & Comparable<C>> {
    /**
     * Finds all arc paths up to (including) the specified maximal cost.
     *
     * @param startVertices the set of start vertices
     * @param goalPredicate the goal predicate
     * @param searchLimit   the algorithm-specific search limit
     * @return all paths
     */
    @NonNull Iterable<OrderedPair<ImmutableList<Arc<V, A>>, C>> findAllArcSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C searchLimit);

    /**
     * Finds all arrow paths up to (including) the specified maximal cost.
     *
     * @param startVertices the set of start vertices
     * @param goalPredicate the goal predicate
     * @param searchLimit   the algorithm-specific search limit
     * @return all paths
     */
    @NonNull Iterable<OrderedPair<ImmutableList<A>, C>> findAllArrowSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C searchLimit);

    /**
     * Finds all vertex paths up to (including) the specified maximal cost.
     *
     * @param startVertices the set of start vertices
     * @param goalPredicate the goal predicate
     * @param searchLimit       the algorithm-specific search limit
     * @return all paths
     */
    @NonNull Iterable<OrderedPair<ImmutableList<V>, C>> findAllVertexSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C searchLimit);
}
