package org.jhotdraw8.graph;

public interface MutableIntDirectedGraph extends IntDirectedGraph {
    /**
     * Adds a vertex to the graph.
     */
    void addVertex();

    /**
     * Removes a vertex from the graph.
     *
     * @param vidx index of vertex v
     */
    void removeVertex(int vidx);

    /**
     * Adds an arrow from vertex v to vertex u.
     *
     * @param vidx index of vertex v
     * @param uidx index of vertex u
     */
    void addArrow(int vidx, int uidx);

    /**
     * Removes an arrow from vertex v to vertex u.
     *
     * @param vidx index of vertex v
     * @param uidx index  of vertex u
     */
    void removeArrow(int vidx, int uidx);

    /**
     * Removes the k-th outgoing arrow from vertex v.
     *
     * @param vidx index of vertex v
     * @param k    index of arrow to be removed
     */
    void removeArrowAt(int vidx, int k);
}
