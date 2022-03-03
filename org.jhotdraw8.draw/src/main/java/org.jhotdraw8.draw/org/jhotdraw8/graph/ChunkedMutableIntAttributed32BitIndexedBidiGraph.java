/*
 * @(#)MutableIndexedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ListHelper;
import org.jhotdraw8.util.Preconditions;

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
 */
public class ChunkedMutableIntAttributed32BitIndexedBidiGraph implements MutableIndexedBidiGraph
        , IntAttributedIndexedBidiGraph {
    private static final boolean CLEAR_UNUSED_ELEMENTS = true;
    private static final int CLEAR_VALUE = 99;
    /**
     * Value to which sizes are rounded up.
     * Must be a power of 2.
     */
    private final int sizeRounding = 4;
    private final int sizeRoundingMask = sizeRounding - 1;
    /**
     * Number of vertices per chunk.
     * Must be a power of 2.
     */
    private final int chunkSize = 4;
    private final int chunkShift = Integer.numberOfTrailingZeros(chunkSize);
    private final int chunkMask = chunkSize - 1;

    /**
     * Array of chunks for arrows to next vertices.
     */
    private @NonNull Chunk[] nextChunks = new Chunk[0];
    /**
     * Array of chunks for arrows to previous vertices.
     */
    private @NonNull Chunk[] prevChunks = new Chunk[0];
    private int initialChunkCapacity = 4;

    public ChunkedMutableIntAttributed32BitIndexedBidiGraph() {
    }

    private class Chunk {
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
         *                        // TODO Sizes are rounded up by {@link #sizeRounding}
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

        public Chunk(int capacity) {
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
        private int findIndexOf(int v, int u) {
            int from = getSiblingsFromOffset(v);
            int to = getSiblingsToOffset(v);
            int result = Arrays.binarySearch(chunk, from, to, u);
            return result < 0 ? result + from : result - from;
        }

        public int getSiblingCount(int v) {
            int vIndex = v & chunkMask;
            int sizesOffset = getSizesOffset();
            return vIndex == 0
                    ? chunk[sizesOffset + vIndex]
                    : chunk[sizesOffset + vIndex] - chunk[sizesOffset + vIndex - 1];
        }

        public int getVertexData(int v) {
            int vIndex = v & chunkMask;
            return chunk[vIndex];
        }

        public void setVertexData(int v, int data) {
            int vIndex = v & chunkMask;
            chunk[vIndex] = data;
        }

        public int getArrow(int v, int k) {
            Preconditions.checkIndex(k, getArrowCount(v));
            return chunk[getArrowsFromOffset(v) + k];
        }

        private int getSiblingsFromOffset(int v) {
            int vIndex = v & chunkMask;
            int indicesOffset = getSiblingsFromOffset();
            return vIndex == 0
                    ? indicesOffset
                    : indicesOffset + chunk[getSizesOffset() + vIndex - 1]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        private int getSiblingsToOffset(int v) {
            int vIndex = v & chunkMask;
            return getSiblingsFromOffset()
                    + chunk[getSizesOffset() + vIndex]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        private int getSiblingsFromOffset() {
            return chunkSize * 2;
        }

        public int getSibling(int v, int k) {
            Preconditions.checkIndex(k, getArrowCount(v));
            return chunk[getSiblingsFromOffset(v) + k];
        }

        private int getSizesOffset() {
            return chunkSize;
        }

        private int getArrowsFromOffset() {
            return chunkSize * 2 + capacity;
        }

        private int getArrowsToOffset() {
            return chunk.length - (gapIndex == chunkSize - 1 ? gapSize : 0);
        }

        private int getSiblingsToOffset() {
            return chunkSize * 2 + capacity - (gapIndex == chunkSize - 1 ? gapSize : 0);
        }

        private int getArrowCount(int v) {
            int vIndex = v & chunkMask;
            int sizesOffset = getSizesOffset();
            return vIndex == 0
                    ? chunk[sizesOffset + vIndex]
                    : chunk[sizesOffset + vIndex] - chunk[sizesOffset + vIndex - 1];
        }

        private int getArrowsFromOffset(int v) {
            int vIndex = v & chunkMask;
            int arrowsOffset = getArrowsFromOffset();
            return vIndex == 0
                    ? arrowsOffset
                    : arrowsOffset + chunk[getSizesOffset() + vIndex - 1]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        private int getArrowsToOffset(int v) {
            int vIndex = v & chunkMask;
            return getArrowsFromOffset()
                    + chunk[getSizesOffset() + vIndex]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        /**
         * Adds an arrow from vertex v to vertex u, if it is not already present.
         *
         * @param v    index of vertex v
         * @param u    index of vertex u
         * @param data the arrow data
         * @return true on success
         */
        private boolean tryToAddArrow(int v, int u, int data) {
            int result = findIndexOf(v, u);
            if (result >= 0) {
                return false;
            }
            if (free < 1) {
                grow();
            }
            int siblingsFrom = getSiblingsFromOffset(v);
            int siblingsTo = getSiblingsToOffset(v);
            int arrowDataFrom = getArrowsFromOffset(v);
            int arrowDataTo = getArrowsToOffset(v);
            int insertionIndex = ~result;
            int vIndex = v & chunkMask;

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

            int sizesOffset = getSizesOffset();
            for (int i = sizesOffset + vIndex, n = sizesOffset + chunkSize; i < n; i++) {
                chunk[i]++;
            }

            return true;
        }

        /**
         * Removes an arrow from vertex v to vertex u, if it is present.
         *
         * @param v index of vertex v
         * @param u index of vertex u
         * @return true on success
         */
        private boolean tryToRemoveArrow(int v, int u) {
            int result = findIndexOf(v, u);
            if (result < 0) {
                return false;
            }
            removeArrowAt(v, result);
            return true;
        }

        /**
         * Removes an arrow from vertex v to the a vertex u at the specified
         * index.
         *
         * @param v            index of vertex v
         * @param removalIndex index of vertex u
         * @return returns the removed arrow u
         */
        private int removeArrowAt(int v, int removalIndex) {
            int siblingsFrom = getSiblingsFromOffset(v);
            int siblingsTo = getSiblingsToOffset(v);
            int arrowDataFrom = getArrowsFromOffset(v);
            int arrowDataTo = getArrowsToOffset(v);
            int vIndex = v & chunkMask;
            int u = chunk[siblingsFrom + removalIndex];

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

            int sizesOffset = getSizesOffset();
            for (int i = sizesOffset + vIndex, n = sizesOffset + chunkSize; i < n; i++) {
                chunk[i]--;
            }
            return u;
        }

        private void grow() {
            int newCapacity = capacity * 2;
            if (newCapacity <= capacity) {
                throw new OutOfMemoryError("can not grow to newCapacity=" + newCapacity + ", current capacity=" + capacity);
            }

            int[] newChunk = new int[chunkSize * 2 + newCapacity * 2];

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

            int siblingsGapFromOffset = getSiblingsToOffset(gapIndex);
            int arrowsGapFromOffset = getArrowsToOffset(gapIndex);
            int siblingsGapToOffset = siblingsGapFromOffset + gapSize;
            int arrowsGapToOffset = arrowsGapFromOffset + gapSize;
            int arrowsFromOffset = getArrowsFromOffset();
            int deltaCapacity = newCapacity - capacity;

            System.arraycopy(chunk, 0, newChunk, 0, siblingsGapFromOffset);
            System.arraycopy(chunk, arrowsFromOffset, newChunk, arrowsFromOffset + deltaCapacity, getArrowsToOffset(gapIndex) - arrowsFromOffset);
            if (CLEAR_UNUSED_ELEMENTS) {
                Arrays.fill(newChunk, siblingsGapFromOffset, siblingsGapToOffset + deltaCapacity, CLEAR_VALUE);
                Arrays.fill(newChunk, arrowsGapFromOffset + deltaCapacity, arrowsGapToOffset + deltaCapacity * 2, CLEAR_VALUE);
            }
            int length = getSiblingsToOffset() - siblingsGapToOffset;
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
        private void insertAtGap(int u, int data, int siblingsFrom, int siblingsTo, int arrowDataFrom, int arrowDataTo, int insertionIndex) {
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
            int length = siblingsTo - siblingsFrom - insertionIndex;
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
        private void insertBeforeGap(int u, int data, int siblingsFrom, int siblingsTo, int arrowDataFrom, int arrowDataTo, int insertionIndex) {
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
            int siblingsStartOfGapOffset = getSiblingsToOffset(gapIndex);
            int arrowDataStartOfGapOffset = getArrowsToOffset(gapIndex);
            System.arraycopy(chunk, siblingsFrom + insertionIndex, chunk, siblingsTo + free, siblingsStartOfGapOffset - siblingsFrom - insertionIndex);
            System.arraycopy(chunk, arrowDataFrom + insertionIndex, chunk, arrowDataTo + free, arrowDataStartOfGapOffset - arrowDataFrom - insertionIndex);

            // shift up to make room for the new element
            System.arraycopy(chunk, siblingsFrom + insertionIndex, chunk, siblingsFrom + insertionIndex + 1, siblingsTo - siblingsFrom - insertionIndex);
            System.arraycopy(chunk, arrowDataFrom + insertionIndex, chunk, arrowDataFrom + insertionIndex + 1, siblingsTo - siblingsFrom - insertionIndex);

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
        private void insertAfterGap(int u, int data, int siblingsFrom, int siblingsTo, int arrowDataFrom, int arrowDataTo, int insertionIndex) {
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
            int siblingsGapFrom = getSiblingsToOffset(gapIndex);
            int arrowsGapFrom = siblingsGapFrom + capacity;
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
        private void removeAtGap(int siblingsFrom, int siblingsTo, int arrowDataFrom, int arrowDataTo, int removalIndex) {
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
            int length = siblingsTo - removalIndex - siblingsFrom - 1;
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
        private void removeBeforeGap(int siblingsFrom, int siblingsTo, int arrowDataFrom, int arrowDataTo, int removalIndex) {
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
            int siblingsStartOfGapOffset = getSiblingsToOffset(gapIndex);
            int arrowDataStartOfGapOffset = getArrowsToOffset(gapIndex);
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
        private void removeAfterGap(int siblingsFrom, int siblingsTo, int arrowDataFrom, int arrowDataTo, int removalIndex) {
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
            int siblingsGapFrom = getSiblingsToOffset(gapIndex);
            int arrowsGapFrom = siblingsGapFrom + capacity;
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

    @Override
    public void addArrow(int v, int u) {
        addArrow(v, u, 0);
    }

    @Override
    public void addArrow(int v, int u, int data) {
        Chunk vChunk = getNextChunk(v);
        Chunk uChunk = getPrevChunk(u);
        if (vChunk.tryToAddArrow(v, u, data)) {
            uChunk.tryToAddArrow(u, v, data);
            arrowCount++;
        }
    }


    @Override
    public int findIndexOfPrevAsInt(int v, int u) {
        Chunk chunk = getPrevChunk(v);
        return chunk.findIndexOf(v, u);
    }

    @Override
    public int findIndexOfNextAsInt(int v, int u) {
        Chunk chunk = getNextChunk(v);
        return chunk.findIndexOf(v, u);
    }


    @Override
    public void addVertex() {
        grow(vertexCount + 1);
        vertexCount++;
    }

    @Override
    public void addVertex(int vidx) {
        if (vidx < vertexCount) {
            throw new UnsupportedOperationException();
        }
        grow(vertexCount + 1);
        vertexCount++;
    }

    @Override
    public void removeAllPrev(int vidx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllNext(int v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeNext(int v, int i) {
        int u = getNextChunk(v).removeArrowAt(v, i);
        getPrevChunk(u).tryToRemoveArrow(u,v);
    }

    @Override
    public void removeVertex(int v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public int getNextAsInt(int v, int k) {
        return getNextChunk(v).getSibling(v, k);
    }

    @Override
    public int getNextArrowAsInt(int v, int k) {
        return getNextChunk(v).getArrow(v, k);
    }

    @Override
    public int getPrevArrowAsInt(int v, int k) {
        return getPrevChunk(v).getArrow(v, k);
    }

    @Override
    public int getVertexAsInt(int v) {
        return getNextChunk(v).getVertexData(v);
    }

    @Override
    public int getNextCount(int v) {
        return getNextChunk(v).getSiblingCount(v);
    }

    private Chunk getNextChunk(int vidx) {
        return getOrCreateChunk(nextChunks, vidx);
    }

    private Chunk getPrevChunk(int vidx) {
        return getOrCreateChunk(prevChunks, vidx);
    }

    private Chunk getOrCreateChunk(Chunk[] chunks, int v) {
        @NonNull Chunk chunk = chunks[v >>> chunkShift];
        if (chunk == null) {
            chunk = new Chunk(initialChunkCapacity);
            chunks[v >>> chunkShift] = chunk;
        }
        return chunk;
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public int getPrevAsInt(int v, int k) {
        return getPrevChunk(v).getSibling(v, k);
    }

    @Override
    public int getPrevCount(int v) {
        return getPrevChunk(v).getSiblingCount(v);
    }

    private void grow(int capacity) {
        int chunkedCapacity = (capacity + chunkSize - 1) >>> chunkShift;
        Chunk[] temp = (Chunk[]) ListHelper.grow(vertexCount, chunkedCapacity, 1, nextChunks);
        if (temp.length < chunkedCapacity) {
            throw new IllegalStateException("too much capacity requested:" + capacity);
        }
        nextChunks = temp;
        prevChunks = (Chunk[]) ListHelper.grow(vertexCount, chunkedCapacity, 1, prevChunks);
    }

}
