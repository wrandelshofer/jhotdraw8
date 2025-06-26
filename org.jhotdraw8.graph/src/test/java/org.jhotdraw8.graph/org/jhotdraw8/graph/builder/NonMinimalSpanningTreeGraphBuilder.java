package org.jhotdraw8.graph.builder;

import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;

public class NonMinimalSpanningTreeGraphBuilder {
    /**
     * The graph has more edges than the minimal spanning tree.
     * <p>
     * The shortest path from A to B is A→D→E→B = 5.
     * <p>
     * The shortest path from E to C is either E→C = 9, or E→F→C = 9.
     *
     * <pre>
     * A--8--B     C
     * |   / |   / |
     * 1  6  2  9  4
     * |/    |/    |
     * D--3--E--5--F
     * </pre>
     *
     * @return Graph with more edges than the minimal spanning tree.
     */
    public DirectedGraph<String, Integer> build() {

        SimpleMutableDirectedGraph<String, Integer> builder = new SimpleMutableDirectedGraph<>();
        builder.addVertex("A");
        builder.addVertex("B");
        builder.addVertex("C");
        builder.addVertex("D");
        builder.addVertex("E");
        builder.addVertex("F");

        builder.addBidiArrow("A", "B", 8);
        builder.addBidiArrow("A", "D", 1);
        builder.addBidiArrow("B", "D", 6);
        builder.addBidiArrow("B", "E", 2);
        builder.addBidiArrow("C", "E", 9);
        builder.addBidiArrow("C", "F", 4);
        builder.addBidiArrow("D", "E", 3);
        builder.addBidiArrow("E", "F", 5);
        return builder;
    }

}
