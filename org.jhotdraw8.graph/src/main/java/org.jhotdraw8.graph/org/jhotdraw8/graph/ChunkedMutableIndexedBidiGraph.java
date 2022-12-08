/*
 * @(#)ChunkedMutableIndexedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.precondition.Preconditions;
import org.jhotdraw8.collection.ListHelper;
import org.jhotdraw8.collection.enumerator.AbstractIntEnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.AbstractLongEnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IntEnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.LongEnumeratorSpliterator;
import org.jhotdraw8.collection.primitive.IntArrayDeque;
import org.jhotdraw8.graph.algo.AddToIntSet;

import java.util.Arrays;
import java.util.function.BiFunction;


/**
 * A mutable indexed bi-directional graph. The data structure of the graph
 * is split up into {@link GraphChunk}s.
 */
public class ChunkedMutableIndexedBidiGraph implements MutableIndexedBidiGraph,
        IntAttributedIndexedBidiGraph {


    /**
     * Number of vertices per chunk.
     * Must be a power of 2.
     */
    private final int chunkSize;
    /**
     * Number of bits that we need to shift a vertex index to the right to get
     * the chunk that contains it.
     * This is log2(chunkSize).
     */
    private final int chunkShift;
    /**
     * If a new chunk is created, then we reserve for each vertex this amount
     * of space for arrows.
     */
    private final int initialArityCapacity;
    /**
     * Number of vertices in the graph.
     */
    int vertexCount = 0;

    /**
     * Number of arrows in the graph.
     */
    int arrowCount = 0;

    /**
     * Array of chunks for arrows to next vertices.
     */
    private @NonNull GraphChunk @NonNull [] nextChunks = new GraphChunk[0];
    /**
     * Array of chunks for arrows to previous vertices.
     */
    private @NonNull GraphChunk @NonNull [] prevChunks = new GraphChunk[0];

    /**
     * Factory for creating new chunks.
     */
    private @NonNull BiFunction<Integer, Integer, GraphChunk> chunkFactory = SingleArrayCsrGraphChunk::new;

    public ChunkedMutableIndexedBidiGraph() {
        this(256, 4);
    }

    public ChunkedMutableIndexedBidiGraph(final int chunkSize, final int initialArityCapacity) {
        this(chunkSize, initialArityCapacity, SingleArrayCsrGraphChunk::new);
    }

    public ChunkedMutableIndexedBidiGraph(final int chunkSize, final int initialArityCapacity,
                                          @NonNull BiFunction<Integer, Integer, GraphChunk> chunkFactory) {
        if (Integer.bitCount(chunkSize) != 1) {
            throw new IllegalArgumentException("chunkSize=" + chunkSize + " is not a power of 2");
        }
        this.chunkSize = chunkSize;
        this.initialArityCapacity = chunkSize * initialArityCapacity;
        this.chunkShift = Integer.numberOfTrailingZeros(chunkSize);
        this.chunkFactory = chunkFactory;
    }

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
     * present. Does nothing if there is already an arrow.
     *
     * @param v    index of vertex 'v'
     * @param u    index of vertex 'u'
     * @param data the arrow data
     * @return true if the arrow was absent
     */
    public boolean addArrowIfAbsentAsInt(final int v, final int u, final int data) {
        final GraphChunk uChunk = getPrevChunk(u);
        boolean added = uChunk.tryAddArrow(u, v, data, false);
        if (added) {
            final GraphChunk vChunk = getNextChunk(v);
            vChunk.tryAddArrow(v, u, data, false);
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
    public boolean addOrUpdateArrowAsInt(final int v, final int u, final int data) {
        final GraphChunk uChunk = getPrevChunk(u);
        final boolean added = uChunk.tryAddArrow(u, v, data, true);
        final GraphChunk vChunk = getNextChunk(v);
        vChunk.tryAddArrow(v, u, data, true);
        if (added) {
            arrowCount++;
        }
        return added;
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
        grow(v + 1);
        vertexCount = v + 1;
    }

    /**
     * Searches for predecessor vertices of the specified vertex.
     *
     * @param vidx    the index of the vertex
     * @param dfs     whether depth-first-search should be used instead of breadth-first-search
     * @param visited the set of visited vertices
     * @return the enumerator provides the vertex index
     */
    public @NonNull IntEnumeratorSpliterator searchPrevVertices(final int vidx, boolean dfs, final @NonNull AddToIntSet visited) {
        return new SearchVertexEnumeratorSpliterator(vidx, prevChunks, dfs, visited);
    }

    /**
     * Searches for predecessor vertex data of the specified vertex.
     *
     * @param v       a vertex
     * @param dfs     whether depth-first-search should be used instead of breadth-first-search
     * @param visited the set of visited vertices
     * @return the enumerator provides the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator searchPrevVertexData(final int v, boolean dfs, final @NonNull AddToIntSet visited) {
        return new SearchVertexDataEnumeratorSpliterator(v, prevChunks, dfs, visited);
    }

    /**
     * Searches for successor vertices of the specified vertex.
     *
     * @param v       a vertex
     * @param dfs     whether depth-first-search should be used instead of breadth-first-search
     * @param visited the set of visited vertices
     * @return the enumerator provides the vertex index
     */
    public @NonNull IntEnumeratorSpliterator searchNextVertices(final int v, boolean dfs, final @NonNull AddToIntSet visited) {
        return new SearchVertexEnumeratorSpliterator(v, nextChunks, dfs, visited);
    }

    /**
     * Searches for successor vertex data of the specified vertex.
     *
     * @param v       a vertex
     * @param dfs     whether depth-first-search should be used instead of breadth-first-search
     * @param visited the set of visited vertices
     * @return the enumerator provides the vertex data in the 32 high-bits
     * and the vertex index in the 32 low-bits of the long.
     */
    public @NonNull LongEnumeratorSpliterator searchNextVertexData(final int v, boolean dfs, final @NonNull AddToIntSet visited) {
        return new SearchVertexDataEnumeratorSpliterator(v, nextChunks, dfs, visited);
    }

    /**
     * Removes all vertices and arrows from the graph.
     */
    public void clear() {
        Arrays.fill(nextChunks, null);
        Arrays.fill(prevChunks, null);
        arrowCount = 0;
        vertexCount = 0;
    }


    @Override
    public int findIndexOfNextAsInt(final int v, final int u) {
        final GraphChunk chunk = getNextChunk(v);
        return chunk.indexOf(v, u);
    }

    @Override
    public int findIndexOfPrevAsInt(final int v, final int u) {
        final GraphChunk chunk = getPrevChunk(v);
        return chunk.indexOf(v, u);
    }

    @Override
    public int getArrowCount() {
        return arrowCount;
    }

    @Override
    public int getNextArrowAsInt(final int v, final int i) {
        return getNextChunk(v).getArrow(v, i);
    }

    @Override
    public int getNextAsInt(final int v, final int i) {
        return getNextChunk(v).getSibling(v, i);
    }

    private @NonNull GraphChunk getNextChunk(final int v) {
        return getOrCreateChunk(nextChunks, v);
    }

    @Override
    public int getNextCount(final int v) {
        return getNextChunk(v).getSiblingCount(v);
    }

    @NonNull GraphChunk getOrCreateChunk(final GraphChunk[] chunks, final int v) {
        @NonNull GraphChunk chunk = chunks[v >>> chunkShift];
        if (chunk == null) {
            chunk = chunkFactory.apply(chunkSize, initialArityCapacity);
            chunks[v >>> chunkShift] = chunk;
        }
        return chunk;
    }

    @Override
    public int getPrevArrowAsInt(final int v, final int i) {
        return getPrevChunk(v).getArrow(v, i);
    }

    @Override
    public int getPrevAsInt(final int v, final int k) {
        return getPrevChunk(v).getSibling(v, k);
    }

    private @NonNull GraphChunk getPrevChunk(final int v) {
        return getOrCreateChunk(prevChunks, v);
    }

    @Override
    public int getPrevCount(final int v) {
        return getPrevChunk(v).getSiblingCount(v);
    }

    @Override
    public int getVertexDataAsInt(final int v) {
        return getNextChunk(v).getVertexData(v);
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    private void grow(final int capacity) {
        final int chunkedCapacity = (capacity + chunkSize - 1) >>> chunkShift;
        nextChunks = (GraphChunk[]) ListHelper.grow(chunkedCapacity, 1, nextChunks);
        prevChunks = (GraphChunk[]) ListHelper.grow(chunkedCapacity, 1, prevChunks);
    }

    @Override
    public void removeAllNextAsInt(final int v) {
        Preconditions.checkIndex(v, vertexCount);
        final GraphChunk chunk = getNextChunk(v);
        final int from = chunk.getSiblingsFromOffset(v);
        int siblingCount = chunk.getSiblingCount(v);
        final int to = siblingCount + from;
        int[] a = chunk.getSiblingsArray();
        int[] next = new int[to - from];
        System.arraycopy(a, from, next, 0, next.length);
        for (int i = next.length - 1; i >= 0; i--) {
            final int u = next[i];
            final GraphChunk prevChunk = getPrevChunk(u);
            prevChunk.tryRemoveArrow(u, v);
        }
        chunk.removeAllArrows(v);
        arrowCount -= siblingCount;
    }

    @Override
    public void removeAllPrevAsInt(final int v) {
        Preconditions.checkIndex(v, vertexCount);
        final GraphChunk chunk = getPrevChunk(v);
        final int from = chunk.getSiblingsFromOffset(v);
        int[] a = chunk.getSiblingsArray();
        int siblingCount = chunk.getSiblingCount(v);
        final int to = siblingCount + from;
        int[] next = new int[to - from];
        System.arraycopy(a, from, next, 0, next.length);
        for (int i = next.length - 1; i >= 0; i--) {
            final int u = next[i];
            final GraphChunk nextChunk = getNextChunk(u);
            nextChunk.tryRemoveArrow(u, v);
        }
        chunk.removeAllArrows(v);
        arrowCount -= siblingCount;
    }

    @Override
    public void removeNextAsInt(final int v, final int index) {
        Preconditions.checkIndex(index, getNextCount(v));
        final int u = getNextChunk(v).removeArrowAt(v, index);
        getPrevChunk(u).tryRemoveArrow(u, v);
        arrowCount--;
    }

    @Override
    public void removePrevAsInt(final int v, final int index) {
        Preconditions.checkIndex(index, getPrevCount(v));
        final int u = getPrevChunk(v).removeArrowAt(v, index);
        getNextChunk(u).tryRemoveArrow(u, v);
        arrowCount--;
    }

    @Override
    public void removeVertexAsInt(final int v) {
        throw new UnsupportedOperationException();
    }

    public void setVertexDataAsInt(final int v, final int data) {
        Preconditions.checkIndex(v, vertexCount);
        getNextChunk(v).setVertexData(v, data);
        getPrevChunk(v).setVertexData(v, data);
    }

    private class SearchVertexDataEnumeratorSpliterator extends AbstractLongEnumeratorSpliterator {

        private final GraphChunk[] chunks;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;
        private final boolean dfs;

        protected SearchVertexDataEnumeratorSpliterator(final int root, final GraphChunk[] chunks, boolean dfs, final @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.chunks = chunks;
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
            final int v = dfs ? deque.removeLastAsInt() : deque.removeFirstAsInt();
            final GraphChunk chunk = getOrCreateChunk(chunks, v);
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

    private class SearchVertexEnumeratorSpliterator extends AbstractIntEnumeratorSpliterator {

        private final GraphChunk[] chunks;
        private final @NonNull IntArrayDeque deque = new IntArrayDeque();
        private final @NonNull AddToIntSet visited;
        private final boolean dfs;

        protected SearchVertexEnumeratorSpliterator(final int root, final GraphChunk[] chunks, boolean dfs, final @NonNull AddToIntSet visited) {
            super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
            this.chunks = chunks;
            this.visited = visited;
            this.dfs = dfs;
            if (visited.addAsInt(root)) {
                deque.addLastAsInt(root);
            }
        }

        @Override
        public boolean moveNext() {
            if (deque.isEmpty()) {
                return false;
            }
            final int v = dfs ? deque.removeLastAsInt() : deque.removeFirstAsInt();
            final GraphChunk chunk = getOrCreateChunk(chunks, v);
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

    public @NonNull BiFunction<Integer, Integer, GraphChunk> getChunkFactory() {
        return chunkFactory;
    }

    public void setChunkFactory(@NonNull BiFunction<Integer, Integer, GraphChunk> chunkFactory) {
        this.chunkFactory = chunkFactory;
    }
}
