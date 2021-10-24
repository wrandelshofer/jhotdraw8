package org.jhotdraw8.graph;

public class SimpleMutableIntBidiGraphTest extends AbstractMutableIntBidiGraphTest {
    @Override
    protected MutableIntBidiGraph newInstance(int vertexCount) {
        return new SimpleMutableIntBidiGraph(vertexCount);
    }

    @Override
    protected MutableIntDirectedGraph newInstance(MutableIntDirectedGraph g) {
        return new SimpleMutableIntBidiGraph(g);
    }

    @Override
    protected MutableIntBidiGraph newInstance(MutableIntBidiGraph g) {
        return new SimpleMutableIntBidiGraph(g);
    }

    @Override
    protected MutableIntBidiGraph newInstance() {
        return new SimpleMutableIntBidiGraph();
    }
}
