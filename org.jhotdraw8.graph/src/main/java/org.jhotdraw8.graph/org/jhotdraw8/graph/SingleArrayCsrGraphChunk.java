/*
 * @(#)SingleArrayCsrGraphChunk.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import java.util.Arrays;

/**
 * Stores a chunk of a graph using a "Compressed Sparse Row" representation.
 * <p>
 * Uses one shared gap between the list of sibling vertices/arrows.
 * Moves the gap at each insertion/deletion operation. This may make it
 * slow for updates.
 * <p>
 * The vertex data, the siblings list and arrows list are stored in
 * a single array.
 */
public class SingleArrayCsrGraphChunk implements GraphChunk {
    private static final boolean CLEAR_UNUSED_ELEMENTS = false;
    private static final int CLEAR_VALUE = 99;

    private final int vertexCount;
    /**
     * The total capacity for arrows in this chunk.
     */
    private int capacity;
    /**
     * The free capacity for arrows in this chunk.
     */
    private int free;
    /**
     * The local index of the vertex, that contains the gap.
     */
    private int gapIndex;
    /**
     * The size of the gap.
     */
    private int gapSize;

    /**
     * Chunk array layout:
     * <pre>
     * vertices: [ data ... ] // Stores the vertex data for each vertex.
     *                        // There are {@link #vertexCount} vertices in this array.
     *
     * sizes: [ ⌈cumulated⌉, ... ]
     *                        // Stores the number of siblings for each vertex.
     *                        // There are {@link #vertexCount} vertices in this array
     *                        // Sizes are cumulated. size[i] = sizes[i] - sizes[i - 1]
     *
     *
     * siblings: [ siblingsOfVertex0, siblingsOfVertex1, gap, siblingsOfVertex2, ... ]
     *                         // Stores the siblings of the vertices.
     *                         // There are {@link #capacity} vertices in this array.
     *                         //
     *                         // The siblings of a vertex are stored in a consecutive sequence.
     *                         // The siblings of a vertex are sorted by index, so that a
     *                         // binary search can be used to find a specific sibling.
     *                         // There is one gap of size {@link #gapSize} after the vertex with
     *                         // index{@link #gapIndex}.
     *
     * arrows: [ data, ... ] // Stores the arrow data of the vertices.
     *                       // There are {@link #capacity} vertices in this array.
     *                       // The structure is the same as for the siblings.
     * </pre>
     *
     * @param free the initial free space for arrows
     * @return a new chunk
     */
    private int[] chunk;

