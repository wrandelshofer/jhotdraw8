/*
 * @(#)GraphvizReader.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.MutableDirectedGraph;
import org.jhotdraw8.graph.SimpleMutableBidiGraph;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reads a graph from a graphviz "dot" file.
 * <p>
 * Parses the following productions. <b>Does not support subgraphs!</b>
 * <pre>{@literal
 * graph        : [ strict ] (graph | digraph) [ ID ] '{' stmt_list '}'
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
public class GraphvizReader<V, A> {
    private final @NonNull BiFunction<String, Map<String, String>, V> vertexFactory;
    private final @NonNull Function<Map<String, String>, A> arrowFactory;

    private final @NonNull Supplier<MutableDirectedGraph<V, A>> graphFactory;

    public GraphvizReader(@NonNull final Function<String, V> vertexFactory,
                          @NonNull final Function<Map<String, String>, A> arrowFactory) {
        this.vertexFactory = (id, map) -> vertexFactory.apply(id);
        this.arrowFactory = arrowFactory;
        this.graphFactory = () -> new SimpleMutableBidiGraph<>(16, 16);
    }

    public GraphvizReader(@NonNull final BiFunction<String, Map<String, String>, V> vertexFactory,
                          @NonNull final Function<Map<String, String>, A> arrowFactory,
                          @NonNull final Supplier<MutableDirectedGraph<V, A>> graphFactory) {
        this.vertexFactory = vertexFactory;
        this.arrowFactory = arrowFactory;
        this.graphFactory = graphFactory;
    }

    public static @NonNull GraphvizReader<String, String> newInstance() {
        return new GraphvizReader<>(
                (id, map) -> id,
                Object::toString,
                () -> new SimpleMutableBidiGraph<>(16, 16));
    }

    public MutableDirectedGraph<V, A> read(@NonNull Path file) throws IOException {
        try (Reader r = Files.newBufferedReader(file)) {
            return read(r);
        }
    }

    public MutableDirectedGraph<V, A> read(@NonNull Reader r) throws IOException {
        final MutableDirectedGraph<V, A> g = graphFactory.get();
        parseGraph(new StreamTokenizer(r), g, new LinkedHashMap<>());

        return g;
    }

    public MutableDirectedGraph<V, A> read(@NonNull String str) throws IOException {
        final MutableDirectedGraph<V, A> g = graphFactory.get();
        try (StringReader r = new StringReader(str)) {
            StreamTokenizer tt = new StreamTokenizer(r);
            tt.resetSyntax();
            tt.wordChars('a', 'z');
            tt.wordChars('A', 'Z');
            tt.wordChars(128 + 32, 255);
            tt.whitespaceChars(0, ' ');
            tt.commentChar('/');
            tt.quoteChar('"');
            tt.quoteChar('\'');
            tt.wordChars('0', '9');
            tt.wordChars('.', '.');

            parseGraph(tt, g, new LinkedHashMap<>());
            return g;
        }
    }

    /**
     * Parses the graph production.
     */
    private void parseGraph(@NonNull StreamTokenizer tt, @NonNull MutableDirectedGraph<V, A> g, @NonNull Map<String, V> vertices) throws IOException {
        if (tt.nextToken() != StreamTokenizer.TT_WORD) {
            throwException(tt, "graph: `strict`, `graph` or expected `digraph`");
        }
        if (!"strict".equals(tt.sval)) {
            tt.pushBack();
        }
        if (tt.nextToken() != StreamTokenizer.TT_WORD) {
            throwException(tt, "graph: `graph` or expected `digraph`");
        }
        if (!"graph".equals(tt.sval) && !"digraph".equals(tt.sval)) {
            throwException(tt, "graph: `graph` or expected `digraph`");
        }

        String id;
        if (tt.nextToken() == StreamTokenizer.TT_WORD) {
            id = tt.sval;
        } else {
            tt.pushBack();
            id = null;
        }

        if (tt.nextToken() != '{') {
            throwException(tt, "graph: expected `{`");
        }
        while (tt.nextToken() != '}' && tt.ttype != StreamTokenizer.TT_EOF) {
            tt.pushBack();
            parseStmtLst(tt, g, vertices);
        }
        if (tt.ttype != '}') {
            throwException(tt, "graph: expected `}`");
        }
    }

    /**
     * Parses the stmtList production.
     */
    private void parseStmtLst(@NonNull StreamTokenizer tt, @NonNull MutableDirectedGraph<V, A> g, @NonNull Map<String, V> vertexMap) throws IOException {
        do {
            tt.pushBack();
            parseStmt(tt, g, vertexMap);
            if (tt.nextToken() == ';') {
                tt.nextToken();
            }
        } while (tt.ttype != '}' && tt.ttype != StreamTokenizer.TT_EOF);
        tt.pushBack();
    }

    /**
     * Parses the stmt production.
     */
    private void parseStmt(@NonNull StreamTokenizer tt, @NonNull MutableDirectedGraph<V, A> g, @NonNull Map<String, V> vertexMap) throws IOException {
        if (tt.nextToken() != '"' && tt.ttype != StreamTokenizer.TT_WORD) {
            throwException(tt, "stmt: expected `node_id`");
        }
        String node_id = tt.sval;
        Map<String, String> attrList;
        boolean isDefinitelyNodeStmt = false;
        boolean isDefinitelyEdgeStmt = false;
        if (tt.nextToken() == '[') {
            isDefinitelyNodeStmt = true;
            tt.pushBack();
            attrList = parseAttrList(tt, g);
        } else {
            tt.pushBack();
            attrList = null;
        }

        if (!vertexMap.containsKey(node_id)) {
            final V vertex = vertexFactory.apply(node_id, attrList);
            vertexMap.put(node_id, vertex);
            g.addVertex(vertex);
        }

        if (!isDefinitelyNodeStmt && tt.nextToken() == '-') {
            isDefinitelyEdgeStmt = true;
            tt.pushBack();
            parseEdgeRhs(tt, g, node_id, vertexMap);
        }
    }

    private void parseEdgeRhs(@NonNull StreamTokenizer tt, @NonNull MutableDirectedGraph<V, A> g, @NonNull String node_id, @NonNull Map<String, V> vertexMap) throws IOException {
        List<OrderedPair<String, String>> arrows = new ArrayList<>();
        do {
            tt.pushBack();
            if (tt.nextToken() != '-') {
                throwException(tt, "edgeRHS: edgeop expected");
            }
            if (tt.nextToken() != '-' && tt.ttype != '>') {
                throwException(tt, "edgeRHS: `--` or `->` expected");
            }
            boolean isEdge = tt.ttype == '-';

            if (tt.nextToken() != '"' && tt.ttype != StreamTokenizer.TT_WORD) {
                throwException(tt, "edgeRHS: expected `node_id`");
            }

            String next_node_id = tt.sval;
            if (!vertexMap.containsKey(next_node_id)) {
                final V vertex = vertexFactory.apply(next_node_id, Collections.emptyMap());
                vertexMap.put(next_node_id, vertex);
                g.addVertex(vertex);
            }
            arrows.add(new OrderedPair<>(node_id, next_node_id));

            if (isEdge) {
                arrows.add(new OrderedPair<>(next_node_id, node_id));
            }
            node_id = next_node_id;
        } while (tt.nextToken() == '-');
        tt.pushBack();

        Map<String, String> attrList = null;
        if (tt.nextToken() == '[') {
            tt.pushBack();
            attrList = parseAttrList(tt, g);
        } else {
            tt.pushBack();
        }

        for (OrderedPair<String, String> arrow : arrows) {
            g.addArrow(vertexMap.get(arrow.first()), vertexMap.get(arrow.second()), attrList == null ? null :
                    arrowFactory.apply(attrList));
        }

    }

    private @NonNull Map<String, String> parseAttrList(@NonNull StreamTokenizer tt, @NonNull MutableDirectedGraph<V, A> g) throws IOException {
        Map<String, String> attrList = new LinkedHashMap<>();
        if (tt.nextToken() != '[') {
            throwException(tt, "attr_list: expected `[`");
        }
        while (tt.nextToken() != ']' && tt.ttype != StreamTokenizer.TT_EOF) {
            parseAList(tt, g, attrList);
        }
        return attrList;
    }

    private void parseAList(@NonNull StreamTokenizer tt, @NonNull MutableDirectedGraph<V, A> g, @NonNull Map<String, String> attrList) throws IOException {
        do {
            tt.pushBack();
            if (tt.nextToken() != '"' && tt.ttype != StreamTokenizer.TT_WORD) {
                throwException(tt, "a_list: expected `ID`");
            }
            String key = tt.sval;
            if (tt.nextToken() != '=') {
                throwException(tt, "a_list: expected `=`");
            }
            if (tt.nextToken() != '"' && tt.ttype != StreamTokenizer.TT_WORD) {
                throwException(tt, "a_list: expected `ID`");
            }
            String value = tt.sval;
            attrList.put(key, value);
            if (tt.nextToken() != ',' && tt.ttype != ';') {
                tt.pushBack();
            }
        } while ((tt.nextToken() == '"' || tt.ttype == StreamTokenizer.TT_WORD));
        tt.pushBack();

    }

    private void throwException(StreamTokenizer tt, String message) throws IOException {
        throw new IOException(message + " but found " + tt);
    }
}
