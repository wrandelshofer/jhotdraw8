/*
 * @(#)StronglyConnectedComponentsAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.algo;

import org.jhotdraw8.collection.primitive.IntList;
import org.jhotdraw8.graph.DirectedGraph;
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
 * Tests class {@link StronglyConnectedComponentsAlgo}.
 */
public class StronglyConnectedComponentsAlgoTest extends AbstractGraphAlgoTest {


    @TestFactory
    public List<DynamicTest> dynamicTestsSearchStronglyConnectedComponents() {
        return Arrays.asList(
                dynamicTest("1", () -> testSearchStronglyConnectedComponents(createDisjointGraph(), createDisjointConnectedComponents())),
                dynamicTest("2", () -> testSearchStronglyConnectedComponents(createLoopGraph(), createLoopConnectedComponents())),
                dynamicTest("3", () -> testSearchStronglyConnectedComponents(createTarjanFig3Graph(), createTarjanFig3ConnectedComponents()))
        );
    }

    void testSearchStronglyConnectedComponents(DirectedGraph<String, Integer> graph, List<IntList> expectedSets) {

        List<List<String>> actualSets = new StronglyConnectedComponentsAlgo().findStronglyConnectedComponents(graph);


        Set<HashSet<String>> expectedSetsSet = expectedSets.stream().map(c -> {
            HashSet<String> s = new HashSet<String>();
            for (Integer i : c) {
                s.add(graph.getVertex(i));
            }
            return s;
        }).collect(Collectors.toSet());
        Set<HashSet<String>> actualSetsSet = actualSets.stream().map(HashSet::new).collect(Collectors.toSet());
        assertEquals(expectedSetsSet, actualSetsSet);
    }
}