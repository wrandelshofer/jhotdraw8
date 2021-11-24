/*
 * @(#)IntAnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.LongArrayDeque;
import org.jhotdraw8.graph.ImmutableDirectedGraph;
import org.jhotdraw8.graph.IndexedDirectedGraph;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

/**
 * Builder for creating arbitrary vertex sequences from a directed graph.
 * <p>
 * The builder searches for paths using a breadth-first search.<br>
 * Returns the first vertex sequence that it finds.<br>
 * Returns nothing if there is no vertex sequence.
 *
 * @author Werner Randelshofer
 */
public class IndexedArbitraryVertexPathFinder<C extends Number & Comparable<C>> extends AbstractIndexedVertexSequenceFinder<C> {


    /**
     * Creates a new instance.
     *
     * @param graph a graph
     */
    public IndexedArbitraryVertexPathFinder(@NonNull IndexedDirectedGraph graph,
                                            @NonNull BiFunction<Integer, Integer, C> costFunction,
                                            @NonNull C maxCost, C zero, @NonNull BiFunction<C, C, C> sumFunction) {
        this(graph::getNextVertices,
                costFunction, maxCost, zero, sumFunction);
    }


    /**
     * Creates a new instance.
     *
     * @param nextNodesFunction Accessor function to next nodes in graph.
     */
    public IndexedArbitraryVertexPathFinder(@NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction,
                                            @NonNull BiFunction<Integer, Integer, C> costFunction,
                                            @NonNull C maxCost, C zero, @NonNull BiFunction<C, C, C> sumFunction) {
        super(nextNodesFunction, costFunction, maxCost, zero, sumFunction);
    }

    public static <V, A> IndexedArbitraryVertexPathFinder<Integer> newIntCostInstance(ImmutableDirectedGraph<V, A> graph) {
        return new IndexedArbitraryVertexPathFinder<Integer>(vertex -> (Spliterator.OfInt) graph.getNextVertices(vertex), (u, v) -> 1, Integer.MAX_VALUE, 0, Integer::sum);
    }

    /**
     * Enumerates all vertex paths from start to goal up to the specified maximal path length.
     *
     * @param start   the start vertex
     * @param goal    the goal predicate
     * @param maxCost the maximal length of a path
     * @return the enumerated paths
     */
    public @NonNull List<ImmutableList<Integer>> findAllVertexPaths(int start,
                                                                    @NonNull IntPredicate goal,
                                                                    C maxCost) {
        List<IndexedVertexBackLink<C>> backlinks = new ArrayList<>();
        searchAll(start, goal,
                getNextNodesFunction(),
                backlinks, maxCost, zero, costFunction, sumFunction);
        List<ImmutableList<Integer>> vertexPaths = new ArrayList<>(backlinks.size());
        Deque<Integer> path = new ArrayDeque<>();
        for (IndexedVertexBackLink<C> list : backlinks) {
            path.clear();
            for (IndexedVertexBackLink<C> backlink = list; backlink != null; backlink = backlink.parent) {
                path.addFirst(backlink.vertex);
            }
            vertexPaths.add(ImmutableLists.copyOf(path));
        }
        return vertexPaths;
    }


    private static class MyIntConsumer implements IntConsumer {
        int value;

        @Override
        public void accept(int value) {
            this.value = value;
        }
    }

