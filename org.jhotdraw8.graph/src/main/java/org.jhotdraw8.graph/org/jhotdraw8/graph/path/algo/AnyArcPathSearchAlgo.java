/*
 * @(#)AnyArcPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;

/// Searches an arbitrary path from a set of start vertices to a set of goal
/// vertices using a breadth-first search algorithm.
///
/// This algorithm **ignores** cost limit. If you need it, use one of
/// the shortest path search algorithms.
///
/// Expected run time:
/// <dl>
///     <dt>When the algorithm returns a back link</dt><dd>less or equal O( |A| + |V| ) within max depth</dd>
///     <dt>When the algorithm returns null</dt><dd>exactly O( |A| + |V| ) within max depth</dd>
/// </dl>
///
/// References:
/// <dl>
///     <dt>Robert Sedgewick, Kevin Wayne. (2011)</dt>
///     <dd>Algorithms, 4th Edition. Chapter 4. Breadth-First Search.
///          <a href="https://algs4.cs.princeton.edu/home/">algs4.cs.princeton.edu</a>
///     </dd>
/// </dl>
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
/// @param <C> the cost number type
public class AnyArcPathSearchAlgo<V, A> implements ArcPathSearchAlgo<V, A> {
    public AnyArcPathSearchAlgo() {
    }

    /// {@inheritDoc}
    ///
    /// @param startVertices    the set of start vertices
    /// @param goalPredicate    the goal predicate
    /// @param nextArcsFunction the next arcs function
    /// @param maxDepth         the maximal depth (inclusive) of the search
    ///                         Must be {@literal >= 0}.
    /// @param costLimit        the cost limit is **ignored**
    /// @param costFunction     the cost function
    /// @param visited
    /// @return
    public @Nullable ArcBackLinkWithCost<V, A> search(Iterable<V> startVertices,
                                                      Predicate<V> goalPredicate,
                                                      Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                                      int maxDepth,
                                                      int costLimit,
                                                      ToIntFunction3<V, V, A> costFunction,
                                                      AddToSet<V> visited) {
        return ArcBackLink.toArcBackLinkWithCost(
                search(startVertices, goalPredicate, nextArcsFunction,
                        new HashSet<V>()::add,
                        maxDepth),
                costFunction);
    }

    /// Search engine method.
    ///
    /// @param startVertices    the set of start vertices
    /// @param goalPredicate    the goal predicate
    /// @param nextArcsFunction the next arcs function
    /// @param visited          the set of visited vertices (see [AddToSet])
    /// @param maxDepth         the maximal depth (inclusive) of the search
    ///                         Must be {@literal >= 0}.
    /// @return on success: a back link, otherwise: null
    public @Nullable ArcBackLink<V, A> search(Iterable<V> startVertices,
                                              Predicate<V> goalPredicate,
                                              Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                              AddToSet<V> visited,
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