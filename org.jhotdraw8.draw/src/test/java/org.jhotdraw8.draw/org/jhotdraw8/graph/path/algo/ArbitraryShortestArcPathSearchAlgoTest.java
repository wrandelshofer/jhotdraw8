/*
 * @(#)ArbitraryShortestArcPathSearchAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.DumpGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.graph.path.CombinedSequenceFinder;
import org.jhotdraw8.graph.path.SimpleCombinedSequenceFinder;
import org.jhotdraw8.util.TriFunction;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Tests {@link CombinedSequenceFinder}.
 *
 * @author Werner Randelshofer
 */
public class ArbitraryShortestArcPathSearchAlgoTest {

    public ArbitraryShortestArcPathSearchAlgoTest() {
    }

    /**
     * <pre>
     * __|  1  |  2  |  3  |  4  |  5  |   6
     * 1 |       7.0   9.0               14.0
     * 2 | 7.0        10.0  15.0
     * 3 |                  11.0          2.0
     * 4 |                         6.0
     * 5 |                                9.0
     * 6 |14.0                     9.0
     * </pre>
     *
     * @return
     */
    private @NonNull DirectedGraph<Integer, Double> createGraph() {
        SimpleMutableDirectedGraph<Integer, Double> builder = new SimpleMutableDirectedGraph<>();


        builder.addVertex(1);
        builder.addVertex(2);
        builder.addVertex(3);
        builder.addVertex(4);
        builder.addVertex(5);
        builder.addVertex(6);
        builder.addBidiArrow(1, 2, 7.0);
        builder.addArrow(1, 3, 9.0);
        builder.addBidiArrow(1, 6, 14.0);
        builder.addArrow(2, 3, 10.0);
        builder.addArrow(2, 4, 15.0);
        builder.addArrow(3, 4, 11.0);
        builder.addArrow(3, 6, 2.0);
        builder.addArrow(4, 5, 6.0);
        builder.addBidiArrow(5, 6, 9.0);
        return builder;
    }


    @Test
    public void testCreateGraph() {
        final DirectedGraph<Integer, Double> graph = createGraph();

        final String expected
                = "1 -> 2, 3, 6.\n"
                + "2 -> 1, 3, 4.\n"
                + "3 -> 4, 6.\n"
                + "4 -> 5.\n"
                + "5 -> 6.\n"
                + "6 -> 1, 5.";

        final String actual = DumpGraph.dumpAsAdjacencyList(graph);

        assertEquals(expected, actual);
    }


    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFindShortestVertexPath() {
        return Arrays.asList(
                dynamicTest("0", () -> doFindShortestVertexPath(1, 1, ImmutableLists.of(1), 0.0)),
                dynamicTest("1", () -> doFindShortestVertexPath(1, 5, ImmutableLists.of(1, 3, 6, 5), 20.0)),
                dynamicTest("2", () -> doFindShortestVertexPath(1, 4, ImmutableLists.of(1, 3, 4), 20.0)),
                dynamicTest("3", () -> doFindShortestVertexPath(2, 6, ImmutableLists.of(2, 3, 6), 12.0))
        );
    }

