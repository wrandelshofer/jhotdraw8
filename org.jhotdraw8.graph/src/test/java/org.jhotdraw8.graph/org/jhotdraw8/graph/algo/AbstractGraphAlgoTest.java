/*
 * @(#)AbstractGraphAlgoTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.algo;


import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.collection.primitive.IntList;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;

import java.util.List;

/**
 * Base class for graph algorithm tests.
 */
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
    protected DirectedGraph<String, Integer> createDisjointGraph() {
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

    protected List<IntList> createDisjointConnectedComponents() {
        return List.of(IntArrayList.of(3, 2), IntArrayList.of(1, 0), IntArrayList.of(7, 6), IntArrayList.of(5, 4));
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
    protected DirectedGraph<String, Integer> createLoopGraph() {
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

    protected List<IntList> createLoopConnectedComponents() {
        return List.of(IntArrayList.of(0, 1, 2, 3));
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

    /**
     * <p>
     * References:
     * <dl>
     *     <dt>Stackoverflow. Non-recursive version of Tarjan's algorithm.
     *     Copyright Ivan Stoev. CC BY-SA 4.0 license.</dt>
     *     <dd><a href="https://stackoverflow.com/questions/46511682/non-recursive-version-of-tarjans-algorithm">stackoverflow.com</a></dd>
     * </dl>
     * <pre>
     *              ┌───┐      ┌───┐
     *   ┌─────────→│ 1 │─────→│ 2 │───────┐
     *   │          └───┘      └───┘       │
     *   │                       │         │
     * ┌───┐                     │         ↓
     * │ 8 │←────────────────────┘       ┌───┐
     * └───┘               ┌─────────────│ 3 │
     *   │                 │     ┌─────→ └───┘
     *   ↓                 │     │         ↓
     * ┌───┐               │     │       ┌───┐
     * │ 7 │ ←─────────────┘     │       │ 4 │
     * └───┘                     │       └───┘
     *   │                       │         │
     *   │          ┌───┐      ┌───┐       │
     *   └────────→ │ 6 │←─────│ 5 │←──────┘
     *              └───┘      └───┘
     * </pre>
     * Strongly connected components:
     * <pre>
     *     1, 2, 8
     *     3, 4, 5, 7
     *     6
     * </pre>
     *
     * @return a graph with a loop
     */
    protected DirectedGraph<String, Integer> createTarjanFig3Graph() {
        SimpleMutableDirectedGraph<String, Integer> builder = new SimpleMutableDirectedGraph<>();
        builder.addVertex("1");
        builder.addVertex("2");
        builder.addVertex("3");
        builder.addVertex("4");
        builder.addVertex("5");
        builder.addVertex("6");
        builder.addVertex("7");
        builder.addVertex("8");

        builder.addArrow("1", "2", 1);
        builder.addArrow("2", "3", 1);
        builder.addArrow("2", "8", 1);
        builder.addArrow("3", "4", 1);
        builder.addArrow("3", "7", 1);
        builder.addArrow("4", "5", 1);
        builder.addArrow("5", "3", 1);
        builder.addArrow("5", "6", 1);
        builder.addArrow("7", "4", 1);
        builder.addArrow("8", "1", 1);
        builder.addArrow("8", "7", 1);
        return builder;
    }

    protected List<IntList> createTarjanFig3ConnectedComponents() {
        return List.of(IntArrayList.of(0, 1, 7), IntArrayList.of(2, 3, 4, 6), IntArrayList.of(5));
    }
}