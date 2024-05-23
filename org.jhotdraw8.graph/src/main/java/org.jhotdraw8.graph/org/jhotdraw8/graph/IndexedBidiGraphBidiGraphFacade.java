/*
 * @(#)IndexedBidiGraphWrapper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;


import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides a {@link BidiGraph} facade for an {@link IndexedBidiGraph}.
 */
public class IndexedBidiGraphBidiGraphFacade implements BidiGraph<Integer, Integer> {
    private final IndexedBidiGraph graph;

    public IndexedBidiGraphBidiGraphFacade(IndexedBidiGraph graph) {
        this.graph = graph;
    }

    @Override
    public int getArrowCount() {
        return graph.getArrowCount();
    }

    @Override
    public Integer getPrev(Integer vertex, int index) {
        return graph.getPrevAsInt(vertex, index);
    }

    @Override
    public Integer getPrevArrow(Integer vertex, int index) {
        return graph.getPrevArrowAsInt(vertex, index);
    }

    @Override
    public int getPrevCount(Integer vertex) {
        return graph.getPrevCount(vertex);
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
    public int findIndexOfPrev(Integer v, Integer u) {
        return graph.findIndexOfPrevAsInt(v, u);
    }

    @Override
    public int findIndexOfNext(Integer v, Integer u) {
        return graph.findIndexOfNextAsInt(v, u);
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
