package org.jhotdraw8.graph;

public class SimpleMutableIntDirectedGraphTest extends AbstractMutableIntDirectedGraphTest {
    @Override
    protected MutableIntDirectedGraph newInstance() {
        return new SimpleMutableIntDirectedGraph();
    }

    @Override
    protected MutableIntDirectedGraph newInstance(int vertexCount) {
        return new SimpleMutableIntDirectedGraph(vertexCount);
    }

    @Override
    protected MutableIntDirectedGraph newInstance(MutableIntDirectedGraph g) {
        return new SimpleMutableIntDirectedGraph(g);
    }
}
