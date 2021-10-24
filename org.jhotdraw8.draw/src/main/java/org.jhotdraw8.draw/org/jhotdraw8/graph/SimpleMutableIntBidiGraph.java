package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

public class SimpleMutableIntBidiGraph
        extends AbstractMutableIntBidiGraph
        implements MutableIntBidiGraph, IntBidiGraph {

    public SimpleMutableIntBidiGraph() {
        this(0);
    }

    public SimpleMutableIntBidiGraph(int vertexCount) {
        super(vertexCount);
    }

    public SimpleMutableIntBidiGraph(@NonNull IntDirectedGraph g) {
        super(g);
    }

    @Override
    public void addVertex() {
        buildAddVertex();
    }

    @Override
    public void removeVertex(int vidx) {
        buildRemoveVertex(vidx);
    }

    @Override
    public void addArrow(int vidx, int uidx) {
        buildAddArrow(vidx, uidx);
    }

    @Override
    public void removeArrow(int vidx, int uidx) {
        buildRemoveArrow(vidx, uidx);
    }

    @Override
    public void removeArrowAt(int vidx, int k) {
        buildRemoveArrowAt(vidx, k);

    }
}
