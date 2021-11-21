/* @(#)AnyPathBuilderTest.java
 * Copyright (c) 2017 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.path.AllBreadthFirstSequencesBuilder;
import org.jhotdraw8.graph.path.ArbitraryBreadthFirstSequenceBuilder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * AnyPathBuilderTest.
 *
 * @author Werner Randelshofer
 */
public class ArbitraryArcAndArrowSequenceBuilderTest {

    public ArbitraryArcAndArrowSequenceBuilderTest() {
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

        final String actual = DumpGraphAlgorithm.dumpAsAdjacencyList(graph);

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
        ArbitraryBreadthFirstSequenceBuilder<Integer, Double> instance = newInstance(graph);
        @Nullable OrderedPair<ImmutableList<Integer>, Integer> actual = instance.findVertexSequence(start, goal, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals(expected, actual.first());
    }

    @NonNull
    private ArbitraryBreadthFirstSequenceBuilder<Integer, Double> newInstance(DirectedGraph<Integer, Double> graph) {
        ArbitraryBreadthFirstSequenceBuilder<Integer, Double> instance = new ArbitraryBreadthFirstSequenceBuilder<>(graph::getNextArcs);
        return instance;
    }

    @NonNull
    private AllBreadthFirstSequencesBuilder<Integer, Double> newAllInstance(DirectedGraph<Integer, Double> graph) {
        AllBreadthFirstSequencesBuilder<Integer, Double> instance = new AllBreadthFirstSequencesBuilder<>(graph::getNextArcs);
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
        ArbitraryBreadthFirstSequenceBuilder<Integer, Double> instance = newInstance(graph);
        OrderedPair<ImmutableList<Integer>, Integer> actual = instance.findVertexSequenceOverWaypoints(waypoints, Integer.MAX_VALUE);
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

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFindAllPaths() {
        DirectedGraph<Integer, Double> graph = createGraph2();

        return Arrays.asList(
                dynamicTest("1", () -> testFindAllPaths(graph, 1, 5, 5, Arrays.asList(
                        ImmutableLists.of(1, 3, 5),
                        ImmutableLists.of(1, 3, 4, 5),
                        ImmutableLists.of(1, 2, 3, 5),
                        ImmutableLists.of(1, 2, 3, 4, 5)
                ))),
                dynamicTest("2", () -> testFindAllPaths(graph, 1, 5, 4, Arrays.asList(
                        ImmutableLists.of(1, 3, 5),
                        ImmutableLists.of(1, 3, 4, 5),
                        ImmutableLists.of(1, 2, 3, 5)
                )))
        );
    }

    private void testFindAllPaths(@NonNull DirectedGraph<Integer, Double> graph, int start, int goal, int maxLength, List<ImmutableList<Integer>> expected) {
        AllBreadthFirstSequencesBuilder<Integer, Double> instance = newAllInstance(graph);
        List<ImmutableList<Integer>> actual = ImmutableLists.ofIterable(instance.findAllVertexSequences(start,
                a -> a == goal, maxLength)).stream().map(OrderedPair::first).collect(Collectors.toList());
        assertEquals(expected, actual);
    }
}