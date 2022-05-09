/*
 * @(#)IntAttributedIndexedDirectedGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

/**
 * This interface adds accessors for vertex data and arrow data that is
 * represented by an int.
 */
public interface IntAttributedIndexedDirectedGraph extends IndexedDirectedGraph {
    /**
     * Returns the specified successor (next) arrow data of the specified vertex.
     *
     * @param v a vertex
     * @param i index of next vertex
     * @return the arrow data
     */
    @Override
    int getNextArrowAsInt(int v, int i);

    /**
     * Returns the data of the specified vertex.
     *
     * @param vertex a vertex
     * @return the vertex data
     */
    int getVertexDataAsInt(int vertex);
}
