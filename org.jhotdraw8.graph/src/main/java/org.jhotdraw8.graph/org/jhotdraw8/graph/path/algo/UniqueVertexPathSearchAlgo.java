/*
 * @(#)UniqueOnDigVertexPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithAncestorSet;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jhotdraw8.icollection.PersistentHashSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Queue;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/// See [UniqueArcPathSearchAlgo] for a description of this
/// algorithm.
///
/// @param <V> the vertex data type
public class UniqueVertexPathSearchAlgo<V> implements VertexPathSearchAlgo<V> {
    private enum SearchResultType {SUCCESS_UNIQUE_PATH, FAILURE_NO_PATH, FAILURE_NOT_UNIQUE}

    public UniqueVertexPathSearchAlgo() {
    }

    /// {@inheritDoc}
    ///
    /// @param startVertices        the set of start vertices
    /// @param goalPredicate        the goal predicate
    /// @param nextVerticesFunction the next vertices function
    /// @param maxDepth             the maximal depth (inclusive) of the search
    ///                             Must be `>= 0`.
    /// @param costLimit            the cost limit is **ignored**
    /// @param costFunction         the cost function
    /// @param visited
    /// @return on success: a back link, otherwise: null
    @Override
    public @Nullable VertexBackLinkWithCost<V> search(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth,
            int costLimit,
            ToIntBiFunction<V, V> costFunction,
            AddToSet<V> visited) {
        return VertexBackLinkWithAncestorSet.toVertexBackLinkWithCost(
                search(startVertices, goalPredicate, nextVerticesFunction, maxDepth),
                costFunction);
    }

    /// Search engine method.
    ///
    /// @param startVertices        the set of start vertices
    /// @param goalPredicate        the goal predicate
    /// @param nextVerticesFunction the next vertices function
    /// @param maxDepth             the maximal depth (inclusive) of the search.
    /// @return on success: a back link, otherwise: null
    public @Nullable VertexBackLinkWithAncestorSet<V> search(
            final Iterable<V> startVertices,
            final Predicate<V> goalPredicate,
            final Function<V, Iterable<V>> nextVerticesFunction,
            final int maxDepth) {
        AlgoArguments.checkMaxDepth(maxDepth);

        VertexBackLinkWithAncestorSet<V> result = null;
        for (final V startVertex : StreamSupport.stream(startVertices.spliterator(), false).collect(Collectors.toSet())) {
            final SimpleOrderedPair<SearchResultType, VertexBackLinkWithAncestorSet<V>> innerResult
                    = searchSingleStartVertex(startVertex, goalPredicate, nextVerticesFunction, maxDepth);
            final SearchResultType resultType = innerResult.first();
            final @Nullable VertexBackLinkWithAncestorSet<V> backLink = innerResult.second();

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

    /// Search engine method with a single start vertex.
    ///
    /// This algorithm does not work with start sets with
    /// multiple vertices because the used visited set
    /// cannot distinguish from where a vertex is visited
    /// and whether it is visited on a path or a walk
    /// (the latter is ignored when determining whether
    /// the result is unique).
    private SimpleOrderedPair<SearchResultType, @Nullable VertexBackLinkWithAncestorSet<V>>
    searchSingleStartVertex(final V startVertex,
                            final Predicate<V> goalPredicate,
                            final Function<V, Iterable<V>> nextVerticesFunction,
                            final int maxDepth) {
        final Queue<VertexBackLinkWithAncestorSet<V>> queue = new ArrayDeque<>(16);
        final SequencedMap<V, Integer> visitedCount = new LinkedHashMap<>(16);
        visitedCount.put(startVertex, 1);
        queue.add(new VertexBackLinkWithAncestorSet<>(startVertex, null, PersistentHashSet.of(startVertex)));

        VertexBackLinkWithAncestorSet<V> found = null;
        while (!queue.isEmpty()) {
            final VertexBackLinkWithAncestorSet<V> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                if (found != null) {
                    return new SimpleOrderedPair<>(SearchResultType.FAILURE_NOT_UNIQUE, null);
                }
                found = u;
            }
            if (u.getDepth() < maxDepth) {
                PersistentSet<V> uAncestors = u.removeAncestors();
                for (final V v : nextVerticesFunction.apply(u.getVertex())) {
                    final PersistentSet<V> vAncestors = uAncestors.adding(v);
                    if (vAncestors != uAncestors) {//the sequence does not intersect with itself (it is a path!)
                        if (visitedCount.merge(v, 1, Integer::sum) == 1) {
                            queue.add(new VertexBackLinkWithAncestorSet<>(v, u, vAncestors));
                        }
                    }
                }
            }
        }

        // Check if any of the preceding nodes has a non-unique path
        for (VertexBackLinkWithAncestorSet<V> node = found; node != null; node = node.getParent()) {
            if (visitedCount.get(node.getVertex()) > 1) {
                return new SimpleOrderedPair<>(SearchResultType.FAILURE_NOT_UNIQUE, null);
            }
        }

        return found == null
                ? new SimpleOrderedPair<>(SearchResultType.FAILURE_NO_PATH, null)
                : new SimpleOrderedPair<>(SearchResultType.SUCCESS_UNIQUE_PATH, found);
    }
}


