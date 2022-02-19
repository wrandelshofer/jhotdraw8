/*
 * @(#)MutableIndexedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IntEnumeratorSpliterator;
import org.jhotdraw8.collection.IntIntArrayEnumeratorSpliterator;
import org.jhotdraw8.collection.ListHelper;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

import static java.lang.Math.max;

/**
 * A mutable indexed bi-directional graph.
 */
public class SimpleMutableIndexedBidiGraph implements MutableIndexedBidiGraph {
    private final int maxArity;
    private final int stride;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <pre>
     * [ arrowCount, vertexIndex... ]
     * </pre>
     */
    private int[] prevArrows;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <pre>
     * [ arrowCount, vertexIndex... ]
     * </pre>
     */
    private int[] nextArrows;
    private int vertexCount;
    private int arrowCount;

    /**
     * Creates a new instance.
     *
     * @param vertexCapacity the initial vertex capacity
     * @param maxArity       the maximal number of arrows per vertex
     */
    public SimpleMutableIndexedBidiGraph(int vertexCapacity, int maxArity) {
        if (vertexCapacity < 0) {
            throw new IllegalArgumentException("vertexCount=" + vertexCapacity);
        }
        if (maxArity < 0) {
            throw new IllegalArgumentException("maxArity=" + maxArity);
        }
        this.vertexCount = 0;
        this.maxArity = maxArity;
        this.stride = 1 + maxArity;
        this.nextArrows = new int[vertexCapacity * stride];
        this.prevArrows = new int[vertexCapacity * stride];
    }

    @Override
    public void addArrow(int vidx, int uidx) {
        int vOffset = vidx * stride;
        int vNextCount = nextArrows[vOffset] + 1;
        int uOffset = uidx * stride;
        int uPrevCount = prevArrows[uOffset] + 1;
        if (vNextCount >= maxArity || uPrevCount >= maxArity) {
            throw new IndexOutOfBoundsException("Not enough capacity for a new arrow " + vidx + "->" + uidx);
        }
        nextArrows[vOffset + vNextCount] = uidx;
        nextArrows[vOffset] = vNextCount;
        prevArrows[uOffset + uPrevCount] = vidx;
        prevArrows[uOffset] = uPrevCount;
        arrowCount++;
    }

    /**
     * Adds {@code addend} to every vertex index that is greater or
     * equal {@code vidx}.
     *
     * @param src    the source arrow array
     * @param vsrc   the vertex index in the source
     * @param dest   the destination source arrow
     * @param vdst   the vertex index in the destination
     * @param length the number of vertices to change
     * @param vidx   the vertex index
     * @param addend the number to add
     */
    private void addToVectorIndices(int[] src, int vsrc, int[] dest, int vdst, int length, int vidx, int addend) {
        int srcOffset = vsrc * stride;
        int dstOffset = vdst * stride;
        for (int v = 0; v < length; v++) {
            int nDest = dest[dstOffset];
            int nSrc = src[srcOffset];
            dest[dstOffset] = nSrc;
            for (int i = 1; i <= nSrc; i++) {
                int uidx = src[srcOffset + i];
                if (uidx >= vidx) {
                    dest[dstOffset + i] = uidx + addend;
                }
            }
            if (nDest > nSrc) {
                for (int i = nSrc + 1; i <= nDest; i++) {
                    dest[dstOffset + i] = 0;
                }
            }
            srcOffset += stride;
            dstOffset += stride;
        }
    }

    @Override
    public void addVertex() {
        grow(vertexCount + 1);
        vertexCount += 1;
    }

    @Override
    public void addVertex(int vidx) {
        grow(max(vertexCount + 1, vidx));
        int newVertexCount = max(vidx, vertexCount + 1);
        if (vidx < vertexCount) {
            addToVectorIndices(nextArrows, 0, nextArrows, 0, vidx, vidx, 1);
            addToVectorIndices(nextArrows, vidx, nextArrows, vidx + 1, vertexCount - vidx, vidx, 1);
            addToVectorIndices(prevArrows, 0, prevArrows, 0, vidx, vidx, 1);
            addToVectorIndices(prevArrows, vidx, prevArrows, vidx + 1, vertexCount - vidx, vidx, 1);
        }
        int vOffset = vidx * stride;
        Arrays.fill(nextArrows, vOffset, vOffset + stride, 0);
        Arrays.fill(prevArrows, vOffset, vOffset + stride, 0);
        vertexCount = newVertexCount;
    }

