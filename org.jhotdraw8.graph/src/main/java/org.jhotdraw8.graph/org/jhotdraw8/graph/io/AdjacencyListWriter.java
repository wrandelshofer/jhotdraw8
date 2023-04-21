/*
 * @(#)AdjacencyListWriter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.DirectedGraph;

import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;

/**
 * Writes a graph as an adjacency list.
 */
public class AdjacencyListWriter {
    /**
     * Creates a new instance.
     */
    public AdjacencyListWriter() {
    }

    /**
     * Dumps the graph for debugging purposes.
     *
     * @param <V>   the vertex data type
     * @param <A>   the arrow data type
     * @param graph the graph to be dumped
     * @return the dump
     */
    public <V, A> String write(@NonNull DirectedGraph<V, A> graph) {
        StringWriter w = new StringWriter();
        try {
            write(w, graph, Object::toString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return w.toString();
    }

    /**
     * Dumps the graph for debugging purposes.
     *
     * @param <V>   the vertex data type
     * @param <A>   the arrow data type
     * @param w     the writer
     * @param graph the graph to be dumped
     * @throws java.io.IOException if writing fails
     */
    public <V, A> void write(@NonNull Appendable w, @NonNull DirectedGraph<V, A> graph) throws IOException {
        write(w, graph, Object::toString);
    }

    /**
     * Dumps the graph for debugging purposes.
     *
     * @param <V>              the vertex data type
     * @param <A>              the arrow data type
     * @param w                the writer
     * @param graph            the graph to be dumped
     * @param toStringFunction a function which converts a vertex to a string
     * @throws java.io.IOException if writing fails
     */
    public <V, A> void write(@NonNull Appendable w, @NonNull DirectedGraph<V, A> graph, @NonNull Function<V, String> toStringFunction) throws IOException {
        {
            int i = 0;
            for (V v : graph.getVertices()) {
                if (i != 0) {
                    w.append("\n");
                }
                w.append(toStringFunction.apply(v)).append(" -> ");
                int j = 0;
                for (V u : graph.getNextVertices(v)) {
                    if (j != 0) {
                        w.append(", ");
                    }
                    w.append(toStringFunction.apply(u));
                    j++;
                }
                w.append('.');
                i++;
            }
        }
    }
}
