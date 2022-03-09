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
import java.util.BitSet;
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
    private char[] prev;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <pre>
     * [ vertexData0, vertexData1, arrowCount, vertexIndex... ]
     * </pre>
     */
    private char[] next;

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
    public MutableIntAttributed16BitIndexedBidiGraph(int vertexCapacity, int maxArity) {
        if (vertexCapacity < 0) {
            throw new IllegalArgumentException("vertexCount=" + vertexCapacity);
        }
        if (maxArity < 0) {
            throw new IllegalArgumentException("maxArity=" + maxArity);
        }
        this.vertexCount = 0;
        this.maxArity = maxArity;
        this.stride = VERTEX_DATA_SIZE + 1 + maxArity;
        this.next = new char[vertexCapacity * stride];
        this.prev = new char[vertexCapacity * stride];
        this.nextArrow = new int[vertexCapacity * maxArity];
        this.prevArrow = new int[vertexCapacity * maxArity];
    }

    /**
     * Removes all vertices and all arrows.
     */
    public void clear() {
        vertexCount = 0;
        arrowCount = 0;
        Arrays.fill(next, (char) 0);
        Arrays.fill(prev, (char) 0);
        Arrays.fill(nextArrow, 0);
        Arrays.fill(prevArrow, 0);
    }

    @Override
    public void addArrowAsInt(int v, int u) {
        addArrowAsInt(v, u, 0);
    }

    public void addArrowAsInt(int v, int u, int arrowData) {
        int vOffset = v * stride + VERTEX_DATA_SIZE;
        int vNewNextCount = next[vOffset] + 1;
        int uOffset = u * stride + VERTEX_DATA_SIZE;
        int uNewPrevCount = prev[uOffset] + 1;
        if (vNewNextCount > maxArity || uNewPrevCount > maxArity) {
            throw new IndexOutOfBoundsException("Not enough capacity for a new arrow " + v + "->" + u);
        }
        next[vOffset + vNewNextCount] = (char) u;
        next[vOffset] = (char) vNewNextCount;
        nextArrow[v * maxArity + vNewNextCount - 1] = arrowData;
        prevArrow[u * maxArity + uNewPrevCount - 1] = arrowData;
        prev[uOffset + uNewPrevCount] = (char) v;
        prev[uOffset] = (char) uNewPrevCount;
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
    private void addToVectorIndices(char[] src, int vsrc, char[] dest, int vdst, int length, int vidx, int addend) {
        int srcOffset = vsrc * stride + VERTEX_DATA_SIZE;
        int dstOffset = vdst * stride + VERTEX_DATA_SIZE;
        for (int v = 0; v < length; v++) {
            int nDest = dest[dstOffset];
            int nSrc = src[srcOffset];
            dest[dstOffset] = (char) nSrc;
            for (int i = 1; i <= nSrc; i++) {
                int uidx = src[srcOffset + i];
                if (uidx >= vidx) {
                    dest[dstOffset + i] = (char) (uidx + addend);
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
    public void addVertexAsInt(int v) {
        grow(max(vertexCount + 1, v));
        int newVertexCount = max(v, vertexCount + 1);
        int vOffset = v * stride + VERTEX_DATA_SIZE;
        if (v < vertexCount) {
            addToVectorIndices(next, 0, next, 0, v, v, 1);
            addToVectorIndices(next, v, next, v + 1, vertexCount - v, v, 1);
            addToVectorIndices(prev, 0, prev, 0, v, v, 1);
            addToVectorIndices(prev, v, prev, v + 1, vertexCount - v, v, 1);
            System.arraycopy(nextArrow, v * maxArity, nextArrow, (v + 1) * maxArity, (vertexCount - v) * maxArity);
            System.arraycopy(prevArrow, v * maxArity, prevArrow, (v + 1) * maxArity, (vertexCount - v) * maxArity);
            Arrays.fill(next, vOffset, vOffset + stride, (char) 0);
            Arrays.fill(prev, vOffset, vOffset + stride, (char) 0);
            Arrays.fill(nextArrow, v * maxArity, (v + 1) * maxArity, 0);
            Arrays.fill(prevArrow, v * maxArity, (v + 1) * maxArity, 0);
        }
        vertexCount = newVertexCount;
    }

    @Override
    public int findIndexOfNextAsInt(int v, int u) {
        int vOffset = v * stride + VERTEX_DATA_SIZE + 1;
        for (int i = vOffset, end = vOffset + next[vOffset - 1]; i < end; i++) {
            if (next[i] == u) {
                return i - vOffset;
            }
        }
        return -1;
    }

    @Override
    public int findIndexOfPrevAsInt(int v, int u) {
        int vOffset = v * stride + VERTEX_DATA_SIZE + 1;
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
    public int getNextAsInt(int v, int index) {
        int vOffset = v * stride + VERTEX_DATA_SIZE;
        Preconditions.checkIndex(index, next[vOffset]);
        return next[vOffset + index + 1];
    }

    @Override
    public int getNextCount(int v) {
        return next[v * stride + VERTEX_DATA_SIZE];
    }

    @Override
    public int getPrevAsInt(int v, int i) {
        int vOffset = v * stride + VERTEX_DATA_SIZE;
        Preconditions.checkIndex(i, prev[vOffset]);
        return prev[vOffset + i + 1];
    }


    @Override
    public int getPrevCount(int v) {
        return prev[v * stride + VERTEX_DATA_SIZE];
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }


    private void grow(int capacity) {
        char[] temp = ListHelper.grow(vertexCount, capacity, stride, next);
        if (temp.length < capacity * stride) {
            throw new IllegalStateException("too much capacity requested:" + capacity);
        }
        next = temp;
        prev = ListHelper.grow(vertexCount, capacity, stride, prev);
        nextArrow = ListHelper.grow(vertexCount, capacity, maxArity, nextArrow);
        prevArrow = ListHelper.grow(vertexCount, capacity, maxArity, prevArrow);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator nextVerticesSpliterator(int v) {
        int vOffset = v * stride + VERTEX_DATA_SIZE;
        return new IntCharArrayEnumeratorSpliterator(vOffset + 1, vOffset + 1 + next[0], next);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator prevVerticesSpliterator(int v) {
        int vOffset = v * stride + VERTEX_DATA_SIZE;
        return new IntCharArrayEnumeratorSpliterator(vOffset + 1, vOffset + 1 + prev[vOffset], prev);
    }

    @Override
    public void removeAllPrevAsInt(int v) {
        Preconditions.checkIndex(v, vertexCount);
        int vOffset = v * stride + VERTEX_DATA_SIZE;
        int vPrevCount = prev[vOffset];
        for (int i = vPrevCount; i >= 0; i--) {
            removePrev(v, i);
        }
        prev[vOffset] = 0;
    }

    @Override
    public void removeAllNextAsInt(int v) {
        Preconditions.checkIndex(v, vertexCount);
        int vOffset = v * stride + VERTEX_DATA_SIZE;
        int vNextCount = next[vOffset];
        for (int i = vNextCount; i >= 0; i--) {
            removeNextAsInt(v, i);
        }
    }

    @Override
    public void removeNextAsInt(int v, int index) {
        int uidx = getNextAsInt(v, index);
        int vOffset = v * stride + VERTEX_DATA_SIZE;
        int vNewNextCount = next[vOffset] - 1;
        int uOffset = uidx * stride + VERTEX_DATA_SIZE;
        int uNewPrevCount = prev[uOffset] - 1;
        int vIndex = findIndexOfPrevAsInt(uidx, v);
        if (vIndex < 0 || index < 0) {
            throw new NoSuchElementException("There is no arrow " + v + "->" + uidx);
        }
        int vArrowOffset = v * maxArity;
        if (index < vNewNextCount) {
            System.arraycopy(next, vOffset + index + 2, next, vOffset + index + 1, vNewNextCount - index);
            System.arraycopy(nextArrow, vArrowOffset + index + 1, nextArrow, vArrowOffset + index, vNewNextCount - index);
        }
        int uArrowOffset = uidx * maxArity;
        if (vIndex < uNewPrevCount) {
            System.arraycopy(prev, uOffset + vIndex + 2, prev, uOffset + vIndex + 1, uNewPrevCount - vIndex);
            System.arraycopy(prevArrow, uArrowOffset + vIndex + 1, prevArrow, uArrowOffset + vIndex, uNewPrevCount - vIndex);
        }
        next[vOffset + vNewNextCount + 1] = 0;
        prev[uOffset + uNewPrevCount + 1] = 0;
        nextArrow[vArrowOffset + vNewNextCount] = 0;
        prevArrow[uArrowOffset + uNewPrevCount] = 0;
        next[vOffset] = (char) vNewNextCount;
        prev[uOffset] = (char) uNewPrevCount;
        arrowCount--;
    }

    public void removePrev(int vidx, int i) {
        int uidx = getPrevAsInt(vidx, i);
        int vOffset = vidx * stride + VERTEX_DATA_SIZE;
        int vNewPrevCount = prev[vOffset] - 1;
        int uOffset = uidx * stride + VERTEX_DATA_SIZE;
        int uNewNextCount = next[uOffset] - 1;
        int vIndex = findIndexOfNextAsInt(uidx, vidx);
        if (vIndex < 0 || i < 0) {
            throw new NoSuchElementException("There is no arrow " + vidx + "->" + uidx);
        }
        int vArrowOffset = vidx * maxArity;
        if (i < vNewPrevCount) {
            System.arraycopy(prev, vOffset + i + 2, prev, vOffset + i + 1, vNewPrevCount - i);
            System.arraycopy(prevArrow, vArrowOffset + i + 1, prevArrow, vArrowOffset + i, vNewPrevCount - i);
        }
        int uArrowOffset = uidx * maxArity;
        if (vIndex < uNewNextCount) {
            System.arraycopy(next, uOffset + vIndex + 2, next, uOffset + vIndex + 1, uNewNextCount - vIndex);
            System.arraycopy(nextArrow, uArrowOffset + vIndex + 1, nextArrow, uArrowOffset + vIndex, uNewNextCount - vIndex);
        }
        prev[vOffset + vNewPrevCount] = 0;
        next[uOffset + uNewNextCount] = 0;
        prevArrow[vArrowOffset + vNewPrevCount] = 0;
        nextArrow[uArrowOffset + uNewNextCount] = 0;
        prev[vOffset] = (char) (vNewPrevCount);
        next[uOffset] = (char) (uNewNextCount);
        arrowCount--;
    }

    @Override
    public void removeVertexAsInt(int v) {
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
        int vOffset = (vertexCount - 1) * stride + VERTEX_DATA_SIZE;
        Arrays.fill(next, vOffset, vOffset + stride, (char) 0);
        Arrays.fill(prev, vOffset, vOffset + stride, (char) 0);
        Arrays.fill(nextArrow, (vertexCount - 1) * maxArity, (vertexCount) * maxArity, 0);
        Arrays.fill(prevArrow, (vertexCount - 1) * maxArity, (vertexCount) * maxArity, 0);
        vertexCount--;
    }

    private static class BreadthFirstSpliteratorOfChar extends AbstractIntEnumeratorSpliterator {
        private final char[] array;
        private final int stride;
        private final int offset;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;

        /**
         * @param array  the array
         * @param stride the stride
         * @param offset
         */
        protected BreadthFirstSpliteratorOfChar(int root, char[] array, int stride,
                                                int offset, @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.array = array;
            this.stride = stride;
            this.offset = offset;
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

            current = deque.removeFirst();
            int currentOffset = current * stride + offset;
            int size = array[currentOffset];
            for (int i = 0; i < size; i++) {
                char vidx = array[i + currentOffset];
                if (visited.addAsInt(vidx)) {
                    deque.addLastAsInt(vidx);
                }
            }
            return true;
        }
    }

    private static class BreadthFirstSpliteratorOfLongChar extends AbstractLongEnumeratorSpliterator {
        private final char[] array;
        private final int stride;
        private final int offset;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;

        /**
         * @param array  the array
         * @param stride the stride
         * @param offset
         */
        protected BreadthFirstSpliteratorOfLongChar(int root, char[] array, int stride,
                                                    int offset, @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.array = array;
            this.stride = stride;
            this.offset = offset;
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
            int currentIdx = deque.removeFirst();
            int currentDataOffset = currentIdx * stride;
            current = ((long) array[currentDataOffset] << 48) | ((long) array[currentDataOffset] << 32) | currentIdx & 0xffff_ffffL;
            int currentOffset = currentDataOffset + offset;
            int size = array[currentOffset];
            for (int i = currentOffset + 1, end = currentOffset + size; i <= end; i++) {
                char vidx = array[i];
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
    public void setVertexAsInt(int vidx, int data) {
        int offset = vidx * stride;
        prev[offset] = next[offset] = (char) (data >>> 16);
        prev[offset + 1] = next[offset + 1] = (char) data;
    }

    @Override
    public int getNextArrowAsInt(int v, int i) {
        return nextArrow[v * maxArity + i];
    }

    @Override
    public int getPrevArrowAsInt(int v, int i) {
        return prevArrow[v * maxArity + i];
    }

    public int getVertexAsInt(int vidx) {
        return getVertexDataFromNextAsInt(vidx);
    }

    public int getVertexDataFromNextAsInt(int vidx) {
        int offset = vidx * stride;
        return (next[offset] << 16) | next[offset + 1];
    }

    public int getVertexDataFromPrevAsInt(int vidx) {
        int offset = vidx * stride;
        return (prev[offset] << 16) | prev[offset + 1];
    }

    /**
     * Returns a breadth first spliterator that starts at the specified vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator
     */
    public IntEnumeratorSpliterator breadthFirstIntSpliterator(int vidx) {
        return breadthFirstIntSpliterator(vidx, AddToIntSet.addToBitSet(new BitSet(vertexCount)));
    }

    public IntEnumeratorSpliterator breadthFirstIntSpliterator(int vidx, @NonNull AddToIntSet visited) {
        return new BreadthFirstSpliteratorOfChar(vidx, next, stride,
                0, visited);
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator
     */
    public IntEnumeratorSpliterator backwardBreadthFirstIntSpliterator(int vidx) {
        return backwardBreadthFirstIntSpliterator(vidx, AddToIntSet.addToBitSet(new BitSet(vertexCount)));
    }

    public IntEnumeratorSpliterator backwardBreadthFirstIntSpliterator(int vidx, @NonNull AddToIntSet visited) {
        return new BreadthFirstSpliteratorOfChar(vidx, prev, stride,
                0, visited);
    }

    /**
     * Returns a breadth first spliterator that starts at the specified vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public LongEnumeratorSpliterator breadthFirstLongSpliterator(int vidx) {
        return breadthFirstLongSpliterator(vidx, AddToIntSet.addToBitSet(new BitSet(vertexCount)));
    }

    public LongEnumeratorSpliterator breadthFirstLongSpliterator(int vidx, @NonNull AddToIntSet visited) {
        return new BreadthFirstSpliteratorOfLongChar(vidx, next, stride,
                0, visited);
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public LongEnumeratorSpliterator backwardBreadthFirstLongSpliterator(int vidx) {
        return backwardBreadthFirstLongSpliterator(vidx, AddToIntSet.addToBitSet(new BitSet(vertexCount)));
    }

    public LongEnumeratorSpliterator backwardBreadthFirstLongSpliterator(int vidx, @NonNull AddToIntSet visited) {
        return new BreadthFirstSpliteratorOfLongChar(vidx, prev, stride,
                0, visited);
    }
}
