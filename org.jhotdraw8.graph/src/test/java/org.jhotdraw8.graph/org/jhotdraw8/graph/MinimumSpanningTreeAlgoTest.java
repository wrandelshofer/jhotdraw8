/*
 * @(#)MinimumSpanningTreeAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.collection.mapped.MappedList;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.algo.MinimumSpanningTreeAlgo;
import org.jhotdraw8.graph.builder.MinimalSpanningTreeGraphBuilder;
import org.jhotdraw8.graph.builder.NonMinimalSpanningTreeGraphBuilder;
import org.jhotdraw8.graph.io.AdjacencyListWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinimumSpanningTreeAlgoTest {


    @Test
    public void findMinimumSpanningTreeGraphToDoubleFunction() {
        DirectedGraph<String, Integer> nonMst = new NonMinimalSpanningTreeGraphBuilder().build();
        DirectedGraph<String, Integer> expectedMst = new MinimalSpanningTreeGraphBuilder().build();
        SimpleMutableDirectedGraph<String, Integer> actualMst = new MinimumSpanningTreeAlgo().findMinimumSpanningTreeGraph(nonMst, Function.identity());
        assertEquals(new AdjacencyListWriter().write(expectedMst), new AdjacencyListWriter().write(actualMst));
    }

    @Test
    public void findMinimumSpanningTreeGraphTriFunction() {
        DirectedGraph<String, Integer> nonMst = new NonMinimalSpanningTreeGraphBuilder().build();
        DirectedGraph<String, Integer> expectedMst = new MinimalSpanningTreeGraphBuilder().build();
        SimpleMutableDirectedGraph<String, Integer> actualMst = new MinimumSpanningTreeAlgo().findMinimumSpanningTreeGraph(
                nonMst, (va, vb, a) -> a);
        assertEquals(new AdjacencyListWriter().write(expectedMst), new AdjacencyListWriter().write(actualMst));
    }

    @Test
    public void findMinimumSpanningTreeCollectionArrows() {
        DirectedGraph<String, Integer> nonMst = new NonMinimalSpanningTreeGraphBuilder().build();
        DirectedGraph<String, Integer> expectedMst = new MinimalSpanningTreeGraphBuilder().build();
        List<Arc<String, Integer>> arcs = new ArrayList<>();
        for (String vertex : nonMst.getVertices()) {
            arcs.addAll(nonMst.getNextArcs(vertex));
        }
        arcs.sort(Comparator.comparing(Arc::getArrow));
        List<OrderedPair<String, String>> includedList = new ArrayList<>();
        List<OrderedPair<String, String>> rejectedList = new ArrayList<>();
        SimpleMutableDirectedGraph<String, OrderedPair<String, String>> actualMst = new MinimumSpanningTreeAlgo().findMinimumSpanningTreeGraph(
                nonMst.getVertices(),
                new MappedList<>(
                        arcs, a -> new SimpleOrderedPair<String, String>(a.getStart(), a.getEnd())),
                includedList, rejectedList);
        assertEquals(new AdjacencyListWriter().write(expectedMst), new AdjacencyListWriter().write(actualMst));
    }


}