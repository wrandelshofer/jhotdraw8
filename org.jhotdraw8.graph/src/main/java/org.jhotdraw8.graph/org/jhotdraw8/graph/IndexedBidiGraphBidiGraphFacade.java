/*
 * @(#)IndexedBidiGraphWrapper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides a {@link BidiGraph} facade for an {@link IndexedBidiGraph}.
 */
public class IndexedBidiGraphBidiGraphFacade implements BidiGraph<Integer, Integer> {
    private final @NonNull IndexedBidiGraph graph;

    public IndexedBidiGraphBidiGraphFacade(@NonNull IndexedBidiGraph graph) {
        this.graph = graph;
    }

    @Override
    public int getArrowCount() {
        return graph.getArrowCount();
    }

    @Override
    public @NonNull Integer getPrev(@NonNull Integer vertex, int index) {
        return graph.getPrevAsInt(vertex, index);
    }

    @Override
    public @NonNull Integer getPrevArrow(@NonNull Integer vertex, int index) {
        return graph.getPrevArrowAsInt(vertex, index);
    }

    @Override
    public int getPrevCount(@NonNull Integer vertex) {
        return graph.getPrevCount(vertex);
    }

    @Override
    public @NonNull Integer getVertex(int index) {
        return index;
    }

    @Override
    public @NonNull Integer getNext(@NonNull Integer v, int index) {
        return graph.getNextAsInt(v, index);
    }

    @Override
    public @NonNull Integer getNextArrow(@NonNull Integer v, int index) {
        return graph.getNextArrowAsInt(v, index);
    }

    @Override
    public int findIndexOfPrev(@NonNull Integer v, @NonNull Integer u) {
        return graph.findIndexOfPrevAsInt(v, u);
    }

    @Override
    public int findIndexOfNext(@NonNull Integer v, @NonNull Integer u) {
        return graph.findIndexOfNextAsInt(v, u);
    }

    @Override
    public int getNextCount(@NonNull Integer v) {
        return graph.getNextCount(v);
    }

    @Override
    public @NonNull Set<Integer> getVertices() {
        LinkedHashSet<Integer> set = new LinkedHashSet<>(graph.getVertexCount());
        for (int i = 0, n = graph.getVertexCount(); i < n; i++) {
            set.add(i);
        }
        return set;
    }
}
