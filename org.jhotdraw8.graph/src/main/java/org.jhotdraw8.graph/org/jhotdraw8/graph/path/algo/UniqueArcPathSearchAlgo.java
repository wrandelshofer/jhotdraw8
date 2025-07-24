/*
 * @(#)UniqueOnDigArcPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithAncestorSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.SequencedMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Searches a globally unique vertex path from a set of start vertices to a
 * set of goal vertices using a breadth-first search algorithm on a directed
 * (potentially cyclic) graph (DIG).
 * <p>
 * Uniqueness is global up to (inclusive) the specified maximal depth.
 * <p>
 * This algorithm <b>ignores</b> cost limit. If you need it, use one of
 * the shortest path search algorithms.
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
 *          <a href="https://algs4.cs.princeton.edu/home/">algs4.cs.princeton.edu</a></dd>
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
public class UniqueArcPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements ArcPathSearchAlgo<V, A, C> {
    private enum SearchResultType {SUCCESS_UNIQUE_PATH, FAILURE_NO_PATH, FAILURE_NOT_UNIQUE}

    public UniqueArcPathSearchAlgo() {
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
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            int maxDepth,
            C zero,
            C costLimit,
            Function3<V, V, A, C> costFunction,
            BiFunction<C, C, C> sumFunction, AddToSet<V> visited) {
        AlgoArguments.checkZero(zero);
        return ArcBackLinkWithAncestorSet.toArcBackLinkWithCost(
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
     * @return on success: a back link, otherwise: null
     */
    public @Nullable ArcBackLinkWithAncestorSet<V, A> search(
            final Iterable<V> startVertices,
            final Predicate<V> goalPredicate,
            final Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            final int maxDepth) {

        ArcBackLinkWithAncestorSet<V, A> result = null;
        for (final V startVertex : StreamSupport.stream(startVertices.spliterator(), false).collect(Collectors
                .toCollection(LinkedHashSet::new))) {
            final SimpleOrderedPair<SearchResultType, ArcBackLinkWithAncestorSet<V, A>> innerResult
                    = searchSingleStartVertex(startVertex, goalPredicate, nextArcsFunction, maxDepth);
            final SearchResultType resultType = innerResult.first();
            final @Nullable ArcBackLinkWithAncestorSet<V, A> backLink = innerResult.second();

            if (resultType == SearchResultType.FAILURE_NOT_UNIQUE) {
                return null; // Duplicate!
            } else if (resultType == SearchResultType.SUCCESS_UNIQUE_PATH) {
                if (result == null) {
                    result = backLink;
                } else {
                    return null; // Duplicate!
                }
            }
        }
        return result;
    }

    /**
     * Search engine method with a single start vertex.
     * <p>
     * This algorithm does not work with start sets with
     * multiple vertices because the used visited set
     * cannot distinguish from where a vertex is visited
     * and whether it is visited on a path or a walk
     * (the latter is ignored when determining whether
     * the result is unique).
     */
    private SimpleOrderedPair<SearchResultType, @Nullable ArcBackLinkWithAncestorSet<V, A>>
    searchSingleStartVertex(
            final V startVertex,
            final Predicate<V> goalPredicate,
            final Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            final int maxDepth) {
        AlgoArguments.checkMaxDepth(maxDepth);

        final Queue<ArcBackLinkWithAncestorSet<V, A>> queue = new ArrayDeque<>(16);
        final SequencedMap<V, Integer> visitedCount = new LinkedHashMap<>(16);
        visitedCount.put(startVertex, 1);
        queue.add(new ArcBackLinkWithAncestorSet<>(startVertex, null, null, ChampSet.of(startVertex)));

        ArcBackLinkWithAncestorSet<V, A> found = null;
        while (!queue.isEmpty()) {
            final ArcBackLinkWithAncestorSet<V, A> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                if (found != null) {
                    return new SimpleOrderedPair<>(SearchResultType.FAILURE_NOT_UNIQUE, null); // path is not unique!
                }
                found = u;
            }
            if (u.getDepth() < maxDepth) {
                PersistentSet<V> uAncestors = u.removeAncestors();
                for (final Arc<V, A> v : nextArcsFunction.apply(u.getVertex())) {
                    final PersistentSet<V> vAncestors = uAncestors.add(v.getEnd());
                    if (vAncestors != uAncestors) {//the sequence does not intersect with itself (it is a path!)
                        if (visitedCount.merge(v.getEnd(), 1, Integer::sum) == 1) {
                            final ArcBackLinkWithAncestorSet<V, A> backLink = new ArcBackLinkWithAncestorSet<>(v.getEnd(), v.getArrow(), u, vAncestors);
                            queue.add(backLink);
                        }
                    }
                }
            }
        }

        // Check if any of the preceding nodes has a non-unique path
        for (ArcBackLinkWithAncestorSet<V, A> node = found; node != null; node = node.getParent()) {
            if (visitedCount.get(node.getVertex()) > 1) {
                return new SimpleOrderedPair<>(SearchResultType.FAILURE_NOT_UNIQUE, null); // path is not unique!
            }
        }

        return found == null
                ? new SimpleOrderedPair<>(SearchResultType.FAILURE_NO_PATH, null)
                : new SimpleOrderedPair<>(SearchResultType.SUCCESS_UNIQUE_PATH, found);
    }
}


