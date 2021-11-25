/* @(#)AnyPathBuilderTest.java
 * Copyright (c) 2017 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.DumpGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.graph.path.AllPathsFinder;
import org.jhotdraw8.graph.path.SequenceFinder;
import org.jhotdraw8.graph.path.SimpleAllPathsFinder;
import org.jhotdraw8.graph.path.SimpleSequenceFinder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * AnyPathBuilderTest.
 *
 * @author Werner Randelshofer
 */
public class ArbitraryArcPathSearchAlgoTest {

    public ArbitraryArcPathSearchAlgoTest() {
    }

    private @NonNull DirectedGraph<Integer, Double> createGraph() {
        SimpleMutableDirectedGraph<Integer, Double> builder = new SimpleMutableDirectedGraph<>();

        // __|  1  |  2  |  3  |  4  |  5  |   6
        // 1 |       7.0   9.0               14.0
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
    public @NonNull List<DynamicTest> dynamicTestsFindVertexPath_3args() throws Exception {
        return Arrays.asList(
                dynamicTest("1", () -> testFindVertexPath_3args(1, 5, ImmutableLists.of(1, 6, 5))),
                dynamicTest("2", () -> testFindVertexPath_3args(1, 4, ImmutableLists.of(1, 2, 4))),
                dynamicTest("3", () -> testFindVertexPath_3args(2, 6, ImmutableLists.of(2, 1, 6)))
        );
    }


    /**
     * Test of findAnyVertexPath method, of class AnyPathBuilder.
     */
    public void testFindVertexPath_3args(@NonNull Integer start, @NonNull Integer goal, ImmutableList<Integer> expected) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        SequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        @Nullable OrderedPair<ImmutableList<Integer>, Double> actual = instance.findVertexSequence(start, goal, Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals(expected, actual.first());
    }

    @NonNull
    private SequenceFinder<Integer, Double, Double> newInstance(DirectedGraph<Integer, Double> graph) {
        SequenceFinder<Integer, Double, Double> instance = SimpleSequenceFinder.newDoubleCostInstance(
                graph::getNextArcs,
                (u, v, a) -> a,
                new ArbitraryArcPathSearchAlgo<>());
        return instance;
    }

    @NonNull
    private AllPathsFinder<Integer, Double, Double> newAllInstance(DirectedGraph<Integer, Double> graph) {
        AllPathsFinder<Integer, Double, Double> instance = new SimpleAllPathsFinder<>(graph::getNextArcs, 0.0, (u, v, a) -> a, Double::sum);
        return instance;
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFindVertexPathOverWaypoints() throws Exception {
        return Arrays.asList(
                dynamicTest("1", () -> testFindVertexPathOverWaypoints(Arrays.asList(1, 5), ImmutableLists.of(1, 6, 5))),
                dynamicTest("2", () -> testFindVertexPathOverWaypoints(Arrays.asList(1, 4), ImmutableLists.of(1, 2, 4))),
                dynamicTest("3", () -> testFindVertexPathOverWaypoints(Arrays.asList(2, 6), ImmutableLists.of(2, 1, 6))),
                dynamicTest("4", () -> testFindVertexPathOverWaypoints(Arrays.asList(1, 6, 5), ImmutableLists.of(1, 6, 5)))
        );
    }

    /**
     * Test of findAnyVertexPath method, of class AnyPathBuilder.
     */
    private void testFindVertexPathOverWaypoints(@NonNull List<Integer> waypoints, ImmutableList<Integer> expResult) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        SequenceFinder<Integer, Double, Double> instance = newInstance(graph);
        OrderedPair<ImmutableList<Integer>, Double> actual = instance.findVertexSequenceOverWaypoints(waypoints, Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals(expResult, actual.first());
    }


    private @NonNull DirectedGraph<Integer, Double> createGraph2() {
        // __|  1  |  2  |  3  |  4  |  5
        // 1 |       1.0   1.0
        // 2 |             1.0
        // 3 |                   1.0   1.0
        // 4 |                         1.0
        //
        //


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


}