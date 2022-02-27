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
    private static final boolean CLEAR_THE_GAP = true;
    /**
     * Number of vertices per chunk.
     * Must be a power of 2.
     */
    private final int chunkShift = 2;
    /**
     * Number of vertices per chunk.
     * Must be a power of 2.
     */
    private final int chunkSize = 1 << chunkShift;
    private final int chunkMask = chunkSize - 1;

    /**
     * Array of chunks for arrows to next vertices.
     */
    private @NonNull int[][] nextChunks = new int[0][0];
    /**
     * Array of chunks for arrows to previous vertices.
     */
    private @NonNull int[][] prevChunks = new int[0][0];

    /**
     * Creates a chunk with the specified initial capacity for each vertex
     * and the specified free space.
     * Chunk layout:
     * <pre>
     * header: [  free, vgap ]
     *                        // free=number of unused arrows
     *                        // vgap=index of the vertex that contains the unused arrows
     *
     * vertexData: [ data ... ] // 1 element for each vertex in the chunk
     *
     * sizes: [ cumulatedSize, ..., totalSize ]
     *                          //1 element for each vertex in the chunk
     *                          //+ 1 element for the total size used
     *
     * indices: [ index, ... ] // 1 element for each arrow
     *                         // the arrows are sorted by index, so that binary
     *                         // search can be used
     *
     * arrowData: [ data, ... ] // 1 elements for each arrow
     *                         // starts at sentinel
     *
     * Functions:
     * int freeOffset() = 0;
     * int sizesOffset() = 2+chunkSize;
     * int totalSizeOffset() = 3+chunkSize*2;
     * int indicesOffset() = 3+chunkSize*2;
     * int totalSize() =chunk[totalSizeOffset()];
     * int arrowDataOffset() = 3+chunkSize*2+1+totalSize();
     * int size(int vidx) = chunk[sizesOffset()+vidx+1] - chunk[sizesOffset()+vidx];
     * int capacity() = chunk[totalSizeOffset()] + chunk[freeOffset()];
     * int indicesFromInclusiveOffset(int vidx) = indicesOffset()+chunk[sizesOffset()+vidx]+(vidx&lt;vgap?0:free);
     * int indicesToExclusiveOffset(int vidx) = indicesOffset()+size(vidx)+(vidx&lt;free?0:free);
     * int arrowDataFromInclusiveOffset(int vidx) = indicesOffset()+chunk[sizesOffset()+vidx]+(vidx&lt;vgap?0:free);
     * int arrowDataToExclusiveOffset(int vidx) = indicesOffset()+size(vidx)+(vidx&lt;free?0:free);
     * int vertexDataOffset(int vidx) = 2+vidx;
     *
     * vertexData: [ data ... ] // 1 element for each vertex in the chunk
     * capacities: [ size ... ] // 1 element for each vertex in the chunk
     * sizes: [ size ...] // 1 element for each vertex in the chunk
     * </pre>
     * When arrows are inserted, capacity for the indices is taken from other vertices
     * in the chunk until the chunk runs out of free space.
     *
     * @param free the initial free space for arrows
     * @return a new chunk
     */
    private int[] createChunk(int free) {
        int length = chunkSize * 2 // space needed for vertex data, sizes
                + 3//space needed for free, vgap and totalSize
                + free * 2; // space needed for indices and arrow data of each arrow
        int[] chunk = new int[length];
        chunk[chunkFreeOffset()] = free;
        return chunk;
    }

    private int chunkFreeOffset() {
        return 0;
    }

    private int chunkGapOffset() {
        return 1;
    }

    private int chunkGetSizesOffset() {
        return 2 + chunkSize;
    }

    private int chunkGetTotalSizeOffset() {
        return 2 + chunkSize * 2;
    }

    private int chunkGetIndicesOffset() {
        return 3 + chunkSize * 2;
    }

    private int chunkGetTotalSize(int[] chunk) {
        return chunk[chunkGetTotalSizeOffset()];
    }

    private int chunkGetArrowDataOffset(int[] chunk) {
        return 3 + chunkSize * 2 + chunkGetTotalSize(chunk) + chunkGetFree(chunk);
    }

    private int chunkGetSize(int[] chunk, int vidx) {
        int localVidx = vidx & chunkMask;
        return chunk[chunkGetSizesOffset() + localVidx + 1] - chunk[chunkGetSizesOffset() + localVidx];
    }

    private int chunkGetCapacity(int[] chunk) {
        return chunk[chunkGetTotalSizeOffset()] + chunk[chunkFreeOffset()];
    }

    private int chunkGetFree(int[] chunk) {
        return chunk[chunkFreeOffset()];
    }

    private void chunkSetFree(int[] chunk, int free) {
        chunk[chunkFreeOffset()] = free;
    }

    private int chunkGetGap(int[] chunk) {
        return chunk[chunkGapOffset()];
    }

    private void chunkSetGap(int[] chunk, int gap) {
        chunk[chunkGapOffset()] = gap & chunkMask;
    }

    private int chunkGetIndicesFromInclusiveOffset(int[] chunk, int vidx) {
        int vlocal = vidx & chunkMask;
        return chunkGetIndicesOffset() + chunk[chunkGetSizesOffset() + vlocal] + (vlocal <= chunkGetGap(chunk) ? 0 : chunkGetFree(chunk));
    }

    private int chunkGetIndicesToExclusiveOffset(int[] chunk, int vidx) {
        int vlocal = vidx & chunkMask;
        return chunkGetIndicesOffset() + chunk[chunkGetSizesOffset() + vlocal + 1] + (vlocal <= chunkGetGap(chunk) ? 0 : chunkGetFree(chunk));
    }

    private int chunkGetArrowDataFromInclusiveOffset(int[] chunk, int vidx) {
        int vlocal = vidx & chunkMask;
        return chunkGetArrowDataOffset(chunk) + chunk[chunkGetSizesOffset() + vlocal] + (vlocal <= chunkGetGap(chunk) ? 0 : chunkGetFree(chunk));
    }

    private int chunkGetArrowDataToExclusiveOffset(int[] chunk, int vidx) {
        int vlocal = vidx & chunkMask;
        return chunkGetArrowDataOffset(chunk) + chunkGetSize(chunk, vlocal) + (vlocal <= chunkGetGap(chunk) ? 0 : chunkGetFree(chunk));
    }

    private int chunkVertexDataOffset(int[] chunk, int vidx) {
        return 2 + vidx;
    }

    int vertexCount = 0;
    int arrowCount = 0;

    @Override
    public void addArrow(int vidx, int uidx) {
        addArrow(vidx, uidx, 0);
    }

    @Override
    public void addArrow(int vidx, int uidx, int data) {
        int[] vChunk = getNextChunk(vidx);
        int[] uChunk = getPrevChunk(uidx);
        if (chunkTryToAddIndex(vChunk, vidx, uidx, data)) {
            chunkTryToAddIndex(uChunk, uidx, vidx, data);
            arrowCount++;
        }
    }

    /**
     * Adds the index of vertex u to the index list of vertex v
     * if it is not already present.
     *
     * @param chunk a chunk
     * @param vidx  index of vertex v
     * @param uidx  index of vertex u
     * @param data  the arrow data
     * @return true on success
     */
    private boolean chunkTryToAddIndex(int[] chunk, int vidx, int uidx, int data) {
        int result = chunkFindIndexOf(chunk, vidx, uidx);
        if (result >= 0) {
            return false;
        }
        int free = chunkGetFree(chunk);
        if (free < 1) {
            throw new UnsupportedOperationException("implement me");
        }
        int indicesFrom = chunkGetIndicesFromInclusiveOffset(chunk, vidx);
        int indicesTo = chunkGetIndicesToExclusiveOffset(chunk, vidx);
        int arrowDataFrom = chunkGetArrowDataFromInclusiveOffset(chunk, vidx);
        int arrowDataTo = chunkGetArrowDataToExclusiveOffset(chunk, vidx);
        int insertionIndex = ~result;
        int localVidx = vidx & chunkMask;
        int gap = chunkGetGap(chunk);
        if (gap < localVidx) {
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
            int indicesStartOfGapOffset = chunkGetIndicesToExclusiveOffset(chunk, gap);
            int arrowDataStartOfGapOffset = chunkGetArrowDataToExclusiveOffset(chunk, gap);
            int length = indicesFrom + insertionIndex - indicesStartOfGapOffset;
            if (arrowDataStartOfGapOffset + free < chunk.length) {
                System.arraycopy(chunk, indicesStartOfGapOffset + free, chunk, indicesStartOfGapOffset, length);
                System.arraycopy(chunk, arrowDataStartOfGapOffset + free, chunk, arrowDataStartOfGapOffset, length);
            }

            // insert the element at insertion index
            chunk[indicesFrom + insertionIndex - free] = uidx;
            chunk[arrowDataFrom + insertionIndex - free] = data;

            // reopen the gap by shifting the remainder of the indices down
            System.arraycopy(chunk, indicesFrom + insertionIndex, chunk, indicesFrom + insertionIndex - free + 1, indicesTo - indicesFrom - insertionIndex);
            System.arraycopy(chunk, arrowDataFrom + insertionIndex, chunk, arrowDataFrom + insertionIndex - free + 1, indicesTo - indicesFrom - insertionIndex);

            if (CLEAR_THE_GAP && (arrowDataStartOfGapOffset + free < chunk.length)) {
                Arrays.fill(chunk, indicesTo - free + 1, indicesTo + free - 1, free - 1);
                Arrays.fill(chunk, arrowDataTo - free + 1, arrowDataTo + free - 1, free - 1);
            }

        } else if (gap > localVidx) {
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
            int indicesStartOfGapOffset = chunkGetIndicesToExclusiveOffset(chunk, gap);
            int arrowDataStartOfGapOffset = chunkGetArrowDataToExclusiveOffset(chunk, gap);
            System.arraycopy(chunk, indicesTo, chunk, indicesTo + free, indicesStartOfGapOffset - indicesTo);
            System.arraycopy(chunk, arrowDataTo, chunk, arrowDataTo + free, indicesStartOfGapOffset - indicesTo);

            // shift up to make room for the new element
            System.arraycopy(chunk, indicesTo + insertionIndex, chunk, indicesTo + insertionIndex + 1, indicesFrom - indicesTo - insertionIndex);
            System.arraycopy(chunk, arrowDataTo + insertionIndex, chunk, arrowDataTo + insertionIndex + 1, indicesFrom - indicesTo - insertionIndex);

            // insert the element at insertion index
            chunk[indicesFrom + insertionIndex] = uidx;
            chunk[arrowDataFrom + insertionIndex] = data;


        } else {
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
            chunk[indicesFrom + insertionIndex] = uidx;
            chunk[arrowDataFrom + insertionIndex] = data;
        }


        chunkSetGap(chunk, vidx);
        chunkSetFree(chunk, free - 1);

        for (int i = chunkGetSizesOffset() + localVidx + 1, n = chunkGetTotalSizeOffset(); i <= n; i++) {
            chunk[i]++;
        }

        return true;
    }


    @Override
    public int findIndexOfPrevAsInt(int vidx, int uidx) {
        int[] chunk = getPrevChunk(vidx);
        return chunkFindIndexOf(chunk, vidx, uidx);
    }

    @Override
    public int findIndexOfNextAsInt(int vidx, int uidx) {
        int[] chunk = getNextChunk(vidx);
        return chunkFindIndexOf(chunk, vidx, uidx);
    }

    /**
     * Finds the index of vertex u in the index list of vertex v.
     *
     * @param chunk a chunk
     * @param vidx  index of vertex v
     * @param uidx  index of vertex u
     * @return the index of u or (-index -1) if u is not in the index list.
     */
    private int chunkFindIndexOf(int[] chunk, int vidx, int uidx) {
        int from = chunkGetIndicesFromInclusiveOffset(chunk, vidx);
        int to = chunkGetIndicesToExclusiveOffset(chunk, vidx);
        int result = Arrays.binarySearch(chunk, from, to, uidx);
        return result < 0 ? result + from : result - from;
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
    public void removeAllNext(int vidx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeNext(int vidx, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeVertex(int vidx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public int getNextAsInt(int vidx, int k) {
        int[] chunk = getNextChunk(vidx);
        int size = chunkGetSize(chunk, vidx);
        Preconditions.checkIndex(k, size);
        return chunk[chunkGetIndicesFromInclusiveOffset(chunk, vidx) + k];
    }

    @Override
    public int getNextArrowAsInt(int vidx, int index) {
        int[] chunk = getNextChunk(vidx);
        int size = chunkGetSize(chunk, vidx);
        Preconditions.checkIndex(index, size);
        return chunk[chunkGetArrowDataFromInclusiveOffset(chunk, vidx) + index];
    }

    @Override
    public int getPrevArrowAsInt(int vidx, int index) {
        int[] chunk = getPrevChunk(vidx);
        int size = chunkGetSize(chunk, vidx);
        Preconditions.checkIndex(index, size);
        return chunk[chunkGetArrowDataFromInclusiveOffset(chunk, vidx) + index];
    }

    @Override
    public int getVertexAsInt(int vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNextCount(int vidx) {
        int[] chunk = getNextChunk(vidx);
        return chunkGetSize(chunk, vidx);
    }

    private int[] getNextChunk(int vidx) {
        return getOrCreateChunk(nextChunks, vidx);
    }

    private int[] getPrevChunk(int vidx) {
        return getOrCreateChunk(prevChunks, vidx);
    }

    private int[] getOrCreateChunk(int[][] chunks, int vidx) {
        @NonNull int[] chunk = chunks[vidx >>> chunkShift];
        if (chunk == null) {
            chunk = createChunk(chunkSize * 2);
            chunks[vidx >>> chunkShift] = chunk;
        }
        return chunk;
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public int getPrevAsInt(int vidx, int i) {
        int[] chunk = getPrevChunk(vidx);
        int size = chunkGetSize(chunk, vidx);
        Preconditions.checkIndex(i, size);
        return chunk[chunkGetIndicesFromInclusiveOffset(chunk, vidx) + i];
    }

    @Override
    public int getPrevCount(int vidx) {
        int[] chunk = getPrevChunk(vidx);
        return chunkGetSize(chunk, vidx);
    }

    private void grow(int capacity) {
        int chunkedCapacity = (capacity + chunkSize - 1) >>> chunkShift;
        int[][] temp = (int[][]) ListHelper.grow(vertexCount, chunkedCapacity, 1, nextChunks);
        if (temp.length < chunkedCapacity) {
            throw new IllegalStateException("too much capacity requested:" + capacity);
        }
        nextChunks = temp;
        prevChunks = (int[][]) ListHelper.grow(vertexCount, chunkedCapacity, 1, prevChunks);
    }

}
