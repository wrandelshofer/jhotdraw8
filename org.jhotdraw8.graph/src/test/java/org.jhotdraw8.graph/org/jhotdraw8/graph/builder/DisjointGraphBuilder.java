package org.jhotdraw8.graph.builder;

import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.collection.primitive.IntList;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;

import java.util.List;

/**
 * Builds a graph with two disjoint vertex sets.
 * Each disjoint vertex set contains two pairs of vertices that are strongly connected.
 * <pre>
 * a ←─1─→ b ──1─→ c ←─1─→ d
 *
 * A ←─1─→ B ──1─→ C ←─1─→ D
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>This class has been derived from JHotDraw 8</dt>
 *     <dd>Copyright © 2021 by the authors and contributors of JHotDraw. MIT License.</dd>
 * </dl>
 */
public class DisjointGraphBuilder {

    /**
     * Builds the graph.
     */
    public DirectedGraph<String, Integer> build() {
        SimpleMutableDirectedGraph<String, Integer> builder = new SimpleMutableDirectedGraph<>();
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

    /**
     * Builds the sets of strongly connected components in the graph.
     */
    public List<IntList> buildStronglyConnectedComponents() {
        return List.of(IntArrayList.of(3, 2), IntArrayList.of(1, 0), IntArrayList.of(7, 6), IntArrayList.of(5, 4));
    }

}
