/*
 * @(#)AllWalksSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jhotdraw8.collection.enumerator.AbstractEnumerator;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jhotdraw8.icollection.persistent.PersistentList;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;

/// Iterates over all walks from a set of start vertices to a set of goal
/// vertices using a breadth-first search.
///
/// Only enumerates walks that contain a goal once.
///
/// Expected run time: The enumeration of all walks in a graph is NP-complete.
/// (Because counting paths is #P-complete).
///
/// Only run this algorithm on acyclic graphs. In graphs with cycles the
/// algorithm may run out of space, and will then throw a
/// [IllegalStateException].
/// In an acyclic graph, the algorithm will return paths (a path is a walk
/// that only contains each vertex once).
///
/// References:
/// <dl>
///     <dt>Leslie G. Valiant. (1979)</dt>
///     <dd>The Complexity of Enumeration and Reliability Problems.
///         Chapter 4. Some #P-complete problems. Item 11. S-T CONNECTEDNESS
///        <a href="https://www.math.cmu.edu/~af1p/Teaching/MCC17/Papers/enumerate.pdf">math.cmu.edu</a>
///     </dd>
/// </dl>
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
/// @param <C> the cost number type
/// @param <E> the element type of the path
public class AllWalksSpliterator<V, A, E> extends AbstractEnumerator<OrderedPair<PersistentList<E>, Integer>> {
    private final Queue<ArcBackLinkWithCost<V, A>> queue = new ArrayDeque<>();
    private final Predicate<V> goalPredicate;
    private final int maxDepth;
    private final int maxCost;
    private final ToIntFunction3<V, V, A> costFunction;
    private final Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final Function<ArcBackLinkWithCost<V, A>,
            OrderedPair<PersistentList<E>, Integer>> sequenceFunction;

    /// Creates a new instance.
    ///
    /// @param startVertices    the set of start vertices
    /// @param goalPredicate    the goal predicate
    /// @param nextArcsFunction the next arcs function
    /// @param sequenceFunction the function that maps back links to a sequence
    /// @param maxDepth         the maximal depth (inclusive) of the search
    ///                         Must be {@literal >= 0}.
    /// @param maxCost          the maximal cost (inclusive) of a sequence
    ///                         Must be {@literal >= zero}.
    /// @param costFunction     the cost function.
    public AllWalksSpliterator(Iterable<V> startVertices,
                               Predicate<V> goalPredicate,
                               Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                               Function<ArcBackLinkWithCost<V, A>,
                                       OrderedPair<PersistentList<E>, Integer>> sequenceFunction,
                               int maxDepth,
                               int maxCost,
                               ToIntFunction3<V, V, A> costFunction) {
        super(Long.MAX_VALUE, 0);
        AlgoArguments.checkMaxDepthMaxCostArguments(maxDepth, maxCost);

        this.maxDepth = maxDepth;
        this.maxCost = maxCost;
        this.goalPredicate = goalPredicate;
        this.nextArcsFunction = nextArcsFunction;
        this.sequenceFunction = sequenceFunction;

        this.costFunction = new CheckedNonNegativeArcCostFunction3<>(costFunction);
        for (V start : startVertices) {
            queue.add(new ArcBackLinkWithCost<>(start, null, null, 0));
        }

    }

    /// {@inheritDoc}
    ///
    /// @return true on success
    /// @throws IllegalStateException if the underlying queue runs out of space
    @Override
    public boolean moveNext() {
        while (!queue.isEmpty()) {
            ArcBackLinkWithCost<V, A> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                this.current = sequenceFunction.apply(u);
                return true;
            }
            if (u.getDepth() < maxDepth) {
                for (Arc<V, A> v : nextArcsFunction.apply(u.getVertex())) {
                    int cost = (u.getCost() + costFunction.apply(u.getVertex(), v.getEnd(), v.getArrow()));
                    if (cost <= maxCost) {
                        ArcBackLinkWithCost<V, A> newNode = new ArcBackLinkWithCost<>(v.getEnd(), v.getArrow(), u, cost);
                        queue.add(newNode);
                    }
                }
            }
        }
        return false;
    }
}
