/*
 * @(#)MutableIndexedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A mutable indexed bi-directional graph.
 * <p>
 * Only arrows can be added or removed.
 * The number of vertices and the maximal arity of the graph can not be changed.
 * <p>
 * The arrow data is a long of which only
 * {@code 64-(32-Integer.numberOfLeadingZeros(vertexCount))} lower bits are
 * preserved.
 * <p>
 * For example, if {@code vertexCount=999} then only 54 bits of the arrow
 * data are preserved.
 */
public class MutableIndexedBidiGraph implements IndexedBidiGraph {
    /**
     * The array contains {@code stride} elements for each vertex.
     * <p>
     * The first element is the number of previous vertices. The following
     * elements are indices of the previous vertices.
     * <pre>
     *     [ count, prev_(0), ..., prev_(maxArity - 1) ]
     * </pre>
     */
    private long[] prevArrows;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <p>
     * The first element is the number of next vertices. The following
     * elements are indices of the next vertices.
     * <pre>
     *     [ count, next_(0), ..., next_(maxArity - 1) ]
     * </pre>
     */
    private long[] nextArrows;

    private final int vertexCount;
    private int arrowCount;
    private final int maxArity;
    private final int stride;

    private final long indexMask;
    private final long dataMask;
    private final int dataShift;

    public MutableIndexedBidiGraph(int vertexCount, int maxArity, int arrowDataBitCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("vertexCount=" + vertexCount);
        }
        if (maxArity < 0) {
            throw new IllegalArgumentException("maxArity=" + maxArity);
        }

        this.dataShift = (32 - Integer.numberOfLeadingZeros(vertexCount));
        if (64 - dataShift < arrowDataBitCount) {
            throw new IllegalArgumentException("arrowDataBitCount=" + arrowDataBitCount + " is larger than " + (64 - dataShift));
        }
        this.dataMask = (1L << arrowDataBitCount) - 1;
        this.indexMask = 1L ^ dataMask;


        this.vertexCount = vertexCount;
        this.maxArity = maxArity;
        this.stride = maxArity + 1;
        this.nextArrows = new long[vertexCount * stride];
        this.prevArrows = new long[vertexCount * stride];

    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    public void addArrow(int vidx, int uidx) {
        addArrow(vidx, uidx, 0L);
    }

    public void addArrow(int vidx, int uidx, long data) {
        int vOffset = vidx * stride;
        int vNextCount = (int) nextArrows[vOffset];
        int uOffset = uidx * stride;
        int uPrevCount = (int) prevArrows[uOffset];
        if (vNextCount >= maxArity || uPrevCount >= maxArity) {
            throw new IndexOutOfBoundsException("Not enough capacity for a new arrow " + vidx + "->" + uidx);
        }
        nextArrows[vOffset + vNextCount] = uidx | ((data & dataMask) << dataShift);
        nextArrows[vOffset] = vNextCount + 1;
        prevArrows[uOffset + uPrevCount] = vidx | ((data & dataMask) << dataShift);
        prevArrows[uOffset] = uPrevCount + 1;
        arrowCount++;
    }

    public void removeArrow(int vidx, int uidx) {
        int vOffset = vidx * stride;
        int vNextCount = (int) nextArrows[vOffset];
        int uOffset = uidx * stride;
        int uPrevCount = (int) prevArrows[uOffset];
        int vIndex = findIndexOfPrev(uidx, vidx);
        int uIndex = findIndexOfNext(vidx, uidx);
        if (vIndex == -1 || uIndex == -1) {
            throw new NoSuchElementException("There is no arrow " + vidx + "->" + uidx);
        }
        if (uIndex < vNextCount - 1) {
            System.arraycopy(nextArrows, vOffset + uIndex + 2, nextArrows, vOffset + uIndex + 1, vNextCount - uIndex - 1);
        }
        if (vIndex < uPrevCount - 1) {
            System.arraycopy(prevArrows, uOffset + vIndex + 2, prevArrows, uOffset + vIndex + 1, uPrevCount - vIndex - 1);
        }
        nextArrows[vOffset + vNextCount] = 0;
        prevArrows[uOffset + uPrevCount] = 0;
        nextArrows[vOffset] = vNextCount - 1;
        prevArrows[uOffset] = uPrevCount - 1;
        arrowCount--;
    }

