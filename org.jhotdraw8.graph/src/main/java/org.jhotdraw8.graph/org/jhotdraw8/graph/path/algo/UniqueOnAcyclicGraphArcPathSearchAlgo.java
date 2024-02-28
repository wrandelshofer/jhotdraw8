/*
 * @(#)UniqueOnDagArcPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Searches a globally unique vertex path from a set of start vertices to a
 * set of goal vertices using a breadth-first search algorithm on a directed
 * acyclic graph.
 * <p>
 * Uniqueness is global up to (inclusive) the specified maximal depth.
 * <p>
 * This algorithm <b>ignores</b> cost limit. If you need it, use one of
 * the shortest path search algorithms.
 * <p>
 * The graph must be <b>acyclic</b>.
 * (If the graph has cycles, then this algorithm incorrectly considers a
 * path as non-unique, if it can be reached by a walk.)
 * <p>
 * Performance characteristics:
 * <dl>
 *     <dt>When the algorithm returns a back link</dt><dd>exactly O( |A| + |V| ) within max depth</dd>
 *     <dt>When the algorithm returns null</dt><dd>less or equal O( |A| + |V| ) within max depth</dd>
 * </dl>
 * <p>
 * References:
 * <dl>
 *     <dt>Robert Sedgewick, Kevin Wayne. (2011)</dt>
 *     <dd>Algorithms, 4th Edition. Chapter 4. Breadth-First Search.
 *          <a href="https://www.math.cmu.edu/~af1p/Teaching/MCC17/Papers/enumerate.pdf">math.cmu.edu</a></dd>
 *
 *     <dt>Sampath Kannan, Sanjeef Khanna, Sudeepa Roy. (2008)</dt>
 *     <dd>STCON in Directed Unique-Path Graphs.
 *          Chapter 2.1 Properties of Unique-Path Graphs.
 *          <a href="https://www.cis.upenn.edu/~sanjeev/papers/fsttcs08_stcon.pdf">cis.upenn.edu</a></dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class UniqueOnAcyclicGraphArcPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements ArcPathSearchAlgo<V, A, C> {
    public UniqueOnAcyclicGraphArcPathSearchAlgo() {
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
    public @Nullable ArcBackLinkWithCost<V, A, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            int maxDepth,
            @NonNull C zero,
            @NonNull C costLimit,
            @NonNull Function3<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction, @NonNull AddToSet<V> visited) {
        AlgoArguments.checkZero(zero);
        return ArcBackLink.toArcBackLinkWithCost(
                search(startVertices, goalPredicate, nextArcsFunction, maxDepth),
                zero, costFunction, sumFunction);
    }


    /**
     * Search engine method.
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param maxDepth         the maximal depth (inclusive) of the search
     *                         Must be {@literal >= 0}.
     * @return
     */
    public @Nullable ArcBackLink<V, A> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            int maxDepth) {
        AlgoArguments.checkMaxDepth(maxDepth);

        Queue<ArcBackLink<V, A>> queue = new ArrayDeque<>(16);
        SequencedMap<V, Integer> visitedCount = new LinkedHashMap<>(16);
        for (V s : startVertices) {
            if (visitedCount.put(s, 1) == null) {
                queue.add(new ArcBackLink<>(s, null, null));
            }
        }

        ArcBackLink<V, A> found = null;
        while (!queue.isEmpty()) {
            ArcBackLink<V, A> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                if (found != null) {
                    return null;// path is not unique!
                }
                found = u;
            }
            if (u.getDepth() < maxDepth) {
                for (Arc<V, A> v : nextArcsFunction.apply(u.getVertex())) {
                    if (visitedCount.merge(v.getEnd(), 1, Integer::sum) == 1) {
                        queue.add(new ArcBackLink<>(v.getEnd(), v.getArrow(), u));
                    }
                }
            }
        }

        // Check if any of the preceding nodes has a non-unique path
        for (ArcBackLink<V, A> node = found; node != null; node = node.getParent()) {
            if (visitedCount.get(node.getVertex()) > 1) {
                return null;// path is not unique!
            }
        }
        return found;
    }
}


