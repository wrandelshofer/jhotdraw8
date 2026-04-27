/*
 * @(#)AttributedIndexedDirectedGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;


import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/// This interface provides read-only indexed access to a directed graph `G = (V, A)` with
/// vertex and arrow attributes of the generic types `V` and `A`.
///
///   - `G` is a tuple `(V, A)`.
///   - `V` is the set of vertices with elements `v_i ∈ V. i ∈{0, ..., vertexCount - 1}`.
///   - `A` is the set of ordered pairs with elements `(v_i, v_j)_k ∈ A. i,j ∈{0, ..., vertexCount - 1}. k ∈{0, ..., arrowCount - 1}`.
///
///
/// This interface provides access to the following data in addition to the interface [IndexedDirectedGraph]:
///
///   - The vertex `v_i ∈ V` .
///   - The arrow `a_k ∈ A`.
///   - The arrow `a_(i,j) ∈ A`.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
public interface AttributedIndexedDirectedGraph<V, A> extends IndexedDirectedGraph {
    /// Returns the data of the specified arrow.
    ///
    /// @param index index of arrow
    /// @return arrow data
    A getArrow(int index);

    /// Returns the data of the specified vertex.
    ///
    /// @param index index of vertex
    /// @return vertex data
    V getVertex(int index);

    /// Returns the index of the vertex.
    ///
    /// @param vertex a vertex
    /// @return index of vertex
    int getVertexIndex(V vertex);

    /// Returns the specified successor (next) arrow of the specified vertex.
    ///
    /// @param v     a vertex
    /// @param index index of next arrow
    /// @return the specified arrow
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

    /// Returns the direct successor vertices of the specified vertex.
    ///
    /// @param vertexIndex a vertex
    /// @return a collection view on the direct successor vertices of vertex
    /// with the arrow pointing to the vertex
    default Collection<Map.Entry<Integer, A>> getNextIntEntries(int vertexIndex) {
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
            public Map.Entry<Integer, A> next() {
                int i = index++;
                return new AbstractMap.SimpleEntry<>(
                        getNextAsInt(vertex, i),
                        getNextArrow(vertex, i)
                );
            }
        }

        return new AbstractCollection<>() {
            @Override
            public Iterator<Map.Entry<Integer, A>> iterator() {
                return new NextVertexAndArrowIterator(vertexIndex);
            }

            @Override
            public int size() {
                return getNextCount(vertexIndex);
            }
        };
    }

}
