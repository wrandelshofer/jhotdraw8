package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class GraphSearchTest {
    private @NonNull DirectedGraph<String, Integer> createDisjointGraph() {
        DirectedGraphBuilder<String, Integer> builder = new DirectedGraphBuilder<>();
        builder.addVertex("a");
        builder.addVertex("b");
        builder.addVertex("c");
        builder.addVertex("d");
        builder.addVertex("A");
        builder.addVertex("B");
        builder.addVertex("C");
        builder.addVertex("D");

        builder.addBidiArrow("a", "b", 1);
        builder.addArrow("b", "c", 1);
        builder.addBidiArrow("c", "d", 1);
        builder.addBidiArrow("A", "B", 1);
        builder.addArrow("B", "C", 1);
        builder.addBidiArrow("C", "D", 1);
        return builder;
    }

    private @NonNull DirectedGraph<String, Integer> createLoopGraph() {
        DirectedGraphBuilder<String, Integer> builder = new DirectedGraphBuilder<>();
        builder.addVertex("a");
        builder.addVertex("b");
        builder.addVertex("c");
        builder.addVertex("d");

        builder.addArrow("a", "b", 1);
        builder.addArrow("b", "c", 1);
        builder.addBidiArrow("c", "d", 1);
        builder.addBidiArrow("d", "a", 1);
        return builder;
    }

    private @NonNull DirectedGraph<String, Integer> createNonMSTGraph() {
        // Graph with more edges than the minimal spanning tree:
        // A--1--B     C
        // |   / |   / |
        // 5  3  2  4  5
        // |/    |/    |
        // D--4--E--7--F

        DirectedGraphBuilder<String, Integer> builder = new DirectedGraphBuilder<>();
        builder.addVertex("A");
        builder.addVertex("B");
        builder.addVertex("C");
        builder.addVertex("D");
        builder.addVertex("E");
        builder.addVertex("F");

        builder.addBidiArrow("A", "B", 1);
        builder.addBidiArrow("A", "D", 5);
        builder.addBidiArrow("B", "D", 3);
        builder.addBidiArrow("B", "E", 2);
        builder.addBidiArrow("C", "E", 4);
        builder.addBidiArrow("C", "F", 5);
        builder.addBidiArrow("D", "E", 4);
        builder.addBidiArrow("E", "F", 7);
        return builder;
    }

    private @NonNull DirectedGraph<String, Integer> createMSTGraph() {
        // Graph with only the edges for minimal spanning tree:
        // A--1--B     C
        //     / |   / |
        //    3  2  4  5
        //  /    |/    |
        // D     E     F

        DirectedGraphBuilder<String, Integer> builder = new DirectedGraphBuilder<>();
        builder.addVertex("A");
        builder.addVertex("B");
        builder.addVertex("C");
        builder.addVertex("D");
        builder.addVertex("E");
        builder.addVertex("F");

        builder.addBidiArrow("A", "B", 1);
        builder.addBidiArrow("B", "E", 2);
        builder.addBidiArrow("B", "D", 3);
        builder.addBidiArrow("C", "E", 4);
        builder.addBidiArrow("C", "F", 5);
        return builder;
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFindDisjointSets() {
        return Arrays.asList(
                dynamicTest("1", () -> testFindDisjointSets(createDisjointGraph(), 2)),
                dynamicTest("2", () -> testFindDisjointSets(createLoopGraph(), 1))
        );
    }

    void testFindDisjointSets(@NonNull DirectedGraph<String, Integer> graph, int expectedSetCount) {

        List<Set<String>> actualSets = DisjointSets.findDisjointSets(graph);

        assertEquals(expectedSetCount, actualSets.size());
    }


    @Test
    public void findMinimumSpanningTree() {
        DirectedGraph<String, Integer> nonMst = createNonMSTGraph();
        DirectedGraph<String, Integer> expectedMst = createMSTGraph();
        DirectedGraphBuilder<String, Integer> actualMst = MinimumSpanningTree.findMinimumSpanningTreeGraph(nonMst, Integer::doubleValue);
        assertEquals(DumpGraphs.dumpAsAdjacencyList(expectedMst), DumpGraphs.dumpAsAdjacencyList(actualMst));
    }


    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsSearchStronglyConnectedComponents() {
        return Arrays.asList(
                dynamicTest("1", () -> testSearchStronglyConnectedComponents(createDisjointGraph(), 4)),
                dynamicTest("2", () -> testSearchStronglyConnectedComponents(createLoopGraph(), 1))
        );
    }

    void testSearchStronglyConnectedComponents(@NonNull DirectedGraph<String, Integer> graph, int expectedSetCount) {

        List<List<String>> actualSets = StronglyConnectedComponents.findStronglyConnectedComponents(graph);

        assertEquals(expectedSetCount, actualSets.size());
    }
}