    public SingleArrayCsrGraphChunk(final int vertexCount, final int initialArrowCapacity) {
        this.vertexCount = vertexCount;
        this.capacity = this.free = this.gapSize = initialArrowCapacity;
        this.gapIndex = 0;
        chunk = new int[this.vertexCount * 2 + initialArrowCapacity * 2];

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(chunk, getSiblingsFromOffset(), chunk.length, CLEAR_VALUE);
        }
    }

    /**
     * Finds the index of vertex u in the sibling list of vertex v.
     *
     * @param v index of vertex v
     * @param u index of vertex u
     * @return the index of u or (-index -1) if u is not in the index list.
     */
    @Override
    public int indexOf(final int v, final int u) {
        final int from = getSiblingsFromOffset(v);
        final int to = from + getSiblingCount(v);

        final int result = Arrays.binarySearch(chunk, from, to, u);
        return result < 0 ? result + from : result - from;
    }

    @Override
    public int[] getSiblingsArray() {
        return chunk;
    }

    @Override
    public int getSiblingCount(final int v) {
        final int vIndex = v & (vertexCount - 1);
        final int sizesOffset = getSizesOffset();
        return vIndex == 0
                ? chunk[sizesOffset + vIndex]
                : chunk[sizesOffset + vIndex] - chunk[sizesOffset + vIndex - 1];
    }

    @Override
    public int getVertexData(final int v) {
        final int vIndex = v & (vertexCount - 1);
        return chunk[vIndex];
    }

    @Override
    public void setVertexData(final int v, final int data) {
        final int vIndex = v & (vertexCount - 1);
        chunk[vIndex] = data;
    }

    @Override
    public int getArrow(final int v, final int k) {
        return chunk[getArrowsFromOffset(v) + k];
    }

    @Override
    public int getSiblingsFromOffset(final int v) {
        final int vIndex = v & (vertexCount - 1);
        final int siblingsOffset = getSiblingsFromOffset();
        return vIndex == 0
                ? siblingsOffset
                : siblingsOffset + chunk[getSizesOffset() + vIndex - 1]
                + (vIndex <= gapIndex ? 0 : gapSize);
    }

    int getSiblingsToOffset(final int v) {
        final int vIndex = v & (vertexCount - 1);
        return getSiblingsFromOffset()
                + chunk[getSizesOffset() + vIndex]
                + (vIndex <= gapIndex ? 0 : gapSize);
    }

    int getSiblingsFromOffset() {
        return vertexCount * 2;
    }

    @Override
    public int getSibling(final int v, final int k) {
        return chunk[getSiblingsFromOffset(v) + k];
    }

    int getSizesOffset() {
        return vertexCount;
    }

    int getArrowsFromOffset() {
        return vertexCount * 2 + capacity;
    }

    int getSiblingsToOffset() {
        return vertexCount * 2 + capacity - (gapIndex == vertexCount - 1 ? gapSize : 0);
    }

    int getArrowsFromOffset(final int v) {
        final int vIndex = v & (vertexCount - 1);
        final int arrowsOffset = getArrowsFromOffset();
        return vIndex == 0
                ? arrowsOffset
                : arrowsOffset + chunk[getSizesOffset() + vIndex - 1]
                + (vIndex <= gapIndex ? 0 : gapSize);
    }

    int getArrowsToOffset(final int v) {
        final int vIndex = v & (vertexCount - 1);
        return getArrowsFromOffset()
                + chunk[getSizesOffset() + vIndex]
                + (vIndex <= gapIndex ? 0 : gapSize);
    }

    /**
     * Adds an arrow from vertex v to vertex u with the provided arrow data.
     * Optionally updates the arrow data if the arrow is present.
     *
     * @param v               index of vertex v
     * @param u               index of vertex u
     * @param data            the arrow data
     * @param updateIfPresent sets the data if the arrow is present
     * @return true if a new arrow was added
     */
    @Override
    public boolean tryAddArrow(final int v, final int u, final int data, final boolean updateIfPresent) {
        final int result = indexOf(v, u);
        if (result >= 0) {
            if (updateIfPresent) {
                setArrowAt(v, result, data);
            }
            return false;
        }
        if (free < 1) {
            grow();
        }
        final int siblingsFrom = getSiblingsFromOffset(v);
        final int siblingCount = getSiblingCount(v);
        final int siblingsTo = siblingCount + siblingsFrom;
        final int insertionIndex = ~result;
        final int vIndex = v & (vertexCount - 1);

        if (gapIndex < vIndex) {
            insertAfterGap(u, data, siblingsFrom, siblingsTo, insertionIndex);
        } else if (gapIndex > vIndex) {
            insertBeforeGap(u, data, siblingsFrom, siblingsTo, insertionIndex);
        } else {
            insertAtGap(u, data, siblingsFrom, siblingsTo, insertionIndex);
        }

        gapIndex = vIndex;
        free--;
        gapSize--;

        final int sizesOffset = getSizesOffset();
        for (int i = sizesOffset + vIndex, n = sizesOffset + vertexCount; i < n; i++) {
            chunk[i]++;
        }

        return true;
    }

    void setArrowAt(final int v, final int index, final int data) {
        chunk[getArrowsFromOffset(v) + index] = data;
    }

    /**
     * Removes an arrow from vertex v to vertex u, if it is present.
     *
     * @param v index of vertex v
     * @param u index of vertex u
     * @return true on success
     */
    @Override
    public boolean tryRemoveArrow(final int v, final int u) {
        final int result = indexOf(v, u);
        if (result < 0) {
            return false;
        }
        removeArrowAt(v, result);
        return true;
    }

    /**
     * Removes all arrows starting at vertex v.
     *
     * @param v index of vertex v
     */
    @Override
    public void removeAllArrows(final int v) {
        final int vIndex = v & (vertexCount - 1);
        final int size = getSiblingCount(vIndex);
        if (size == 0) {
            return;
        }

        int from = getSiblingsFromOffset(v);
        if (gapIndex > vIndex) {
            // BEFORE:
            // ...,,,,::::;;;; = siblings list of different vertices
            //                       the list with the colons ':' is the sibling list of 'v'.
            //
            // siblings = [........::::::;;;;;gap,,,,];
            //                     ^     ^    ^
            //                     from  to   gapFrom
            // AFTER:
            //
            // siblings = [........::::::gap;;;;;,,,,];
            //                     ^       ^
            //                     from    to

            final int gapFrom = getSiblingsToOffset(gapIndex);
            final int to = getSiblingsToOffset(v);
            System.arraycopy(chunk, to, chunk, to + gapSize, gapFrom - to);
            System.arraycopy(chunk, to + capacity, chunk, to + gapSize + capacity, gapFrom - to);
        } else if (gapIndex < vIndex) {
            // ....|,,,,|::::|;;;;| = siblings list of different vertices
            //                        the list with the colons ':' is the sibling list of 'v'.
            //
            // BEFORE:
            // siblings = [........gap,,,,,::::::;;;;;];
            //                     ^       ^     ^
            //                     gapFrom from  to
            // AFTER:
            // siblings = [........,,,,,::::::gap;;;;;];
            //                          ^     ^
            //                          from  to
            final int gapFrom = getSiblingsToOffset(gapIndex);
            System.arraycopy(chunk, gapFrom + gapSize, chunk, gapFrom, from - gapFrom - gapSize);
            System.arraycopy(chunk, gapFrom + gapSize + capacity, chunk, gapFrom + capacity, from - gapFrom - gapSize);
            from -= gapSize;
        }
        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(chunk, from, from + size + gapSize, CLEAR_VALUE);
            Arrays.fill(chunk, from + capacity, from + capacity + size + gapSize, CLEAR_VALUE);
        }

        final int sizesOffset = getSizesOffset();
        for (int i = sizesOffset + vIndex, n = sizesOffset + vertexCount; i < n; i++) {
            chunk[i] -= size;
        }
        gapIndex = vIndex;
        gapSize += size;
        free += size;
    }

    /**
     * Removes an arrow from vertex v to the a vertex u at the specified
     * index.
     *
     * @param v            index of vertex v
     * @param removalIndex index of vertex u
     * @return returns the removed arrow u
     */
    @Override
    public int removeArrowAt(final int v, final int removalIndex) {
        final int siblingsFrom = getSiblingsFromOffset(v);
        final int siblingsTo = getSiblingCount(v) + siblingsFrom;
        final int vIndex = v & (vertexCount - 1);
        final int u = chunk[siblingsFrom + removalIndex];

        if (gapIndex < vIndex) {
            removeAfterGap(siblingsFrom, siblingsTo, removalIndex);
        } else if (gapIndex > vIndex) {
            removeBeforeGap(siblingsFrom, siblingsTo, removalIndex);
        } else {
            removeAtGap(siblingsFrom, siblingsTo, removalIndex);
        }

        gapIndex = vIndex;
        free++;
        gapSize++;

        final int sizesOffset = getSizesOffset();
        for (int i = sizesOffset + vIndex, n = sizesOffset + vertexCount; i < n; i++) {
            chunk[i]--;
        }
        return u;
    }

    void grow() {
        final int newCapacity = capacity * 2;
        if (newCapacity <= capacity) {
            throw new OutOfMemoryError("can not grow to newCapacity=" + newCapacity + ", current capacity=" + capacity);
        }

        final int[] newChunk = new int[vertexCount * 2 + newCapacity * 2];

        // CASE 1: the gap is not at the end of the siblings/arrows:
        // -------
        // BEFORE:
        // ...|,,,,|::::|;;;;| = siblings list of different vertices
        //
        // siblings = [........::::::gap;;;;;,,,,];
        // arrows =   [........::::::gap;;;;;,,,,];
        //
        // AFTER:
        //
        // siblings = [........::::::gap+capacity;;;;;,,,,];
        // arrows   = [........::::::gap+capacity;;;;;,,,,];

        // CASE 2: the gap is at the end of the siblings/arrows:
        // -------
        // BEFORE:
        // ...|,,,,|::::|;;;;| = siblings list of different vertices
        //
        // siblings = [........::::::;;;;;,,,,gap];
        // arrows =   [........::::::;;;;;,,,,gap];
        //
        // AFTER:
        //
        // siblings = [........::::::;;;;;,,,,gap+capacity];
        // arrows =   [........::::::;;;;;,,,,gap+capacity];

        final int siblingsGapFromOffset = getSiblingsToOffset(gapIndex);
        final int arrowsGapFromOffset = getArrowsToOffset(gapIndex);
        final int siblingsGapToOffset = siblingsGapFromOffset + gapSize;
        final int arrowsGapToOffset = arrowsGapFromOffset + gapSize;
        final int arrowsFromOffset = getArrowsFromOffset();
        final int deltaCapacity = newCapacity - capacity;

        System.arraycopy(chunk, 0, newChunk, 0, siblingsGapFromOffset);
        System.arraycopy(chunk, arrowsFromOffset, newChunk, arrowsFromOffset + deltaCapacity, getArrowsToOffset(gapIndex) - arrowsFromOffset);
        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(newChunk, siblingsGapFromOffset, siblingsGapToOffset + deltaCapacity, CLEAR_VALUE);
            Arrays.fill(newChunk, arrowsGapFromOffset + deltaCapacity, arrowsGapToOffset + deltaCapacity * 2, CLEAR_VALUE);
        }
        final int length = getSiblingsToOffset() - siblingsGapToOffset;
        if (length > 0) {
            System.arraycopy(chunk, siblingsGapToOffset, newChunk, siblingsGapToOffset + deltaCapacity, length);
            System.arraycopy(chunk, arrowsGapToOffset, newChunk, arrowsGapToOffset + deltaCapacity * 2, length);
        }

        this.chunk = newChunk;
        this.free = free + deltaCapacity;
        this.capacity = newCapacity;
        this.gapSize = gapSize + deltaCapacity;
    }

    /**
     * Inserts vertex 'u' in the siblings list of vertex 'v'.
     * <p>
     * The gap is located at the end of the siblings list of vertex 'v'.
     *
     * @param u
     * @param data
     * @param from
     * @param to
     * @param insertionIndex
     */
    void insertAtGap(final int u, final int data, final int from, final int to, final int insertionIndex) {
        // BEFORE:
        // ÷ = insertionIndex of 'u' in the siblings list of vertex 'v'
        // ...,,,,::::;;;; = siblings list of different vertices
        //                       the list with the colons ':' is the sibling list of 'v'.
        //
        // siblings = [........::::÷::gap;;;;;,,,,];
        //                     ^      ^
        //                     from   to
        // AFTER:
        //
        // siblings = [........::::u÷::gp;;;;;,,,,];
        //                     ^       ^
        //                     from    to

        // shift up to make room for the new element
        int arrowDataFrom = from + capacity;
        final int length = to - from - insertionIndex;
        if (length > 0) {
            System.arraycopy(chunk, from + insertionIndex, chunk, from + insertionIndex + 1, length);
            System.arraycopy(chunk, arrowDataFrom + insertionIndex, chunk, arrowDataFrom + insertionIndex + 1, length);
        }

        // insert the element at insertion index
        chunk[from + insertionIndex] = u;
        chunk[arrowDataFrom + insertionIndex] = data;
    }

    /**
     * Inserts vertex 'u' in the siblings list of vertex 'v'.
     * <p>
     * The siblings list of vertex 'v' is located somewhere
     * before the siblings list that contains the gap.
     *
     * @param u
     * @param data
     * @param from
     * @param to
     * @param insertionIndex
     */
    void insertBeforeGap(final int u, final int data, final int from, final int to, final int insertionIndex) {
        // BEFORE:
        // ÷ = insertionIndex of 'u' in the siblings list of vertex 'v'
        // ...,,,,::::;;;; = siblings list of different vertices
        //                       the list with the colons ':' is the sibling list of 'v'.
        //
        // siblings = [........::::÷::;;;;;gap,,,,];
        //                     ^      ^
        //                     from   to
        // AFTER:
        //
        // siblings = [........::::u÷::gp;;;;;,,,,];
        //                     ^       ^
        //                     from    to

        // close the gap by shifting up
        final int siblingsStartOfGapOffset = getSiblingsToOffset(gapIndex);
        int length = siblingsStartOfGapOffset - to;
        System.arraycopy(chunk, to, chunk, to + free, length);
        System.arraycopy(chunk, to + capacity, chunk, to + capacity + free, length);

        // shift up to make room for the new element
        length = to - from - insertionIndex;
        System.arraycopy(chunk, from + insertionIndex, chunk, from + insertionIndex + 1, length);
        System.arraycopy(chunk, from + capacity + insertionIndex, chunk, from + capacity + insertionIndex + 1, length);

        // insert the element at insertion index
        chunk[from + insertionIndex] = u;
        chunk[from + capacity + insertionIndex] = data;

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(chunk, to + 1, to + free, CLEAR_VALUE);
            Arrays.fill(chunk, to + capacity + 1, to + capacity + free, CLEAR_VALUE);
        }
    }

    /**
     * Inserts vertex 'u' in the siblings list of vertex 'v'.
     * <p>
     * The siblings list of vertex 'v' is located somewhere after
     * the siblings list that contains the gap.
     *
     * @param u
     * @param data
     * @param from
     * @param to
     * @param insertionIndex
     */
    void insertAfterGap(final int u, final int data,
                        final int from, final int to,
                        final int insertionIndex) {
        // BEFORE:
        // ÷ = insertionIndex of 'u' in the siblings list of vertex 'v'
        // ....,,,,::::;;;; = siblings list of different vertices
        //                        the list with the colons ':' is the sibling list of 'v'.
        //
        // siblings = [........gap,,,,::::÷::;;;;;];
        //                            ^      ^
        //                            from   to
        // AFTER:
        //
        // siblings = [........,,,,::::u÷::gp;;;;;];
        //                         ^       ^
        //                         from    to

        // close the gap by shifting down
        final int siblingsGapFrom = getSiblingsToOffset(gapIndex);
        final int arrowsGapFrom = siblingsGapFrom + capacity;
        int length = from + insertionIndex - siblingsGapFrom - free;
        if (length > 0) {
            System.arraycopy(chunk, siblingsGapFrom + free, chunk, siblingsGapFrom, length);
            System.arraycopy(chunk, arrowsGapFrom + free, chunk, arrowsGapFrom, length);
        }

        // insert the element at insertion index
        int arrowDataFrom = from + capacity;
        chunk[from + insertionIndex - free] = u;
        chunk[arrowDataFrom + insertionIndex - free] = data;

        // reopen the gap by shifting the remainder of the indices down
        length = to - from - insertionIndex;
        System.arraycopy(chunk, from + insertionIndex, chunk, from + insertionIndex - free + 1, length);
        System.arraycopy(chunk, arrowDataFrom + insertionIndex, chunk, arrowDataFrom + insertionIndex - free + 1, length);

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(chunk, to - free + 1, to, CLEAR_VALUE);
            Arrays.fill(chunk, to + capacity - free + 1, to + capacity, CLEAR_VALUE);
        }
    }

    /**
     * Removes vertex 'u' in the siblings list of vertex 'v'.
     * <p>
     * The gap is located at the end of the siblings list of vertex 'v'.
     *
     * @param from
     * @param to
     * @param removalIndex
     */
    void removeAtGap(final int from, final int to, final int removalIndex) {
        // ÷ = index after removal index, u = element to be removed
        // ...,,,,::::;;;; = siblings list of different vertices
        //                       the list with the colons ':' is the sibling list of 'v'.
        //
        // BEFORE:
        // siblings = [........::::u÷::gp;;;;;,,,,];
        //                     ^       ^
        //                     from    to
        // AFTER:
        // siblings = [........::::÷::gap;;;;;,,,,];
        //                     ^      ^
        //                     from   to

        // shift down to remove the room of the element
        final int length = to - removalIndex - from - 1;
        if (length > 0) {
            System.arraycopy(chunk, from + removalIndex + 1, chunk, from + removalIndex, length);
            System.arraycopy(chunk, from + capacity + removalIndex + 1, chunk, from + capacity + removalIndex, length);
        }
        if (CLEAR_UNUSED_ELEMENTS) {
            chunk[to - 1] = CLEAR_VALUE;
            chunk[to + capacity - 1] = CLEAR_VALUE;
        }
    }

    /**
     * Removes vertex 'u' from the siblings list of vertex 'v'.
     * <p>
     * The siblings list of vertex 'v' is located somewhere
     * before the siblings list that contains the gap.
     *
     * @param from
     * @param to
     * @param removalIndex
     */
    void removeBeforeGap(final int from, final int to, final int removalIndex) {
        // ÷ = index after removal index, u = element to be removed
        // ...,,,,::::;;;; = siblings list of different vertices
        //                       the list with the colons ':' is the sibling list of 'v'.
        // BEFORE:
        // siblings = [........::::u÷::;;;;;gp,,,,];
        //                     ^       ^
        //                     from    to
        // AFTER:
        // siblings = [........::::÷::gap;;;;;,,,,];
        //                     ^      ^
        //                     from   to
        //

        // shift up to close the gap
        final int gapFrom = getSiblingsToOffset(gapIndex);
        int length = gapFrom - to;
        System.arraycopy(chunk, to, chunk, to + free, length);
        System.arraycopy(chunk, to + capacity, chunk, to + capacity + free, length);

        // shift down to remove the room of the removed element
        length = to - from - removalIndex;
        System.arraycopy(chunk, from + removalIndex + 1, chunk, from + removalIndex, length);
        System.arraycopy(chunk, from + capacity + removalIndex + 1, chunk, from + capacity + removalIndex, length);

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(chunk, to - 1, to + free, CLEAR_VALUE);
            Arrays.fill(chunk, to + capacity - 1, to + capacity + free, CLEAR_VALUE);
        }
    }

    /**
     * Removes vertex 'u' from the siblings list of vertex 'v'.
     * <p>
     * The siblings list of vertex 'v' is located somewhere after
     * the siblings list that contains the gap.
     *
     * @param from
     * @param to
     * @param removalIndex
     */
    void removeAfterGap(final int from, final int to, final int removalIndex) {
        // ....|,,,,|::::|;;;;| = siblings list of different vertices
        //                        the list with the colons ':' is the sibling list of 'v'.
        //
        // BEFORE:
        // siblings = [........gp,,,,::::u÷::;;;;;];
        //                           ^       ^
        //                           from    to
        // AFTER:
        // siblings = [........,,,,::::÷::gap;;;;;];
        //                         ^      ^
        //                         from   to

        // shift down to close the gap
        final int siblingsGapFrom = getSiblingsToOffset(gapIndex);
        final int arrowsGapFrom = siblingsGapFrom + capacity;
        int length = from + removalIndex - siblingsGapFrom - gapSize;
        if (length > 0) {
            System.arraycopy(chunk, siblingsGapFrom + free, chunk, siblingsGapFrom, length);
            System.arraycopy(chunk, arrowsGapFrom + free, chunk, arrowsGapFrom, length);
        }

        // reopen the gap by shifting the remainder of the indices down
        length = to - from - removalIndex - 1;
        System.arraycopy(chunk, from + removalIndex + 1, chunk, from + removalIndex - free, length);
        System.arraycopy(chunk, from + capacity + removalIndex + 1, chunk, from + capacity + removalIndex - free, length);

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(chunk, to - free - 1, to, CLEAR_VALUE);
            Arrays.fill(chunk, to + capacity - free - 1, to + capacity, CLEAR_VALUE);
        }
    }
}
