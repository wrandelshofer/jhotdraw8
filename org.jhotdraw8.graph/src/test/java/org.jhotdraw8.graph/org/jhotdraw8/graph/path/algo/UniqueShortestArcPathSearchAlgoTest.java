/*
 * @(#)ShortestUniqueArcPathSearchAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.graph.io.AdjacencyListWriter;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * UniqueShortestPathBuilderTest.
 *
 */
public class UniqueShortestArcPathSearchAlgoTest {

    public UniqueShortestArcPathSearchAlgoTest() {
    }

    private DirectedGraph<Integer, Double> createGraph() {
        SimpleMutableDirectedGraph<Integer, Double> builder = new SimpleMutableDirectedGraph<>();

        // __|  1  |  2  |  3  |  4  |  5  |   6
        // 1 |       7.0   9.0  14.0         14.0
        // 2 | 7.0        10.0  15.0
        // 3 |                  11.0          2.0
        // 4 |                         6.0
        // 5 |                                9.0
        // 6 |14.0                     9.0
        //
        //

        builder.addVertex(1);
        builder.addVertex(2);
        builder.addVertex(3);
        builder.addVertex(4);
        builder.addVertex(5);
        builder.addVertex(6);
        builder.addBidiArrow(1, 2, 7.0);
        builder.addArrow(1, 3, 9.0);
        builder.addArrow(1, 4, 14.0);
        builder.addBidiArrow(1, 6, 14.0);
        builder.addArrow(2, 3, 10.0);
        builder.addArrow(2, 4, 15.0);
        builder.addArrow(3, 4, 11.0);
        builder.addArrow(3, 6, 2.0);
        builder.addArrow(4, 5, 6.0);
        builder.addBidiArrow(5, 6, 9.0);
        return builder;
    }

    /**
     * <pre>
     *  __|  1  |  2  |  3  |  4  |  5  |
     *  1 |       1.0   1.0
     *  2 |                   1.0
     *  3 |                   1.0
     *  4 |                         1.0
     * </pre>
     *
     * @return
     */
    private DirectedGraph<Integer, Double> createDiamondGraph() {
        SimpleMutableDirectedGraph<Integer, Double> builder = new SimpleMutableDirectedGraph<>();


        builder.addVertex(1);
        builder.addVertex(2);
        builder.addVertex(3);
        builder.addVertex(4);
        builder.addVertex(5);
        builder.addArrow(1, 2, 1.0);
        builder.addArrow(1, 3, 1.0);
        builder.addArrow(2, 4, 1.0);
        builder.addArrow(3, 4, 1.0);
        builder.addArrow(4, 5, 1.0);
        return builder;
    }


    @Test
    public void testCreateGraph() {
        final DirectedGraph<Integer, Double> graph = createGraph();

        final String expected
                = "1 -> 2, 3, 4, 6.\n"
                + "2 -> 1, 3, 4.\n"
                + "3 -> 4, 6.\n"
                + "4 -> 5.\n"
                + "5 -> 6.\n"
                + "6 -> 1, 5.";

        final String actual = new AdjacencyListWriter().write(graph);

        assertEquals(expected, actual);
    }


    @TestFactory
    public List<DynamicTest> dynamicTestsFindShortestVertexPath() {
        DirectedGraph<Integer, Double> graph = createGraph();
        DirectedGraph<Integer, Double> diamondGraph = createDiamondGraph();
        return Arrays.asList(
                dynamicTest("graph.nonunique", () -> testFindShortestVertexPath(graph, 1, 5, null, 0.0)),
                dynamicTest("graph.2", () -> testFindShortestVertexPath(graph, 1, 4, VectorList.of(1, 4), 14.0)),
                dynamicTest("graph.3", () -> testFindShortestVertexPath(graph, 2, 6, VectorList.of(2, 3, 6), 12.0)),
                dynamicTest("graph.nopath", () -> testFindShortestVertexPath(graph, 2, 99, null, 0.0)),
                dynamicTest("diamond.1.nonunique", () -> testFindShortestVertexPath(diamondGraph, 1, 4, null, 0.0)),
                dynamicTest("diamond.2.nonunique", () -> testFindShortestVertexPath(diamondGraph, 1, 5, null, 0.0))
        );
    }

