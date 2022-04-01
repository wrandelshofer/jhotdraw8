/*
 * @(#)MultiArrayGraphChunk.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
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
 * separate arrays.
 */
public class MultiArrayCsrGraphChunk implements GraphChunk {
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
     * Stores the vertex data for each vertex.
     * There are {@link #vertexCount} vertices in this array.
     */
    private final int[] vertices;
    /**
     * Stores the number of siblings for each vertex.
     * There are {@link #vertexCount} vertices in this array
     * Sizes are cumulated. size[i] = sizes[i] - sizes[i - 1]
     */
    private final int[] sizes;
    /**
     * Stores the siblings of the vertices.
     * There are {@link #capacity} vertices in this array.
     * <p>
     * The siblings of a vertex are stored in a consecutive sequence.
     * The siblings of a vertex are sorted by index, so that a
     * binary search can be used to find a specific sibling.
     * There is one gap of size {@link #gapSize} after the vertex with
     * index{@link #gapIndex}.
     */
    private int[] siblings;
    /**
     * Stores the arrow data of the vertices.
     * There are {@link #capacity} vertices in this array.
     * The structure is the same as for the siblings.
     */
    private int[] arrows;


    public MultiArrayCsrGraphChunk(int vertexCount, final int initialArrowCapacity) {
        this.vertexCount = vertexCount;
        this.capacity = this.free = this.gapSize = initialArrowCapacity;
        this.gapIndex = 0;
        vertices = new int[vertexCount];
        sizes = new int[vertexCount];
        siblings = new int[initialArrowCapacity];
        arrows = new int[initialArrowCapacity];

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(vertices, CLEAR_VALUE);
            Arrays.fill(siblings, CLEAR_VALUE);
            Arrays.fill(arrows, CLEAR_VALUE);
        }
    }

    /**
     * Finds the index of vertex u in the sibling list of vertex v.
     *
     * @param v index of vertex v
     * @param u index of vertex u
     * @return the index of u or (-index -1) if u is not in the index list.
     */
    public int indexOf(final int v, final int u) {
        final int from = getSiblingsFromOffset(v);
        final int to = from + getSiblingCount(v);

        final int result = Arrays.binarySearch(siblings, from, to, u);
        return result < 0 ? result + from : result - from;
    }

    public int[] getSiblingsArray() {
        return siblings;
    }

    public int getSiblingCount(final int v) {
        final int vIndex = v & (vertexCount - 1);
        return vIndex == 0
                ? sizes[vIndex]
                : sizes[vIndex] - sizes[vIndex - 1];
    }

    public int getVertexData(final int v) {
        return vertices[v & (vertexCount - 1)];
    }

    public void setVertexData(final int v, final int data) {
        vertices[v & (vertexCount - 1)] = data;
    }

    public int getArrow(final int v, final int k) {
        return arrows[getArrowsFromOffset(v) + k];
    }

    public int getSiblingsFromOffset(final int v) {
        final int vIndex = v & (vertexCount - 1);
        return vIndex == 0
                ? 0
                : sizes[vIndex - 1] + (vIndex <= gapIndex ? 0 : gapSize);
    }

    int getSiblingsToOffset(final int v) {
        final int vIndex = v & (vertexCount - 1);
        return sizes[vIndex]
                + (vIndex <= gapIndex ? 0 : gapSize);
    }

    public int getSibling(final int v, final int k) {
        return siblings[getSiblingsFromOffset(v) + k];
    }

    int getArrowsFromOffset(final int v) {
        final int vIndex = v & (vertexCount - 1);
        return vIndex == 0
                ? 0
                : sizes[vIndex - 1]
                + (vIndex <= gapIndex ? 0 : gapSize);
    }

    int getSiblingsToOffset() {
        return capacity - (gapIndex == vertexCount - 1 ? gapSize : 0);
    }


