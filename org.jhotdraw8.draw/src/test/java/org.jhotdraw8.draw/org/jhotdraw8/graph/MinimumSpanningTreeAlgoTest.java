package org.jhotdraw8.graph;

import org.jhotdraw8.collection.MappedList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.algo.MinimumSpanningTreeAlgo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinimumSpanningTreeAlgoTest extends AbstractGraphAlgoTest {


    @Test
    public void findMinimumSpanningTreeGraphToDoubleFunction() {
        DirectedGraph<String, Integer> nonMst = createNonMSTGraph();
        DirectedGraph<String, Integer> expectedMst = createMSTGraph();
        SimpleMutableDirectedGraph<String, Integer> actualMst = new MinimumSpanningTreeAlgo().findMinimumSpanningTreeGraph(nonMst, Function.identity());
        assertEquals(DumpGraph.dumpAsAdjacencyList(expectedMst), DumpGraph.dumpAsAdjacencyList(actualMst));
    }

    @Test
    public void findMinimumSpanningTreeGraphTriFunction() {
        DirectedGraph<String, Integer> nonMst = createNonMSTGraph();
        DirectedGraph<String, Integer> expectedMst = createMSTGraph();
        SimpleMutableDirectedGraph<String, Integer> actualMst = new MinimumSpanningTreeAlgo().findMinimumSpanningTreeGraph(
                nonMst, (va, vb, a) -> a);
        assertEquals(DumpGraph.dumpAsAdjacencyList(expectedMst), DumpGraph.dumpAsAdjacencyList(actualMst));
    }

    @Test
    public void findMinimumSpanningTreeCollectionArrows() {
        DirectedGraph<String, Integer> nonMst = createNonMSTGraph();
        DirectedGraph<String, Integer> expectedMst = createMSTGraph();
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
                        arcs, a -> new OrderedPair<String, String>(a.getStart(), a.getEnd())),
                includedList, rejectedList);
        assertEquals(DumpGraph.dumpAsAdjacencyList(expectedMst), DumpGraph.dumpAsAdjacencyList(actualMst));
    }


}