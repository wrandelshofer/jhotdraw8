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


/**
 * A mutable indexed bi-directional graph that uses a chunked
 * compressed row storage (CRS) format.
 * <p>
 * Supports up to {@code 2^16 - 1} vertices.
 * <p>
 * This graph does not support multiple arrows between the same vertices.
 * <p>
 * This implementation uses chunks with a fixed number of vertices.
 * A chunk uses a compressed sparse row representation (CSR).
 * <p>
 * This implementation is efficient, if the graph is changed rarely. Changes
 * are expensive, because this implementation uses a single gap with
 * free elements in each chunk. The gap needs to be shifted around for
 * every insertion and removal of an arrow in the graph.
 * <p>
 * References:
 * <dl>
 *     <dt>JHotDraw 8</dt>
 *     <dd> This class has been derived from JHotDraw 8.
 *      © 2018 by the authors and contributors of JHotDraw. MIT License.</dd>
 * </dl>
 */
public class CrsBidiGraph implements MutableIndexedBidiGraph
        , IntAttributedIndexedBidiGraph {


    /**
     * Number of vertices per chunk.
     * Must be a power of 2.
     */
    private final int chunkSize;
    private final int chunkShift;

    /**
     * Array of chunks for arrows to next vertices.
     */
    private @NonNull CrsChunk[] nextChunks = new CrsChunk[0];
    /**
     * Array of chunks for arrows to previous vertices.
     */
    private @NonNull CrsChunk[] prevChunks = new CrsChunk[0];
    private final int initialArityCapacity;

    public CrsBidiGraph() {
        this(256, 4);
    }

    public CrsBidiGraph(final int chunkSize, final int initialArityCapacity) {
        if (Integer.bitCount(chunkSize) != 1) {
            throw new IllegalArgumentException("chunkSize=" + chunkSize + " is not a power of 2");
        }
        this.chunkSize = chunkSize;
        this.initialArityCapacity = chunkSize * initialArityCapacity;
        this.chunkShift = Integer.numberOfTrailingZeros(chunkSize);
    }


    int vertexCount = 0;
    int arrowCount = 0;

    /**
     * Adds the arrow if it is absent.
     *
     * @param v index of vertex 'v'
     * @param u index of vertex 'u'
     */
    @Override
    public void addArrowAsInt(final int v, final int u) {
        addArrowAsInt(v, u, 0);
    }

    /**
     * Adds the arrow if it is absent.
     *
     * @param v index of vertex 'v'
     * @param u index of vertex 'u'
     */
    @Override
    public void addArrowAsInt(final int v, final int u, final int data) {
        addArrowIfAbsentAsInt(v, u, data);
    }


    /**
     * Adds the arrow if its absent, updates the arrow data if the arrow is
     * present.
     *
     * @param v    index of vertex 'v'
     * @param u    index of vertex 'u'
     * @param data the arrow data
     * @return true if the arrow was absent
     */
    public boolean addOrUpdateArrowAsInt(final int v, final int u, final int data) {
        final CrsChunk uChunk = getPrevChunk(u);
        final boolean added = uChunk.addArrow(u, v, data, true);
        final CrsChunk vChunk = getNextChunk(v);
        vChunk.addArrow(v, u, data, true);
        if (added) {
            arrowCount++;
        }
        return added;
    }


    /**
     * Adds the arrow if its absent, updates the arrow data if the arrow is
     * present.
     *
     * @param v    index of vertex 'v'
     * @param u    index of vertex 'u'
     * @param data the arrow data
     * @return true if the arrow was absent
     */
    public boolean addArrowIfAbsentAsInt(final int v, final int u, final int data) {
        final CrsChunk uChunk = getPrevChunk(u);
        boolean added = uChunk.addArrow(u, v, data, false);
        if (added) {
            final CrsChunk vChunk = getNextChunk(v);
            vChunk.addArrow(v, u, data, false);
            arrowCount++;
        }
        return added;
    }


    @Override
    public int findIndexOfPrevAsInt(final int v, final int u) {
        final CrsChunk chunk = getPrevChunk(v);
        return chunk.indexOf(v, u);
    }

    @Override
    public int findIndexOfNextAsInt(final int v, final int u) {
        final CrsChunk chunk = getNextChunk(v);
        return chunk.indexOf(v, u);
    }


    @Override
    public void addVertexAsInt() {
        grow(vertexCount + 1);
        vertexCount++;
    }

    @Override
    public void addVertexAsInt(final int v) {
        if (v < vertexCount) {
            throw new UnsupportedOperationException();
        }
        grow(vertexCount + 1);
        vertexCount++;
    }

    @Override
    public void removeAllPrevAsInt(final int v) {
        final CrsChunk chunk = getPrevChunk(v);
        final int from = chunk.getSiblingsFromOffset(v);
        int[] a = chunk.getSiblingsArray();
        final int to = chunk.getSiblingCount(v) + from;
        int[] next = new int[to - from];
        System.arraycopy(a, from, next, 0, next.length);
        for (int i = next.length - 1; i >= 0; i--) {
            final int u = next[i];
            final CrsChunk nextChunk = getNextChunk(u);
            nextChunk.tryToRemoveArrow(u, v);
        }
        chunk.removeAllArrows(v);
    }

    @Override
    public void removeAllNextAsInt(final int v) {
        final CrsChunk chunk = getNextChunk(v);
        final int from = chunk.getSiblingsFromOffset(v);
        final int to = chunk.getSiblingCount(v) + from;
        int[] a = chunk.getSiblingsArray();
        int[] next = new int[to - from];
        System.arraycopy(a, from, next, 0, next.length);
        for (int i = next.length - 1; i >= 0; i--) {
            final int u = next[i];
            final CrsChunk prevChunk = getPrevChunk(u);
            prevChunk.tryToRemoveArrow(u, v);
        }
        chunk.removeAllArrows(v);
    }

    @Override
    public void removeNextAsInt(final int v, final int index) {
        Preconditions.checkIndex(index, getNextCount(v));
        final int u = getNextChunk(v).removeArrowAt(v, index);
        getPrevChunk(u).tryToRemoveArrow(u, v);
        arrowCount--;
    }

    @Override
    public void removePrevAsInt(final int v, final int index) {
        Preconditions.checkIndex(index, getPrevCount(v));
        final int u = getPrevChunk(v).removeArrowAt(v, index);
        getNextChunk(u).tryToRemoveArrow(u, v);
        arrowCount--;
    }

    @Override
    public void removeVertexAsInt(final int v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public int getNextAsInt(final int v, final int index) {
        return getNextChunk(v).getSibling(v, index);
    }

    @Override
    public int getNextArrowAsInt(final int v, final int k) {
        return getNextChunk(v).getArrow(v, k);
    }

    @Override
    public int getPrevArrowAsInt(final int v, final int k) {
        return getPrevChunk(v).getArrow(v, k);
    }

    @Override
    public int getVertexAsInt(final int v) {
        return getNextChunk(v).getVertexData(v);
    }

    public void setVertexAsInt(final int v, final int data) {
        getNextChunk(v).setVertexData(v, data);
        getPrevChunk(v).setVertexData(v, data);
    }

    @Override
    public int getNextCount(final int v) {
        return getNextChunk(v).getSiblingCount(v);
    }

    private CrsChunk getNextChunk(final int v) {
        return getOrCreateChunk(nextChunks, v);
    }

    private CrsChunk getPrevChunk(final int v) {
        return getOrCreateChunk(prevChunks, v);
    }

    CrsChunk getOrCreateChunk(final CrsChunk[] chunks, final int v) {
        @NonNull CrsChunk chunk = chunks[v >>> chunkShift];
        if (chunk == null) {
            chunk = new CrsSingleArrayChunk(chunkSize, initialArityCapacity);
            chunks[v >>> chunkShift] = chunk;
        }
        return chunk;
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public int getPrevAsInt(final int v, final int k) {
        return getPrevChunk(v).getSibling(v, k);
    }

    @Override
    public int getPrevCount(final int v) {
        return getPrevChunk(v).getSiblingCount(v);
    }

    private void grow(final int capacity) {
        final int chunkedCapacity = (capacity + chunkSize - 1) >>> chunkShift;
        final CrsChunk[] temp = (CrsChunk[]) ListHelper.grow(vertexCount, chunkedCapacity, 1, nextChunks);
        if (temp.length < chunkedCapacity) {
            throw new IllegalStateException("too much capacity requested:" + capacity);
        }
        nextChunks = temp;
        prevChunks = (CrsChunk[]) ListHelper.grow(vertexCount, chunkedCapacity, 1, prevChunks);
    }

    public void clear() {
        Arrays.fill(nextChunks, null);
        Arrays.fill(prevChunks, null);
        arrowCount = 0;
        vertexCount = 0;
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator backwardBreadthFirstLongSpliterator(final int vidx) {
        return backwardBreadthFirstLongSpliterator(vidx, new DenseIntSet8Bit(vertexCount));
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator backwardBreadthFirstLongSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new BreadthFirstLongSpliterator(vidx, prevChunks, visited);
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator backwardDepthFirstLongSpliterator(final int vidx) {
        return backwardDepthFirstLongSpliterator(vidx, new DenseIntSet8Bit(vertexCount));
    }

    /**
     * Returns a backward breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator backwardDepthFirstLongSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new DepthFirstLongSpliterator(vidx, prevChunks, visited);
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator breadthFirstLongSpliterator(final int vidx) {
        return breadthFirstLongSpliterator(vidx, new DenseIntSet8Bit(vertexCount));
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator breadthFirstLongSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new BreadthFirstLongSpliterator(vidx, nextChunks, visited);
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator depthFirstLongSpliterator(final int vidx) {
        return depthFirstLongSpliterator(vidx, new DenseIntSet8Bit(vertexCount));
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator depthFirstLongSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new DepthFirstLongSpliterator(vidx, nextChunks, visited);
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx the index of the vertex
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull IntEnumeratorSpliterator breadthFirstIntSpliterator(final int vidx) {
        return breadthFirstIntSpliterator(vidx, new DenseIntSet8Bit(vertexCount));
    }

    /**
     * Returns a breadth first spliterator that starts at the specified
     * vertex.
     *
     * @param vidx    the index of the vertex
     * @param visited the set of visited vertices
     * @return the spliterator contains the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull IntEnumeratorSpliterator breadthFirstIntSpliterator(final int vidx, final @NonNull AddToIntSet visited) {
        return new BreadthFirstIntSpliterator(vidx, nextChunks, visited);
    }

    private class BreadthFirstLongSpliterator extends AbstractLongEnumeratorSpliterator {

        private final CrsChunk[] chunks;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;

        protected BreadthFirstLongSpliterator(final int root, final CrsChunk[] chunks, final @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.chunks = chunks;
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
            final int v = deque.removeFirstAsInt();
            final CrsChunk chunk = getOrCreateChunk(chunks, v);
            current = ((long) chunk.getVertexData(v)) << 32 | (v & 0xffff_ffffL);
            final int from = chunk.getSiblingsFromOffset(v);
            final int to = chunk.getSiblingCount(v) + from;
            final int[] a = chunk.getSiblingsArray();
            for (int i = from; i < to; i++) {
                final int u = a[i];
                if (visited.addAsInt(u)) {
                    deque.addLastAsInt(u);
                }
            }
            return true;
        }
    }

    private class DepthFirstLongSpliterator extends AbstractLongEnumeratorSpliterator {

        private final CrsChunk[] chunks;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;

        protected DepthFirstLongSpliterator(final int root, final CrsChunk[] chunks, final @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.chunks = chunks;
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
            final int v = deque.removeFirstAsInt();
            final CrsChunk chunk = getOrCreateChunk(chunks, v);
            current = ((long) chunk.getVertexData(v)) << 32 | (v & 0xffff_ffffL);
            final int from = chunk.getSiblingsFromOffset(v);
            final int to = chunk.getSiblingCount(v) + from;
            int[] a = chunk.getSiblingsArray();

            for (int i = from; i < to; i++) {
                final int u = a[i];
                if (visited.addAsInt(u)) {
                    deque.addFirstAsInt(u);
                }
            }
            return true;
        }
    }

    private class BreadthFirstIntSpliterator extends AbstractIntEnumeratorSpliterator {

        private final CrsChunk[] chunks;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;

        protected BreadthFirstIntSpliterator(final int root, final CrsChunk[] chunks, final @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.chunks = chunks;
            this.visited = visited;
            if (visited.addAsInt(root)) {
                deque.addLastAsInt(root);
            }
        }

        @Override
        public boolean moveNext() {
            if (deque.isEmpty()) {
                return false;
            }
            final int v = deque.removeFirstAsInt();
            final CrsChunk chunk = getOrCreateChunk(chunks, v);
            current = v;
            final int from = chunk.getSiblingsFromOffset(v);
            final int to = chunk.getSiblingCount(v) + from;
            int[] a = chunk.getSiblingsArray();

            for (int i = from; i < to; i++) {
                final int u = a[i];
                if (visited.addAsInt(u)) {
                    deque.addLastAsInt(u);
                }
            }
            return true;
        }
    }
}