    @Override
    public int findIndexOfNext(int vidxa, int vidxb) {
        int offset = vidxa * stride;
        for (int i = offset + 1, n = nextArrows[offset]; i < n; i++) {
            if (nextArrows[i] == vidxb) {
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
        for (int i = offset + 1, n = prevArrows[offset]; i < n; i++) {
            if (prevArrows[i] == vidxb) {
                return i - offset;
            }
        }
        return -1;
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public int getNext(int vidx, int i) {
        int offset = vidx * stride;
        Objects.checkIndex(i, nextArrows[offset]);
        return nextArrows[offset + i + 1];
    }

    @Override
    public int getNextCount(int vidx) {
        return nextArrows[vidx * stride];
    }

    @Override
    public int getPrev(int vidx, int i) {
        int offset = vidx * stride;
        Objects.checkIndex(i, prevArrows[offset]);
        return prevArrows[offset + i + 1];
    }


    @Override
    public int getPrevCount(int vidx) {
        return prevArrows[vidx * stride];
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }


    private void grow(int capacity) {
        int[] temp = ListHelper.grow(vertexCount, capacity, stride, nextArrows);
        if (temp.length < capacity * stride) {
            throw new IllegalStateException("too much capacity requested:" + capacity);
        }
        nextArrows = temp;
        prevArrows = ListHelper.grow(vertexCount, capacity, stride, prevArrows);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator nextVerticesSpliterator(int vidx) {
        int offset = vidx * stride;
        return new IntIntArrayEnumeratorSpliterator(offset + 1, offset + 1 + nextArrows[offset], nextArrows);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator prevVerticesSpliterator(int vidx) {
        int offset = vidx * stride;
        return new IntIntArrayEnumeratorSpliterator(offset + 1, offset + 1 + prevArrows[offset], prevArrows);
    }

    @Override
    public void removeAllPrev(int vidx) {
        Objects.checkIndex(vidx, vertexCount);
        int vOffset = vidx * stride;
        int vPrevCount = prevArrows[vOffset];
        for (int i = vPrevCount; i >= 0; i--) {
            int uidx = prevArrows[vOffset + i];
            int uOffset = uidx * stride;
            int uNextCount = nextArrows[uOffset];
            int vIndex = findIndexOfNext(uidx, vidx);
            if (vIndex < uNextCount - 1) {
                System.arraycopy(nextArrows, uOffset + vIndex + 2, nextArrows, uOffset + vIndex + 1, uNextCount - vIndex - 1);
            }
            nextArrows[uOffset + uNextCount] = 0;
            nextArrows[uOffset] = uNextCount - 1;
            prevArrows[vOffset + i + 1] = 0;
        }
        prevArrows[vOffset] = 0;
    }

    @Override
    public void removeAllNext(int vidx) {
        Objects.checkIndex(vidx, vertexCount);
        int vOffset = vidx * stride;
        int vNextCount = nextArrows[vOffset];
        for (int i = vNextCount; i >= 0; i--) {
            int uidx = nextArrows[vOffset + i];
            int uOffset = uidx * stride;
            int uPrevCount = prevArrows[uOffset];
            int vIndex = findIndexOfPrev(uidx, vidx);
            if (vIndex < uPrevCount - 1) {
                System.arraycopy(prevArrows, uOffset + vIndex + 2, prevArrows, uOffset + vIndex + 1, uPrevCount - vIndex - 1);
            }
            prevArrows[uOffset + uPrevCount] = 0;
            prevArrows[uOffset] = uPrevCount - 1;
            nextArrows[vOffset + i + 1] = 0;
        }
        nextArrows[vOffset] = 0;
    }

    @Override
    public void removeNext(int vidx, int i) {
        int uidx = getNext(vidx, i);
        int vOffset = vidx * stride;
        int vNextCount = nextArrows[vOffset];
        int uOffset = uidx * stride;
        int uPrevCount = prevArrows[uOffset];
        int vIndex = findIndexOfPrev(uidx, vidx);
        if (vIndex == -1 || i == -1) {
            throw new NoSuchElementException("There is no arrow " + vidx + "->" + uidx);
        }
        if (i < vNextCount - 1) {
            System.arraycopy(nextArrows, vOffset + i + 2, nextArrows, vOffset + i + 1, vNextCount - i - 1);
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
    public void removeVertex(int vidx) {
        Objects.checkIndex(vidx, vertexCount);
        removeAllNext(vidx);
        removeAllPrev(vidx);
        if (vidx < vertexCount - 1) {
            addToVectorIndices(nextArrows, 0, nextArrows, 0, vidx, vidx, -1);
            addToVectorIndices(nextArrows, vidx, nextArrows, vidx + 1, vertexCount - vidx, vidx, -1);
            addToVectorIndices(prevArrows, 0, prevArrows, 0, vidx, vidx, -1);
            addToVectorIndices(prevArrows, vidx, prevArrows, vidx + 1, vertexCount - vidx, vidx, -1);
        }
        int vOffset = (vertexCount - 1) * stride;
        Arrays.fill(nextArrows, vOffset, vOffset + stride, 0);
        Arrays.fill(prevArrows, vOffset, vOffset + stride, 0);
        vertexCount--;
    }
}
