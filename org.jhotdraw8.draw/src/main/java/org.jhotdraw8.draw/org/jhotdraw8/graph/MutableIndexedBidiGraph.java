package org.jhotdraw8.graph;

import java.util.NoSuchElementException;

/**
 * MutableIndexedBidiGraph.
 *
 * @author Werner Randelshofer
 */
public interface MutableIndexedBidiGraph extends IndexedBidiGraph {
    /**
     * Adds an arrow from vertex 'v' to vertex 'u' with arrow data 0.
     *
     * @param v index of vertex 'v'
     * @param u index of vertex 'u'
     */
    default void addArrowAsInt(int v, int u) {
        addArrowAsInt(v, u, 0);
    }

    /**
     * Adds an arrow from vertex 'v' to vertex 'u'.
     *
     * @param v    index of vertex 'v'
     * @param u    index of vertex 'u'
     * @param data the arrow data
     */
    void addArrowAsInt(int v, int u, int data);

    /**
     * Adds a vertex to the graph.
     */
    void addVertexAsInt();

    /**
     * Adds a vertex at the specified index to the graph.
     *
     * @param v index of vertex 'v'
     */
    void addVertexAsInt(int v);

    /**
     * Removes all arrows ending at the specified vertex.
     *
     * @param v index of vertex 'v'
     */
    void removeAllPrevAsInt(int v);

    /**
     * Removes all arrows starting at the specified vertex.
     *
     * @param v index of vertex 'v'
     */
    void removeAllNextAsInt(int v);

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
    void removeNextAsInt(int v, int index);

    /**
     * Removes vertex 'v'
     *
     * @param v index of vertex 'v'
     */
    void removeVertexAsInt(int v);
}
