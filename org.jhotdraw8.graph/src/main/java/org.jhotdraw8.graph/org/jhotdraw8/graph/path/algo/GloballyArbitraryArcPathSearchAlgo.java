/*
 * @(#)GloballyArbitraryArcPathSearchAlgo.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.TriFunction;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;

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
 * This algorithm <b>ignores</b> cost limit. If you need it, use one of
 * the shortest path search algorithms.
 * <p>
 * Expected run time:
 * <dl>
 *     <dt>When the algorithm returns a back link</dt><dd>less or equal O( |A| + |V| ) within max depth</dd>
 *     <dt>When the algorithm returns null</dt><dd>exactly O( |A| + |V| ) within max depth</dd>
 * </dl>
 * <p>
 * References:
 * <dl>
 *     <dt>Robert Sedgewick, Kevin Wayne. (2011)</dt>
 *     <dd>Algorithms, 4th Edition. Chapter 4. Breadth-First Search.
 *          <a href="https://www.math.cmu.edu/~af1p/Teaching/MCC17/Papers/enumerate.pdf">math.cmu.edu</a>
 *     </dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class GloballyArbitraryArcPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements ArcPathSearchAlgo<V, A, C> {
    public GloballyArbitraryArcPathSearchAlgo() {
    }

    /**
     * {@inheritDoc}
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param maxDepth         the maximal depth (inclusive) of the search
     *                         Must be {@literal >= 0}.
     * @param zero             the zero cost value
     * @param costLimit        the cost limit is <b>ignored</b>
     * @param costFunction     the cost function
     * @param sumFunction      the sum function for adding two cost values
     * @param visited
     * @return
     */
    @Override
    public @Nullable ArcBackLinkWithCost<V, A, C> search(@NonNull Iterable<V> startVertices,
                                                         @NonNull Predicate<V> goalPredicate,
                                                         @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                                         int maxDepth,
                                                         @NonNull C zero,
                                                         @NonNull C costLimit,
                                                         @NonNull TriFunction<V, V, A, C> costFunction,
                                                         @NonNull BiFunction<C, C, C> sumFunction, @NonNull AddToSet<V> visited) {
        AlgoArguments.checkZero(zero);
        return ArcBackLink.toArcBackLinkWithCost(
                search(startVertices, goalPredicate, nextArcsFunction,
                        new HashSet<V>()::add,
                        maxDepth),
                zero, costFunction, sumFunction);
    }

    /**
     * Search engine method.
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param visited          the set of visited vertices (see {@link AddToSet})
     * @param maxDepth         the maximal depth (inclusive) of the search
     *                         Must be {@literal >= 0}.
     * @return on success: a back link, otherwise: null
     */
    public @Nullable ArcBackLink<V, A> search(@NonNull Iterable<V> startVertices,
                                              @NonNull Predicate<V> goalPredicate,
                                              @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                              @NonNull AddToSet<V> visited,
                                              int maxDepth) {
        AlgoArguments.checkMaxDepth(maxDepth);

        Queue<ArcBackLink<V, A>> queue = new ArrayDeque<>(16);
        for (V s : startVertices) {
            ArcBackLink<V, A> rootBackLink = new ArcBackLink<>(s, null, null);
            if (visited.add(s)) {
                queue.add(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            ArcBackLink<V, A> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                return u;
            }

            if (u.getDepth() < maxDepth) {
                for (Arc<V, A> v : nextArcsFunction.apply(u.getVertex())) {
                    if (visited.add(v.getEnd())) {
                        queue.add(new ArcBackLink<>(v.getEnd(), v.getArrow(), u));
                    }
                }
            }
        }

        return null;
    }
}