    /**
     * Searches breadth-first for a path from root to goal.
     *
     * @param starts    the starting points of the search
     * @param goal      the goal of the search
     * @param visited   a predicate with side effect. The predicate returns true
     *                  if the specified vertex has been visited, and marks the specified vertex
     *                  as visited.
     * @param maxCost the maximal path length
     * @param zero
     * @param costFunction
     * @return a back link on success, null on failure
     */
    public @Nullable IndexedVertexBackLink<C> search(@NonNull Iterable<Integer> starts,
                                                     @NonNull IntPredicate goal,
                                                     @NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction,
                                                     @NonNull AddToIntSet visited,
                                                     @NonNull C maxCost,
                                                     @NonNull C zero,
                                                     @NonNull BiFunction<Integer, Integer, C> costFunction) {
        Queue<IndexedVertexBackLink<C>> queue = new ArrayDeque<>(32);
        MyIntConsumer consumer = new MyIntConsumer();
        for (Integer start : starts) {
            IndexedVertexBackLink<C> rootBackLink = new IndexedVertexBackLink<C>(start, null, zero);
            if (visited.add(start)) {
                queue.add(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            IndexedVertexBackLink<C> node = queue.remove();
            int vertex = node.vertex;
            if (goal.test(vertex)) {
                return node;
            }

            C currentCost = node.getCost();
            Spliterator.OfInt spliterator = nextNodesFunction.apply(vertex);
            while (spliterator.tryAdvance(consumer)) {
                    if (visited.add(consumer.value)) {
C cost = sumFunction.apply(currentCost, costFunction.apply(vertex, consumer.value));
                        IndexedVertexBackLink<C> backLink = new IndexedVertexBackLink<C>(consumer.value, node,
                                cost);
                        queue.add(backLink);
                    }
            }
        }

        return null;
    }

    @Override
    protected boolean tryToReach(@NonNull Iterable<Integer> start, @NonNull IntPredicate goal, @NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction, @NonNull AddToIntSet visited, @NonNull C maxCost, @NonNull C zero, @NonNull BiFunction<Integer, Integer, C> costFunction) {
        return false;
    }

    /**
     * Searches breadth-first whether a path from root to goal exists.
     *
     * @param root    the starting point of the search
     * @param goal    the goal of the search
     * @param visited a predicate with side effect. The predicate returns true
     *                if the specified vertex has been visited, and marks the specified vertex
     *                as visited.
     * @param maxCost the maximal path length
     * @return true on success, false on failure
     */
    public @Nullable boolean tryToReach(@NonNull int root,
                                        @NonNull IntPredicate goal,
                                        @NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction,
                                        @NonNull AddToIntSet visited,
                                        @NonNull C maxCost,
                                        @NonNull C zero,
                                        @NonNull BiFunction<Integer, Integer, C> costFunction) {
        LongArrayDeque queue = new LongArrayDeque(32);
        long rootBackLink = newSearchNode(root, maxCost.intValue());

        MyIntConsumer consumer = new MyIntConsumer();
        if (visited.add(root)) {
            queue.addLast(rootBackLink);
        }

        while (!queue.isEmpty()) {
            long node = queue.removeFirst();
            int vertex = searchNodeGetVertex(node);
            if (goal.test(vertex)) {
                return true;
            }

            int maxRemaining = searchNodeGetMaxRemaining(node);
            if (maxRemaining > 0) {
                Spliterator.OfInt spliterator = nextNodesFunction.apply(vertex);
                while (spliterator.tryAdvance(consumer)) {
                    if (visited.add(consumer.value)) {
                        long backLink = newSearchNode(consumer.value, maxRemaining - 1);
                        queue.addLast(backLink);
                    }
                }
            }
        }

        return false;
    }

    /**
     * A SearchNode stores for a given vertex, how long the remaining
     * path to gaol may be until we abort the search.
     *
     * @param vertex       a vertex
     * @param maxCost number of remaining path elements until abort
     * @return a SearchNode
     */
    private long newSearchNode(int vertex, int maxCost) {
        return (long) vertex << 32 | (long) maxCost;
    }

    private int searchNodeGetVertex(long primitiveBackLink) {
        return (int) (primitiveBackLink >> 32);
    }

    private int searchNodeGetMaxRemaining(long primitiveBackLink) {
        return (int) primitiveBackLink;
    }

    private void searchAll(int startVertex, @NonNull IntPredicate goal,
                           @NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction,
                           @NonNull List<IndexedVertexBackLink<C>> backlinks,
                           @NonNull C maxCost,
                           @NonNull C zero,
                           @NonNull BiFunction<Integer, Integer, C> costFunction,
                           @NonNull BiFunction<C, C, C> sumFunction) {
        IndexedVertexBackLink<C> start = new IndexedVertexBackLink<C>(startVertex, null, zero);
        Deque<IndexedVertexBackLink<C>> stack = new ArrayDeque<>();
        stack.push(start);
        MyIntConsumer consumer = new MyIntConsumer();
        while (!stack.isEmpty()) {
            IndexedVertexBackLink<C> current = stack.pop();
            if (goal.test(current.vertex)) {
                backlinks.add(current);
            }
            final C currentCost = current.getCost();
            Spliterator.OfInt spliterator = nextNodesFunction.apply(current.vertex);
            while (spliterator.tryAdvance(consumer)) {
                final C newCost = sumFunction.apply(currentCost, costFunction.apply(current.vertex, consumer.value));
                if (newCost.compareTo(maxCost) <= 0) {
                    stack.push(new IndexedVertexBackLink<C>(consumer.value, current, newCost));
                }
            }
        }
    }

}
