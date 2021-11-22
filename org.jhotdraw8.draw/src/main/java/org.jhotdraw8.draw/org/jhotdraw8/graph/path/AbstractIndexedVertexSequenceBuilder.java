/*
 * @(#)AbstractIntPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public abstract class AbstractIndexedVertexSequenceBuilder implements VertexSequenceBuilder<Integer, Integer> {
    private final @NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction;
    private final int maxLength = Integer.MAX_VALUE;

    public AbstractIndexedVertexSequenceBuilder(@NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction) {
        this.nextNodesFunction = nextNodesFunction;
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<Integer>, Integer> findVertexSequence(@NonNull Iterable<Integer> startVertices, @NonNull Predicate<Integer> goalPredicate, @NonNull Integer maxCost) {
        return findShortestVertexPath(startVertices, (IntPredicate) goalPredicate::test);
    }

    @Override
    public OrderedPair<ImmutableList<Integer>, Integer> findVertexSequenceOverWaypoints(@NonNull Iterable<Integer> waypoints, @NonNull Integer maxCostBetweenWaypoints) {
        return VertexSequenceBuilder.findVertexSequenceOverWaypoints(waypoints, (start, goal) -> findVertexSequence(start, goal, maxCostBetweenWaypoints), 0, Integer::sum);
    }

    /**
     * Builds a VertexPath through the graph which goes from the specified start
     * vertex to the specified goal vertex.
     * <p>
     * This method uses a breadth first search and returns the first result that
     * it finds.
     *
     * @param start the start vertex
     * @param goal  the goal vertex
     * @return a VertexPath if traversal is possible, null otherwise
     */
    public @Nullable OrderedPair<ImmutableList<Integer>, Integer> findShortestVertexPath(int start, int goal) {
        return findShortestVertexPath(Collections.singletonList(start), (IntPredicate) i -> i == goal);
    }

    /**
     * Checks whether a VertexPath through the graph which goes from the specified start
     * vertex to the specified goal vertex exists.
     * <p>
     * This method uses a breadth first search.
     *
     * @param start the start vertex
     * @param goal  the goal vertex
     * @return true if a traversal is possible, false otherwise
     */
    public boolean isReachable(int start, int goal) {
        return isReachable(start, (IntPredicate) i -> i == goal);
    }


    /**
     * Builds a VertexPath through the graph which goes from the specified start
     * vertex to the specified goal vertex.
     * <p>
     * This method uses a breadth first search and returns the first result that
     * it finds.
     * <p>
     * References:
     * <dl>
     *     <dt>Wikipedia, Dijkstra's algorithm, Practical optimizations and infinite graphs</dt>
     *     <dd><a href="https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm#Practical_optimizations_and_infinite_graphs">
     *      wikipedia.org</a></dd>
     * </dl>
     *
     * @param starts         the start vertex
     * @param goalPredicate the goal predicate
     * @return a VertexPath if traversal is possible, null otherwise
     */
    public @Nullable OrderedPair<ImmutableList<Integer>, Integer> findShortestVertexPath(Iterable<Integer> starts, @NonNull IntPredicate goalPredicate) {
        return findShortestVertexPath(starts, goalPredicate, addToBitSet(new BitSet()));
    }

    public @Nullable OrderedPair<ImmutableList<Integer>, Integer> findShortestVertexPath(int start, @NonNull IntPredicate goalPredicate, @NonNull AddToIntSet visited) {
        return findShortestVertexPath(Collections.singletonList(start), goalPredicate, visited);
    }

    public @Nullable OrderedPair<ImmutableList<Integer>, Integer> findShortestVertexPath(Iterable<Integer> starts, @NonNull IntPredicate goalPredicate, @NonNull AddToIntSet visited) {
        IndexedBackLink current = search(starts, goalPredicate, visited);
        if (current == null) {
            return null;
        }
        Deque<Integer> vertices = new ArrayDeque<Integer>();
        for (IndexedBackLink i = current; i != null; i = i.getParent()) {
            vertices.addFirst(i.getVertex());
        }
        return new OrderedPair<>(ImmutableLists.copyOf(vertices), vertices.size());
    }


    public boolean isReachable(int start, @NonNull IntPredicate goalPredicate) {
        return isReachable(start, goalPredicate, addToBitSet(new BitSet()));
    }

    public static AddToIntSet addToBitSet(BitSet bitSet) {
        return i -> {
            boolean b = bitSet.get(i);
            if (!b) {
                bitSet.set(i);
            }
            return !b;
        };
    }


    /**
     * Builds a VertexPath through the graph which traverses the specified
     * waypoints.
     * <p>
     * This method uses a breadth first path search between waypoints.
     *
     * @param waypoints waypoints, the iteration sequence of this collection
     *                  determines how the waypoints are traversed
     * @return a VertexPath if traversal is possible, null otherwise
     */
    public @Nullable ImmutableList<Integer> findVertexSequenceOverWaypoints(@NonNull Iterable<Integer> waypoints) {
        try {
            return findVertexPathOverWaypointsNonNull(waypoints);
        } catch (PathBuilderException e) {
            return null;
        }
    }

    /**
     * Builds a VertexPath through the graph which traverses the specified
     * waypoints.
     * <p>
     * This method uses a breadth first path search between waypoints.
     *
     * @param waypoints waypoints, the iteration sequence of this collection
     *                  determines how the waypoints are traversed
     * @return a VertexPath
     * @throws PathBuilderException if the path cannot be constructed
     */
    public @Nullable ImmutableList<Integer> findVertexPathOverWaypointsNonNull(@NonNull Iterable<Integer> waypoints) throws PathBuilderException {
        Iterator<Integer> i = waypoints.iterator();
        List<Integer> pathElements = new ArrayList<>(16);
        if (!i.hasNext()) {
            return null;
        }
        int start = i.next();
        pathElements.add(start); // root element
        while (i.hasNext()) {
            int goal = i.next();
            IndexedBackLink back = search(Collections.singletonList(start), vi -> vi == goal,
                    new LinkedHashSet<>()::add);
            if (back == null) {
                throw new PathBuilderException("Could not find path from " + start + " to " + goal + ".");
            } else {
                int index = pathElements.size();
                for (; back.getParent() != null; back = back.getParent()) {
                    pathElements.add(index, back.getVertex());
                }
            }
            start = goal;
        }
        return ImmutableLists.copyOf(pathElements);
    }

    public @NonNull Function<Integer, Spliterator.OfInt> getNextNodesFunction() {
        return nextNodesFunction;
    }

    private @Nullable AbstractIndexedVertexSequenceBuilder.IndexedBackLink search(Iterable<Integer> start,
                                                                                  @NonNull IntPredicate goalPredicate,
                                                                                  @NonNull AddToIntSet visited) {
        return search(start, goalPredicate, nextNodesFunction, visited, maxLength);
    }

    private boolean isReachable(int start,
                                @NonNull IntPredicate goalPredicate,
                                @NonNull AddToIntSet visited) {
        return isReachable(start, goalPredicate, nextNodesFunction, visited, maxLength);
    }

    protected abstract @Nullable AbstractIndexedVertexSequenceBuilder.IndexedBackLink search(Iterable<Integer> start,
                                                                                             IntPredicate goal,
                                                                                             Function<Integer, Spliterator.OfInt> nextNodesFunction,
                                                                                             @NonNull AddToIntSet visited, int maxLength);

    protected abstract boolean isReachable(int start,
                                           IntPredicate goal,
                                           Function<Integer, Spliterator.OfInt> nextNodesFunction,
                                           @NonNull AddToIntSet visited, int maxLength);

    protected abstract static class IndexedBackLink {
        public IndexedBackLink() {
        }

        abstract IndexedBackLink getParent();

        abstract int getVertex();
    }

}
