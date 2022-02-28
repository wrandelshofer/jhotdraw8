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
                Arrays.fill(chunk, getIndicesOffset(), chunk.length, -1);
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
            int from = getSiblingsFromInclusiveOffset(v);
            int to = getSiblingsToExclusiveOffset(v);
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
            return chunk[getArrowsFromInclusiveOffset(v) + k];
        }

        private int getSiblingsFromInclusiveOffset(int v) {
            int vIndex = v & chunkMask;
            int indicesOffset = getIndicesOffset();
            return vIndex == 0
                    ? indicesOffset
                    : indicesOffset + chunk[getSizesOffset() + vIndex - 1]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        private int getSiblingsToExclusiveOffset(int v) {
            int vIndex = v & chunkMask;
            return getIndicesOffset()
                    + chunk[getSizesOffset() + vIndex]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        private int getIndicesOffset() {
            return chunkSize * 2;
        }

        public int getSibling(int v, int k) {
            Preconditions.checkIndex(k, getArrowCount(v));
            return chunk[getSiblingsFromInclusiveOffset(v) + k];
        }

        private int getSizesOffset() {
            return chunkSize;
        }

        private int getArrowsOffset() {
            return chunkSize * 2 + capacity;
        }

        private int getArrowCount(int v) {
            int vIndex = v & chunkMask;
            int sizesOffset = getSizesOffset();
            return vIndex == 0
                    ? chunk[sizesOffset + vIndex]
                    : chunk[sizesOffset + vIndex] - chunk[sizesOffset + vIndex - 1];
        }

        private int getArrowsFromInclusiveOffset(int v) {
            int vIndex = v & chunkMask;
            int arrowsOffset = getArrowsOffset();
            return vIndex == 0
                    ? arrowsOffset
                    : arrowsOffset + chunk[getSizesOffset() + vIndex - 1]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        private int getArrowsToExclusiveOffset(int v) {
            int vIndex = v & chunkMask;
            return getArrowsOffset()
                    + chunk[getSizesOffset() + vIndex]
                    + (vIndex <= gapIndex ? 0 : gapSize);
        }

        /**
         * Adds an arrow from vertex u to vertex v, if it is not already present.
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
                throw new UnsupportedOperationException("implement growing of chunk");
            }
            int siblingsFrom = getSiblingsFromInclusiveOffset(v);
            int siblingsTo = getSiblingsToExclusiveOffset(v);
            int arrowDataFrom = getArrowsFromInclusiveOffset(v);
            int arrowDataTo = getArrowsToExclusiveOffset(v);
            int insertionIndex = ~result;
            int vIndex = v & chunkMask;

            if (gapIndex < vIndex) {
                insertBeforeGap(u, data, siblingsFrom, siblingsTo, arrowDataFrom, arrowDataTo, insertionIndex);
            } else if (gapIndex > vIndex) {
                insertAfterGap(u, data, siblingsFrom, siblingsTo, arrowDataFrom, arrowDataTo, insertionIndex);
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

        private void insertAtGap(int u, int data, int indicesFrom, int indicesTo, int arrowDataFrom, int arrowDataTo, int insertionIndex) {
            // BEFORE:
            //                        insertionIndex
            // indices = [........::::÷::gap;;;;;,,,,];
            //                    ^      ^
            //                    from   to
            // AFTER:
            //                            inserted element before insertionIndex
            // indices = [........::::i÷::gp;;;;;,,,,];
            //                    ^      ^
            //                    from   to

            // shift up to make room for the new element
            System.arraycopy(chunk, indicesTo + insertionIndex, chunk, indicesTo + insertionIndex + 1, indicesFrom - indicesTo - insertionIndex);
            System.arraycopy(chunk, arrowDataTo + insertionIndex, chunk, arrowDataTo + insertionIndex + 1, indicesFrom - indicesTo - insertionIndex);

            // insert the element at insertion index
            chunk[indicesFrom + insertionIndex] = u;
            chunk[arrowDataFrom + insertionIndex] = data;
        }

        private void insertAfterGap(int u, int data, int indicesFrom, int indicesTo, int arrowDataFrom, int arrowDataTo, int insertionIndex) {
            // BEFORE:
            //                        insertionIndex
            // indices = [........::::÷::;;;;;gap,,,,];
            //                    ^      ^
            //                    from   to
            // AFTER:
            //                         inserted element before insertionIndex
            // indices = [........::::i÷::gp;;;;;,,,,];
            //                    ^       ^
            //                    from    to

            // close the gap by shifting up
            int indicesStartOfGapOffset = getSiblingsToExclusiveOffset(gapIndex);
            System.arraycopy(chunk, indicesTo, chunk, indicesTo + free, indicesStartOfGapOffset - indicesTo);
            System.arraycopy(chunk, arrowDataTo, chunk, arrowDataTo + free, indicesStartOfGapOffset - indicesTo);

            // shift up to make room for the new element
            System.arraycopy(chunk, indicesTo + insertionIndex, chunk, indicesTo + insertionIndex + 1, indicesFrom - indicesTo - insertionIndex);
            System.arraycopy(chunk, arrowDataTo + insertionIndex, chunk, arrowDataTo + insertionIndex + 1, indicesFrom - indicesTo - insertionIndex);

            // insert the element at insertion index
            chunk[indicesFrom + insertionIndex] = u;
            chunk[arrowDataFrom + insertionIndex] = data;
        }

        private void insertBeforeGap(int u, int data, int indicesFrom, int indicesTo, int arrowDataFrom, int arrowDataTo, int insertionIndex) {
            // BEFORE:
            //                               insertionIndex
            // indices = [........gap,,,,::::÷::;;;;;];
            //                           ^      ^
            //                          from   to
            // AFTER:
            //                            inserted element before insertionIndex
            // indices = [........,,,,::::i÷::gp;;;;;];
            //                        ^       ^
            //                       from    to

            // close the gap by shifting down
            int indicesStartOfGapOffset = getSiblingsToExclusiveOffset(gapIndex);
            int arrowDataStartOfGapOffset = getArrowsToExclusiveOffset(gapIndex);
            int length = indicesFrom + insertionIndex - indicesStartOfGapOffset;
            if (arrowDataStartOfGapOffset + free < chunk.length) {
                System.arraycopy(chunk, indicesStartOfGapOffset + free, chunk, indicesStartOfGapOffset, length);
                System.arraycopy(chunk, arrowDataStartOfGapOffset + free, chunk, arrowDataStartOfGapOffset, length);
            }

            // insert the element at insertion index
            chunk[indicesFrom + insertionIndex - free] = u;
            chunk[arrowDataFrom + insertionIndex - free] = data;

            // reopen the gap by shifting the remainder of the indices down
            System.arraycopy(chunk, indicesFrom + insertionIndex, chunk, indicesFrom + insertionIndex - free + 1, indicesTo - indicesFrom - insertionIndex);
            System.arraycopy(chunk, arrowDataFrom + insertionIndex, chunk, arrowDataFrom + insertionIndex - free + 1, indicesTo - indicesFrom - insertionIndex);

            if (CLEAR_UNUSED_ELEMENTS && (arrowDataStartOfGapOffset + free < chunk.length)) {
                Arrays.fill(chunk, indicesTo - free + 1, indicesTo + free - 1, -1);
                Arrays.fill(chunk, arrowDataTo - free + 1, arrowDataTo + free - 1, -1);
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
        throw new UnsupportedOperationException();
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
