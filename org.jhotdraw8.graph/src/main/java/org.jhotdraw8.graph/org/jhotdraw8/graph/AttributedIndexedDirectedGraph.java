/*
 * @(#)AttributedIndexedDirectedGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

import java.util.*;

/**
 * This interface provides read-only indexed access to a directed graph {@code G = (V, A) } with
 * vertex and arrow attributes of the generic types {@code V} and {@code A}.
 * <ul>
 * <li>{@code G} is a tuple {@code (V, A) }.</li>
 * <li>{@code V} is the set of vertices with elements {@code v_i ∈ V. i ∈ {0, ..., vertexCount - 1} }.</li>
 * <li>{@code A} is the set of ordered pairs with elements {@code  (v_i, v_j)_k ∈ A. i,j ∈ {0, ..., vertexCount - 1}. k ∈ {0, ..., arrowCount - 1} }.</li>
 * </ul>
 * <p>
 * This interface provides access to the following data in addition to the interface {@link IndexedDirectedGraph}:
 * <ul>
 * <li>The vertex {@code v_i ∈ V} .</li>
 * <li>The arrow {@code a_k ∈ A}.</li>
 * <li>The arrow {@code a_(i,j) ∈ A}.</li>
 * </ul>
 *
 * @author wr
 */
public interface AttributedIndexedDirectedGraph<V, A> extends IndexedDirectedGraph {
    /**
     * Returns the data of the specified arrow.
     *
     * @param index index of arrow
     * @return arrow data
     */
    A getArrow(int index);

    /**
     * Returns the data of the specified vertex.
     *
     * @param index index of vertex
     * @return vertex data
     */
    @NonNull V getVertex(int index);

    /**
     * Returns the index of the vertex.
     *
     * @param vertex a vertex
     * @return index of vertex
     */
    int getVertexIndex(V vertex);

    /**
     * Returns the specified successor (next) arrow of the specified vertex.
     *
     * @param v     a vertex
     * @param index index of next arrow
     * @return the specified arrow
     */
    A getNextArrow(int v, int index);

        /*
    /**
     * Returns the arrow if b is next of a.
     *
     * @param a a vertex
     * @param b a vertex
     * @return the arrow or null if b is not next of a
     * /
        default A findArrow(int a, int b) {
        int index = findIndexOfNext(a, b);
        return index < 0 ? null : getArrow(a, index);
    }*/

    /**
     * Returns the direct successor vertices of the specified vertex.
     *
     * @param vertexIndex a vertex
     * @return a collection view on the direct successor vertices of vertex
     * with the arrow pointing to the vertex
     */
    default @NonNull Collection<Map.Entry<Integer, A>> getNextIntEntries(int vertexIndex) {
        class NextVertexAndArrowIterator implements Iterator<Map.Entry<Integer, A>> {

            private int index;
            private final int vertex;
            private final int nextCount;

            public NextVertexAndArrowIterator(int vertex) {
                this.vertex = vertex;
                this.nextCount = getNextCount(vertex);
            }

            @Override
            public boolean hasNext() {
                return index < nextCount;
            }

            @Override
            public Map.@NonNull Entry<Integer, A> next() {
                int i = index++;
                return new AbstractMap.SimpleEntry<>(
                        getNextAsInt(vertex, i),
                        getNextArrow(vertex, i)
                );
            }
        }

        return new AbstractCollection<Map.Entry<Integer, A>>() {
            @Override
            public @NonNull Iterator<Map.Entry<Integer, A>> iterator() {
                return new NextVertexAndArrowIterator(vertexIndex);
            }

            @Override
            public int size() {
                return getNextCount(vertexIndex);
            }
        };
    }

}
