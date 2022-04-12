/*
 * @(#)StronglyConnectedComponentsAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.algo.StronglyConnectedComponentsAlgo;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class StronglyConnectedComponentsAlgoTest extends AbstractGraphAlgoTest {


    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsSearchStronglyConnectedComponents() {
        return Arrays.asList(
                dynamicTest("1", () -> testSearchStronglyConnectedComponents(createDisjointGraph(), 4)),
                dynamicTest("2", () -> testSearchStronglyConnectedComponents(createLoopGraph(), 1))
        );
    }

    void testSearchStronglyConnectedComponents(@NonNull DirectedGraph<String, Integer> graph, int expectedSetCount) {

        List<List<String>> actualSets = new StronglyConnectedComponentsAlgo().findStronglyConnectedComponents(graph);

        assertEquals(expectedSetCount, actualSets.size());
    }
}