/*
 * @(#)DisjointSetsAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.graph.algo.DisjointSetsAlgo;
import org.jhotdraw8.graph.builder.DisjointGraphBuilder;
import org.jhotdraw8.graph.builder.LoopGraphBuilder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class DisjointSetsAlgoTest {


    @TestFactory
    public List<DynamicTest> dynamicTestsFindDisjointSets() {
        return Arrays.asList(
                dynamicTest("1", () -> testFindDisjointSets(new DisjointGraphBuilder().build(), 2)),
                dynamicTest("2", () -> testFindDisjointSets(new LoopGraphBuilder().build(), 1))
        );
    }

    void testFindDisjointSets(DirectedGraph<String, Integer> graph, int expectedSetCount) {

        List<Set<String>> actualSets = new DisjointSetsAlgo().findDisjointSets(graph);

        assertEquals(expectedSetCount, actualSets.size());
    }


}