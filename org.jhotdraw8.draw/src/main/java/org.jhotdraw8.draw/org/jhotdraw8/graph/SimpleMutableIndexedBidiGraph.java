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
 * This implementation uses large contiguous arrays. Each row occupies
 * {@code maxArity + 1} elements in the array for the arrows.
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
    private int[] prev;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <pre>
     * [ arrowCount, vertexIndex... ]
     * </pre>
     */
    private int[] next;
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
        this.stride = 2 + maxArity;
        this.next = new int[vertexCapacity * stride];
        this.prev = new int[vertexCapacity * stride];
    }

    @Override
    public void addArrowAsInt(int v, int u) {
        int vOffset = v * stride;
        int vNewNextCount = next[vOffset] + 1;
        int uOffset = u * stride;
        int uNewPrevCount = prev[uOffset] + 1;
        if (vNewNextCount > maxArity || uNewPrevCount > maxArity) {
            throw new IndexOutOfBoundsException("Not enough capacity for a new arrow " + v + "->" + u);
        }
        next[vOffset + vNewNextCount] = u;
        next[vOffset] = vNewNextCount;
        prev[uOffset + uNewPrevCount] = v;
        prev[uOffset] = uNewPrevCount;
        arrowCount++;
    }

    /**
     * Adds an arrow but ignores the arrow data.
     *
     * @param v    index of vertex 'v'
     * @param u    index of vertex 'u'
     * @param data the arrow data is ignored
     */
    @Override
    public void addArrowAsInt(int v, int u, int data) {
        addArrowAsInt(v, u);
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
    public void addVertexAsInt() {
        grow(vertexCount + 1);
        vertexCount += 1;
    }

    @Override
    public void addVertexAsInt(int v) {
        grow(max(vertexCount + 1, v));
        int newVertexCount = max(v, vertexCount + 1);
        if (v < vertexCount) {
            addToVectorIndices(next, 0, next, 0, v, v, 1);
            addToVectorIndices(next, v, next, v + 1, vertexCount - v, v, 1);
            addToVectorIndices(prev, 0, prev, 0, v, v, 1);
            addToVectorIndices(prev, v, prev, v + 1, vertexCount - v, v, 1);
        }
        int vOffset = v * stride;
        Arrays.fill(next, vOffset, vOffset + stride, 0);
        Arrays.fill(prev, vOffset, vOffset + stride, 0);
        vertexCount = newVertexCount;
    }

    @Override
    public int findIndexOfNextAsInt(int v, int u) {
        int vOffset = v * stride + 1;
        for (int i = vOffset, n = vOffset + next[vOffset - 1]; i < n; i++) {
            if (next[i] == u) {
                return i - vOffset;
            }
        }
        return -1;
    }

    @Override
    public int findIndexOfPrevAsInt(int v, int u) {
        int vOffset = v * stride + 1;
        for (int i = vOffset, n = vOffset + prev[vOffset - 1]; i < n; i++) {
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
        int vOffset = v * stride;
        Preconditions.checkIndex(index, next[vOffset]);
        return next[vOffset + index + 1];
    }

    @Override
    public int getPrevArrowAsInt(int v, int index) {
        return getPrevAsInt(v, index);
    }

    @Override
    public int getNextArrowAsInt(int v, int index) {
        return getNextAsInt(v, index);
    }

    @Override
    public int getNextCount(int v) {
        return next[v * stride];
    }

    @Override
    public int getPrevAsInt(int v, int i) {
        int vOffset = v * stride;
        Preconditions.checkIndex(i, prev[vOffset]);
        return prev[vOffset + i + 1];
    }


    @Override
    public int getPrevCount(int v) {
        return prev[v * stride];
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }


    private void grow(int capacity) {
        int[] temp = ListHelper.grow(vertexCount, capacity, stride, next);
        if (temp.length < capacity * stride) {
            throw new IllegalStateException("too much capacity requested:" + capacity);
        }
        next = temp;
        prev = ListHelper.grow(vertexCount, capacity, stride, prev);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator nextVerticesSpliterator(int v) {
        int vOffset = v * stride;
        return new IntIntArrayEnumeratorSpliterator(vOffset + 1, vOffset + 1 + next[vOffset], next);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator prevVerticesSpliterator(int v) {
        int vOffset = v * stride;
        return new IntIntArrayEnumeratorSpliterator(vOffset + 1, vOffset + 1 + prev[vOffset], prev);
    }

    @Override
    public void removeAllPrevAsInt(int v) {
        Preconditions.checkIndex(v, vertexCount);
        int vOffset = v * stride;
        int vPrevCount = prev[vOffset];
        for (int i = vPrevCount; i >= 0; i--) {
            int uidx = prev[vOffset + i];
            int uOffset = uidx * stride;
            int uNextCount = next[uOffset];
            int vIndex = findIndexOfNextAsInt(uidx, v);
            if (vIndex < uNextCount - 1) {
                System.arraycopy(next, uOffset + vIndex + 2, next, uOffset + vIndex + 1, uNextCount - vIndex - 1);
            }
            next[uOffset + uNextCount] = 0;
            next[uOffset] = uNextCount - 1;
            prev[vOffset + i + 1] = 0;
        }
        prev[vOffset] = 0;
    }

    @Override
    public void removeAllNextAsInt(int v) {
        Preconditions.checkIndex(v, vertexCount);
        int vOffset = v * stride;
        int vNextCount = next[vOffset];
        for (int i = vNextCount; i >= 0; i--) {
            int uidx = next[vOffset + i];
            int uOffset = uidx * stride;
            int uPrevCount = prev[uOffset];
            int vIndex = findIndexOfPrevAsInt(uidx, v);
            if (vIndex < uPrevCount - 1) {
                System.arraycopy(prev, uOffset + vIndex + 2, prev, uOffset + vIndex + 1, uPrevCount - vIndex - 1);
            }
            prev[uOffset + uPrevCount] = 0;
            prev[uOffset] = uPrevCount - 1;
            next[vOffset + i + 1] = 0;
        }
        next[vOffset] = 0;
    }

    @Override
    public void removeNextAsInt(int v, int index) {
        int uidx = getNextAsInt(v, index);
        int vOffset = v * stride;
        int vNextCount = next[vOffset];
        int uOffset = uidx * stride;
        int uPrevCount = prev[uOffset];
        int vIndex = findIndexOfPrevAsInt(uidx, v);
        if (vIndex < 0 || index < 0) {
            throw new NoSuchElementException("There is no arrow " + v + "->" + uidx);
        }
        if (index < vNextCount - 1) {
            System.arraycopy(next, vOffset + index + 2, next, vOffset + index + 1, vNextCount - index - 1);
        }
        if (vIndex < uPrevCount - 1) {
            System.arraycopy(prev, uOffset + vIndex + 2, prev, uOffset + vIndex + 1, uPrevCount - vIndex - 1);
        }
        next[vOffset + vNextCount] = 0;
        prev[uOffset + uPrevCount] = 0;
        next[vOffset] = vNextCount - 1;
        prev[uOffset] = uPrevCount - 1;
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
        }
        int vOffset = (vertexCount - 1) * stride;
        Arrays.fill(next, vOffset, vOffset + stride, 0);
        Arrays.fill(prev, vOffset, vOffset + stride, 0);
        vertexCount--;
    }

    private static class BreadthFirstSpliteratorOfInt extends AbstractIntEnumeratorSpliterator {
        private final int[] array;
        private final int stride;
        private final int offset;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;

        /**
         * @param array  the array
         * @param stride the stride
         * @param offset
         */
        protected BreadthFirstSpliteratorOfInt(int root, int[] array, int stride,
                                               int offset, @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.array = array;
            this.stride = stride;
            this.offset = offset;
            this.visited = visited;
            deque.addFirstAsInt(root);
        }

        @Override
        public boolean moveNext() {
            boolean added = false;
            while (!deque.isEmpty() && !added) {
                current = deque.removeFirst();
                added = visited.addAsInt(current);
            }
            if (!added) {
                return false;
            }

            int currentOffset = current * stride + offset;
            int size = array[currentOffset];
            deque.addLastAllAsInt(array, currentOffset + 1, size);
            return true;
        }
    }


    /**
     * Sets the vertex data for the specified vertex.
     *
     * @param vidx the index of the vertex
     * @param data the vertex data
     */

    public void setVertexData(int vidx, int data) {
        next[vidx * stride] = data;
        prev[vidx * stride] = data;
    }

    /**
     * Returns a breadth first spliterator that starts at the specified vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator
     */
    public IntEnumeratorSpliterator breadthFirstIntSpliterator(int vidx) {
        return new BreadthFirstSpliteratorOfInt(vidx, next, stride,
                0, AddToIntSet.addToBitSet(new BitSet(vertexCount)));
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator
     */
    public IntEnumeratorSpliterator backwardBreadthFirstIntSpliterator(int vidx) {
        return new BreadthFirstSpliteratorOfInt(vidx, prev, stride,
                0, AddToIntSet.addToBitSet(new BitSet(vertexCount)));
    }
}
