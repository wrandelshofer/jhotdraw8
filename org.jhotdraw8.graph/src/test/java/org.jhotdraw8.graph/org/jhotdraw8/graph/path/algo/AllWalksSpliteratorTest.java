/*
 * @(#)AllWalksSpliteratorTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.graph.path.CombinedAllSequencesFinder;
import org.jhotdraw8.graph.path.SimpleCombinedAllSequencesFinder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * AnyPathBuilderTest.
 *
 * @author Werner Randelshofer
 */
public class AllWalksSpliteratorTest {

    private @NonNull CombinedAllSequencesFinder<Integer, Double, Double> newAllInstance(final DirectedGraph<Integer, Double> graph) {
        return new SimpleCombinedAllSequencesFinder<>(graph::getNextArcs, 0.0, (u, v, a) -> a, Double::sum);
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

    private @NonNull DirectedGraph<Integer, Double> createGraphWithCycles2() {
        // __|  1  |  2  |  3  |  4  |  5
        // 1 |       1.0   1.0         1.0
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
        b.addVertex(1);

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
                dynamicTest("1", () -> testFindAllPaths(graph, 1, 5, 4, Arrays.asList(
                        VectorList.of(1, 3, 5),
                        VectorList.of(1, 2, 3, 5),
                        VectorList.of(1, 3, 4, 5),
                        VectorList.of(1, 2, 3, 4, 5)
                ))),
                dynamicTest("2", () -> testFindAllPaths(graph, 1, 5, 3, Arrays.asList(
                        VectorList.of(1, 3, 5),
                        VectorList.of(1, 2, 3, 5),
                        VectorList.of(1, 3, 4, 5)
                )))
        );
    }

    private void testFindAllPaths(@NonNull DirectedGraph<Integer, Double> graph, int start, int goal, double maxCost, List<ImmutableList<Integer>> expected) {
        CombinedAllSequencesFinder<Integer, Double, Double> instance = newAllInstance(graph);
        List<ImmutableList<Integer>> actual = StreamSupport.stream(instance.findAllVertexSequences(
                        Collections.singletonList(start),
                        a -> a == goal, Integer.MAX_VALUE, maxCost).spliterator(), false)
                .map(OrderedPair::first).collect(Collectors.toList());
        assertEquals(expected, actual);
    }
}