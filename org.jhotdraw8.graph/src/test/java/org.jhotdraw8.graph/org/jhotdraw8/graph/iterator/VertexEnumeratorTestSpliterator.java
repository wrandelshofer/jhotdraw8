/*
 * @(#)VertexEnumeratorTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.iterator;

import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.io.GraphvizReader;
import org.jhotdraw8.graph.io.GraphvizWriter;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VertexEnumeratorTestSpliterator {

    private DirectedGraph<Integer, Map<String, String>> createGraph() throws Exception {
        final GraphvizReader<Integer, Map<String, String>> builder =
                new GraphvizReader<>(Integer::parseInt,
                        Function.identity());

        return builder.read(
                "graph {\n"
                        + "1; 2; 3; 4; 5; 6; 7;\n"
                        + "1 -> 2;\n"
                        + "1 -> 3;\n"
                        + "1 -> 6;\n"
                        + "2 -> 1;\n"
                        + "2 -> 3;\n"
                        + "2 -> 4;\n"
                        + "3 -> 4;\n"
                        + "3 -> 6;\n"
                        + "4 -> 5;\n"
                        + "5 -> 6;\n"
                        + "6 -> 1;\n"
                        + "6 -> 5;\n"
                        + "7 -> 2;\n"
                        + "}"
        );
    }

    public Object[][] anyPathProvider() throws Exception {
        final DirectedGraph<Integer, Map<String, String>> graph = createGraph();

        return new Object[][]{
                {graph, 1, 5, Arrays.asList(1, 2, 3, 6, 4, 5)},
                {graph, 1, 4, Arrays.asList(1, 2, 3, 6, 4)},
                {graph, 2, 6, Arrays.asList(2, 1, 3, 4, 6)}
        };
    }

    @Test
    public void testCreateGraph() throws Exception {
        final DirectedGraph<Integer, Map<String, String>> graph = createGraph();

        final String input =
                "graph {\n"
                        + "1 -> 2, 3, 6.\n"
                        + "2 -> 1, 3, 4.\n"
                        + "3 -> 4, 6.\n"
                        + "4 -> 5.\n"
                        + "5 -> 6.\n"
                        + "6 -> 1, 5.\n"
                        + "7 -> 2.\n"
                        + "}";

        final String actual = new GraphvizWriter().write(graph);
        System.out.println(actual);

        final String expected = "digraph G {\n" +
                "1\n" +
                "2\n" +
                "3\n" +
                "4\n" +
                "5\n" +
                "6\n" +
                "7\n" +
                "1 -> 2\n" +
                "1 -> 3\n" +
                "1 -> 6\n" +
                "2 -> 1\n" +
                "2 -> 3\n" +
                "2 -> 4\n" +
                "3 -> 4\n" +
                "3 -> 6\n" +
                "4 -> 5\n" +
                "5 -> 6\n" +
                "6 -> 1\n" +
                "6 -> 5\n" +
                "7 -> 2\n" +
                "}\n";


        assertEquals(expected, actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIterateWithAnyPathProvider() throws Exception {
        for (final Object[] args : anyPathProvider()) {
            testIterate((DirectedGraph<Integer, Double>) args[0], (Integer) args[1], (Integer) args[2], (List<Integer>) args[3]);
        }
    }

    public void testIterate(final DirectedGraph<Integer, Double> graph, final Integer start, final Integer goal, final List<Integer> expResult) throws Exception {
        System.out.println("testIterate start:" + start + " goal:" + goal + " expResult:" + expResult);
        final BfsDfsVertexSpliterator<Integer> instance = new BfsDfsVertexSpliterator<>(graph::getNextVertices, start, false);
        final Iterator<Integer> iterator = Spliterators.iterator(instance);
        final List<Integer> result = new ArrayList<>();
        while (iterator.hasNext()) {
            final Integer next = iterator.next();
            result.add(next);
            if (next.equals(goal)) {
                break;
            }
        }
        System.out.println("actual:" + result);
        assertEquals(expResult, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testForEachRemainingWithAnyPathProvider() throws Exception {
        for (final Object[] args : anyPathProvider()) {
            testTryAdvance((DirectedGraph<Integer, Double>) args[0], (Integer) args[1], (Integer) args[2], (List<Integer>) args[3]);
        }
    }

    public void testTryAdvance(final DirectedGraph<Integer, Double> graph, final Integer start, final Integer goal, final List<Integer> expResult) throws Exception {
        System.out.println("testForEachRemaining start:" + start + " goal:" + goal + " expResult:" + expResult);
        final BfsDfsVertexSpliterator<Integer> instance = new BfsDfsVertexSpliterator<>(graph::getNextVertices, start, false);
        final List<Integer> result = new ArrayList<>();
        while (instance.tryAdvance(result::add)) {
            if (result.get(result.size() - 1).equals(goal)) {
                break;
            }
        }

        System.out.println("actual:" + result);
        assertEquals(expResult, result);
    }

}