/*
 * @(#)IndexedDirectedGraphWrapper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;


import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides a {@link DirectedGraph} facade for an {@link IndexedDirectedGraph}.
 */
public class IndexedDirectedGraphDirectedGraphFacade implements DirectedGraph<Integer, Integer> {
    private final IndexedDirectedGraph graph;

    public IndexedDirectedGraphDirectedGraphFacade(IndexedDirectedGraph graph) {
        this.graph = graph;
    }

    @Override
    public int getArrowCount() {
        return graph.getArrowCount();
    }

    @Override
    public Integer getVertex(int index) {
        return index;
    }

    @Override
    public Integer getNext(Integer v, int index) {
        return graph.getNextAsInt(v, index);
    }

    @Override
    public Integer getNextArrow(Integer v, int index) {
        return graph.getNextArrowAsInt(v, index);
    }

    @Override
    public int getNextCount(Integer v) {
        return graph.getNextCount(v);
    }

    @Override
    public Set<Integer> getVertices() {
        LinkedHashSet<Integer> set = new LinkedHashSet<>(graph.getVertexCount());
        for (int i = 0, n = graph.getVertexCount(); i < n; i++) {
            set.add(i);
        }
        return set;
    }
}
