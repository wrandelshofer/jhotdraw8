/*
 * @(#)DisjointSetsAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.algo.DisjointSetsAlgo;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class DisjointSetsAlgoTest extends AbstractGraphAlgoTest {


    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFindDisjointSets() {
        return Arrays.asList(
                dynamicTest("1", () -> testFindDisjointSets(createDisjointGraph(), 2)),
                dynamicTest("2", () -> testFindDisjointSets(createLoopGraph(), 1))
        );
    }

    void testFindDisjointSets(@NonNull DirectedGraph<String, Integer> graph, int expectedSetCount) {

        List<Set<String>> actualSets = new DisjointSetsAlgo().findDisjointSets(graph);

        assertEquals(expectedSetCount, actualSets.size());
    }


}