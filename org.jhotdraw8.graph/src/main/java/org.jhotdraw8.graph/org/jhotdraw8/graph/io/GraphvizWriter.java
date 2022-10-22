/*
 * @(#)GraphvizWriter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.DirectedGraph;

import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;

/**
 * Writes a graph into a graphviz "dot" file.
 * <p>
 * Writes the following productions. <b>Does not write subgraphs!</b>
 * <pre>{@literal
 * graph        : digrap [ ID ] '{' stmt_list '}'
 * stmt_list    : [ stmt [ ';' ] stmt_list ]
 * stmt         : node_stmt
 *              | edge_stmt
 *              | attr_stmt
 *              | ID '=' ID
 *              | subgraph
 * attr_stmt    : (graph | node | edge) attr_list
 * attr_list    : '[' [ a_list ] ']' [ attr_list ]
 * a_list       : ID '=' ID [ (';' | ',') ] [ a_list ]
 * edge_stmt    : (node_id | subgraph) edgeRHS [ attr_list ]
 * edgeRHS      : edgeop (node_id | subgraph) [ edgeRHS ]
 * node_stmt    : node_id [ attr_list ]  * node_id : ID [ port ]
 * port         : ':' ID [ ':' compass_pt ]  * | ':' compass_pt
 * subgraph     : [ subgraph [ ID ] ] '{' stmt_list '}'
 * compass_pt   : (n | ne | e | se | s | sw | w | nw | c | _)
 * edgeop       : -> | --
 * }</pre>
 * <p>
 * References:
 * <dl>
 *     <dt>Graphviz. DOT Language.</dt>
 *     <dd><a href="https://graphviz.org/doc/info/lang.html">graphviz.org</a></dd>
 * </dl>
 */
public class GraphvizWriter {
    /**
     * Creates a new instance.
     */
    public GraphvizWriter() {
    }

    /**
     * Dumps a directed graph into a String which can be rendered with the
     * graphviz "dot" tool.
     *
     * @param <V>   the vertex data type
     * @param <A>   the arrow data type
     * @param w     the writer
     * @param graph the graph
     * @throws java.io.IOException if writing fails
     */
    public <V, A> void write(@NonNull Appendable w, @NonNull DirectedGraph<V, A> graph) throws IOException {
        write(w, graph, v -> "\"" + v + '"', null, null, null);
    }

    /**
     * Dumps a directed graph into a String which can be rendered with the
     * graphviz "dot" tool.
     *
     * @param <V>            the vertex data type
     * @param <A>            the arrow data type
     * @param w              the writer
     * @param graph          the graph
     * @param vertexToString a function that converts a vertex to a String for
     *                       use as vertex name
     * @throws java.io.IOException if writing fails
     */
    public <V, A> void write(@NonNull Appendable w, @NonNull DirectedGraph<V, A> graph,
                             @NonNull Function<V, String> vertexToString) throws IOException {
        write(w, graph, vertexToString, null, null, "G");
    }

    /**
     * Dumps a directed graph into a String which can be rendered with the
     * graphviz "dot" tool.
     *
     * @param <V>              the vertex data type
     * @param <A>              the arrow data type
     * @param graph            the graph
     * @param vertexToString   a function that converts a vertex to a String for
     *                         use as vertex name
     * @param vertexAttributes a function that converts a vertex to a String for
     *                         use as vertex attributes
     * @param arrowAttributes  a function that converts an arrow to a String for
     *                         use as arrow attributes
     * @return the "dot" string
     */
    public <V, A> String write(@NonNull DirectedGraph<V, A> graph,
                               @NonNull Function<V, String> vertexToString,
                               @NonNull Function<V, String> vertexAttributes,
                               @NonNull Function<A, String> arrowAttributes) {
        StringWriter w = new StringWriter();
        try {
            write(w, graph, vertexToString, vertexAttributes, arrowAttributes, "G");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return w.toString();
    }

    /**
     * Dumps a directed graph into a String which can be rendered with the
     * graphviz "dot" tool.
     *
     * @param <V>              the vertex data type
     * @param <A>              the arrow data type
     * @param w                the writer
     * @param graph            the graph
     * @param vertexToString   a function that converts a vertex to a String for
     *                         use as vertex name
     * @param vertexAttributes a function that converts a vertex to a String for
     *                         use as vertex attributes
     * @param arrowAttributes  a function that converts an arrow to a String for
     *                         use as arrow attributes
     * @param graphId          the id of the graph
     * @throws java.io.IOException if writing fails
     */
    public <V, A> void write(final @NonNull Appendable w,
                             final @NonNull DirectedGraph<V, A> graph,
                             final @NonNull Function<V, String> vertexToString,
                             final @Nullable Function<V, String> vertexAttributes,
                             final @Nullable Function<A, String> arrowAttributes,
                             final @Nullable String graphId) throws IOException {
        w.append("digraph");
        if (graphId != null) {
            w.append(" ").append(graphId);
        }
        w.append(" {\n");

        // dump vertices
        for (V v : graph.getVertices()) {
            final String vertexName = vertexToString.apply(v);
            if (vertexName == null) {
                continue;
            }
            final String vattr = vertexAttributes == null ? null : vertexAttributes.apply(v);
            //if (graph.getNextCount(v) == 0 || vattr != null && !vattr.isEmpty()) {
            w.append(vertexName);
            if (vattr != null && !vattr.isEmpty()) {
                w.append(" [").append(vattr).append("]");
            }
            //w.append(";");
            w.append('\n');
            // }
        }

        // dump arrows
        for (V start : graph.getVertices()) {
            for (Arc<V, A> arc : graph.getNextArcs(start)) {
                final V end = arc.getEnd();
                final A arrow = arc.getArrow();
                final String startVertexName = vertexToString.apply(start);
                final String endVertexName = vertexToString.apply(end);
                if (startVertexName == null || endVertexName == null) {
                    continue;
                }
                w.append(startVertexName);
                w.append(" -> ")
                        .append(endVertexName);
                if (arrowAttributes != null) {
                    final String attrString = arrowAttributes.apply(arrow);
                    if (attrString != null && !attrString.isEmpty()) {
                        w.append(" [");
                        w.append(attrString);
                        w.append("]");
                    }
                    //w.append(";");
                }
                w.append('\n');
            }
        }

        w.append("}\n");
    }

    /**
     * Dumps the graph graph into a String which can be rendered with the
     * graphviz "dot" tool.
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
}
