/*
 * @(#)ChunkedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

/**
 * Interface for a chunk of a {@link ChunkedMutableIndexedBidiGraph}.
 * <p>
 * A chunk contains a sub-set of the vertices and arrows of a graph.
 * <p>
 * The sub-set contains a power-of-two of vertices.
 * Multi-graphs are not supported.
 * <p>
 * A chunk only contains arrows in one direction. For a bidirectional graph,
 * we need one chunk for arrows in the 'next' direction, and one chunk for
 * the arrows in 'prev' direction.
 * <p>
 * Since a chunk also contains vertex data and arrow data, the data must
 * be stored twice, once in the 'next' direction, and once in the 'prev'
 * direction.
 * <p>
 * A chunk stores the following data:
 * <dl>
 *     <dt>vertex data</dt><dd>one 32-bit int data element for each vertex</dd>
 *     <dt>siblings</dt><dd>one 32-bit int index for each sibling</dd>
 *     <dt>arrow data</dt><dd>one 32-bit int data element for each arrow
 *     to a sibling</dd>
 * </dl>
 * Example of a bidirectional graph with a chunk-size of 2, and the following
 * graph:
 * <pre>
 * The vertices have indices 0 through 4 and the same data.
 * The arrows have negative values.
 *
 *       -1  -12
 *     0 ─→ 1 ─→ 2
 *          │    │
 *      -14 ↓    ↓ -23
 *          4 ←─ 3
 *           -34
 *
 * nextChunks[0] = vertices: 0, 1
 *                 siblings: 0→1, 1→2, 1→4,
 *                 arrows:   -1, -12, -14
 *
 * nextChunks[1] = vertices: 2, 3
 *                 siblings: 2→3, 3→4
 *                 arrows:   -23, -34
 *
 *
 * nextChunks[2] = vertices: 4, ∅
 *                 siblings:
 *                 arrows:
 *
 * prevChunks[0] = vertices: 0, 1
 *                 siblings: 1←0
 *                 arrows:   -1
 *
 * prevChunks[1] = vertices: 2, 3
 *                 siblings: 2←1, 3←2
 *                 arrows:   -12, -23
 *
 *
 * prevChunks[2] = vertices: 4, ∅
 *                 siblings: 4←1, 4←3
 *                 arrows:   -13  -34
 *
 * </pre>
 */
public interface GraphChunk {
    /**
     * Tries to add an arrow from vertex {@code v} to vertex {@code u}.
     *
     * @param v               the index of vertex v
     * @param u               the index of vertex u
     * @param data            the arrow data
     * @param updateIfPresent when true, updates the arrow data if the arrow is
     *                        already present
     * @return true, if the arrow is already present
     */
    boolean tryAddArrow(final int v, final int u, final int data, final boolean updateIfPresent);

    /**
     * Gets the index of the arrow vertex {@code v} to {@code u}.
     *
     * @param v the index of vertex v
     * @param u the index of vertex u
     * @return the index of the arrow if present,
     * {@code ~insertionIndex} if the arrow is absent
     */
    int indexOf(final int v, final int u);

    /**
     * Gets the {@code from}-offset at which the siblings array contains
     * indices of sibling vertices for the vertex {@code v}.
     *
     * @param v the index of vertex v
     * @return the {@code from}-offset in the siblings array
     * @see #getSiblingsArray()
     */
    int getSiblingsFromOffset(final int v);

    /**
     * Gets the number of siblings of vertex {@code v}.
     *
     * @param v the index of vertex v
     * @return the number of siblings
     */
    int getSiblingCount(final int v);

    /**
     * Gets the siblings array. This is a single array for all siblings.
     * <p>
     * Use {@link #getSiblingsFromOffset(int)} and {@link #getSiblingCount(int)}
     * to access the siblings of a specific vertex.
     * <p>
     * The content of this array and the offsets changes when arrows are
     * added or removed!
     *
     * @return the siblings array
     */
    int[] getSiblingsArray();

    /**
     * Tries to remove an arrow from vertex {@code v} to vertex {@code u}.
     *
     * @param v the index of vertex v
     * @param u the index of vertex u
     * @return true, if there was an arrow
     */
    boolean tryRemoveArrow(final int v, final int u);

    /**
     * Removes all arrows from vertex {@code v}.
     *
     * @param v the index of vertex v
     */
    void removeAllArrows(final int v);

    int removeArrowAt(final int v, final int removalIndex);

    int getSibling(final int v, final int k);

    int getArrow(final int v, final int k);

    int getVertexData(final int v);

    void setVertexData(final int v, final int data);
}
