package org.jhotdraw8.graph;

import java.util.NoSuchElementException;

/**
 * FIXME ensure that the methods do not clash with {@link MutableIndexedBidiGraph}
 */
public interface MutableIndexedDirectedGraph extends IndexedDirectedGraph {
    /**
     * Adds an arrow from vertex 'v' to vertex 'u'.
     *
     * @param v index of vertex 'v'
     * @param u index of vertex 'u'
     */
    int addArrowAsInt(int v, int u);


    /**
     * Adds a vertex to the graph.
     */
    void addVertexAsInt();


    /**
     * Removes an arrow from vertex 'v' to vertex 'u'
     *
     * @param v index of vertex 'v'
     * @param u index of vertex 'u'
     * @throws NoSuchElementException if there is no such arrow
     */
    default void removeArrowAsInt(int v, int u) {
        removeNextAsInt(v, findIndexOfNextAsInt(v, u));
    }

    /**
     * Removes the i-th arrow starting at vertex 'v'
     *
     * @param v     index of vertex 'v'
     * @param index the index of the arrow starting at 'v'
     * @throws NoSuchElementException if there is no such arrow
     */
    int removeNextAsInt(int v, int index);


    /**
     * Removes vertex 'v'
     *
     * @param v index of vertex 'v'
     */
    void removeVertexAsInt(int v);
}
