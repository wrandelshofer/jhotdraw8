/*
 * @(#)ImmutableAttributed32BitIndexedBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IntArrayEnumerator;
import org.jhotdraw8.collection.IntEnumerator;
import org.jhotdraw8.util.Preconditions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ImmutableDirectedGraph. Uses int-arrays for storage.
 * <p>
 * Supports up to {@code 2^31 - 1} vertices.
 * <p>
 * Uses a representation that is similar to a compressed row storage for
 * matrices (CRS). A bidirectional graph is represented with 7 arrays:
 * {@code nextOffset}, {@code next}, {@code nextArrows}, {@code prevOffset},
 * {@code prev}, {@code prevArrows}, and {@code vertices}.
 * <dl>
 *     <dt>{@code nextOffset}</dt>
 *     <dd>Holds for each vertex  {@code v}, the offset into the arrays
 *     {@code next}, and {@code nextArrows}.
 *     The data for vertex {@code v} can be found in these arrays in the
 *     elements from {@code nextOffset[v]}(inclusive) to
 *     {@code nextOffset[v + 1]} (exclusive).</dd>
 *     <dt>{@code next}</dt>
 *     <dd>Holds for each arrow from a vertex {@code v} to a vertex {@code u}
 *     the index of {@code u}.</dd>
 *     <dt>{@code nextArrows}</dt>
 *     <dd>Holds for each arrow from a vertex {@code v} to a vertex {@code u}
 *     the data associated to the arrow.</dd>
 *     <dt>{@code prevOffset}, {@code next}, {@code nextArrows}</dt>
 *     <dd>These arrays have the same structure as {@code nextOffset},
 *     {@code next}, {@code nextArrows} but they they store for a vertex
 *     {@code v} the data for ingoing arrows from a vertex {@code u}
 *     to the vertex {@code v}</dd>
 *     <dt>{@code vertices}</dt>
 *     <dd>Holds for each vertex {@code v} the data associated to the vertex.</dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public class ImmutableAttributed32BitIndexedBidiGraph<V, A> implements AttributedIndexedBidiGraph<V, A>, BidiGraph<V, A> {

    /**
     * Holds the indices to the next vertices.
     * <p>
     * The indices are stored in consecutive runs for each vertex,
     * starting at the offset given by {@code nextOffset}.
     * <p>
     * Given vertex index {@code vi < nextOffset.length - 1}<br>
     * then<br>
     * {@code offset = nextOffset[vi]}
     * {@code count = nextOffset[vi+1] - offset}
     * <p>
     * Given vertex index {@code vi == nextOffset.length - 1}<br>
     * then<br>
     * {@code offset = nextOffset[vi]}
     * {@code count = nextOffset.length - offset}
     */
    protected final @NonNull int[] next;
    protected final @NonNull int[] prev;

    /**
     * Holds offsets into the {@link #next} table and the
     * {@link #nextArrows} table.
     * <p>
     * Given vertex index {@code vi},<br>
     * {@code nextOffset[vi]} yields the offset {@code ai}
     * in the tables {@link #next} table and the {@link #nextArrows}.
     * <p>
     * Given vertex index {@code vi < nextOffset.length - 1},<br>
     * {@code nextOffset[vi+1]) - nextOffset[vi]} yields the
     * number of outgoing arrows of that vertex.
     * <p>
     * Given vertex index {@code vi == nextOffset.length - 1},<br>
     * {@code nextOffset.length - nextOffset[vi]} yields the
     * number of outgoing arrows of that vertex.
     */
    protected final @NonNull int[] nextOffset;
    protected final @NonNull int[] prevOffset;

    /**
     * Holds the arrow objects.
     * <p>
     * The arrows are stored in consecutive runs for each vertex,
     * starting at the offset given by {@code nextOffset}.
     * <p>
     * See {@link #next}.
     */
    protected final @NonNull A[] nextArrows;
    protected final @NonNull A[] prevArrows;
    /**
     * Holds the vertex objects.
     * <p>
     * Given vertex index {@code vi},<br>
     * {@code vertices[vi|} yields the vertex {@code v}.
     */
    protected final @NonNull V[] vertices;
    /**
     * Maps vertices the vertex indices.
     * <p>
     * Given vertex {@code v},<br>
     * {@code vertexToIndexMap.get(v)} yields the vertex index {@code vi}.
     */
    protected final @NonNull Map<V, Integer> vertexToIndexMap;

    /**
     * Creates a new instance from the specified graph.
     *
     * @param graph a graph
     */
    public ImmutableAttributed32BitIndexedBidiGraph(@NonNull AttributedIndexedBidiGraph<V, A> graph) {

        final int arrowCount = graph.getArrowCount();
        final int vertexCount = graph.getVertexCount();

        this.next = new int[arrowCount];
        this.prev = new int[arrowCount];

        @SuppressWarnings("unchecked")
        A[] uncheckedNextArrows = (A[]) new Object[arrowCount];
        this.nextArrows = uncheckedNextArrows;
        @SuppressWarnings("unchecked")
        A[] uncheckedPrevArrows = (A[]) new Object[arrowCount];
        this.prevArrows = uncheckedPrevArrows;
        this.nextOffset = new int[vertexCount];
        this.prevOffset = new int[vertexCount];
        @SuppressWarnings("unchecked")
        V[] uncheckedVertices = (V[]) new Object[vertexCount];
        this.vertices = uncheckedVertices;
        this.vertexToIndexMap = new HashMap<>(vertexCount);

        int nextOffset = 0;
        int prevOffset = 0;
        for (int vi = 0; vi < vertexCount; vi++) {
            this.nextOffset[vi] = nextOffset;
            V v = graph.getVertex(vi);
            this.vertices[vi] = v;
            vertexToIndexMap.put(v, vi);
            for (int i = 0, n = graph.getNextCount(vi); i < n; i++) {
                next[nextOffset] = graph.getNextAsInt(vi, i);
                this.nextArrows[nextOffset] = graph.getNextArrow(vi, i);
                nextOffset++;
            }
            for (int i = 0, n = graph.getPrevCount(vi); i < n; i++) {
                prev[prevOffset] = graph.getPrevAsInt(vi, i);
                this.prevArrows[prevOffset] = graph.getPrevArrow(vi, i);
                prevOffset++;
            }
        }
    }

    /**
     * Creates a new instance from the specified graph.
     *
     * @param graph a graph
     */
    public ImmutableAttributed32BitIndexedBidiGraph(@NonNull BidiGraph<V, A> graph) {

        final int arrowCount = graph.getArrowCount();
        final int vertexCount = graph.getVertexCount();

        this.next = new int[arrowCount];
        this.prev = new int[arrowCount];
        @SuppressWarnings("unchecked")
        A[] uncheckedArrows = (A[]) new Object[arrowCount];
        this.nextArrows = uncheckedArrows;
        @SuppressWarnings("unchecked")
        A[] uncheckedPrevArrows = (A[]) new Object[arrowCount];
        this.prevArrows = uncheckedPrevArrows;
        this.nextOffset = new int[vertexCount];
        this.prevOffset = new int[vertexCount];
        @SuppressWarnings("unchecked")
        V[] uncheckedVertices = (V[]) new Object[vertexCount];
        this.vertices = uncheckedVertices;
        this.vertexToIndexMap = new HashMap<>(vertexCount);

        {
            int vi = 0;
            for (V v : graph.getVertices()) {
                vertexToIndexMap.put(v, vi);
                vi++;
            }
        }

        {
            int nextOffset = 0;
            int prevOffset = 0;
            int vi = 0;
            for (V v : graph.getVertices()) {
                this.nextOffset[vi] = nextOffset;
                this.vertices[vi] = v;
                for (Arc<V, A> arc : graph.getNextArcs(v)) {
                    next[nextOffset] = vertexToIndexMap.get(arc.getEnd());
                    nextArrows[nextOffset] = arc.getArrow();
                    nextOffset++;
                }
                for (Arc<V, A> arc : graph.getPrevArcs(v)) {
                    prev[prevOffset] = vertexToIndexMap.get(arc.getStart());
                    prevArrows[prevOffset] = arc.getArrow();
                    prevOffset++;
                }
                vi++;
            }
        }
    }


    @Override
    public @NonNull A getArrow(int index) {
        return nextArrows[index];
    }

    public @NonNull A getArrow(int vertex, int index) {
        return nextArrows[getArrowIndex(vertex, index)];
    }

    @Override
    public int getArrowCount() {
        return next.length;
    }

    protected int getArrowIndex(int vi, int i) {
        if (i < 0 || i >= getNextCount(vi)) {
            throw new IllegalArgumentException("i(" + i + ") < 0 || i >= " + getNextCount(vi));
        }
        return nextOffset[vi] + i;
    }

    @Override
    public @NonNull V getNext(@NonNull V v, int i) {
        return vertices[getNextAsInt(vertexToIndexMap.get(v), i)];
    }

    @Override
    public @NonNull A getNextArrow(int v, int i) {
        Preconditions.checkIndex(i, getNextCount(v));
        return nextArrows[nextOffset[v] + i];
    }

    @Override
    public @NonNull A getNextArrow(@NonNull V v, int i) {
        return getNextArrow(getVertexIndex(v), i);
    }

    @Override
    public int getNextAsInt(int v, int i) {
        Preconditions.checkIndex(i, getNextCount(v));
        return next[nextOffset[v] + i];
    }

    @Override
    public int getNextArrowAsInt(int v, int i) {
        return getNextAsInt(v, i);
    }

    @Override
    public int getNextCount(int v) {
        final int offset = nextOffset[v];
        final int offset2 = (v == nextOffset.length - 1) ? nextOffset.length : nextOffset[v + 1];
        return offset2 - offset;
    }

    @Override
    public int getNextCount(@NonNull V v) {
        return getNextCount(vertexToIndexMap.get(v));
    }

    @Override
    public @NonNull V getPrev(@NonNull V vertex, int i) {
        return vertices[getPrevAsInt(vertexToIndexMap.get(vertex), i)];
    }

    @Override
    public A getPrevArrow(int vi, int i) {
        Preconditions.checkIndex(i, getPrevCount(vi));
        return prevArrows[prevOffset[vi] + i];
    }

    @Override
    public @NonNull A getPrevArrow(@NonNull V v, int i) {
        return getPrevArrow(getVertexIndex(v), i);
    }

    @Override
    public int getPrevAsInt(int v, int i) {
        Preconditions.checkIndex(i, getPrevCount(v));
        return prev[prevOffset[v] + i];
    }

    @Override
    public int getPrevArrowAsInt(int v, int i) {
        return getPrevAsInt(v, i);
    }

    @Override
    public int getPrevCount(@NonNull V vertex) {
        return getPrevCount(vertexToIndexMap.get(vertex));
    }

    @Override
    public int getPrevCount(int v) {
        final int offset = prevOffset[v];
        final int offset2 = (v == prevOffset.length - 1) ? prevOffset.length : prevOffset[v + 1];
        return offset2 - offset;
    }

    @Override
    public @NonNull V getVertex(int index) {
        return vertices[index];
    }

    @Override
    public int getVertexCount() {
        return nextOffset.length;
    }

    @Override
    public int getVertexIndex(V vertex) {
        Integer index = vertexToIndexMap.get(vertex);
        return index == null ? -1 : index;
    }

    @Override
    public @NonNull Set<V> getVertices() {
        return Collections.unmodifiableSet(vertexToIndexMap.keySet());

    }

    @Override
    public @NonNull IntEnumerator nextVerticesEnumerator(int v) {
        final int offset = nextOffset[v];
        final int nextOffset = (v == this.nextOffset.length - 1) ? this.next.length : this.nextOffset[v + 1];
        return new IntArrayEnumerator(this.next, offset, nextOffset);
    }
}
