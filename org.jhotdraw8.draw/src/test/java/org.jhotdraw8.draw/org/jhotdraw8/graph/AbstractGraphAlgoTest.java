/*
 * @(#)AbstractGraphAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

public abstract class AbstractGraphAlgoTest {
    /**
     * <pre>
     * a ←─1─→ b ──1─→ c ←─1─→ d
     *
     * A ←─1─→ B ──1─→ C ←─1─→ D
     * </pre>
     *
     * @return a graph with two disjoint vertex sets
     */
    protected @NonNull DirectedGraph<String, Integer> createDisjointGraph() {
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
     * <pre>
     * a ──1─→ b
     * ↑       │
     * 1       1
     * ↓       ↓
     * d ←─1─→ c
     * </pre>
     *
     * @return a graph with a loop
     */
    protected @NonNull DirectedGraph<String, Integer> createLoopGraph() {
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
    @NonNull
    protected DirectedGraph<String, Integer> createNonMSTGraph() {

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
    @NonNull
    protected DirectedGraph<String, Integer> createMSTGraph() {

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