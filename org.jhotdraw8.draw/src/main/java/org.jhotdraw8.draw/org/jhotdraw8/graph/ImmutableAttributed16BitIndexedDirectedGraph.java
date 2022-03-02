/*
 * @(#)UShortImmutableDirectedGraph.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IntCharArrayEnumeratorSpliterator;
import org.jhotdraw8.collection.IntEnumeratorSpliterator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ImmutableDirectedGraph. Uses char-arrays for storage.
 * <p>
 * Supports up to {@code 2^16 - 1} vertices.
 * <p>
 * Uses a representation that is similar to a compressed row storage for
 * matrices (CRS). A directed graph is represented with 4 arrays:
 * {@code nextOffset}, {@code next}, {@code nextArrows}, {@cde vertices}
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
public class ImmutableAttributed16BitIndexedDirectedGraph<V, A> implements AttributedIndexedDirectedGraph<V, A>, DirectedGraph<V, A> {

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
    protected final @NonNull char[] next;

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
    protected final @NonNull char[] nextOffset;

    /**
     * Holds the arrow objects.
     * <p>
     * The arrows are stored in consecutive runs for each vertex,
     * starting at the offset given by {@code nextOffset}.
     * <p>
     * See {@link #next}.
     */
    protected final @NonNull A[] nextArrows;
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
    protected final @NonNull Map<V, Character> vertexToIndexMap;

    /**
     * Creates a new instance from the specified graph.
     *
     * @param graph a graph
     */
    public ImmutableAttributed16BitIndexedDirectedGraph(@NonNull AttributedIndexedDirectedGraph<V, A> graph) {
        final int arrowCount = graph.getArrowCount();
        final int vertexCount = graph.getVertexCount();

        if (arrowCount + vertexCount >= Character.MAX_VALUE) {
            throw new IllegalArgumentException("arrowCount+vertexCount >= " + Character.MAX_VALUE + ". arrowCount=" + arrowCount + ", vertexCount=" + vertexCount);
        }

        this.next = new char[arrowCount];

        @SuppressWarnings("unchecked")
        A[] uncheckedArrows = (A[]) new Object[arrowCount];
        this.nextArrows = uncheckedArrows;
        this.nextOffset = new char[vertexCount];
        @SuppressWarnings("unchecked")
        V[] uncheckedVertices = (V[]) new Object[vertexCount];
        this.vertices = uncheckedVertices;
        this.vertexToIndexMap = new HashMap<>(vertexCount);

        char offset = 0;
        for (char vi = 0; vi < vertexCount; vi++) {
            nextOffset[vi] = offset;
            V v = graph.getVertex(vi);
            this.vertices[vi] = v;
            vertexToIndexMap.put(v, vi);
            for (int i = 0, n = graph.getNextCount(vi); i < n; i++) {
                next[offset] = (char) graph.getNextAsInt(vi, i);
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
    public ImmutableAttributed16BitIndexedDirectedGraph(@NonNull DirectedGraph<V, A> graph) {
        final int arrowCount = graph.getArrowCount();
        final int vertexCount = graph.getVertexCount();
        if (arrowCount + vertexCount >= Character.MAX_VALUE) {
            throw new IllegalArgumentException("arrowCount+vertexCount >= " + Character.MAX_VALUE + ". arrowCount=" + arrowCount + ", vertexCount=" + vertexCount);
        }

        this.next = new char[arrowCount];
        @SuppressWarnings("unchecked")
        A[] uncheckedArrows = (A[]) new Object[arrowCount];
        this.nextArrows = uncheckedArrows;
        this.nextOffset = new char[vertexCount];
        @SuppressWarnings("unchecked")
        V[] uncheckedVertices = (V[]) new Object[vertexCount];
        this.vertices = uncheckedVertices;
        this.vertexToIndexMap = new HashMap<>(vertexCount);

        //    Map<V, Integer> vertexToIndexMap = new HashMap<>(vertexCapacity);
        {
            char vi = 0;
            for (V v : graph.getVertices()) {
                vertexToIndexMap.put(v, vi);
                vi++;
            }
        }

        {
            char offset = 0;
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
    public @NonNull V getNext(@NonNull V vertex, int i) {
        return vertices[getNextAsInt(vertexToIndexMap.get(vertex), i)];
    }

    @Override
    public @NonNull A getNextArrow(int vi, int i) {
        if (i < 0 || i >= getNextCount(vi)) {
            throw new IllegalArgumentException("i(" + i + ") < 0 || i >= " + getNextCount(vi));
        }
        return nextArrows[nextOffset[vi] + i];
    }

    @Override
    public @NonNull A getNextArrow(@NonNull V v, int i) {
        return this.getNextArrow(getVertexIndex(v), i);
    }

    @Override
    public int getNextAsInt(int vidx, int i) {
        if (i < 0 || i >= getNextCount(vidx)) {
            throw new IllegalArgumentException("i(" + i + ") < 0 || i >= " + getNextCount(vidx));
        }
        return next[nextOffset[vidx] + i];
    }

    @Override
    public int getNextCount(int vidx) {
        final int offset = nextOffset[vidx];
        final int nextOffset = (vidx == this.nextOffset.length - 1) ? this.next.length : this.nextOffset[vidx + 1];
        return nextOffset - offset;
    }

    @Override
    public int getNextCount(@NonNull V vertex) {
        return getNextCount(vertexToIndexMap.get(vertex));
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
        Character index = vertexToIndexMap.get(vertex);
        return index == null ? -1 : index;
    }

    @Override
    public @NonNull Set<V> getVertices() {
        return Collections.unmodifiableSet(vertexToIndexMap.keySet());

    }

    @Override
    public @NonNull IntEnumeratorSpliterator nextVerticesSpliterator(int vi) {
        final int offset = nextOffset[vi];
        final int nextOffset = (vi == this.nextOffset.length - 1) ? this.next.length : this.nextOffset[vi + 1];
        return new IntCharArrayEnumeratorSpliterator(offset, nextOffset, this.next);
    }
}
