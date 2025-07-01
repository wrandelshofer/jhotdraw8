package org.jhotdraw8.graph.builder;

import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.collection.primitive.IntList;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;

import java.util.List;

/**
 * Builds the graph that is shown in Tarjans paper on figure 3.
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
 * References:
 * <dl>
 *     <dt>Robert Tarjan (1972). Depth-first search and linear graph algorithms.
 *     </dt>
 *     <dd><a href="http://www.cs.ucsb.edu/~gilbert/cs240a/old/cs240aSpr2011/slides/TarjanDFS.pdf">cs.ucsb.edu</a></dd>
 * </dl>
 */
public class TarjanFig3GraphBuilder {

    /**
     * Builds the graph.
     */
    public DirectedGraph<String, Integer> build() {
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

    /**
     * Builds the sets of strongly connected components in the graph.
     */
    public List<IntList> buildStronglyConnectedComponents() {
        return List.of(IntArrayList.of(0, 1, 7), IntArrayList.of(2, 3, 4, 6), IntArrayList.of(5));
    }

}
