/*
 * @(#)UniqueOrOneHopVertexPathSearchAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.io.AdjacencyListWriter;
import org.jhotdraw8.graph.path.SimpleVertexSequenceFinder;
import org.jhotdraw8.graph.path.VertexSequenceFinder;
import org.jhotdraw8.graph.path.algo.UniqueOrOneHopVertexPathSearchAlgo;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * UniqueShortestPathBuilderTest.
 *
 * @author Werner Randelshofer
 */
public class UniqueOrOneHopVertexPathSearchAlgoTest {

    public UniqueOrOneHopVertexPathSearchAlgoTest() {
    }

    private @NonNull DirectedGraph<Integer, Double> createGraph() {
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

    private @NonNull DirectedGraph<Integer, Double> createDiamondGraph() {
        SimpleMutableDirectedGraph<Integer, Double> builder = new SimpleMutableDirectedGraph<>();

        // __|  1  |  2  |  3  |  4  |  5  |
        // 1 |       1.0   1.0
        // 2 |                   1.0
        // 3 |                   1.0
        // 4 |                         1.0
        //
        //

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
    public @NonNull List<DynamicTest> dynamicTestsFindUniqueVertexPath() {
        DirectedGraph<Integer, Double> graph = createGraph();
        DirectedGraph<Integer, Double> diamondGraph = createDiamondGraph();
        return Arrays.asList(
                dynamicTest("graph.nonunique", () -> testFindUniqueVertexPath(graph, 1, 5, null)),
                dynamicTest("graph.2.nonunique but one hop", () -> testFindUniqueVertexPath(graph, 1, 4, VectorList.of(1, 4))),
                dynamicTest("graph.3", () -> testFindUniqueVertexPath(graph, 2, 6, null)),
                dynamicTest("graph.nopath", () -> testFindUniqueVertexPath(graph, 2, 99, null)),
                dynamicTest("diamond.1.nonunique", () -> testFindUniqueVertexPath(diamondGraph, 1, 4, null)),
                dynamicTest("diamond.2.nonunique", () -> testFindUniqueVertexPath(diamondGraph, 1, 5, null))
        );
    }

    /**
     * Test of findAnyPath method, of class UniqueShortestPathBuilder.
     */
    public void testFindUniqueVertexPath(@NonNull DirectedGraph<Integer, Double> graph, @NonNull Integer start, @NonNull Integer goal, ImmutableList<Integer> expPath) throws Exception {

        VertexSequenceFinder<Integer, Integer> instance = SimpleVertexSequenceFinder.newIntCostInstance(
                graph::getNextVertices, new UniqueOrOneHopVertexPathSearchAlgo<>());
        SimpleOrderedPair<ImmutableList<Integer>, Integer> result = instance.findVertexSequence(start, goal::equals, Integer.MAX_VALUE);
        if (expPath == null) {
            assertNull(result);
        } else {
            assertNotNull(result);
            assertEquals(expPath, result.first());
        }
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFindUniqueMultiGoalPath() throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        DirectedGraph<Integer, Double> diamondGraph = createDiamondGraph();
        return Arrays.asList(
                dynamicTest("graph.1.nonunique but one hop", () -> testFindUniqueMultiGoalPath(graph, 1, VectorList.of(5, 6), VectorList.of(1, 6))),
                dynamicTest("graph.2.nonunique but one hop", () -> testFindUniqueMultiGoalPath(graph, 1, VectorList.of(4, 5), VectorList.of(1, 4))),
                dynamicTest("graph.3", () -> testFindUniqueMultiGoalPath(graph, 2, VectorList.of(3, 6), VectorList.of(2, 3))),
                dynamicTest("graph.4.nonunique but one hop", () -> testFindUniqueMultiGoalPath(graph, 1, VectorList.of(6, 5), VectorList.of(1, 6))),
                dynamicTest("graph.5.nonunique but one hop", () -> testFindUniqueMultiGoalPath(graph, 1, VectorList.of(5, 4), VectorList.of(1, 4))),
                dynamicTest("graph.6.nonunique but one hop", () -> testFindUniqueMultiGoalPath(graph, 2, VectorList.of(6, 3), VectorList.of(2, 3))),
                dynamicTest("graph.7.unreachable", () -> testFindUniqueMultiGoalPath(graph, 2, VectorList.of(600, 300), null)),
                dynamicTest("diamond.1.nonunique but one hop", () -> testFindUniqueMultiGoalPath(diamondGraph, 1, VectorList.of(2, 3), VectorList.of(1, 2)))
        );
    }

    /**
     * Test of findAnyPath method, of class UniqueShortestPathBuilder.
     */
    public void testFindUniqueMultiGoalPath(@NonNull DirectedGraph<Integer, Double> graph, @NonNull Integer start, @NonNull ImmutableList<Integer> multiGoal, ImmutableList<Integer> expResult) throws Exception {
        VertexSequenceFinder<Integer, Integer> instance = SimpleVertexSequenceFinder.newIntCostInstance(
                graph::getNextVertices, new UniqueOrOneHopVertexPathSearchAlgo<>());

        // Find unique path to any of the goals
        SimpleOrderedPair<ImmutableList<Integer>, Integer> actualPath = instance.findVertexSequence(start, multiGoal::contains, Integer.MAX_VALUE);
        if (expResult == null) {
            assertNull(actualPath);
        } else {
            assertNotNull(actualPath);
            assertEquals(expResult, actualPath.first());
        }
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
    public @NonNull List<DynamicTest> dynamicTestsFindUniqueVertexPathOverWaypoints() throws Exception {
        return Arrays.asList(
                dynamicTest("1", () -> testFindUniqueVertexPathOverWaypoints(VectorList.of(1, 3, 5), null)),
                dynamicTest("2", () -> testFindUniqueVertexPathOverWaypoints(VectorList.of(1, 4), VectorList.of(1, 4))),
                dynamicTest("3", () -> testFindUniqueVertexPathOverWaypoints(VectorList.of(2, 6), null)),
                dynamicTest("4", () -> testFindUniqueVertexPathOverWaypoints(VectorList.of(1, 6, 5), VectorList.of(1, 6, 5)))
        );
    }

    /**
     * Test of findAnyVertexPath method, of class AnyPathBuilder.
     */
    private void testFindUniqueVertexPathOverWaypoints(@NonNull ImmutableList<Integer> waypoints, ImmutableList<Integer> expResult) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        VertexSequenceFinder<Integer, Integer> instance = SimpleVertexSequenceFinder.newIntCostInstance(
                graph::getNextVertices, new UniqueOrOneHopVertexPathSearchAlgo<>());
        SimpleOrderedPair<ImmutableList<Integer>, Integer> actual = instance.findVertexSequenceOverWaypoints(waypoints, Integer.MAX_VALUE);
        if (expResult == null) {
            assertNull(actual);
        } else {
            assertEquals(expResult, actual.first());
        }
    }


}