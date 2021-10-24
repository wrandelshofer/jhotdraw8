package org.jhotdraw8.graph;

public class SimpleMutableIntDirectedGraph
        extends AbstractMutableIntDirectedGraph
        implements MutableIntDirectedGraph {

    public SimpleMutableIntDirectedGraph() {
        super();
    }

    public SimpleMutableIntDirectedGraph(int vertexCount) {
        super(vertexCount);
    }

    public SimpleMutableIntDirectedGraph(IntDirectedGraph g) {
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