    /**
     * Test of findAnyPath method, of class AnyShortestPathBuilder.
     */
    public void doFindShortestVertexPath(@NonNull Integer start, @NonNull Integer goal, ImmutableList<Integer> expPath, double expCost) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        OrderedPair<ImmutableList<Integer>, Double> result = instance.findVertexSequence(start, goal,
                Double.MAX_VALUE);
        if (result == null) {
            assertNull(expPath);
        } else {
            assertEquals(expPath, result.first());
            assertEquals(expCost, result.second().doubleValue());
        }
    }

    @NonNull
    private CombinedSequenceFinder<Integer, Double, Double> newInstance(DirectedGraph<Integer, Double> graph) {
        TriFunction<Integer, Integer, Double, Double> costf = (v1, v2, arg) -> arg;
        CombinedSequenceFinder<Integer, Double, Double> instance = SimpleCombinedSequenceFinder.newDoubleCostInstance(
                graph::getNextArcs, costf,
                new ShortestArbitraryArcPathSearchAlgo<>());
        return instance;
    }

    @TestFactory
    public @NonNull List<DynamicTest> testFindShortestEdgeMultiGoalPath() throws Exception {
        return Arrays.asList(
                dynamicTest("0", () -> doFindShortestEdgeMultiGoalPath(1, Arrays.asList(1, 6), ImmutableLists.of())),
                dynamicTest("1", () -> doFindShortestEdgeMultiGoalPath(1, Arrays.asList(5, 6), ImmutableLists.of(9.0, 2.0))),
                dynamicTest("2", () -> doFindShortestEdgeMultiGoalPath(1, Arrays.asList(4, 5), ImmutableLists.of(9.0, 11.0))),
                dynamicTest("3", () -> doFindShortestEdgeMultiGoalPath(2, Arrays.asList(3, 6), ImmutableLists.of(10.0))),
                dynamicTest("4", () -> doFindShortestEdgeMultiGoalPath(1, Arrays.asList(6, 5), ImmutableLists.of(9.0, 2.0))),
                dynamicTest("5", () -> doFindShortestEdgeMultiGoalPath(1, Arrays.asList(5, 4), ImmutableLists.of(9.0, 11.0))),
                dynamicTest("6", () -> doFindShortestEdgeMultiGoalPath(2, Arrays.asList(6, 3), ImmutableLists.of(10.0)))
        );
    }

    /**
     * Test of findAnyPath method, of class AnyShortestPathBuilder.
     */
    public void doFindShortestEdgeMultiGoalPath(@NonNull Integer start, @NonNull List<Integer> multiGoal, ImmutableList<Double> expResult) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);

        // Find a path for each individual goal, and remember the shortest path
        ImmutableList<Double> individualShortestPath = ImmutableLists.of();
        double individualShortestCost = Double.POSITIVE_INFINITY;
        for (Integer goal : multiGoal) {
            OrderedPair<ImmutableList<Double>, Double> resultEntry = instance.findArrowSequence(start, goal,
                    Double.MAX_VALUE);
            assertNotNull(resultEntry);
            ImmutableList<Double> result = resultEntry.first();
            double resultLength = result.stream().mapToDouble(Double::doubleValue).sum();
            if (resultLength < individualShortestCost
                    || resultLength == individualShortestCost && result.size() < individualShortestPath.size()
            ) {
                individualShortestCost = resultLength;
                individualShortestPath = result;
            }
        }

        // Find shortest path to any of the goals
        OrderedPair<ImmutableList<Double>, Double> actualShortestPath = instance.findArrowSequence(List.of(start), multiGoal::contains,
                Double.MAX_VALUE);
        assertNotNull(actualShortestPath);
        double actualCost = actualShortestPath.second();

        assertEquals(individualShortestCost, actualCost);
    }

    @TestFactory
    public @NonNull List<DynamicTest> testFindShortestArrowPath() throws Exception {
        return Arrays.asList(
                dynamicTest("1", () -> doFindShortestArrowPath(1, 5, ImmutableLists.of(9.0, 2.0, 9.0))),
                dynamicTest("2", () -> doFindShortestArrowPath(1, 4, ImmutableLists.of(9.0, 11.0))),
                dynamicTest("3", () -> doFindShortestArrowPath(2, 6, ImmutableLists.of(10.0, 2.0)))
        );
    }

    /**
     * Test of findAnyPath method, of class AnyShortestPathBuilder.
     */
    private void doFindShortestArrowPath(@NonNull Integer start, @NonNull Integer goal, ImmutableList<Double> expResult) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        OrderedPair<ImmutableList<Double>, Double> result = instance.findArrowSequence(start, goal,
                Double.MAX_VALUE);
        assertEquals(expResult, result.first());
    }

    private @NonNull DirectedGraph<Integer, Double> createGraph2() {
        SimpleMutableDirectedGraph<Integer, Double> b = new SimpleMutableDirectedGraph<>();
        b.addVertex(1);
        b.addVertex(2);
        b.addVertex(3);
        b.addVertex(4);
        b.addVertex(5);

        b.addArrow(1, 2, 1.0);
        b.addArrow(1, 3, 1.0);
        b.addArrow(2, 3, 1.0);
        b.addArrow(3, 4, 1.0);
        b.addArrow(3, 5, 1.0);
        b.addArrow(4, 5, 1.0);
        return b;
    }


    @TestFactory
    public @NonNull List<DynamicTest> testFindVertexPathOverWaypoints() throws Exception {
        return Arrays.asList(
                dynamicTest("1", () -> doFindShortestVertexPathOverWaypoints(Arrays.asList(1, 5), ImmutableLists.of(1, 3, 6, 5), 20.0)),
                dynamicTest("2", () -> doFindShortestVertexPathOverWaypoints(Arrays.asList(1, 4), ImmutableLists.of(1, 3, 4), 20.0)),
                dynamicTest("3", () -> doFindShortestVertexPathOverWaypoints(Arrays.asList(2, 6), ImmutableLists.of(2, 3, 6), 12.0)),
                dynamicTest("4", () -> doFindShortestVertexPathOverWaypoints(Arrays.asList(1, 6, 5), ImmutableLists.of(1, 3, 6, 5), 20.0))
        );
    }

    /**
     * Test of findAnyVertexPath method, of class AnyPathBuilder.
     */
    private void doFindShortestVertexPathOverWaypoints(@NonNull List<Integer> waypoints, ImmutableList<Integer> expResult, double expCost) throws Exception {
        ToDoubleFunction<Double> costf = arg -> arg;
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        OrderedPair<ImmutableList<Integer>, Double> actual = instance.findVertexSequenceOverWaypoints(waypoints, Double.MAX_VALUE);
        assertEquals(expResult, actual.first());
        assertEquals(expCost, actual.second().doubleValue());
    }

    @TestFactory
    public @NonNull List<DynamicTest> testFindArrowPathOverWaypoints() throws Exception {
        return Arrays.asList(
                dynamicTest("1", () -> doFindArrowPathOverWaypoints(Arrays.asList(1, 5), ImmutableLists.of(9.0, 2.0, 9.0), 20.0)),
                dynamicTest("2", () -> doFindArrowPathOverWaypoints(Arrays.asList(1, 4), ImmutableLists.of(9.0, 11.0), 20.0)),
                dynamicTest("3", () -> doFindArrowPathOverWaypoints(Arrays.asList(2, 6), ImmutableLists.of(10.0, 2.0), 12.0)),
                dynamicTest("4", () -> doFindArrowPathOverWaypoints(Arrays.asList(1, 6, 5), ImmutableLists.of(9.0, 2.0, 9.0), 20.0))
        );
    }

    /**
     * Test of findAnyVertexPath method, of class AnyPathBuilder.
     */
    private void doFindArrowPathOverWaypoints(@NonNull List<Integer> waypoints, ImmutableList<Double> expResult, double expCost) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        OrderedPair<ImmutableList<Double>, Double> actual = instance.findArrowSequenceOverWaypoints(waypoints, Double.MAX_VALUE);
        assertEquals(expResult, actual.first());
        assertEquals(expCost, actual.second().doubleValue());
    }
}