/*
 * @(#)AllWalksSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.collection.enumerator.AbstractEnumerator;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Iterates over all walks from a set of start vertices to a set of goal
 * vertices using a breadth-first search.
 * <p>
 * Only enumerates walks that contain a goal once.
 * <p>
 * Expected run time: The enumeration of all walks in a graph is NP-complete.
 * (Because counting paths is #P-complete).
 * <p>
 * Only run this algorithm on acyclic graphs. In graphs with cycles the
 * algorithm may run out of space, and will then throw a
 * {@link IllegalStateException}.
 * In an acyclic graph, the algorithm will return paths (a path is a walk
 * that only contains each vertex once).
 * <p>
 * References:
 * <dl>
 *     <dt>Leslie G. Valiant. (1979)</dt>
 *     <dd>The Complexity of Enumeration and Reliability Problems.
 *         Chapter 4. Some #P-complete problems. Item 11. S-T CONNECTEDNESS
 *        <a href="https://www.math.cmu.edu/~af1p/Teaching/MCC17/Papers/enumerate.pdf">math.cmu.edu</a>
 *     </dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 * @param <E> the element type of the path
 */
public class AllWalksSpliterator<V, A, C extends Number & Comparable<C>, E> extends AbstractEnumerator<OrderedPair<ImmutableList<E>, C>> {
    private final @NonNull Queue<ArcBackLinkWithCost<V, A, C>> queue = new ArrayDeque<>();
    private final @NonNull Predicate<V> goalPredicate;
    private final int maxDepth;
    private final @NonNull C maxCost;
    private final @NonNull Function3<V, V, A, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final @NonNull Function<ArcBackLinkWithCost<V, A, C>,
            OrderedPair<ImmutableList<E>, C>> sequenceFunction;

    /**
     * Creates a new instance.
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param sequenceFunction the function that maps back links to a sequence
     * @param maxDepth         the maximal depth (inclusive) of the search
     *                         Must be {@literal >= 0}.
     * @param maxCost          the maximal cost (inclusive) of a sequence
     *                         Must be {@literal >= zero}.
     * @param zero             the zero cost value
     * @param costFunction     the cost function.
     * @param sumFunction      the function for adding two cost values
     */
    public AllWalksSpliterator(@NonNull Iterable<V> startVertices,
                               @NonNull Predicate<V> goalPredicate,
                               @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                               @NonNull Function<ArcBackLinkWithCost<V, A, C>,
                                       OrderedPair<ImmutableList<E>, C>> sequenceFunction,
                               int maxDepth,
                               @NonNull C maxCost,
                               @NonNull C zero,
                               @NonNull Function3<V, V, A, C> costFunction,
                               @NonNull BiFunction<C, C, C> sumFunction) {
        super(Long.MAX_VALUE, 0);
        AlgoArguments.checkMaxDepthMaxCostArguments(maxDepth, zero, maxCost);

        this.maxDepth = maxDepth;
        this.maxCost = maxCost;
        this.goalPredicate = goalPredicate;
        this.nextArcsFunction = nextArcsFunction;
        this.sequenceFunction = sequenceFunction;

        this.costFunction = new CheckedNonNegativeArcCostFunction3<>(zero, costFunction);
        this.sumFunction = sumFunction;
        for (V start : startVertices) {
            queue.add(new ArcBackLinkWithCost<>(start, null, null, zero));
        }

    }

    /**
     * {@inheritDoc}
     *
     * @return true on success
     * @throws IllegalStateException if the underlying queue runs out of space
     */
    @Override
    public boolean moveNext() {
        while (!queue.isEmpty()) {
            ArcBackLinkWithCost<V, A, C> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                this.current = sequenceFunction.apply(u);
                return true;
            }
            if (u.getDepth() < maxDepth) {
                for (Arc<V, A> v : nextArcsFunction.apply(u.getVertex())) {
                    C cost = sumFunction.apply(u.getCost(), costFunction.apply(u.getVertex(), v.getEnd(), v.getArrow()));
                    if (cost.compareTo(maxCost) <= 0) {
                        ArcBackLinkWithCost<V, A, C> newNode = new ArcBackLinkWithCost<>(v.getEnd(), v.getArrow(), u, cost);
                        queue.add(newNode);
                    }
                }
            }
        }
        return false;
    }
}