    /**
     * Adds an arrow from vertex v to vertex u with the provided arrow data.
     * Optionally updates the arrow data if the arrow is present.
     *
     * @param v            index of vertex v
     * @param u            index of vertex u
     * @param data         the arrow data
     * @param setIfPresent sets the data if the arrow is present
     * @return true if a new arrow was added
     */
    public boolean tryAddArrow(final int v, final int u, final int data, final boolean setIfPresent) {
        final int result = indexOf(v, u);
        if (result >= 0) {
            if (setIfPresent) {
                setArrowAt(v, result, data);
            }
            return false;
        }
        if (free < 1) {
            grow();
        }
        final int from = getSiblingsFromOffset(v);
        final int siblingCount = getSiblingCount(v);
        final int to = siblingCount + from;
        final int insertionIndex = ~result;
        final int vIndex = v & (vertexCount - 1);

        if (gapIndex < vIndex) {
            insertAfterGap(u, data, from, to, insertionIndex);
        } else if (gapIndex > vIndex) {
            insertBeforeGap(u, data, from, to, insertionIndex);
        } else {
            insertAtGap(u, data, from, to, insertionIndex);
        }


        gapIndex = vIndex;
        free--;
        gapSize--;

        for (int i = vIndex, n = vertexCount; i < n; i++) {
            sizes[i]++;
        }

        return true;
    }

    void setArrowAt(final int v, final int index, final int data) {
        arrows[getArrowsFromOffset(v) + index] = data;
    }

