/*
 * @(#)MutableIndexedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.*;
import org.jhotdraw8.util.Preconditions;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.Arrays;


/**
 * A mutable indexed bi-directional graph.
 * <p>
 * Supports up to {@code 2^16 - 1} vertices.
 * <p>
 * This graph does not support multiple arrows between the same vertices.
 * <p>
 * This implementation uses chunks with a fixed number of vertices.
 * A chunk uses a compressed sparse row representation (CSR).
 * <p>
 * This implementation is efficient, if the graph is changed rarely. Changes
 * are expensive, because this implementation uses a single gap with
 * free elements in each chunk. The gap needs to be shifted around for
 * every insertion and removal of an arrow in the graph.
 * <p>
 * References:
 * <dl>
 *     <dt>JHotDraw 8</dt>
 *     <dd> This class has been derived from JHotDraw 8.
 *      © 2018 by the authors and contributors of JHotDraw. MIT License.</dd>
 * </dl>
 */
public class ChunkedMutableIntAttributed32BitIndexedBidiGraph implements MutableIndexedBidiGraph
        , IntAttributedIndexedBidiGraph {

    private static final boolean CLEAR_UNUSED_ELEMENTS = false;
    private static final int CLEAR_VALUE = 99;

    /**
     * Number of vertices per chunk.
     * Must be a power of 2.
     */
    private final int chunkSize;
    private final int chunkShift;
    private final int chunkMask;

    /**
     * Array of chunks for arrows to next vertices.
     */
    private @NonNull Chunk[] nextChunks = new Chunk[0];
    /**
     * Array of chunks for arrows to previous vertices.
     */
    private @NonNull Chunk[] prevChunks = new Chunk[0];
    private final int initialChunkCapacity = 4;

    public ChunkedMutableIntAttributed32BitIndexedBidiGraph() {
        this(256);
    }

    public ChunkedMutableIntAttributed32BitIndexedBidiGraph(final int chunkSize) {
        if (Integer.bitCount(chunkSize) != 1) {
            throw new IllegalArgumentException("chunkSize=" + chunkSize + " is not a power of 2");
        }
        this.chunkSize = chunkSize;
        this.chunkShift = Integer.numberOfTrailingZeros(chunkSize);
        this.chunkMask = chunkSize - 1;
    }

    interface Chunk {
        boolean addArrow(final int v, final int u, final int data, final boolean setIfPresent);

        int indexOf(final int v, final int u);

        int getSiblingsFromOffset(final int v);

        int getSiblingCount(final int v);

        int[] getChunkArray();

        boolean tryToRemoveArrow(final int v, final int u);

        void removeAllArrows(final int v);

        int removeArrowAt(final int v, final int removalIndex);

        int getSibling(final int v, final int k);

        int getArrow(final int v, final int k);

        int getVertexData(final int v);

        void setVertexData(final int v, final int data);
    }

    /**
     * A 'Compressed Row Storage' Chunk keeps the list of sibling vertices and
     * sibling arrows close together.
     * <p>
     * Uses one shared gap between the list of sibling vertices/arrows.
     * Moves the gap at each insertion/deletion operation. This may make it
     * slow for updates.
     */
    private class CrsChunk implements Chunk {

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
         *                        // There are {@link #chunkSize} vertices in this array.
         *
         * sizes: [ ⌈cumulated⌉, ... ]
         *                        // Stores the number of siblings for each vertex.
         *                        // There are {@link #chunkSize} vertices in this array
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

        public CrsChunk(final int capacity) {
            this.capacity = this.free = this.gapSize = capacity;
            this.gapIndex = 0;
            chunk = new int[chunkSize * 2 + capacity * 2];

            if (CLEAR_UNUSED_ELEMENTS) {
                Arrays.fill(chunk, getSiblingsFromOffset(), chunk.length, CLEAR_VALUE);
            }
        }

        /**
         * Finds the index of vertex u in the index list of vertex v.
         *
         * @param v index of vertex v
         * @param u index of vertex u
         * @return the index of u or (-index -1) if u is not in the index list.
         */
        public int indexOf(final int v, final int u) {
            final int from = getSiblingsFromOffset(v);
            final int to = from + getSiblingCount(v);

            final int result = Arrays.binarySearch(chunk, from, to, u);
            //final int result = OffsetBinarySearch.binarySearch(chunk, from, to, u);
            return result < 0 ? result + from : result - from;

            // Use linear search if sorting by index is not wanted.
            /*
            for (int i = from; i < to; i++) {
                if (chunk[i] == u) {
                    return i - from;
                }
            }
            return ~(to - from);
            */
        }

        public int[] getChunkArray() {
            return chunk;
        }

        public int getSiblingCount(final int v) {
            final int vIndex = v & chunkMask;
            final int sizesOffset = getSizesOffset();
            return vIndex == 0
                    ? chunk[sizesOffset + vIndex]
                    : chunk[sizesOffset + vIndex] - chunk[sizesOffset + vIndex - 1];
        }

        public int getVertexData(final int v) {
            final int vIndex = v & chunkMask;
            return chunk[vIndex];
        }

        public void setVertexData(final int v, final int data) {
            final int vIndex = v & chunkMask;
            chunk[vIndex] = data;
        }

        public int getArrow(final int v, final int k) {
            Preconditions.checkIndex(k, getArrowCount(v));
            return chunk[getArrowsFromOffset(v) + k];
        }

        public int getSiblingsFromOffset(final int v) {
            final int vIndex = v & chunkMask;
            final int indicesOffset = getSiblingsFromOffset();
            return vIndex == 0
                    ? indicesOffset
                    : indicesOffset + chunk[getSizesOffset() + vIndex - 1]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        int getSiblingsToOffset(final int v) {
            final int vIndex = v & chunkMask;
            return getSiblingsFromOffset()
                    + chunk[getSizesOffset() + vIndex]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        int getSiblingsFromOffset() {
            return chunkSize * 2;
        }

        public int getSibling(final int v, final int k) {
            Preconditions.checkIndex(k, getArrowCount(v));
            return chunk[getSiblingsFromOffset(v) + k];
        }

        int getSizesOffset() {
            return chunkSize;
        }

        int getArrowsFromOffset() {
            return chunkSize * 2 + capacity;
        }

        int getArrowsToOffset() {
            return chunk.length - (gapIndex == chunkSize - 1 ? gapSize : 0);
        }

        int getSiblingsToOffset() {
            return chunkSize * 2 + capacity - (gapIndex == chunkSize - 1 ? gapSize : 0);
        }

        int getArrowCount(final int v) {
            final int vIndex = v & chunkMask;
            final int sizesOffset = getSizesOffset();
            return vIndex == 0
                    ? chunk[sizesOffset + vIndex]
                    : chunk[sizesOffset + vIndex] - chunk[sizesOffset + vIndex - 1];
        }

        int getArrowsFromOffset(final int v) {
            final int vIndex = v & chunkMask;
            final int arrowsOffset = getArrowsFromOffset();
            return vIndex == 0
                    ? arrowsOffset
                    : arrowsOffset + chunk[getSizesOffset() + vIndex - 1]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        int getArrowsToOffset(final int v) {
            final int vIndex = v & chunkMask;
            return getArrowsFromOffset()
                    + chunk[getSizesOffset() + vIndex]
                    + (vIndex <= gapIndex ? 0 : gapSize);
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
        public boolean addArrow(final int v, final int u, final int data, final boolean setIfPresent) {
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
            final int siblingsFrom = getSiblingsFromOffset(v);
            final int siblingCount = getSiblingCount(v);
            final int siblingsTo = siblingCount + siblingsFrom;
            final int arrowDataFrom = getArrowsFromOffset(v);
            final int arrowDataTo = arrowDataFrom + siblingCount;
            final int insertionIndex = ~result;
            final int vIndex = v & chunkMask;

            if (gapIndex < vIndex) {
                insertAfterGap(u, data, siblingsFrom, siblingsTo, arrowDataFrom, arrowDataTo, insertionIndex);
            } else if (gapIndex > vIndex) {
                insertBeforeGap(u, data, siblingsFrom, siblingsTo, arrowDataFrom, arrowDataTo, insertionIndex);
            } else {
                insertAtGap(u, data, siblingsFrom, siblingsTo, arrowDataFrom, arrowDataTo, insertionIndex);
            }


            gapIndex = vIndex;
            free--;
            gapSize--;

            final int sizesOffset = getSizesOffset();
            for (int i = sizesOffset + vIndex, n = sizesOffset + chunkSize; i < n; i++) {
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
        public boolean tryToRemoveArrow(final int v, final int u) {
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
            final int vIndex = v & chunkMask;
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
            for (int i = sizesOffset + vIndex, n = sizesOffset + chunkSize; i < n; i++) {
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
        public int removeArrowAt(final int v, final int removalIndex) {
            final int siblingsFrom = getSiblingsFromOffset(v);
            final int siblingsTo = getSiblingCount(v) + siblingsFrom;
            final int arrowDataFrom = getArrowsFromOffset(v);
            final int arrowDataTo = getArrowsToOffset(v);
            final int vIndex = v & chunkMask;
            final int u = chunk[siblingsFrom + removalIndex];

            if (gapIndex < vIndex) {
                removeAfterGap(siblingsFrom, siblingsTo, arrowDataFrom, arrowDataTo, removalIndex);
            } else if (gapIndex > vIndex) {
                removeBeforeGap(siblingsFrom, siblingsTo, arrowDataFrom, arrowDataTo, removalIndex);
            } else {
                removeAtGap(siblingsFrom, siblingsTo, arrowDataFrom, arrowDataTo, removalIndex);
            }

            gapIndex = vIndex;
            free++;
            gapSize++;

            final int sizesOffset = getSizesOffset();
            for (int i = sizesOffset + vIndex, n = sizesOffset + chunkSize; i < n; i++) {
                chunk[i]--;
            }
            return u;
        }

        void grow() {
            final int newCapacity = capacity * 2;
            if (newCapacity <= capacity) {
                throw new OutOfMemoryError("can not grow to newCapacity=" + newCapacity + ", current capacity=" + capacity);
            }

            final int[] newChunk = new int[chunkSize * 2 + newCapacity * 2];

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
         * @param siblingsFrom
         * @param siblingsTo
         * @param arrowDataFrom
         * @param arrowDataTo
         * @param insertionIndex
         */
        void insertAtGap(final int u, final int data, final int siblingsFrom, final int siblingsTo, final int arrowDataFrom, final int arrowDataTo, final int insertionIndex) {
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
            final int length = siblingsTo - siblingsFrom - insertionIndex;
            if (length > 0) {
                System.arraycopy(chunk, siblingsFrom + insertionIndex, chunk, siblingsFrom + insertionIndex + 1, length);
                System.arraycopy(chunk, arrowDataFrom + insertionIndex, chunk, arrowDataFrom + insertionIndex + 1, length);
            }

            // insert the element at insertion index
            chunk[siblingsFrom + insertionIndex] = u;
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
         * @param siblingsFrom
         * @param siblingsTo
         * @param arrowDataFrom
         * @param arrowDataTo
         * @param insertionIndex
         */
        void insertBeforeGap(final int u, final int data, final int siblingsFrom, final int siblingsTo, final int arrowDataFrom, final int arrowDataTo, final int insertionIndex) {
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
            int length = siblingsStartOfGapOffset - siblingsTo;
            System.arraycopy(chunk, siblingsTo, chunk, siblingsTo + free, length);
            System.arraycopy(chunk, arrowDataTo, chunk, arrowDataTo + free, length);

            // shift up to make room for the new element
            length = siblingsTo - siblingsFrom - insertionIndex;
            System.arraycopy(chunk, siblingsFrom + insertionIndex, chunk, siblingsFrom + insertionIndex + 1, length);
            System.arraycopy(chunk, arrowDataFrom + insertionIndex, chunk, arrowDataFrom + insertionIndex + 1, length);

            // insert the element at insertion index
            chunk[siblingsFrom + insertionIndex] = u;
            chunk[arrowDataFrom + insertionIndex] = data;

            if (CLEAR_UNUSED_ELEMENTS) {
                Arrays.fill(chunk, siblingsTo + 1, siblingsTo + free, CLEAR_VALUE);
                Arrays.fill(chunk, arrowDataTo + 1, arrowDataTo + free, CLEAR_VALUE);
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
         * @param siblingsFrom
         * @param siblingsTo
         * @param arrowDataFrom
         * @param arrowDataTo
         * @param insertionIndex
         */
        void insertAfterGap(final int u, final int data, final int siblingsFrom, final int siblingsTo, final int arrowDataFrom, final int arrowDataTo, final int insertionIndex) {
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
            int length = siblingsFrom + insertionIndex - siblingsGapFrom - free;
            if (length > 0) {
                System.arraycopy(chunk, siblingsGapFrom + free, chunk, siblingsGapFrom, length);
                System.arraycopy(chunk, arrowsGapFrom + free, chunk, arrowsGapFrom, length);
            }

            // insert the element at insertion index
            chunk[siblingsFrom + insertionIndex - free] = u;
            chunk[arrowDataFrom + insertionIndex - free] = data;

            // reopen the gap by shifting the remainder of the indices down
            length = siblingsTo - siblingsFrom - insertionIndex;
            System.arraycopy(chunk, siblingsFrom + insertionIndex, chunk, siblingsFrom + insertionIndex - free + 1, length);
            System.arraycopy(chunk, arrowDataFrom + insertionIndex, chunk, arrowDataFrom + insertionIndex - free + 1, length);

            if (CLEAR_UNUSED_ELEMENTS) {
                Arrays.fill(chunk, siblingsTo - free + 1, siblingsTo, CLEAR_VALUE);
                Arrays.fill(chunk, arrowDataTo - free + 1, arrowDataTo, CLEAR_VALUE);
            }
        }

        /**
         * Inserts vertex 'u' in the siblings list of vertex 'v'.
         * <p>
         * The gap is located at the end of the siblings list of vertex 'v'.
         *
         * @param siblingsFrom
         * @param siblingsTo
         * @param arrowDataFrom
         * @param arrowDataTo
         * @param removalIndex
         */
        void removeAtGap(final int siblingsFrom, final int siblingsTo, final int arrowDataFrom, final int arrowDataTo, final int removalIndex) {
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
            final int length = siblingsTo - removalIndex - siblingsFrom - 1;
            if (length > 0) {
                System.arraycopy(chunk, siblingsFrom + removalIndex + 1, chunk, siblingsFrom + removalIndex, length);
                System.arraycopy(chunk, arrowDataFrom + removalIndex + 1, chunk, arrowDataFrom + removalIndex, length);
            }
            if (CLEAR_UNUSED_ELEMENTS) {
                chunk[siblingsTo - 1] = CLEAR_VALUE;
                chunk[arrowDataTo - 1] = CLEAR_VALUE;
            }
        }

        /**
         * Inserts vertex 'u' in the siblings list of vertex 'v'.
         * <p>
         * The siblings list of vertex 'v' is located somewhere
         * before the siblings list that contains the gap.
         *
         * @param siblingsFrom
         * @param siblingsTo
         * @param arrowDataFrom
         * @param arrowDataTo
         * @param removalIndex
         */
        void removeBeforeGap(final int siblingsFrom, final int siblingsTo, final int arrowDataFrom, final int arrowDataTo, final int removalIndex) {
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
            final int siblingsStartOfGapOffset = getSiblingsToOffset(gapIndex);
            int length = siblingsStartOfGapOffset - siblingsTo;
            System.arraycopy(chunk, siblingsTo, chunk, siblingsTo + free, length);
            System.arraycopy(chunk, arrowDataTo, chunk, arrowDataTo + free, length);

            // shift down to remove the room of the removed element
            length = siblingsTo - siblingsFrom - removalIndex;
            System.arraycopy(chunk, siblingsFrom + removalIndex + 1, chunk, siblingsFrom + removalIndex, length);
            System.arraycopy(chunk, arrowDataFrom + removalIndex + 1, chunk, arrowDataFrom + removalIndex, length);

            if (CLEAR_UNUSED_ELEMENTS) {
                Arrays.fill(chunk, siblingsTo - 1, siblingsTo + free, CLEAR_VALUE);
                Arrays.fill(chunk, arrowDataTo - 1, arrowDataTo + free, CLEAR_VALUE);
            }
        }

        /**
         * Inserts vertex 'u' in the siblings list of vertex 'v'.
         * <p>
         * The siblings list of vertex 'v' is located somewhere after
         * the siblings list that contains the gap.
         *
         * @param siblingsFrom
         * @param siblingsTo
         * @param arrowDataFrom
         * @param arrowDataTo
         * @param removalIndex
         */
        void removeAfterGap(final int siblingsFrom, final int siblingsTo, final int arrowDataFrom, final int arrowDataTo, final int removalIndex) {
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
            int length = siblingsFrom + removalIndex - siblingsGapFrom - gapSize;
            if (length > 0) {
                System.arraycopy(chunk, siblingsGapFrom + free, chunk, siblingsGapFrom, length);
                System.arraycopy(chunk, arrowsGapFrom + free, chunk, arrowsGapFrom, length);
            }

            // reopen the gap by shifting the remainder of the indices down
            length = siblingsTo - siblingsFrom - removalIndex - 1;
            System.arraycopy(chunk, siblingsFrom + removalIndex + 1, chunk, siblingsFrom + removalIndex - free, length);
            System.arraycopy(chunk, arrowDataFrom + removalIndex + 1, chunk, arrowDataFrom + removalIndex - free, length);

            if (CLEAR_UNUSED_ELEMENTS) {
                Arrays.fill(chunk, siblingsTo - free - 1, siblingsTo, CLEAR_VALUE);
                Arrays.fill(chunk, arrowDataTo - free - 1, arrowDataTo, CLEAR_VALUE);
            }
        }
    }


    int vertexCount = 0;
    int arrowCount = 0;

    /**
     * Adds the arrow if it is absent.
     *
     * @param v index of vertex 'v'
     * @param u index of vertex 'u'
     */
    @Override
    public void addArrowAsInt(final int v, final int u) {
        addArrowAsInt(v, u, 0);
    }

    /**
     * Adds the arrow if it is absent.
     *
     * @param v index of vertex 'v'
     * @param u index of vertex 'u'
     */
    @Override
    public void addArrowAsInt(final int v, final int u, final int data) {
        addArrowIfAbsentAsInt(v, u, data);
    }


    /**
     * Adds the arrow if its absent, updates the arrow data if the arrow is
     * present.
     *
     * @param v    index of vertex 'v'
     * @param u    index of vertex 'u'
     * @param data the arrow data
     * @return true if the arrow was absent
     */
    public boolean addOrUpdateArrowAsInt(final int v, final int u, final int data) {
        final Chunk uChunk = getPrevChunk(u);
        final boolean added = uChunk.addArrow(u, v, data, true);
        final Chunk vChunk = getNextChunk(v);
        vChunk.addArrow(v, u, data, true);
        if (added) {
            arrowCount++;
        }
        return added;
    }


    /**
     * Adds the arrow if its absent, updates the arrow data if the arrow is
     * present.
     *
     * @param v    index of vertex 'v'
     * @param u    index of vertex 'u'
     * @param data the arrow data
     * @return true if the arrow was absent
     */
    public boolean addArrowIfAbsentAsInt(final int v, final int u, final int data) {
        final Chunk uChunk = getPrevChunk(u);
        boolean added = uChunk.addArrow(u, v, data, false);
        if (added) {
            final Chunk vChunk = getNextChunk(v);
            vChunk.addArrow(v, u, data, false);
            arrowCount++;
        }
        return added;
    }


    @Override
    public int findIndexOfPrevAsInt(final int v, final int u) {
        final Chunk chunk = getPrevChunk(v);
        return chunk.indexOf(v, u);
    }

    @Override
    public int findIndexOfNextAsInt(final int v, final int u) {
        final Chunk chunk = getNextChunk(v);
        return chunk.indexOf(v, u);
    }


    @Override
    public void addVertexAsInt() {
        grow(vertexCount + 1);
        vertexCount++;
    }

    @Override
    public void addVertexAsInt(final int v) {
        if (v < vertexCount) {
            throw new UnsupportedOperationException();
        }
        grow(vertexCount + 1);
        vertexCount++;
    }

    @Override
    public void removeAllPrevAsInt(final int v) {
        final Chunk chunk = getPrevChunk(v);
        final int from = chunk.getSiblingsFromOffset(v);
        int[] a = chunk.getChunkArray();
        final int to = chunk.getSiblingCount(v) + from;
        int[] next = new int[to - from];
        System.arraycopy(a, from, next, 0, next.length);
        for (int i = next.length - 1; i >= 0; i--) {
            final int u = next[i];
            final Chunk nextChunk = getNextChunk(u);
            nextChunk.tryToRemoveArrow(u, v);
        }
        chunk.removeAllArrows(v);
    }

    @Override
    public void removeAllNextAsInt(final int v) {
        final Chunk chunk = getNextChunk(v);
        final int from = chunk.getSiblingsFromOffset(v);
        final int to = chunk.getSiblingCount(v) + from;
        int[] a = chunk.getChunkArray();
        int[] next = new int[to - from];
        System.arraycopy(a, from, next, 0, next.length);
        for (int i = next.length - 1; i >= 0; i--) {
            final int u = next[i];
            final Chunk prevChunk = getPrevChunk(u);
            prevChunk.tryToRemoveArrow(u, v);
        }
        chunk.removeAllArrows(v);
    }

    @Override
    public void removeNextAsInt(final int v, final int index) {
        Preconditions.checkIndex(index, getNextCount(v));
        final int u = getNextChunk(v).removeArrowAt(v, index);
        getPrevChunk(u).tryToRemoveArrow(u, v);
        arrowCount--;
    }

    @Override
    public void removePrevAsInt(final int v, final int index) {
        Preconditions.checkIndex(index, getPrevCount(v));
        final int u = getPrevChunk(v).removeArrowAt(v, index);
        getNextChunk(u).tryToRemoveArrow(u, v);
        arrowCount--;
    }

    @Override
    public void removeVertexAsInt(final int v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public int getNextAsInt(final int v, final int index) {
        return getNextChunk(v).getSibling(v, index);
    }

    @Override
    public int getNextArrowAsInt(final int v, final int k) {
        return getNextChunk(v).getArrow(v, k);
    }

    @Override
    public int getPrevArrowAsInt(final int v, final int k) {
        return getPrevChunk(v).getArrow(v, k);
    }

    @Override
    public int getVertexAsInt(final int v) {
        return getNextChunk(v).getVertexData(v);
    }

    public void setVertexAsInt(final int v, final int data) {
        getNextChunk(v).setVertexData(v, data);
        getPrevChunk(v).setVertexData(v, data);
    }

    @Override
    public int getNextCount(final int v) {
        return getNextChunk(v).getSiblingCount(v);
    }

    private Chunk getNextChunk(final int v) {
        return getOrCreateChunk(nextChunks, v);
    }

    private Chunk getPrevChunk(final int v) {
        return getOrCreateChunk(prevChunks, v);
    }

    Chunk getOrCreateChunk(final Chunk[] chunks, final int v) {
        @NonNull ChunkedMutableIntAttributed32BitIndexedBidiGraph.Chunk chunk = chunks[v >>> chunkShift];
        if (chunk == null) {
            chunk = new CrsChunk(initialChunkCapacity);
            chunks[v >>> chunkShift] = chunk;
        }
        return chunk;
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public int getPrevAsInt(final int v, final int k) {
        return getPrevChunk(v).getSibling(v, k);
    }

    @Override
    public int getPrevCount(final int v) {
        return getPrevChunk(v).getSiblingCount(v);
    }

    private void grow(final int capacity) {
        final int chunkedCapacity = (capacity + chunkSize - 1) >>> chunkShift;
        final Chunk[] temp = (Chunk[]) ListHelper.grow(vertexCount, chunkedCapacity, 1, nextChunks);
        if (temp.length < chunkedCapacity) {
            throw new IllegalStateException("too much capacity requested:" + capacity);
        }
        nextChunks = temp;
        prevChunks = (Chunk[]) ListHelper.grow(vertexCount, chunkedCapacity, 1, prevChunks);
    }

    public void clear() {
        Arrays.fill(nextChunks, null);
        Arrays.fill(prevChunks, null);
        arrowCount = 0;
        vertexCount = 0;
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator backwardBreadthFirstLongSpliterator(final int vidx) {
        return backwardBreadthFirstLongSpliterator(vidx, new DenseIntSet(vertexCount));
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator backwardBreadthFirstLongSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new BreadthFirstLongSpliterator(vidx, prevChunks, visited);
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator backwardDepthFirstLongSpliterator(final int vidx) {
        return backwardDepthFirstLongSpliterator(vidx, new DenseIntSet(vertexCount));
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator backwardDepthFirstLongSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new DepthFirstLongSpliterator(vidx, prevChunks, visited);
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator breadthFirstLongSpliterator(final int vidx) {
        return breadthFirstLongSpliterator(vidx, new DenseIntSet(vertexCount));
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator breadthFirstLongSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new BreadthFirstLongSpliterator(vidx, nextChunks, visited);
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator depthFirstLongSpliterator(final int vidx) {
        return depthFirstLongSpliterator(vidx, new DenseIntSet(vertexCount));
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator depthFirstLongSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new DepthFirstLongSpliterator(vidx, nextChunks, visited);
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull IntEnumeratorSpliterator breadthFirstIntSpliterator(final int vidx) {
        return breadthFirstIntSpliterator(vidx, new DenseIntSet(vertexCount));
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull IntEnumeratorSpliterator breadthFirstIntSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new BreadthFirstIntSpliterator(vidx, nextChunks, visited);
    }

    private class BreadthFirstLongSpliterator extends AbstractLongEnumeratorSpliterator {

        private final Chunk[] chunks;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;

        protected BreadthFirstLongSpliterator(final int root, final Chunk[] chunks, final @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.chunks = chunks;
            this.visited = visited;
            if (visited.addAsInt(root)) {
                deque.addFirstAsInt(root);
            }
        }

        @Override
        public boolean moveNext() {
            if (deque.isEmpty()) {
                return false;
            }
            final int v = deque.removeFirstAsInt();
            final Chunk chunk = getOrCreateChunk(chunks, v);
            current = ((long) chunk.getVertexData(v)) << 32 | (v & 0xffff_ffffL);
            final int from = chunk.getSiblingsFromOffset(v);
            final int to = chunk.getSiblingCount(v) + from;
            final int[] a = chunk.getChunkArray();
            for (int i = from; i < to; i++) {
                final int u = a[i];
                if (visited.addAsInt(u)) {
                    deque.addLastAsInt(u);
                }
            }
            return true;
        }
    }

    private class DepthFirstLongSpliterator extends AbstractLongEnumeratorSpliterator {

        private final Chunk[] chunks;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;

        protected DepthFirstLongSpliterator(final int root, final Chunk[] chunks, final @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.chunks = chunks;
            this.visited = visited;
            if (visited.addAsInt(root)) {
                deque.addFirstAsInt(root);
            }
        }

        @Override
        public boolean moveNext() {
            if (deque.isEmpty()) {
                return false;
            }
            final int v = deque.removeFirstAsInt();
            final Chunk chunk = getOrCreateChunk(chunks, v);
            current = ((long) chunk.getVertexData(v)) << 32 | (v & 0xffff_ffffL);
            final int from = chunk.getSiblingsFromOffset(v);
            final int to = chunk.getSiblingCount(v) + from;
            int[] a = chunk.getChunkArray();

            // Performance: Unrolled loop to take advantage of out-of-order execution.
            int i = from;
            for (; i < to - 4; i += 4) {
                final int u0 = a[i];
                if (visited.addAsInt(u0)) {
                    deque.addFirstAsInt(u0);
                }
                final int u1 = a[i + 1];
                if (visited.addAsInt(u1)) {
                    deque.addFirstAsInt(u1);
                }
                final int u2 = a[i + 2];
                if (visited.addAsInt(u2)) {
                    deque.addFirstAsInt(u2);
                }
                final int u3 = a[i + 3];
                if (visited.addAsInt(u3)) {
                    deque.addFirstAsInt(u3);
                }
            }
            for (; i < to; i++) {
                final int u = a[i];
                if (visited.addAsInt(u)) {
                    deque.addFirstAsInt(u);
                }
            }
            return true;
        }
    }

    private class BreadthFirstIntSpliterator extends AbstractIntEnumeratorSpliterator {

        private final Chunk[] chunks;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;

        protected BreadthFirstIntSpliterator(final int root, final Chunk[] chunks, final @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.chunks = chunks;
            this.visited = visited;
            if (visited.addAsInt(root)) {
                deque.addLastAsInt(root);
            }
        }

        @Override
        public boolean moveNext() {
            if (deque.isEmpty()) {
                return false;
            }
            final int v = deque.removeFirstAsInt();
            final Chunk chunk = getOrCreateChunk(chunks, v);
            current = v;
            final int from = chunk.getSiblingsFromOffset(v);
            final int to = chunk.getSiblingCount(v) + from;
            int[] a = chunk.getChunkArray();

            // Performance: Unrolled loop to take advantage of out-of-order execution.
            int i = from;
            for (; i < to - 4; i += 4) {
                final int u0 = a[i];
                if (visited.addAsInt(u0)) {
                    deque.addLastAsInt(u0);
                }
                final int u1 = a[i + 1];
                if (visited.addAsInt(u1)) {
                    deque.addLastAsInt(u1);
                }
                final int u2 = a[i + 2];
                if (visited.addAsInt(u2)) {
                    deque.addLastAsInt(u2);
                }
                final int u3 = a[i + 3];
                if (visited.addAsInt(u3)) {
                    deque.addLastAsInt(u3);
                }
            }
            for (; i < to; i++) {
                final int u = a[i];
                if (visited.addAsInt(u)) {
                    deque.addLastAsInt(u);
                }
            }
            return true;
        }
    }
}
