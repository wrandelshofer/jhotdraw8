/*
 * @(#)MutableIntAttributed16BitIndexedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ListHelper;
import org.jhotdraw8.collection.enumerator.AbstractIntEnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.AbstractLongEnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IntEnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IntUShortArrayEnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.LongEnumeratorSpliterator;
import org.jhotdraw8.collection.primitive.DenseIntSet8Bit;
import org.jhotdraw8.collection.primitive.IntArrayDeque;
import org.jhotdraw8.graph.algo.AddToIntSet;
import org.jhotdraw8.graph.precondition.Preconditions;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static java.lang.Math.max;

/**
 * A mutable indexed bi-directional graph.
 * <p>
 * Supports up to {@code 2^16 - 1} vertices.
 * <p>
 * This implementation uses large contiguous arrays. Each row occupies
 * {@code maxArity + 1} elements in the array for the arrows.
 * <p>
 * If the arity of the vertices is unevenly distributed, a dfs- or
 * bfs-search is likely to encounter a different cash line or page for
 * every vertex.
 * <p>
 * XXX delete me, this representation is inefficient
 */
public class MutableIntAttributed16BitIndexedBidiGraph implements MutableIndexedBidiGraph
        , IntAttributedIndexedBidiGraph {

    private final int maxArity;
    private final int stride;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <pre>
     * [ vertexData0, vertexData1, arrowCount, vertexIndex... ]
     * </pre>
     */
    private short[] prev;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <pre>
     * [ vertexData0, vertexData1, arrowCount, vertexIndex... ]
     * </pre>
     */
    private short[] next;

    /**
     * The array contains {@code maxArity} elements for each vertex.
     * <pre>
     * [ arrowData... ]
     * </pre>
     */
    private int[] nextArrow;
    /**
     * The array contains {@code maxArity} elements for each vertex.
     * <pre>
     * [ arrowData... ]
     * </pre>
     */
    private int[] prevArrow;

    private int vertexCount;
    private int arrowCount;
    /**
     * Number of array elements used to store the vertex data
     * in the {@link #prev} and {@link #next} arrays.
     */
    private final static int VERTEX_DATA_SIZE = 2;

    /**
     * Creates a new instance.
     *
     * @param vertexCapacity the initial vertex capacity
     * @param maxArity       the maximal number of arrows per vertex
     */
    public MutableIntAttributed16BitIndexedBidiGraph(final int vertexCapacity, final int maxArity) {
        if (vertexCapacity < 0) {
            throw new IllegalArgumentException("vertexCount=" + vertexCapacity);
        }
        if (maxArity < 0) {
            throw new IllegalArgumentException("maxArity=" + maxArity);
        }
        this.vertexCount = 0;
        this.maxArity = maxArity;
        this.stride = VERTEX_DATA_SIZE + 1 + maxArity;
        this.next = new short[vertexCapacity * stride];
        this.prev = new short[vertexCapacity * stride];
        this.nextArrow = new int[vertexCapacity * maxArity];
        this.prevArrow = new int[vertexCapacity * maxArity];
    }

    /**
     * Removes all vertices and all arrows.
     */
    public void clear() {
        vertexCount = 0;
        arrowCount = 0;
        Arrays.fill(next, (short) 0);
        Arrays.fill(prev, (short) 0);
        Arrays.fill(nextArrow, 0);
        Arrays.fill(prevArrow, 0);
    }

    @Override
    public void addArrowAsInt(final int v, final int u) {
        addArrowAsInt(v, u, 0);
    }

    @Override
    public void addArrowAsInt(final int v, final int u, final int arrowData) {
        Preconditions.checkIndex(v, getVertexCount());
        Preconditions.checkIndex(u, getVertexCount());
        final int vOffset = v * stride + VERTEX_DATA_SIZE;
        final int vNewNextCount = next[vOffset] + 1;
        final int uOffset = u * stride + VERTEX_DATA_SIZE;
        final int uNewPrevCount = prev[uOffset] + 1;
        if (vNewNextCount > maxArity || uNewPrevCount > maxArity) {
            throw new IndexOutOfBoundsException("Not enough capacity for a new arrow " + v + "->" + u);
        }
        next[vOffset + vNewNextCount] = (short) u;
        next[vOffset] = (short) vNewNextCount;
        nextArrow[v * maxArity + vNewNextCount - 1] = arrowData;
        prevArrow[u * maxArity + uNewPrevCount - 1] = arrowData;
        prev[uOffset + uNewPrevCount] = (short) v;
        prev[uOffset] = (short) uNewPrevCount;
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
    private void addToVectorIndices(final short[] src, final int vsrc, final short[] dest, final int vdst, final int length, final int vidx, final int addend) {
        int srcOffset = vsrc * stride + VERTEX_DATA_SIZE;
        int dstOffset = vdst * stride + VERTEX_DATA_SIZE;
        for (int v = 0; v < length; v++) {
            final int nDest = dest[dstOffset];
            final int nSrc = src[srcOffset];
            dest[dstOffset] = (short) nSrc;
            for (int i = 1; i <= nSrc; i++) {
                final int uidx = src[srcOffset + i];
                if (uidx >= vidx) {
                    dest[dstOffset + i] = (short) (uidx + addend);
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
    public void addVertexAsInt() {
        grow(vertexCount + 1);
        vertexCount += 1;
    }

    @Override
    public void addVertexAsInt(final int v) {
        grow(max(vertexCount + 1, v));
        final int newVertexCount = max(v, vertexCount + 1);
        final int vOffset = v * stride + VERTEX_DATA_SIZE;
        if (v < vertexCount) {
            addToVectorIndices(next, 0, next, 0, v, v, 1);
            addToVectorIndices(next, v, next, v + 1, vertexCount - v, v, 1);
            addToVectorIndices(prev, 0, prev, 0, v, v, 1);
            addToVectorIndices(prev, v, prev, v + 1, vertexCount - v, v, 1);
            System.arraycopy(nextArrow, v * maxArity, nextArrow, (v + 1) * maxArity, (vertexCount - v) * maxArity);
            System.arraycopy(prevArrow, v * maxArity, prevArrow, (v + 1) * maxArity, (vertexCount - v) * maxArity);
            Arrays.fill(next, vOffset, vOffset + stride, (short) 0);
            Arrays.fill(prev, vOffset, vOffset + stride, (short) 0);
            Arrays.fill(nextArrow, v * maxArity, (v + 1) * maxArity, 0);
            Arrays.fill(prevArrow, v * maxArity, (v + 1) * maxArity, 0);
        }
        vertexCount = newVertexCount;
    }

    @Override
    public int findIndexOfNextAsInt(final int v, final int u) {
        final int vOffset = v * stride + VERTEX_DATA_SIZE + 1;
        for (int i = vOffset, end = vOffset + next[vOffset - 1]; i < end; i++) {
            if (next[i] == u) {
                return i - vOffset;
            }
        }
        return -1;
    }

    @Override
    public int findIndexOfPrevAsInt(final int v, final int u) {
        final int vOffset = v * stride + VERTEX_DATA_SIZE + 1;
        for (int i = vOffset, end = vOffset + prev[vOffset - 1]; i < end; i++) {
            if (prev[i] == u) {
                return i - vOffset;
            }
        }
        return -1;
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public int getNextAsInt(final int v, final int i) {
        final int vOffset = v * stride + VERTEX_DATA_SIZE;
        Preconditions.checkIndex(i, next[vOffset]);
        return next[vOffset + i + 1];
    }

    @Override
    public int getNextCount(final int v) {
        return next[v * stride + VERTEX_DATA_SIZE];
    }

    @Override
    public int getPrevAsInt(final int v, final int i) {
        final int vOffset = v * stride + VERTEX_DATA_SIZE;
        Preconditions.checkIndex(i, prev[vOffset]);
        return prev[vOffset + i + 1];
    }


    @Override
    public int getPrevCount(final int v) {
        return prev[v * stride + VERTEX_DATA_SIZE];
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }


    private void grow(final int capacity) {
        final short[] temp = ListHelper.grow(capacity, stride, next);
        if (temp.length < capacity * stride) {
            throw new IllegalStateException("too much capacity requested:" + capacity);
        }
        next = temp;
        prev = ListHelper.grow(capacity, stride, prev);
        nextArrow = ListHelper.grow(capacity, maxArity, nextArrow);
        prevArrow = ListHelper.grow(capacity, maxArity, prevArrow);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator nextVerticesEnumerator(final int v) {
        final int vOffset = v * stride + VERTEX_DATA_SIZE;
        return new IntUShortArrayEnumeratorSpliterator(vOffset + 1, vOffset + 1 + next[vOffset], next);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator prevVerticesEnumerator(final int v) {
        final int vOffset = v * stride + VERTEX_DATA_SIZE;
        return new IntUShortArrayEnumeratorSpliterator(vOffset + 1, vOffset + 1 + prev[vOffset], prev);
    }

    @Override
    public void removeAllPrevAsInt(final int v) {
        Preconditions.checkIndex(v, vertexCount);
        final int vOffset = v * stride + VERTEX_DATA_SIZE;
        final int vPrevCount = prev[vOffset];
        for (int i = vPrevCount; i >= 0; i--) {
            removePrevAsInt(v, i);
        }
        prev[vOffset] = 0;
    }

    @Override
    public void removeAllNextAsInt(final int v) {
        Preconditions.checkIndex(v, vertexCount);
        final int vOffset = v * stride + VERTEX_DATA_SIZE;
        final int vNextCount = next[vOffset];
        for (int i = vNextCount; i >= 0; i--) {
            removeNextAsInt(v, i);
        }
    }

    @Override
    public void removeNextAsInt(final int v, final int index) {
        final int uidx = getNextAsInt(v, index);
        final int vOffset = v * stride + VERTEX_DATA_SIZE;
        final int vNewNextCount = next[vOffset] - 1;
        final int uOffset = uidx * stride + VERTEX_DATA_SIZE;
        final int uNewPrevCount = prev[uOffset] - 1;
        final int vIndex = findIndexOfPrevAsInt(uidx, v);
        if (vIndex < 0 || index < 0) {
            throw new NoSuchElementException("There is no arrow " + v + "->" + uidx);
        }
        final int vArrowOffset = v * maxArity;
        if (index < vNewNextCount) {
            System.arraycopy(next, vOffset + index + 2, next, vOffset + index + 1, vNewNextCount - index);
            System.arraycopy(nextArrow, vArrowOffset + index + 1, nextArrow, vArrowOffset + index, vNewNextCount - index);
        }
        final int uArrowOffset = uidx * maxArity;
        if (vIndex < uNewPrevCount) {
            System.arraycopy(prev, uOffset + vIndex + 2, prev, uOffset + vIndex + 1, uNewPrevCount - vIndex);
            System.arraycopy(prevArrow, uArrowOffset + vIndex + 1, prevArrow, uArrowOffset + vIndex, uNewPrevCount - vIndex);
        }
        next[vOffset + vNewNextCount + 1] = 0;
        prev[uOffset + uNewPrevCount + 1] = 0;
        nextArrow[vArrowOffset + vNewNextCount] = 0;
        prevArrow[uArrowOffset + uNewPrevCount] = 0;
        next[vOffset] = (short) vNewNextCount;
        prev[uOffset] = (short) uNewPrevCount;
        arrowCount--;
    }

    @Override
    public void removePrevAsInt(final int vidx, final int i) {
        final int uidx = getPrevAsInt(vidx, i);
        final int vOffset = vidx * stride + VERTEX_DATA_SIZE;
        final int vNewPrevCount = prev[vOffset] - 1;
        final int uOffset = uidx * stride + VERTEX_DATA_SIZE;
        final int uNewNextCount = next[uOffset] - 1;
        final int vIndex = findIndexOfNextAsInt(uidx, vidx);
        if (vIndex < 0 || i < 0) {
            throw new NoSuchElementException("There is no arrow " + vidx + "->" + uidx);
        }
        final int vArrowOffset = vidx * maxArity;
        if (i < vNewPrevCount) {
            System.arraycopy(prev, vOffset + i + 2, prev, vOffset + i + 1, vNewPrevCount - i);
            System.arraycopy(prevArrow, vArrowOffset + i + 1, prevArrow, vArrowOffset + i, vNewPrevCount - i);
        }
        final int uArrowOffset = uidx * maxArity;
        if (vIndex < uNewNextCount) {
            System.arraycopy(next, uOffset + vIndex + 2, next, uOffset + vIndex + 1, uNewNextCount - vIndex);
            System.arraycopy(nextArrow, uArrowOffset + vIndex + 1, nextArrow, uArrowOffset + vIndex, uNewNextCount - vIndex);
        }
        prev[vOffset + vNewPrevCount] = 0;
        next[uOffset + uNewNextCount] = 0;
        prevArrow[vArrowOffset + vNewPrevCount] = 0;
        nextArrow[uArrowOffset + uNewNextCount] = 0;
        prev[vOffset] = (short) (vNewPrevCount);
        next[uOffset] = (short) (uNewNextCount);
        arrowCount--;
    }

    @Override
    public void removeVertexAsInt(final int v) {
        Preconditions.checkIndex(v, vertexCount);
        removeAllNextAsInt(v);
        removeAllPrevAsInt(v);
        if (v < vertexCount - 1) {
            addToVectorIndices(next, 0, next, 0, v, v, -1);
            addToVectorIndices(next, v, next, v + 1, vertexCount - v, v, -1);
            addToVectorIndices(prev, 0, prev, 0, v, v, -1);
            addToVectorIndices(prev, v, prev, v + 1, vertexCount - v, v, -1);
            System.arraycopy(nextArrow, (v + 1) * maxArity, nextArrow, (v) * maxArity, (vertexCount - v - 1) * maxArity);
            System.arraycopy(prevArrow, (v + 1) * maxArity, prevArrow, (v) * maxArity, (vertexCount - v - 1) * maxArity);
        }
        final int vOffset = (vertexCount - 1) * stride + VERTEX_DATA_SIZE;
        Arrays.fill(next, vOffset, vOffset + stride, (short) 0);
        Arrays.fill(prev, vOffset, vOffset + stride, (short) 0);
        Arrays.fill(nextArrow, (vertexCount - 1) * maxArity, (vertexCount) * maxArity, 0);
        Arrays.fill(prevArrow, (vertexCount - 1) * maxArity, (vertexCount) * maxArity, 0);
        vertexCount--;
    }

    private static class VertexEnumeratorOfShortSpliterator extends AbstractIntEnumeratorSpliterator {

        private final short[] array;
        private final int stride;
        private final int offset;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;
        private final boolean dfs;

        /**
         * @param array  the array
         * @param stride the stride
         * @param offset
         * @param dfs
         */
        protected VertexEnumeratorOfShortSpliterator(final int root, final short[] array, final int stride,
                                                     final int offset, @NonNull final AddToIntSet visited, boolean dfs) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.array = array;
            this.stride = stride;
            this.offset = offset;
            this.visited = visited;
            this.dfs = dfs;
            if (visited.addAsInt(root)) {
                deque.addFirstAsInt(root);
            }
        }

        @Override
        public boolean moveNext() {
            if (deque.isEmpty()) {
                return false;
            }

            current = dfs ? deque.removeLastAsInt() : deque.removeFirstAsInt();
            final int currentOffset = current * stride + offset;
            final int size = array[currentOffset] & 0xffff;
            for (int i = currentOffset + 1, end = currentOffset + size + 1; i < end; i++) {
                final int vidx = array[i] & 0xffff;
                if (visited.addAsInt(vidx)) {
                    deque.addLastAsInt(vidx);
                }
            }
            return true;
        }
    }

    private static class VertexEnumeratorOfLongShortSpliterator extends AbstractLongEnumeratorSpliterator {

        private final short[] array;
        private final int stride;
        private final int offset;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;
        private final boolean dfs;

        /**
         * @param array  the array
         * @param stride the stride
         * @param offset the offset
         * @param dfs
         */
        protected VertexEnumeratorOfLongShortSpliterator(final int root, final short[] array, final int stride,
                                                         final int offset, @NonNull final AddToIntSet visited, boolean dfs) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.array = array;
            this.stride = stride;
            this.offset = offset;
            this.visited = visited;
            this.dfs = dfs;
            if (visited.addAsInt(root)) {
                deque.addFirstAsInt(root);
            }
        }

        @Override
        public boolean moveNext() {
            if (deque.isEmpty()) {
                return false;
            }
            final int currentIdx = dfs ? deque.removeLastAsInt() : deque.removeFirstAsInt();
            final int currentDataOffset = currentIdx * stride;
            current = ((long) array[currentDataOffset] << 48) | ((long) array[currentDataOffset] << 32) | currentIdx & 0xffff_ffffL;
            final int currentOffset = currentDataOffset + offset;
            final int size = array[currentOffset];
            for (int i = currentOffset + 1, end = currentOffset + size; i <= end; i++) {
                final int vidx = array[i] & 0xffff;
                if (visited.addAsInt(vidx)) {
                    deque.addLastAsInt(vidx);
                }
            }
            return true;
        }
    }


    /**
     * Sets the vertex data for the specified vertex.
     *
     * @param vidx the index of the vertex
     * @param data the vertex data
     */
    public void setVertexAsInt(final int vidx, final int data) {
        final int offset = vidx * stride;
        prev[offset] = next[offset] = (short) (data >>> 16);
        prev[offset + 1] = next[offset + 1] = (short) data;
    }

    @Override
    public int getNextArrowAsInt(final int v, final int i) {
        return nextArrow[v * maxArity + i];
    }

    @Override
    public int getPrevArrowAsInt(final int v, final int i) {
        return prevArrow[v * maxArity + i];
    }

    @Override
    public int getVertexDataAsInt(final int vidx) {
        return getVertexDataFromNextAsInt(vidx);
    }

    public int getVertexDataFromNextAsInt(final int vidx) {
        final int offset = vidx * stride;
        return (next[offset] << 16) | next[offset + 1];
    }

    public int getVertexDataFromPrevAsInt(final int vidx) {
        final int offset = vidx * stride;
        return (prev[offset] << 16) | prev[offset + 1];
    }

    /**
     * Returns a breadth first spliterator that starts at the specified vertex.
     *
     * @param vidx the index of the vertex
     * @param dfs  whether to search depth-first instead of breadth-first
     * @return the spliterator
     */
    public @NonNull IntEnumeratorSpliterator seachNextVerticesAsInt(final int vidx, boolean dfs) {
        return seachNextVerticesAsInt(vidx, new DenseIntSet8Bit(vertexCount)::addAsInt, dfs);
    }

    public @NonNull IntEnumeratorSpliterator seachNextVerticesAsInt(final int vidx, @NonNull final AddToIntSet visited, boolean dfs) {
        return new VertexEnumeratorOfShortSpliterator(vidx, next, stride,
                VERTEX_DATA_SIZE, visited, dfs);
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @param dfs
     * @return the spliterator
     */
    public @NonNull IntEnumeratorSpliterator searchPrevVerticesAsInt(final int vidx, boolean dfs) {
        return searchPrevVerticesAsInt(vidx, new DenseIntSet8Bit(vertexCount)::addAsInt, dfs);
    }

    public @NonNull IntEnumeratorSpliterator searchPrevVerticesAsInt(final int vidx, @NonNull final AddToIntSet visited, boolean dfs) {
        return new VertexEnumeratorOfShortSpliterator(vidx, prev, stride,
                VERTEX_DATA_SIZE, visited, dfs);
    }

    /**
     * Returns a breadth first spliterator that starts at the specified vertex.
     *
     * @param vidx the index of the vertex
     * @param dfs
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator searchNextVerticesWithVertexData(final int vidx, boolean dfs) {
        return searchNextVerticesWithVertexData(vidx, new DenseIntSet8Bit(vertexCount)::addAsInt, dfs);
    }

    public @NonNull LongEnumeratorSpliterator searchNextVerticesWithVertexData(final int vidx, @NonNull final AddToIntSet visited, boolean dfs) {
        return new VertexEnumeratorOfLongShortSpliterator(vidx, next, stride,
                0, visited, dfs);
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @param dfs
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator searchPrevVerticesWithVertexData(final int vidx, boolean dfs) {
        return searchPrevVerticesWithVertexData(vidx, new DenseIntSet8Bit(vertexCount)::addAsInt, dfs);
    }

    public @NonNull LongEnumeratorSpliterator searchPrevVerticesWithVertexData(final int vidx, @NonNull final AddToIntSet visited, boolean dfs) {
        return new VertexEnumeratorOfLongShortSpliterator(vidx, prev, stride,
                0, visited, dfs);
    }
}
