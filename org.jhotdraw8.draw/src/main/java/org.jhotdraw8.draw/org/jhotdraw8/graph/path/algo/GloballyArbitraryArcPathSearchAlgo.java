/*
 * @(#)AnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.util.TriFunction;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Searches an arbitrary path from a set of start vertices to a set of goal
 * vertices using a breadth-first search algorithm.
 * <p>
 * Expected run time:
 * <dl>
 *     <dt>When a path can be found</dt><dd>less or equal O( |A| + |V| ) within max depth</dd>
 *     <dt>When no path can be found</dt><dd>exactly O( |A| + |V| ) within max depth</dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 */
public class GloballyArbitraryArcPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements ArcPathSearchAlgo<V, A, C> {

    /**
     * {@inheritDoc}
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param zero             the zero cost value
     * @param positiveInfinity the positive infinity value
     * @param searchLimit      the maximal depth of a back link.
     *                         Set this value as small as you can, to prevent
     *                         long search times if the goal can not be reached.
     * @param costFunction     the cost function
     * @param sumFunction      the sum function for adding two cost values
     * @return
     */
    @Override
    public @Nullable ArcBackLink<V, A, C> search(@NonNull Iterable<V> startVertices,
                                                 @NonNull Predicate<V> goalPredicate,
                                                 @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                                 @NonNull C zero,
                                                 @NonNull C positiveInfinity,
                                                 @NonNull C searchLimit,
                                                 @NonNull TriFunction<V, V, A, C> costFunction,
                                                 @NonNull BiFunction<C, C, C> sumFunction) {
        return search(startVertices, goalPredicate, nextArcsFunction,
                new HashSet<V>()::add,
                zero,
                searchLimit.intValue(), costFunction, sumFunction);
    }

    /**
     * Search engine method.
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param visited          the set of visited vertices (see {@link AddToSet})
     * @param zero             the zero cost value
     * @param maxDepth         the maximal depth of a back link
     *                         Set this value as small as you can, to prevent
     *                         long search times if the goal can not be reached.
     * @param costFunction     the cost function
     * @param sumFunction      the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
     */
    public @Nullable ArcBackLink<V, A, C> search(@NonNull Iterable<V> startVertices,
                                                 @NonNull Predicate<V> goalPredicate,
                                                 @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                                 @NonNull AddToSet<V> visited,
                                                 @NonNull C zero,
                                                 int maxDepth,
                                                 @NonNull TriFunction<V, V, A, C> costFunction,
                                                 @NonNull BiFunction<C, C, C> sumFunction) {

        // This is breadth-first search algorithm
        Queue<ArcBackLink<V, A, C>> queue = new ArrayDeque<>(16);
        for (V root : startVertices) {
            ArcBackLink<V, A, C> rootBackLink = new ArcBackLink<>(root, null, null, zero);
            if (visited.add(root)) {
                queue.add(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            ArcBackLink<V, A, C> node = queue.remove();
            if (goalPredicate.test(node.getVertex())) {
                return node;
            }

            if (node.getDepth() < maxDepth) {
                for (Arc<V, A> next : nextArcsFunction.apply(node.getVertex())) {
                    if (visited.add(next.getEnd())) {
                        ArcBackLink<V, A, C> backLink = new ArcBackLink<>(next.getEnd(), next.getData(), node,
                                sumFunction.apply(costFunction.apply(next.getStart(), next.getEnd(), next.getData()),
                                        node.getCost()));
                        queue.add(backLink);
                    }
                }
            }
        }

        return null;
    }
}