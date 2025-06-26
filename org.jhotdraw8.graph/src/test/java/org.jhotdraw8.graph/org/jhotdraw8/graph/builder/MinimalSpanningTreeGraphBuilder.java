package org.jhotdraw8.graph.builder;

import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;

public class MinimalSpanningTreeGraphBuilder {
    /**
     * <pre>
     * A     B     C
     * |     |     |
     * 1     2     4
     * |     |     |
     * D--3--E--5--F
     * </pre>
     *
     * @return Graph with only the edges for minimal spanning tree
     */
    public DirectedGraph<String, Integer> build() {
        SimpleMutableDirectedGraph<String, Integer> builder = new SimpleMutableDirectedGraph<>();
        builder.addVertex("A");
        builder.addVertex("B");
        builder.addVertex("C");
        builder.addVertex("D");
        builder.addVertex("E");
        builder.addVertex("F");

        builder.addBidiArrow("A", "D", 1);
        builder.addBidiArrow("B", "E", 2);
        builder.addBidiArrow("C", "F", 4);
        builder.addBidiArrow("D", "E", 3);
        builder.addBidiArrow("E", "F", 5);
        return builder;
    }

}
