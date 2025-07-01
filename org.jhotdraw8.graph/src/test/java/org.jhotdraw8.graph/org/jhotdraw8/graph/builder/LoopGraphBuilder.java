package org.jhotdraw8.graph.builder;

import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.collection.primitive.IntList;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;

import java.util.List;

/**
 * Builds a graph with a loop.
 * <pre>
 * a ──1─→ b
 * ↑       │
 * 1       1
 * ↓       ↓
 * d ←─1─→ c
 * </pre>
 */
public class LoopGraphBuilder {

    /**
     * Builds the graph.
     */
    public DirectedGraph<String, Integer> build() {
        SimpleMutableDirectedGraph<String, Integer> builder = new SimpleMutableDirectedGraph<>();
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

    /**
     * Builds the sets of strongly connected components in the graph.
     */
    public List<IntList> buildStronglyConnectedComponents() {
        return List.of(IntArrayList.of(0, 1, 2, 3));
    }

}
