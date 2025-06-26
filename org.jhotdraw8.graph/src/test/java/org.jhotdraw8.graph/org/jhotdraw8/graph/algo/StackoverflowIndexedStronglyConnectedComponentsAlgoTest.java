/*
 * @(#)IndexedStronglyConnectedComponentsAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.algo;

import org.jhotdraw8.collection.primitive.IntList;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.ImmutableAttributed16BitIndexedDirectedGraph;
import org.jhotdraw8.graph.builder.DisjointGraphBuilder;
import org.jhotdraw8.graph.builder.LoopGraphBuilder;
import org.jhotdraw8.graph.builder.TarjanFig3GraphBuilder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Tests class {@link IndexedStronglyConnectedComponentsAlgo}.
 */
public class StackoverflowIndexedStronglyConnectedComponentsAlgoTest {


    @TestFactory
    public List<DynamicTest> dynamicTestsSearchStronglyConnectedComponents() {
        return Arrays.asList(
                dynamicTest("1", () -> testSearchStronglyConnectedComponents(new DisjointGraphBuilder().build(), new DisjointGraphBuilder().buildStronglyConnectedComponents())),
                dynamicTest("2", () -> testSearchStronglyConnectedComponents(new LoopGraphBuilder().build(), new LoopGraphBuilder().buildStronglyConnectedComponents())),
                dynamicTest("3", () -> testSearchStronglyConnectedComponents(new TarjanFig3GraphBuilder().build(), new TarjanFig3GraphBuilder().buildStronglyConnectedComponents()))
        );
    }

    void testSearchStronglyConnectedComponents(DirectedGraph<String, Integer> graph, List<IntList> expectedSets) {

        ImmutableAttributed16BitIndexedDirectedGraph<String, Integer> intGraph = new ImmutableAttributed16BitIndexedDirectedGraph<>(graph);

        List<IntList> actualSets = new StackoverflowIndexedStronglyConnectedComponentsAlgo().findStronglyConnectedComponents(intGraph);

        Set<HashSet<Integer>> expectedSetsSet = expectedSets.stream().map(HashSet::new).collect(Collectors.toSet());
        Set<HashSet<Integer>> actualSetsSet = actualSets.stream().map(HashSet::new).collect(Collectors.toSet());
        assertEquals(expectedSetsSet, actualSetsSet);
    }
}