    /**
     * Test of findAnyPath method, of class UniqueShortestPathBuilder.
     */
    public void testFindShortestVertexPath(DirectedGraph<Integer, Double> graph, Integer start, Integer goal, PersistentList<Integer> expPath, double expCost) throws Exception {

        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        SimpleOrderedPair<PersistentList<Integer>, Double> result = instance.findVertexSequence(start, goal,
                Integer.MAX_VALUE, Double.MAX_VALUE);
        if (result == null) {
            assertNull(expPath);
        } else {
            assertEquals(expPath, result.first());
            assertEquals(expCost, result.second().doubleValue());
        }
    }

    private CombinedSequenceFinder<Integer, Double, Double> newInstance(DirectedGraph<Integer, Double> graph) {
        Function3<Integer, Integer, Double, Double> costf = (a, b, arg) -> arg;
        CombinedSequenceFinder<Integer, Double, Double> instance = SimpleCombinedSequenceFinder.newDoubleCostInstance(
                graph::getNextArcs, costf, new UniqueShortestArcPathSearchAlgo<>());
        return instance;
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsFindShortestEdgeMultiGoalPath() throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        DirectedGraph<Integer, Double> diamondGraph = createDiamondGraph();
        return Arrays.asList(
                dynamicTest("graph.1.nonunique", () -> testFindShortestEdgeMultiGoalPath(graph, 1, Arrays.asList(5, 6), null)),
                dynamicTest("graph.2.nonunique", () -> testFindShortestEdgeMultiGoalPath(graph, 1, Arrays.asList(4, 5), null)),
                dynamicTest("graph.3", () -> testFindShortestEdgeMultiGoalPath(graph, 2, Arrays.asList(3, 6), VectorList.of(10.0))),
                dynamicTest("graph.4.nonunique", () -> testFindShortestEdgeMultiGoalPath(graph, 1, Arrays.asList(6, 5), null)),
                dynamicTest("graph.5.nonunique", () -> testFindShortestEdgeMultiGoalPath(graph, 1, Arrays.asList(5, 4), null)),
                dynamicTest("graph.6", () -> testFindShortestEdgeMultiGoalPath(graph, 2, Arrays.asList(6, 3), VectorList.of(10.0))),
                dynamicTest("graph.7.unreachable", () -> testFindShortestEdgeMultiGoalPath(graph, 2, Arrays.asList(600, 300), null)),
                dynamicTest("diamond.1.nonunique", () -> testFindShortestEdgeMultiGoalPath(diamondGraph, 1, Arrays.asList(2, 3), null))
        );
    }

    /**
     * Test of findAnyPath method, of class UniqueShortestPathBuilder.
     */
    public void testFindShortestEdgeMultiGoalPath(DirectedGraph<Integer, Double> graph, Integer start, List<Integer> multiGoal, PersistentList<Double> expResult) throws Exception {
        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);

        // Find shortest path to any of the goals
        SimpleOrderedPair<PersistentList<Double>, Double> actualShortestPath = instance.findArrowSequence(List.of(start), multiGoal::contains,
                Integer.MAX_VALUE, Double.MAX_VALUE);
        double actualLength = actualShortestPath == null ? 0.0 : actualShortestPath.second();

        // Find a path for each individual goal, and remember the shortest path
        double individualShortestLength = Double.POSITIVE_INFINITY;
        for (Integer goal : multiGoal) {
            SimpleOrderedPair<PersistentList<Double>, Double> resultEntry = instance.findArrowSequence(start, goal,
                    Integer.MAX_VALUE, Double.MAX_VALUE);
            if (resultEntry == null) {
                assertNull(expResult);
                return;
            } else {
                PersistentList<Double> result = resultEntry.first();
                double resultLength = result.stream().mapToDouble(Double::doubleValue).sum();
                if (resultLength < individualShortestLength) {
                    individualShortestLength = resultLength;
                }
            }
        }

