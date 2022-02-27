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
     * @param vidx index of vertex 'v'
     * @param uidx index of vertex 'u'
     */
    default void addArrow(int vidx, int uidx) {
        addArrow(vidx, uidx, 0);
    }

    /**
     * Adds an arrow from vertex 'v' to vertex 'u'.
     *
     * @param vidx index of vertex 'v'
     * @param uidx index of vertex 'u'
     * @param data the arrow data
     */
    void addArrow(int vidx, int uidx, int data);

    /**
     * Adds a vertex to the graph.
     */
    void addVertex();

    /**
     * Adds a vertex at the specified index to the graph.
     *
     * @param vidx index of vertex 'v'
     */
    void addVertex(int vidx);

    /**
     * Removes all arrows ending at the specified vertex.
     *
     * @param vidx index of vertex 'v'
     */
    void removeAllPrev(int vidx);

    /**
     * Removes all arrows starting at the specified vertex.
     *
     * @param vidx index of vertex 'v'
     */
    void removeAllNext(int vidx);

    /**
     * Removes an arrow from vertex 'v' to vertex 'u'
     *
     * @param vidx index of vertex 'v'
     * @param uidx index of vertex 'u'
     * @throws NoSuchElementException if there is no such arrow
     */
    default void removeArrow(int vidx, int uidx) {
        removeNext(vidx, findIndexOfNextAsInt(vidx, uidx));
    }

    /**
     * Removes the i-th arrow starting at vertex 'v'
     *
     * @param vidx index of vertex 'v'
     * @param i    the index of the arrow starting at 'v'
     * @throws NoSuchElementException if there is no such arrow
     */
    void removeNext(int vidx, int i);

    /**
     * Removes vertex 'v'
     *
     * @param vidx index of vertex 'v'
     */
    void removeVertex(int vidx);
}