    public void removeArrow(int vidx, int uidx, long data) {
        int vOffset = vidx * stride;
        int vNextCount = (int) nextArrows[vOffset];
        int uOffset = uidx * stride;
        int uPrevCount = (int) prevArrows[uOffset];
        int vIndex = findIndexOfPrev(uidx, vidx, data);
        int uIndex = findIndexOfNext(vidx, uidx, data);
        if (vIndex == -1 || uIndex == -1) {
            throw new NoSuchElementException("There is no arrow " + vidx + "->" + uidx);
        }
        if (uIndex < vNextCount - 1) {
            System.arraycopy(nextArrows, vOffset + uIndex + 2, nextArrows, vOffset + uIndex + 1, vNextCount - uIndex - 1);
        }
        if (vIndex < uPrevCount - 1) {
            System.arraycopy(prevArrows, uOffset + vIndex + 2, prevArrows, uOffset + vIndex + 1, uPrevCount - vIndex - 1);
        }
        nextArrows[vOffset + vNextCount] = 0;
        prevArrows[uOffset + uPrevCount] = 0;
        nextArrows[vOffset] = vNextCount - 1;
        prevArrows[uOffset] = uPrevCount - 1;
        arrowCount--;
    }

    @Override
    public int findIndexOfNext(int vidxa, int vidxb) {
        int offset = vidxa * stride;
        for (int i = offset + 1, n = (int) nextArrows[offset]; i < n; i++) {
            if ((nextArrows[i] & indexMask) == vidxb) {
                return i - offset;
            }
        }
        return -1;
    }

    @Override
    public int findIndexOfPrev(int vidxa, int vidxb) {
        Objects.checkIndex(vidxa, vertexCount);
        Objects.checkIndex(vidxb, vertexCount);
        int offset = vidxa * stride;
        for (int i = offset + 1, n = (int) prevArrows[offset]; i < n; i++) {
            if ((prevArrows[i] & indexMask) == vidxb) {
                return i - offset;
            }
        }
        return -1;
    }

    public int findIndexOfNext(int vidxa, int vidxb, long data) {
        Objects.checkIndex(vidxa, vertexCount);
        Objects.checkIndex(vidxb, vertexCount);
        int offset = vidxa * stride;
        long bWithData = vidxb | ((data & dataMask) << dataShift);
        for (int i = offset + 1, n = (int) nextArrows[offset]; i < n; i++) {
            if (nextArrows[i] == bWithData) {
                return i - offset;
            }
        }
        return -1;
    }

    public int findIndexOfPrev(int vidxa, int vidxb, long data) {
        Objects.checkIndex(vidxa, vertexCount);
        Objects.checkIndex(vidxb, vertexCount);
        int offset = vidxa * stride;
        long bWithData = vidxb | ((data & dataMask) << dataShift);
        for (int i = offset + 1, n = (int) prevArrows[offset]; i < n; i++) {
            if (prevArrows[i] == bWithData) {
                return i - offset;
            }
        }
        return -1;
    }

    @Override
    public int getNext(int vidx, int i) {
        Objects.checkIndex(i, (int) nextArrows[vidx * stride]);
        return (int) (nextArrows[vidx * stride + i] & indexMask);
    }

    @Override
    public int getNextCount(int vidx) {
        return (int) nextArrows[vidx * stride];
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public int getPrev(int vidx, int i) {
        Objects.checkIndex(i, (int) prevArrows[vidx * stride]);
        return (int) (prevArrows[vidx * stride + i] & indexMask);
    }

    public long getPrevArrow(int vertex, int index) {
        Objects.checkIndex(index, (int) prevArrows[vertex * stride]);
        return prevArrows[vertex * stride + index] >>> dataShift;
    }

    public long getNextArrow(int vertex, int index) {
        Objects.checkIndex(index, (int) nextArrows[vertex * stride]);
        return nextArrows[vertex * stride + index] >>> dataShift;
    }

    @Override
    public int getPrevCount(int vidx) {
        return (int) prevArrows[vidx * stride];
    }
}
