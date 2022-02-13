/*
 * @(#)MutableIndexedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.AbstractIntEnumeratorSpliterator;
import org.jhotdraw8.collection.IntEnumeratorSpliterator;
import org.jhotdraw8.collection.IntRangeReadOnlySet;
import org.jhotdraw8.collection.ListHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import static java.lang.Math.max;

/**
 * A mutable indexed bi-directional graph.
 * <p>
 * Arrows can be added or removed.
 * Vertices can only be added.
 * The maximal arity of the graph can not be changed.
 * <p>
 * The arrow data is a long of which only
 * {@code 64-(32-Integer.numberOfLeadingZeros(vertexCount))} lower bits are
 * preserved.
 * <p>
 * For example, if {@code vertexCount=999} then only 54 bits of the arrow
 * data are preserved.
 */
public class MutableIndexedBidiGraph implements IndexedBidiGraph, MutableBidiGraph<Integer, Long> {
    private final int maxArity;
    private final int stride;
    private final long indexMask;
    private final long dataMask;
    private final int dataShift;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <p>
     * The first element is the number of previous vertices. The following
     * elements are indices of the previous vertices.
     * <pre>
     * [ vertexDataAndArrowCount, arrowDataAndVertexIndex), ... ]
     * </pre>
     * The elements are structs that contain two data elements:
     * {@link #structGetData(long)} and {@link #structGetIndex(long)}.
     */
    private long[] prevArrows;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <p>
     * The first element is the number of next vertices. The following
     * elements are indices of the next vertices.
     * <pre>
     * [ vertexDataAndArrowCount, arrowDataAndVertexIndex), ... ]
     * </pre>
     * The elements are structs that contain two data elements:
     * {@link #structGetData(long)} and {@link #structGetIndex(long)}.
     */
    private long[] nextArrows;
    private int vertexCount;
    private int arrowCount;

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

    public void addArrow(int vidx, int uidx) {
        addArrow(vidx, uidx, 0L);
    }

    public void addArrow(int vidx, int uidx, long data) {
        int vOffset = vidx * stride;
        int vNextCount = structGetIndex(nextArrows[vOffset]);
        int uOffset = uidx * stride;
        int uPrevCount = structGetIndex(prevArrows[uOffset]);
        if (vNextCount >= maxArity || uPrevCount >= maxArity) {
            throw new IndexOutOfBoundsException("Not enough capacity for a new arrow " + vidx + "->" + uidx);
        }
        nextArrows[vOffset + vNextCount] = uidx | ((data & dataMask) << dataShift);
        nextArrows[vOffset] = (nextArrows[vOffset] & dataMask) | vNextCount + 1;
        prevArrows[uOffset + uPrevCount] = vidx | ((data & dataMask) << dataShift);
        prevArrows[uOffset] = (prevArrows[vOffset] & dataMask) | uPrevCount + 1;
        arrowCount++;
    }

    public void addVertex() {
        grow(vertexCount + 1);
        vertexCount += 1;
    }

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
        Arrays.fill(nextArrows, vOffset, vOffset + stride, 0L);
        Arrays.fill(prevArrows, vOffset, vOffset + stride, 0L);
        vertexCount = newVertexCount;
    }

    /**
     * Adds {@code increase} to every vertex index that is greater or
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
    private void addToVectorIndices(long[] src, int vsrc, long[] dest, int vdst, int length, int vidx, int addend) {
        int srcOffset = vsrc * stride;
        int dstOffset = vdst * stride;
        for (int v = 0; v < length; v++) {
            int nDest = structGetIndex(dest[dstOffset]);
            int nSrc = structGetIndex(src[srcOffset]);
            dest[dstOffset] = nSrc;
            for (int i = 1; i <= nSrc; i++) {
                long arrow = src[srcOffset + i];
                long uidx = structGetIndex(arrow);
                if (uidx >= vidx) {
                    arrow = (arrow & dataMask) | ((uidx + addend) & indexMask);
                }
                dest[dstOffset + i] = arrow;
            }
            if (nDest > nSrc) {
                for (int i = nSrc + 1; i <= nDest; i++) {
                    dest[dstOffset + i] = 0L;
                }
            }
            srcOffset += stride;
            dstOffset += stride;
        }
    }

    @Override
    public void addVertex(@NonNull Integer integer) {
        addVertex((int) integer);
    }

    @Override
    public void removeVertex(@NonNull Integer integer) {
        removeVertex((int) integer);
    }

    @Override
    public void addArrow(@NonNull Integer integer, @NonNull Integer u, @Nullable Long aLong) {

    }

    @Override
    public void removeArrow(@NonNull Integer integer, @NonNull Integer u, @Nullable Long aLong) {

    }

    @Override
    public void removeArrow(@NonNull Integer integer, @NonNull Integer u) {

    }

    @Override
    public void removeArrowAt(@NonNull Integer integer, int k) {

    }

    @Override
    public int findIndexOfNext(int vidxa, int vidxb) {
        int offset = vidxa * stride;
        for (int i = offset + 1, n = structGetIndex(nextArrows[offset]); i < n; i++) {
            if (structGetIndex(nextArrows[i]) == vidxb) {
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
        for (int i = offset + 1, n = structGetIndex(nextArrows[offset]); i < n; i++) {
            if (nextArrows[i] == bWithData) {
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
        for (int i = offset + 1, n = structGetIndex(prevArrows[offset]); i < n; i++) {
            if (structGetIndex(prevArrows[i]) == vidxb) {
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
        for (int i = offset + 1, n = structGetIndex(prevArrows[offset]); i < n; i++) {
            if (prevArrows[i] == bWithData) {
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
    public @NonNull Collection<Long> getArrows() {
        ArrayList<Long> list = new ArrayList<>(arrowCount);
        int vOffset = 0;
        for (int v = 0; v < vertexCount; v++) {
            for (int i = 0, n = structGetIndex(nextArrows[vOffset]); i < n; i++) {
                list.add(getNextArrow(v, i));
            }
            vOffset += stride;
        }
        return list;
    }

    @Override
    public @NonNull Integer getNext(@NonNull Integer vertex, int index) {
        return getNext((int) vertex, index);
    }

    @Override
    public int getNext(int vidx, int i) {
        Objects.checkIndex(i, structGetIndex(nextArrows[vidx * stride]));
        return structGetIndex(nextArrows[vidx * stride + i]);
    }

    @Override
    public @NonNull Long getNextArrow(@NonNull Integer vertex, int index) {
        return getNextArrow((int) vertex, index);
    }

    public long getNextArrow(int vertex, int index) {
        Objects.checkIndex(index, (int) (nextArrows[vertex * stride] & indexMask));
        return structGetData(nextArrows[vertex * stride + index]);
    }

    @Override
    public int getNextCount(@NonNull Integer vertex) {
        return structGetIndex(nextArrows[vertex * stride]);
    }

    @Override
    public int getNextCount(int vidx) {
        return structGetIndex(nextArrows[vidx * stride]);
    }

    @Override
    public @NonNull Integer getPrev(@NonNull Integer vertex, int index) {
        return getPrev((int) vertex, index);
    }

    @Override
    public int getPrev(int vidx, int i) {
        int offset = vidx * stride;
        Objects.checkIndex(i, structGetIndex(prevArrows[offset]));
        return structGetIndex(prevArrows[offset + i]);
    }

    @Override
    public @NonNull Long getPrevArrow(@NonNull Integer vertex, int index) {
        return getPrevArrow((int) vertex, index);
    }

    public long getPrevArrow(int vertex, int index) {
        int offset = vertex * stride;
        Objects.checkIndex(index, (int) (prevArrows[offset] & indexMask));
        return structGetData(prevArrows[offset + index]);
    }

    public long getVertexData(int vidx) {
        return structGetData(nextArrows[vidx * stride]);
    }

    public long setVertexData(int vidx, long data) {
        return nextArrows[vidx * stride] = structSetData(nextArrows[vidx * stride], data);
    }

    private long structSetData(long struct, long data) {
        return (data << dataShift) | (struct & indexMask);
    }

    @Override
    public int getPrevCount(@NonNull Integer vertex) {
        return getPrevCount((int) vertex);
    }

    @Override
    public int getPrevCount(int vidx) {
        return structGetIndex(prevArrows[vidx * stride]);
    }

    @Override
    public Integer getVertex(int index) {
        Objects.checkIndex(index, vertexCount);
        return index;
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public @NonNull Set<Integer> getVertices() {
        return new IntRangeReadOnlySet(0, vertexCount).asSet();
    }

    private void grow(int capacity) {
        long[] temp = ListHelper.grow(vertexCount, capacity, stride, nextArrows);
        if ((temp.length & dataMask) != 0) {
            throw new IllegalStateException("too much capacity requested:" + temp.length + " available index bits=" + (64 - dataShift));
        }
        nextArrows = temp;
        prevArrows = ListHelper.grow(vertexCount, capacity, stride, prevArrows);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator nextVerticesSpliterator(int vidx) {
        int offset = vidx * stride;
        return new MySpliterator(offset, 0, (int) (nextArrows[offset] & indexMask), nextArrows, indexMask);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator prevVerticesSpliterator(int vidx) {
        int offset = vidx * stride;
        return new MySpliterator(offset, 0, (int) (prevArrows[offset] & indexMask), prevArrows, indexMask);
    }

    public void removeVertex(int vidx) {
        Objects.checkIndex(vidx, vertexCount);
        removeAllArrowsStartingAt(vidx);
        removeAllArrowsEndingAt(vidx);
        if (vidx < vertexCount - 1) {
            addToVectorIndices(nextArrows, 0, nextArrows, 0, vidx, vidx, -1);
            addToVectorIndices(nextArrows, vidx, nextArrows, vidx + 1, vertexCount - vidx, vidx, -1);
            addToVectorIndices(prevArrows, 0, prevArrows, 0, vidx, vidx, -1);
            addToVectorIndices(prevArrows, vidx, prevArrows, vidx + 1, vertexCount - vidx, vidx, -1);
        }
        int vOffset = (vertexCount - 1) * stride;
        Arrays.fill(nextArrows, vOffset, vOffset + stride, 0L);
        Arrays.fill(prevArrows, vOffset, vOffset + stride, 0L);
        vertexCount--;
    }

    /**
     * Removes all arrows ending at the specified vertex.
     *
     * @param vidx index of vertex v
     */
    public void removeAllArrowsEndingAt(int vidx) {
        Objects.checkIndex(vidx, vertexCount);
        int vOffset = vidx * stride;
        int vPrevCount = structGetIndex(prevArrows[vOffset]);
        for (int i = vPrevCount; i >= 0; i--) {
            int uidx = structGetIndex(prevArrows[vOffset + i]);
            int uOffset = uidx * stride;
            int uNextCount = structGetIndex(nextArrows[uOffset]);
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

    /**
     * Removes all arrows starting at the specified vertex.
     *
     * @param vidx index of vertex v
     */
    public void removeAllArrowsStartingAt(int vidx) {
        Objects.checkIndex(vidx, vertexCount);
        int vOffset = vidx * stride;
        int vNextCount = structGetIndex(nextArrows[vOffset]);
        for (int i = vNextCount; i >= 0; i--) {
            int uidx = structGetIndex(nextArrows[vOffset + i]);
            int uOffset = uidx * stride;
            int uPrevCount = (int) (prevArrows[uOffset] & indexMask);
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

    /**
     * Removes an arrow from {@code vidx} to {@code uidx}.
     *
     * @param vidx index of vertex v
     * @param uidx index of vertex u
     * @throws NoSuchElementException if there is no such arrow
     */
    public void removeArrow(int vidx, int uidx) {
        int vOffset = vidx * stride;
        int vNextCount = structGetIndex(nextArrows[vOffset]);
        int uOffset = uidx * stride;
        int uPrevCount = (int) (prevArrows[uOffset] & indexMask);
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

    /**
     * Removes an arrow from {@code vidx} to {@code uidx} that has
     * the specified {@code data}.
     *
     * @param vidx index of vertex v
     * @param uidx index of vertex u
     * @param data the arrow data
     * @throws NoSuchElementException if there is no such arrow
     */
    public void removeArrow(int vidx, int uidx, long data) {
        int vOffset = vidx * stride;
        int vNextCount = structGetIndex(nextArrows[vOffset]);
        int uOffset = uidx * stride;
        int uPrevCount = structGetIndex(prevArrows[uOffset]);
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

    /**
     * Gets an index from the provided struct.
     *
     * @param struct a struct
     * @return index
     */
    private int structGetIndex(long struct) {
        return (int) (struct & indexMask);
    }

    /**
     * Gets data from the provided struct.
     *
     * @param struct a struct
     * @return data
     */
    private int structGetData(long struct) {
        return (int) (struct >>> dataShift);
    }

    private static class MySpliterator extends AbstractIntEnumeratorSpliterator {
        private final int limit;
        private final int offset;
        private final long[] arrows;
        private final long indexMask;
        private int index;

        public MySpliterator(int offset, int lo, int hi, long[] arrows, long indexMask) {
            super(hi - lo, ORDERED | NONNULL | SIZED | SUBSIZED);
            limit = hi;
            index = lo;
            this.offset = offset;
            this.arrows = arrows;
            this.indexMask = indexMask;
        }

        @Override
        public boolean moveNext() {
            if (index < limit) {
                current = (int) (arrows[offset + index++] & indexMask);
                return true;
            }
            return false;
        }

        public @Nullable MySpliterator trySplit() {
            int hi = limit, lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // divide range in half unless too small
                    new MySpliterator(offset, lo, index = mid, arrows, indexMask);
        }

    }
}
