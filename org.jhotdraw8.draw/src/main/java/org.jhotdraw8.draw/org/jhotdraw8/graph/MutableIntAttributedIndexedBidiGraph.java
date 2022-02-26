/*
 * @(#)MutableIndexedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractLongEnumeratorSpliterator;
import org.jhotdraw8.collection.IntEnumeratorSpliterator;
import org.jhotdraw8.collection.ListHelper;
import org.jhotdraw8.collection.LongArrayDeque;
import org.jhotdraw8.collection.LongEnumeratorSpliterator;
import org.jhotdraw8.collection.LongIntArrayEnumeratorSpliterator;
import org.jhotdraw8.util.Preconditions;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.Arrays;
import java.util.BitSet;
import java.util.NoSuchElementException;
import java.util.function.ToIntFunction;

import static java.lang.Math.max;

/**
 * A mutable indexed bi-directional graph.
 * <p>
 * Supports one integer of data for every vertex and for every arrow.
 * <p>
 * The data is stored in a long, which is a struct tat contains the data and
 * the vertex index. See {@link #getStructIndex(long)},
 * {@link #getStructData(long)}, {@link #toStruct(int, int)}.
 *
 * @author Werner Randelshofer
 */
public class MutableIntAttributedIndexedBidiGraph implements MutableIndexedBidiGraph {
    public static final long INDEX_MASK = 0xffff_ffffL;
    public static final int DATA_SHIFT = 32;
    private static final int INDEX_SHIFT = 0;
    private int maxArity;
    private final int stride;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <p>
     * Each arrow elements contains data in the upper 32 bits and a count
     * or index in the lower 32 bits.
     * <pre>
     * [ vertexData|arrowCount, arrowData|vertexIndex... ]
     * </pre>
     */
    private long[] prevArrows;
    /**
     * The array contains {@code stride} elements for each vertex.
     * <p>
     * Each arrow elements contains data in the upper 32 bits and a count
     * or index in the lower 32 bits.
     * <pre>
     * [ vertexData|arrowCount, arrowData|vertexIndex... ]
     * </pre>
     */
    private long[] nextArrows;
    private int vertexCount;
    private int arrowCount;

    /**
     * Creates a new instance.
     *
     * @param vertexCapacity the initial vertex capacity
     * @param maxArity       the maximal number of arrows per vertex
     */
    public MutableIntAttributedIndexedBidiGraph(int vertexCapacity, int maxArity) {
        if (vertexCapacity < 0) {
            throw new IllegalArgumentException("vertexCount=" + vertexCapacity);
        }
        if (maxArity < 0) {
            throw new IllegalArgumentException("maxArity=" + maxArity);
        }
        this.vertexCount = 0;
        this.maxArity = maxArity;
        this.stride = 1 + maxArity;
        this.nextArrows = new long[vertexCapacity * stride];
        this.prevArrows = new long[vertexCapacity * stride];
    }

    @Override
    public void addArrow(int vidx, int uidx) {
        addArrow(vidx, uidx, 0);
    }

