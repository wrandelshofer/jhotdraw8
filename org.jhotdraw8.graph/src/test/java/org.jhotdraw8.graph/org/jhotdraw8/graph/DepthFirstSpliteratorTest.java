/*
 * @(#)DepthFirstSpliteratorTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.io.AdjacencyListWriter;
import org.jhotdraw8.graph.iterator.BfsDfsVertexSpliterator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DepthFirstSpliteratorTest.
 *
 * @author Werner Randelshofer
 */
public class DepthFirstSpliteratorTest {

    /**
     * <pre>
     * 1 ←→ 2
     * 1 → 3
     * 1 ←→ 6
     * 2 → 3
     * 2 → 4
     * 3 → 4
     * 3 → 6
     * 4 → 5
     * 5 ←→ 6
     * </pre>
     *
     * @return
     */
    static @NonNull DirectedGraph<Integer, Double> createGraph() {
        SimpleMutableDirectedGraph<Integer, Double> builder = new SimpleMutableDirectedGraph<>();
        builder.addVertex(1);
        builder.addVertex(2);
        builder.addVertex(3);
        builder.addVertex(4);
        builder.addVertex(5);
        builder.addVertex(6);
        builder.addBidiArrow(1, 2, 7.0);
        builder.addArrow(1, 3, 9.0);
        builder.addBidiArrow(1, 6, 14.0);
        builder.addArrow(2, 3, 10.0);
        builder.addArrow(2, 4, 15.0);
        builder.addArrow(3, 4, 11.0);
        builder.addArrow(3, 6, 2.0);
        builder.addArrow(4, 5, 6.0);
        builder.addBidiArrow(5, 6, 9.0);
        return builder;
    }

    public @NonNull Object[][] anyPathProvider() {
        return new Object[][]{
                {1, 5, Arrays.asList(1, 6, 5)},
                {1, 4, Arrays.asList(1, 6, 5, 3, 4)},
                {2, 6, Arrays.asList(2, 4, 5, 6)}
        };
    }

    @Test
    public void testCreateGraph() {
        final DirectedGraph<Integer, Double> graph = createGraph();

        final String expected
                = "1 -> 2, 3, 6.\n"
                + "2 -> 1, 3, 4.\n"
                + "3 -> 4, 6.\n"
                + "4 -> 5.\n"
                + "5 -> 6.\n"
                + "6 -> 1, 5.";

        final String actual = new AdjacencyListWriter().write(graph);

        assertEquals(expected, actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIterateWithAnyPathProvider() throws Exception {
        for (Object[] args : anyPathProvider()) {
            testIterate((Integer) args[0], (Integer) args[1], (List<Integer>) args[2]);
        }
    }

    /**
     * Test of findAnyVertexPath method, of class DirectedGraphPathBuilderWithArrows.
     */
    static void testIterate(Integer start, Integer goal, List<Integer> expResult) throws Exception {
        DirectedGraph<Integer, Double> graph = createGraph();
        BfsDfsVertexSpliterator<Integer> instance = new BfsDfsVertexSpliterator<>(graph::getNextVertices, start, true);
        List<Integer> result = new ArrayList<>();
        Iterator<Integer> iter = Spliterators.iterator(instance);
        while (iter.hasNext()) {
            final Integer next = iter.next();
            result.add(next);
            if (next.equals(goal)) {
                break;
            }
        }
        assertEquals(result, expResult);
    }

}
