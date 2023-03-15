/*
 * @(#)ArbitraryArcPathSearchAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.ToIntTriFunction;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.graph.io.AdjacencyListWriter;
import org.jhotdraw8.graph.path.CombinedAllSequencesFinder;
import org.jhotdraw8.graph.path.CombinedSequenceFinder;
import org.jhotdraw8.graph.path.SimpleCombinedAllSequencesFinder;
import org.jhotdraw8.graph.path.SimpleCombinedSequenceFinder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
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

        final String actual = new AdjacencyListWriter().write(graph);

        assertEquals(expected, actual);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFindVertexPath_3args() throws Exception {
        return Arrays.asList(
                dynamicTest("1", () -> testFindVertexPath_3args(1, 5, ImmutableArrayList.of(1, 6, 5))),
                dynamicTest("2", () -> testFindVertexPath_3args(1, 4, ImmutableArrayList.of(1, 2, 4))),
                dynamicTest("3", () -> testFindVertexPath_3args(2, 6, ImmutableArrayList.of(2, 1, 6)))
        );
    }


    /**
     * Test of findAnyVertexPath method, of class AnyPathBuilder.
     */
    public void testFindVertexPath_3args(@NonNull Integer start, @NonNull Integer goal, ImmutableList<Integer> expected) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Integer> instance = newInstance(graph);
        @Nullable OrderedPair<ImmutableList<Integer>, Integer> actual = instance.findVertexSequence(start, goal,
                Integer.MAX_VALUE, Integer.MAX_VALUE, new LinkedHashSet<>()::add);
        assertNotNull(actual);
        assertEquals(expected, actual.first());
    }

    @NonNull
    private CombinedSequenceFinder<Integer, Double, Integer> newInstance(DirectedGraph<Integer, Double> graph) {
        ToIntTriFunction<Integer, Integer, Double> costFunction = (u, v, a) -> a.intValue();
        CombinedSequenceFinder<Integer, Double, Integer> instance = SimpleCombinedSequenceFinder.newIntCostInstance(
                graph::getNextArcs,
                costFunction,
                new GloballyArbitraryArcPathSearchAlgo<>());
        return instance;
    }

    @NonNull
    private CombinedAllSequencesFinder<Integer, Double, Double> newAllInstance(DirectedGraph<Integer, Double> graph) {
        CombinedAllSequencesFinder<Integer, Double, Double> instance = new SimpleCombinedAllSequencesFinder<>(graph::getNextArcs, 0.0, (u, v, a) -> a, Double::sum);
        return instance;
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFindVertexPathOverWaypoints() throws Exception {
        return Arrays.asList(
                dynamicTest("1", () -> testFindVertexPathOverWaypoints(Arrays.asList(1, 5), ImmutableArrayList.of(1, 6, 5))),
                dynamicTest("2", () -> testFindVertexPathOverWaypoints(Arrays.asList(1, 4), ImmutableArrayList.of(1, 2, 4))),
                dynamicTest("3", () -> testFindVertexPathOverWaypoints(Arrays.asList(2, 6), ImmutableArrayList.of(2, 1, 6))),
                dynamicTest("4", () -> testFindVertexPathOverWaypoints(Arrays.asList(1, 6, 5), ImmutableArrayList.of(1, 6, 5)))
        );
    }

    /**
     * Test of findAnyVertexPath method, of class AnyPathBuilder.
     */
    private void testFindVertexPathOverWaypoints(@NonNull List<Integer> waypoints, ImmutableList<Integer> expResult) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        CombinedSequenceFinder<Integer, Double, Integer> instance = newInstance(graph);
        OrderedPair<ImmutableList<Integer>, Integer> actual = instance.findVertexSequenceOverWaypoints(waypoints, Integer.MAX_VALUE, Integer.MAX_VALUE,
                () -> new LinkedHashSet<>()::add);
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