        assertEquals(expResult, actualShortestPath == null ? null : actualShortestPath.first());
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsFindShortestEdgePath() throws Exception {
        return Arrays.asList(
                dynamicTest("1.nonunique", () -> testFindShortestEdgePath(1, 5, null)),
                dynamicTest("2", () -> testFindShortestEdgePath(1, 4, VectorList.of(14.0))),
                dynamicTest("3", () -> testFindShortestEdgePath(2, 6, VectorList.of(10.0, 2.0)))
        );
    }

    /**
     * Test of findAnyPath method, of class UniqueShortestPathBuilder.
     */
    private void testFindShortestEdgePath(Integer start, Integer goal, PersistentList<Double> expResult) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        SimpleOrderedPair<PersistentList<Double>, Double> result = instance.findArrowSequence(start, goal, Integer.MAX_VALUE, Double.MAX_VALUE);
        assertEquals(expResult, result == null ? null : result.first());
    }

    private DirectedGraph<Integer, Double> createGraph2() {
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
    public List<DynamicTest> dynamicTestsFindShortestVertexPathOverWaypoints() throws Exception {
        return Arrays.asList(
                dynamicTest("1", () -> testFindShortestVertexPathOverWaypoints(Arrays.asList(1, 3, 5), VectorList.of(1, 3, 6, 5), 20.0)),
                dynamicTest("2", () -> testFindShortestVertexPathOverWaypoints(Arrays.asList(1, 4), VectorList.of(1, 4), 14.0)),
                dynamicTest("3", () -> testFindShortestVertexPathOverWaypoints(Arrays.asList(2, 6), VectorList.of(2, 3, 6), 12.0)),
                dynamicTest("4", () -> testFindShortestVertexPathOverWaypoints(Arrays.asList(1, 6, 5), VectorList.of(1, 3, 6, 5), 20.0))
        );
    }

    /**
     * Test of findAnyVertexPath method, of class AnyPathBuilder.
     */
    private void testFindShortestVertexPathOverWaypoints(List<Integer> waypoints, PersistentList<Integer> expResult, double expCost) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        SimpleOrderedPair<PersistentList<Integer>, Double> actual = instance.findVertexSequenceOverWaypoints(waypoints, Integer.MAX_VALUE, Double.MAX_VALUE);
        if (actual == null) {
            assertNull(expResult);
        } else {
            assertEquals(expResult, actual.first());
            assertEquals(expCost, actual.second().doubleValue());
        }
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsFindEdgePathOverWaypoints() {
        return Arrays.asList(
                dynamicTest("1.nonunique", () -> testFindEdgePathOverWaypoints(Arrays.asList(1, 5), null, 0.0)),
                dynamicTest("2", () -> testFindEdgePathOverWaypoints(Arrays.asList(1, 4), VectorList.of(14.0), 14.0)),
                dynamicTest("3", () -> testFindEdgePathOverWaypoints(Arrays.asList(2, 6), VectorList.of(10.0, 2.0), 12.0)),
                dynamicTest("4", () -> testFindEdgePathOverWaypoints(Arrays.asList(1, 6, 5), VectorList.of(9.0, 2.0, 9.0), 20.0))
        );
    }

    /**
     * Test of findAnyVertexPath method, of class AnyPathBuilder.
     */
    private void testFindEdgePathOverWaypoints(List<Integer> waypoints, PersistentList<Double> expResult, double expCost) throws Exception {
        ToDoubleFunction<Double> costf = arg -> arg;
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        SimpleOrderedPair<PersistentList<Double>, Double> actual = instance.findArrowSequenceOverWaypoints(waypoints, Integer.MAX_VALUE, Double.MAX_VALUE);
        if (actual == null) {
            assertNull(expResult);
        } else {
            assertEquals(expResult, actual.first());
            assertEquals(expCost, actual.second().doubleValue());
        }
    }
}