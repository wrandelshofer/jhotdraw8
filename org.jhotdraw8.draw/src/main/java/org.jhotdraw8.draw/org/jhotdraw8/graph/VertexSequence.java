/*
 * @(#)VertexPath.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.ReadOnlyCollection;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a vertex sequence of a walk through a graph.
 * <p>
 * The same vertex may occur more than once in the sequence.
 *
 * @author Werner Randelshofer
 */
public class VertexSequence<V> {

    private final @NonNull ImmutableList<V> vertices;

    public VertexSequence(@NonNull Collection<? extends V> elements) {
        this.vertices = ImmutableLists.ofCollection(elements);
    }

    public VertexSequence(@NonNull ReadOnlyCollection<V> elements) {
        this.vertices = ImmutableLists.ofCollection(elements);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public VertexSequence(@NonNull V... elements) {
        this.vertices = ImmutableLists.of(elements);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VertexSequence<?> other = (VertexSequence<?>) obj;
        return Objects.equals(this.vertices, other.vertices);
    }

    public V getSecondToLastVertex() {
        return vertices.get(vertices.size() - 2);
    }

    public @NonNull ImmutableList<V> getVertices() {
        return vertices;
    }

    public V getFirstVertex() {
        return vertices.get(0);
    }

    public V getSecondVertex() {
        return vertices.get(1);
    }

    public V getLastVertex() {
        return vertices.get(vertices.size() - 1);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.vertices);
        return hash;
    }

    public int indexOf(V v) {
        return vertices.indexOf(v);
    }

    public boolean isEmpty() {
        return vertices.isEmpty();
    }

    public int numOfVertices() {
        return vertices.size();
    }

    @Override
    public @NonNull String toString() {
        return "VertexPath{" + vertices + '}';
    }

    /**
     * Creates a new VertexPath with the specified vertices.
     *
     * @param <VV>     the vertex type
     * @param vertices the vertices
     * @return the vertex path
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static @NonNull <VV> VertexSequence<VV> of(VV... vertices) {
        return new VertexSequence<>(vertices);
    }

    public @NonNull VertexSequence<V> appendPath(@NonNull VertexSequence<V> nextPath) {
        if (isEmpty()) {
            return nextPath;
        }
        if (nextPath.isEmpty()) {
            return this;
        }
        if (!getLastVertex().equals(nextPath.getFirstVertex())) {
            throw new IllegalArgumentException("Cannot join paths. This last vertex: " + this.getLastVertex() + " next first vertex: " + nextPath.getFirstVertex());
        }
        return new VertexSequence<V>(ImmutableLists.addAll(this.vertices.readOnlySubList(0, numOfVertices() - 1), nextPath.vertices));
    }

}