    public void addArrow(int vidx, int uidx, int data) {
        int vOffset = vidx * stride;
        long vHeader = nextArrows[vOffset];
        int vNewNextCount = getStructIndex(vHeader) + 1;
        int uOffset = uidx * stride;
        long uHeader = prevArrows[uOffset];
        int uNewPrevCount = getStructIndex(uHeader) + 1;
        if (vNewNextCount > maxArity || uNewPrevCount > maxArity) {
            throw new IndexOutOfBoundsException("Not enough capacity for a new arrow " + vidx + "->" + uidx);
        }
        nextArrows[vOffset] = toStruct(getStructData(vHeader), vNewNextCount);
        nextArrows[vOffset + vNewNextCount] = toStruct(data, uidx);
        prevArrows[uOffset] = toStruct(getStructData(uHeader), uNewPrevCount);
        prevArrows[uOffset + uNewPrevCount] = toStruct(data, vidx);
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
    private void addToVectorIndices(long[] src, int vsrc, long[] dest, int vdst, int length, int vidx, int addend) {
        int srcOffset = vsrc * stride;
        int dstOffset = vdst * stride;
        for (int v = 0; v < length; v++) {
            long dstHeader = dest[dstOffset];
            int nDest = getStructIndex(dstHeader);
            long srcHeader = src[srcOffset];
            int nSrc = getStructIndex(srcHeader);
            dest[dstOffset] = toStruct(getStructData(dstHeader), nSrc);
            for (int i = 1; i <= nSrc; i++) {
                long srcArrow = src[srcOffset + i];
                int uidx = getStructIndex(srcArrow);
                if (uidx >= vidx) {
                    dest[dstOffset + i] = toStruct(getStructData(srcArrow), uidx + addend);
                }
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
    public void addVertex() {
        grow(vertexCount + 1);
        vertexCount += 1;
    }

    @Override
    public void addVertex(int vidx) {
        addVertex(vidx, 0);
    }

    public void addVertex(final int vidx, final int data) {
        grow(max(vertexCount + 1, vidx));
        final int newVertexCount = max(vidx, vertexCount + 1);
        final int vOffset = vidx * stride;
        if (vidx < vertexCount) {
            addToVectorIndices(nextArrows, 0, nextArrows, 0, vidx, vidx, 1);
            addToVectorIndices(nextArrows, vidx, nextArrows, vidx + 1, vertexCount - vidx, vidx, 1);
            addToVectorIndices(prevArrows, 0, prevArrows, 0, vidx, vidx, 1);
            addToVectorIndices(prevArrows, vidx, prevArrows, vidx + 1, vertexCount - vidx, vidx, 1);
            Arrays.fill(nextArrows, vOffset + 1, vOffset + stride - 1, 0);
            Arrays.fill(prevArrows, vOffset + 1, vOffset + stride - 1, 0);
        }
        nextArrows[vOffset] = toStruct(data, 0);
        prevArrows[vOffset] = toStruct(data, 0);
        vertexCount = newVertexCount;
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     * <p>
     * The iterator returns a long which is a struct. Use {@link #getStructData(long)}
     * and {@link #getStructIndex(long)} to get fields from the struct.
     *
     * @param vidx the index of the vertex
     * @return the spliterator
     */
    public LongEnumeratorSpliterator backwardBreadthFirstLongSpliterator(int vidx) {
        return new BreadthFirstSpliteratorOfLong(vidx, prevArrows, stride,
                0, AddToIntSet.addToBitSet(new BitSet(vertexCount)), this::getStructIndex);
    }

    /**
     * Returns a breadth first spliterator that starts at the specified vertex.
     * <p>
     * The iterator returns a long which is a struct. Use {@link #getStructData(long)}
     * and {@link #getStructIndex(long)} to get fields from the struct.
     *
     * @param vidx the index of the vertex
     * @return the spliterator
     */
    public LongEnumeratorSpliterator breadthFirstLongSpliterator(int vidx) {
        return new BreadthFirstSpliteratorOfLong(vidx, nextArrows, stride,
                0, AddToIntSet.addToBitSet(new BitSet(vertexCount)), this::getStructIndex);
    }

    @Override
    public int findIndexOfNext(final int vidxa, final int vidxb) {
        final int vOffset = vidxa * stride;
        for (int i = 0, n = getStructIndex(nextArrows[vOffset]); i < n; i++) {
            if (getStructIndex(nextArrows[i + vOffset + 1]) == vidxb) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int findIndexOfPrev(final int vidxa, final int vidxb) {
        final int vOffset = vidxa * stride;
        for (int i = 0, n = getStructIndex(prevArrows[vOffset]); i < n; i++) {
            if (getStructIndex(prevArrows[i + vOffset + 1]) == vidxb) {
                return i;
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
        int vOffset = vidx * stride;
        Preconditions.checkIndex(i, getStructIndex(nextArrows[vOffset]));
        return getStructIndex(nextArrows[vOffset + i + 1]);
    }

    public int getNextArrow(int vidx, int i) {
        int vOffset = vidx * stride;
        Preconditions.checkIndex(i, getStructIndex(nextArrows[vOffset]));
        return getStructData(nextArrows[vOffset + i + 1]);
    }

    @Override
    public int getNextCount(int vidx) {
        return getStructIndex(nextArrows[vidx * stride]);
    }

    /**
     * Gets the vertex data of the specified index from the
     * same table that contains the next arrows.
     *
     * @param vidx index of vertex v
     * @return the vertex data
     */
    public int getNextVertexData(int vidx) {
        return getStructData(nextArrows[vidx * stride]);
    }

    @Override
    public int getPrev(int vidx, int i) {
        int vOffset = vidx * stride;
        Preconditions.checkIndex(i, getStructIndex(prevArrows[vOffset]));
        return getStructIndex(prevArrows[vOffset + i + 1]);
    }


    @Override
    public int getPrevCount(int vidx) {
        return getStructIndex(prevArrows[vidx * stride]);
    }

    /**
     * Gets the vertex data of the specified index from the
     * same table that contains the previous arrows.
     *
     * @param vidx index of vertex v
     * @return the vertex data
     */
    public int getPrevVertexData(int vidx) {
        return getStructData(nextArrows[vidx * stride]);
    }

    /**
     * Gets the data field from the struct.
     *
     * @param struct a long that is a struct
     * @return the index field from the struct
     */
    public int getStructData(long struct) {
        return (int) (struct >>> DATA_SHIFT);
    }

    /**
     * Gets the index field from the struct.
     *
     * @param struct a long that is a struct
     * @return the index field from the struct
     */
    public int getStructIndex(long struct) {
        return (int) struct;
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    private void grow(int capacity) {
        long[] temp = ListHelper.grow(vertexCount, capacity, stride, nextArrows);
        if (temp.length < capacity * stride) {
            throw new IllegalStateException("too much capacity requested:" + capacity);
        }
        nextArrows = temp;
        prevArrows = ListHelper.grow(vertexCount, capacity, stride, prevArrows);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator nextVerticesSpliterator(int vidx) {
        int vOffset = vidx * stride;
        return new LongIntArrayEnumeratorSpliterator(
                vOffset + 1, vOffset + 1 + getStructIndex(nextArrows[0]), nextArrows, INDEX_SHIFT, INDEX_MASK);
    }

    @Override
    public @NonNull IntEnumeratorSpliterator prevVerticesSpliterator(int vidx) {
        int vOffset = vidx * stride;
        return new LongIntArrayEnumeratorSpliterator(
                vOffset + 1, vOffset + 1 + getStructIndex(prevArrows[vOffset]), prevArrows, INDEX_SHIFT, INDEX_MASK);
    }

    @Override
    public void removeAllNext(int vidx) {
        Preconditions.checkIndex(vidx, vertexCount);
        int vOffset = vidx * stride;
        long vHeader = nextArrows[vOffset];
        int vNextCount = getStructIndex(vHeader);
        for (int i = vNextCount - 1; i >= 0; i--) {
            int uidx = getStructIndex(nextArrows[vOffset + i + 1]);
            int uOffset = uidx * stride;
            long uHeader = prevArrows[uOffset];
            int uPrevCount = getStructIndex(uHeader);
            int vIndex = findIndexOfPrev(uidx, vidx);
            if (vIndex < uPrevCount - 1) {
                System.arraycopy(prevArrows, uOffset + vIndex + 2, prevArrows, uOffset + vIndex + 1, uPrevCount - vIndex - 1);
            }
            prevArrows[uOffset + uPrevCount] = 0L;
            prevArrows[uOffset] = toStruct(getStructData(uHeader), uPrevCount - 1);
            nextArrows[vOffset + i + 1] = 0L;
        }
        nextArrows[vOffset] = toStruct(getStructData(vHeader), 0);
    }

    @Override
    public void removeAllPrev(int vidx) {
        Preconditions.checkIndex(vidx, vertexCount);
        int vOffset = vidx * stride;
        long vHeader = prevArrows[vOffset];
        int vPrevCount = getStructIndex(vHeader);
        for (int i = vPrevCount - 1; i >= 0; i--) {
            int uidx = getStructIndex(prevArrows[vOffset + i + 1]);
            int uOffset = uidx * stride;
            long uHeader = nextArrows[uOffset];
            int uNextCount = getStructIndex(uHeader);
            int vIndex = findIndexOfNext(uidx, vidx);
            if (vIndex == -1) {
                throw new IllegalStateException("vidx must be next of uidx. vidx=" + vidx + " uidx=" + uidx);
            }
            if (vIndex < uNextCount - 1) {
                System.arraycopy(nextArrows, uOffset + vIndex + 2, nextArrows, uOffset + vIndex + 1, uNextCount - vIndex - 1);
            }
            nextArrows[uOffset + uNextCount] = 0L;
            nextArrows[uOffset] = toStruct(getStructData(uHeader), uNextCount - 1);
            prevArrows[vOffset + i + 1] = 0L;
        }
        prevArrows[vOffset] = toStruct(getStructData(vHeader), 0);
    }

    @Override
    public void removeNext(int vidx, int i) {
        int uidx = getNext(vidx, i);
        int vOffset = vidx * stride;
        long vHeader = nextArrows[vOffset];
        int vNextCount = getStructIndex(vHeader);
        int uOffset = uidx * stride;
        long uHeader = prevArrows[uOffset];
        int uPrevCount = getStructIndex(uHeader);
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
        nextArrows[vOffset + vNextCount] = 0L;
        prevArrows[uOffset + uPrevCount] = 0L;
        nextArrows[vOffset] = toStruct(getStructData(vHeader), vNextCount - 1);
        prevArrows[uOffset] = toStruct(getStructData(uHeader), uPrevCount - 1);
        arrowCount--;
    }

    @Override
    public void removeVertex(int vidx) {
        Preconditions.checkIndex(vidx, vertexCount);
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

    /**
     * Sets the vertex data for the specified vertex.
     *
     * @param vidx the index of the vertex
     * @param data the vertex data
     */

    public void setVertexData(int vidx, int data) {
        int offset = vidx * stride;
        nextArrows[offset] = toStruct(data, getStructIndex(nextArrows[offset]));
        prevArrows[offset] = toStruct(data, getStructIndex(prevArrows[offset]));
    }

    /**
     * Creates a struct that contains a data field and an index field.
     *
     * @param data  the data field
     * @param index the index field
     * @return
     */
    public long toStruct(int data, int index) {
        return ((long) data << DATA_SHIFT) | (index & INDEX_MASK);
    }

    private static class BreadthFirstSpliteratorOfLong extends AbstractLongEnumeratorSpliterator {
        private final long[] array;
        private final int stride;
        private final int offset;
        private final @NonNull LongArrayDeque deque = new LongArrayDeque();
        private final @NonNull AddToIntSet visited;
        private final @NonNull ToIntFunction<Long> toIntFunction;

        /**
         * @param array         the array
         * @param stride        the stride
         * @param offset
         * @param toIntFunction
         */
        protected BreadthFirstSpliteratorOfLong(int root, long[] array, int stride,
                                                int offset, @NonNull AddToIntSet visited, @NonNull ToIntFunction<Long> toIntFunction) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.array = array;
            this.stride = stride;
            this.offset = offset;
            this.visited = visited;
            this.toIntFunction = toIntFunction;
            deque.addFirstLong(root);
        }

        @Override
        public boolean moveNext() {
            boolean added = false;
            while (!deque.isEmpty() && !added) {
                current = deque.removeFirstLong();
                added = visited.add(toIntFunction.applyAsInt(current));
            }
            if (!added) {
                return false;
            }

            int currentOffset = toIntFunction.applyAsInt(current) * stride + offset;
            int size = toIntFunction.applyAsInt(array[currentOffset]);
            deque.addLastAll(array, currentOffset + 1, size);
            return true;
        }
    }

    public void clear() {
        vertexCount = 0;
        Arrays.fill(nextArrows, 0L);
        Arrays.fill(prevArrows, 0L);
    }

    /**
     * Sets the maximal arity. This also clears the graph!
     *
     * @param maxArity the new maximal arity of the graph
     */
    public void clearAndSetMaxArity(int maxArity) {
        this.maxArity = maxArity;
        vertexCount = 0;
        nextArrows = new long[0];
        prevArrows = new long[0];
    }
}
