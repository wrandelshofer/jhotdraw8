/*
 * @(#)GraphvizReader.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.io;

import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.MutableDirectedGraph;
import org.jhotdraw8.graph.SimpleMutableBidiGraph;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Reads a graph from a graphviz "dot" file.
 * <p>
 * Parses the following productions. <b>Does not support subgraphs!</b>
 * <pre>
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
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>Graphviz. DOT Language.</dt>
 *     <dd><a href="">https://graphviz.org/doc/info/lang.html</a>graphviz.org</dd>
 * </dl>
 */
public class GraphvizReader {
    private final Supplier<MutableDirectedGraph<String, String>> factory = () -> new SimpleMutableBidiGraph<>(16, 16);

    public MutableDirectedGraph<String, String> read(Path file) throws IOException {
        try (Reader r = Files.newBufferedReader(file)) {
            return read(r);
        }
    }

    public MutableDirectedGraph<String, String> read(Reader r) throws IOException {
        final MutableDirectedGraph<String, String> g = factory.get();
        parseGraph(new StreamTokenizer(r), g, new LinkedHashSet<>());

        return g;
    }

    /**
     * Parses the graph production.
     */
    private void parseGraph(StreamTokenizer tt, MutableDirectedGraph<String, String> g, Set<String> vertices) throws IOException {
        if (tt.nextToken() != StreamTokenizer.TT_WORD) {
            throwException(tt, "graph: `strict`, `graph` or expected `digraph`");
        }
        if (!"strict".equals(tt.sval)) tt.pushBack();
        if (tt.nextToken() != StreamTokenizer.TT_WORD) {
            throwException(tt, "graph: `graph` or expected `digraph`");
        }
        if (!"graph".equals(tt.sval) && !"digraph".equals(tt.sval)) {
            throwException(tt, "graph: `graph` or expected `digraph`");
        }
        if (tt.nextToken() != StreamTokenizer.TT_WORD) {
            throwException(tt, "graph: expected `ID`");
        }
        String id = tt.sval;
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
    private void parseStmtLst(StreamTokenizer tt, MutableDirectedGraph<String, String> g, Set<String> vertices) throws IOException {
        do {
            tt.pushBack();
            parseStmt(tt, g, vertices);
            if (tt.nextToken() == ';') {
                tt.nextToken();
            }
        } while (tt.ttype != '}' && tt.ttype != StreamTokenizer.TT_EOF);
        tt.pushBack();
    }

    /**
     * Parses the stmt production.
     */
    private void parseStmt(StreamTokenizer tt, MutableDirectedGraph<String, String> g, Set<String> vertices) throws IOException {
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
        if (vertices.add(node_id)) {
            g.addVertex(node_id);
        }
        //XXX We would like to do g.setVertexData(attrList) here

        if (!isDefinitelyNodeStmt && tt.nextToken() == '-') {
            isDefinitelyEdgeStmt = true;
            tt.pushBack();
            parseEdgeRhs(tt, g, node_id, vertices);
        }
    }

    private void parseEdgeRhs(StreamTokenizer tt, MutableDirectedGraph<String, String> g, String node_id, Set<String> vertices) throws IOException {
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
            if (vertices.add(next_node_id)) {
                g.addVertex(next_node_id);
            }
            arrows.add(new OrderedPair<>(node_id, next_node_id));
            if (isEdge) {
                g.addArrow(next_node_id, node_id, null);
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
            g.addArrow(arrow.first(), arrow.second(), attrList == null ? null : "" + attrList);
        }

    }

    private Map<String, String> parseAttrList(StreamTokenizer tt, MutableDirectedGraph<String, String> g) throws IOException {
        Map<String, String> attrList = new LinkedHashMap();
        if (tt.nextToken() != '[') {
            throwException(tt, "attr_list: expected `[`");
        }
        while (tt.nextToken() != ']' && tt.ttype != StreamTokenizer.TT_EOF) {
            parseAList(tt, g, attrList);
        }
        return attrList;
    }

    private void parseAList(StreamTokenizer tt, MutableDirectedGraph<String, String> g, Map<String, String> attrList) throws IOException {
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

    private IOException throwException(StreamTokenizer tt, String message) throws IOException {

        throw new IOException(message + " but found " + tt);
    }
}
