/*
 * @(#)ImmutableAttributed32BitIndexedDirectedGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.enumerator.IntArraySpliterator;
import org.jhotdraw8.collection.enumerator.IntSpliterator;

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
 * matrices (CRS). A directed graph is represented with 4 arrays:
 * {@code nextOffset}, {@code next}, {@code nextArrows}, and {@code vertices}.
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
 *     <dt>{@code vertices}</dt>
 *     <dd>Holds for each vertex {@code v} the data associated to the vertex.</dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public class ImmutableAttributed32BitIndexedDirectedGraph<V, A> implements AttributedIndexedDirectedGraph<V, A>, DirectedGraph<V, A> {

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
    protected final int @NonNull [] next;

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
    protected final int @NonNull [] nextOffset;

    /**
     * Holds the arrow objects.
     * <p>
     * The arrows are stored in consecutive runs for each vertex,
     * starting at the offset given by {@code nextOffset}.
     * <p>
     * See {@link #next}.
     */
    protected final @NonNull A @NonNull [] nextArrows;
    /**
     * Holds the vertex objects.
     * <p>
     * Given vertex index {@code vi},<br>
     * {@code vertices[vi|} yields the vertex {@code v}.
     */
    protected final @NonNull V @NonNull [] vertices;
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
    public ImmutableAttributed32BitIndexedDirectedGraph(@NonNull AttributedIndexedDirectedGraph<V, A> graph) {

        final int arrowCount = graph.getArrowCount();
        final int vertexCount = graph.getVertexCount();

        this.next = new int[arrowCount];

        @SuppressWarnings("unchecked")
        A[] uncheckedArrows = (A[]) new Object[arrowCount];
        this.nextArrows = uncheckedArrows;
        this.nextOffset = new int[vertexCount];
        @SuppressWarnings("unchecked")
        V[] uncheckedVertices = (V[]) new Object[vertexCount];
        this.vertices = uncheckedVertices;
        this.vertexToIndexMap = new HashMap<>(vertexCount);

        int offset = 0;
        for (int vi = 0; vi < vertexCount; vi++) {
            nextOffset[vi] = offset;
            V v = graph.getVertex(vi);
            this.vertices[vi] = v;
            vertexToIndexMap.put(v, vi);
            for (int i = 0, n = graph.getNextCount(vi); i < n; i++) {
                next[offset] = graph.getNextAsInt(vi, i);
                this.nextArrows[offset] = graph.getNextArrow(vi, i);
                offset++;
            }
        }
    }

    /**
     * Creates a new instance from the specified graph.
     *
     * @param graph a graph
     */
    public ImmutableAttributed32BitIndexedDirectedGraph(@NonNull DirectedGraph<V, A> graph) {

        final int arrowCapacity = graph.getArrowCount();
        final int vertexCapacity = graph.getVertexCount();

        this.next = new int[arrowCapacity];
        @SuppressWarnings("unchecked")
        A[] uncheckedArrows = (A[]) new Object[arrowCapacity];
        this.nextArrows = uncheckedArrows;
        this.nextOffset = new int[vertexCapacity];
        @SuppressWarnings("unchecked")
        V[] uncheckedVertices = (V[]) new Object[vertexCapacity];
        this.vertices = uncheckedVertices;
        this.vertexToIndexMap = new HashMap<>(vertexCapacity);

        {
            int vi = 0;
            for (V v : graph.getVertices()) {
                vertexToIndexMap.put(v, vi);
                vi++;
            }
        }

        {
            int offset = 0;
            int vi = 0;
            for (V v : graph.getVertices()) {

                nextOffset[vi] = offset;
                this.vertices[vi] = v;
                for (Arc<V, A> arc : graph.getNextArcs(v)) {
                    next[offset] = vertexToIndexMap.get(arc.getEnd());
                    nextArrows[offset] = arc.getArrow();
                    offset++;
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
        if (i < 0 || i >= getNextCount(v)) {
            throw new IllegalArgumentException("i(" + i + ") < 0 || i >= " + getNextCount(v));
        }
        return nextArrows[nextOffset[v] + i];
    }

    @Override
    public int getNextArrowAsInt(int v, int i) {
        return getNextAsInt(v, i);
    }

    @Override
    public @NonNull A getNextArrow(@NonNull V v, int i) {
        return this.getNextArrow(getVertexIndex(v), i);
    }

    @Override
    public int getNextAsInt(int v, int i) {
        if (i < 0 || i >= getNextCount(v)) {
            throw new IllegalArgumentException("i(" + i + ") < 0 || i >= " + getNextCount(v));
        }
        return next[nextOffset[v] + i];
    }

    @Override
    public int getNextCount(int v) {
        final int offset = nextOffset[v];
        final int nextOffset = (v == this.nextOffset.length - 1) ? this.next.length : this.nextOffset[v + 1];
        return nextOffset - offset;
    }

    @Override
    public int getNextCount(@NonNull V v) {
        return getNextCount(vertexToIndexMap.get(v));
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
    public @NonNull IntSpliterator nextVerticesEnumerator(int v) {
        final int offset = nextOffset[v];
        final int nextOffset = (v == this.nextOffset.length - 1) ? this.next.length : this.nextOffset[v + 1];
        return new IntArraySpliterator(this.next, offset, nextOffset);
    }
}