    /**
     * Removes an arrow from vertex v to vertex u, if it is present.
     *
     * @param v index of vertex v
     * @param u index of vertex u
     * @return true on success
     */
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
            System.arraycopy(siblings, to, siblings, to + gapSize, gapFrom - to);
            System.arraycopy(arrows, to, arrows, to + gapSize, gapFrom - to);
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
            System.arraycopy(siblings, gapFrom + gapSize, siblings, gapFrom, from - gapFrom - gapSize);
            System.arraycopy(arrows, gapFrom + gapSize, arrows, gapFrom, from - gapFrom - gapSize);
            from -= gapSize;
        }
        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(siblings, from, from + size + gapSize, CLEAR_VALUE);
            Arrays.fill(arrows, from, from + size + gapSize, CLEAR_VALUE);
        }

        for (int i = vIndex, n = vertexCount; i < n; i++) {
            sizes[i] -= size;
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
    public int removeArrowAt(final int v, final int removalIndex) {
        final int from = getSiblingsFromOffset(v);
        final int to = getSiblingCount(v) + from;
        final int vIndex = v & (vertexCount - 1);
        final int u = siblings[from + removalIndex];

        if (gapIndex < vIndex) {
            removeAfterGap(from, to, removalIndex);
        } else if (gapIndex > vIndex) {
            removeBeforeGap(from, to, removalIndex);
        } else {
            removeAtGap(from, to, removalIndex);
        }

        gapIndex = vIndex;
        free++;
        gapSize++;

        for (int i = vIndex, n = vertexCount; i < n; i++) {
            sizes[i]--;
        }
        return u;
    }

    void grow() {
        final int newCapacity = capacity * 2;
        if (newCapacity <= capacity) {
            throw new OutOfMemoryError("can not grow to newCapacity=" + newCapacity + ", current capacity=" + capacity);
        }

        final int[] newSiblings = new int[newCapacity];
        final int[] newArrows = new int[newCapacity];

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

        final int gapFromOffset = getSiblingsToOffset(gapIndex);
        final int gapToOffset = gapFromOffset + gapSize;
        final int deltaCapacity = newCapacity - capacity;

        System.arraycopy(siblings, 0, newSiblings, 0, gapFromOffset);
        System.arraycopy(arrows, 0, newArrows, 0, gapFromOffset);
        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(newSiblings, gapFromOffset, gapToOffset + deltaCapacity, CLEAR_VALUE);
            Arrays.fill(newArrows, gapFromOffset, gapToOffset + deltaCapacity, CLEAR_VALUE);
        }
        final int length = getSiblingsToOffset() - gapToOffset;
        if (length > 0) {
            System.arraycopy(siblings, gapToOffset, newSiblings, gapToOffset + deltaCapacity, length);
            System.arraycopy(arrows, gapToOffset, newArrows, gapToOffset + deltaCapacity, length);
        }

        this.siblings = newSiblings;
        this.arrows = newArrows;
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
    void insertAtGap(final int u, final int data,
                     final int from, final int to,
                     final int insertionIndex) {
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
        final int length = to - from - insertionIndex;
        if (length > 0) {
            System.arraycopy(siblings, from + insertionIndex, siblings, from + insertionIndex + 1, length);
            System.arraycopy(arrows, from + insertionIndex, arrows, from + insertionIndex + 1, length);
        }

        // insert the element at insertion index
        siblings[from + insertionIndex] = u;
        arrows[from + insertionIndex] = data;
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
    void insertBeforeGap(final int u, final int data,
                         final int from, final int to,
                         final int insertionIndex) {


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
        final int gapFrom = getSiblingsToOffset(gapIndex);
        int length = gapFrom - to;
        System.arraycopy(siblings, to, siblings, to + free, length);
        System.arraycopy(arrows, to, arrows, to + free, length);

        // shift up to make room for the new element
        length = to - from - insertionIndex;
        System.arraycopy(siblings, from + insertionIndex, siblings, from + insertionIndex + 1, length);
        System.arraycopy(arrows, from + insertionIndex, arrows, from + insertionIndex + 1, length);

        // insert the element at insertion index
        siblings[from + insertionIndex] = u;
        arrows[from + insertionIndex] = data;

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(siblings, to + 1, to + free, CLEAR_VALUE);
            Arrays.fill(arrows, to + 1, to + free, CLEAR_VALUE);
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
        final int gapFrom = getSiblingsToOffset(gapIndex);
        int length = from + insertionIndex - gapFrom - free;
        if (length > 0) {
            System.arraycopy(siblings, gapFrom + free, siblings, gapFrom, length);
            System.arraycopy(arrows, gapFrom + free, arrows, gapFrom, length);
        }

        // insert the element at insertion index
        siblings[from + insertionIndex - free] = u;
        arrows[from + insertionIndex - free] = data;

        // reopen the gap by shifting the remainder of the indices down
        length = to - from - insertionIndex;
        System.arraycopy(siblings, from + insertionIndex, siblings, from + insertionIndex - free + 1, length);
        System.arraycopy(arrows, from + insertionIndex, arrows, from + insertionIndex - free + 1, length);

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(siblings, to - free + 1, to, CLEAR_VALUE);
            Arrays.fill(arrows, to - free + 1, to, CLEAR_VALUE);
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
    void removeAtGap(final int from, final int to,
                     final int removalIndex) {
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
            System.arraycopy(siblings, from + removalIndex + 1, siblings, from + removalIndex, length);
            System.arraycopy(arrows, from + removalIndex + 1, arrows, from + removalIndex, length);
        }
        if (CLEAR_UNUSED_ELEMENTS) {
            siblings[to - 1] = CLEAR_VALUE;
            arrows[to - 1] = CLEAR_VALUE;
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
    void removeBeforeGap(final int from, final int to,
                         final int removalIndex) {
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
        System.arraycopy(siblings, to, siblings, to + free, length);
        System.arraycopy(arrows, to, arrows, to + free, length);

        // shift down to remove the room of the removed element
        length = to - from - removalIndex;
        System.arraycopy(siblings, from + removalIndex + 1, siblings, from + removalIndex, length);
        System.arraycopy(arrows, from + removalIndex + 1, arrows, from + removalIndex, length);

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(siblings, to - 1, to + free, CLEAR_VALUE);
            Arrays.fill(arrows, to - 1, to + free, CLEAR_VALUE);
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
    void removeAfterGap(final int from, final int to,
                        final int removalIndex) {

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
        final int gapFrom = getSiblingsToOffset(gapIndex);
        int length = from + removalIndex - gapFrom - gapSize;
        if (length > 0) {
            System.arraycopy(siblings, gapFrom + free, siblings, gapFrom, length);
            System.arraycopy(arrows, gapFrom + free, arrows, gapFrom, length);
        }

        // reopen the gap by shifting the remainder of the indices down
        length = to - from - removalIndex - 1;
        System.arraycopy(siblings, from + removalIndex + 1, siblings, from + removalIndex - free, length);
        System.arraycopy(arrows, from + removalIndex + 1, arrows, from + removalIndex - free, length);

        if (CLEAR_UNUSED_ELEMENTS) {
            Arrays.fill(siblings, to - free - 1, to, CLEAR_VALUE);
            Arrays.fill(arrows, to - free - 1, to, CLEAR_VALUE);
        }
    }
}
