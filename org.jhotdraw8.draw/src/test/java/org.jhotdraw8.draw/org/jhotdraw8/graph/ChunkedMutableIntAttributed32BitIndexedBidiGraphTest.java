package org.jhotdraw8.graph;

public class ChunkedMutableIntAttributed32BitIndexedBidiGraphTest extends AbstractMutableIndexedBidiGraphTest {
    @Override
    protected MutableIndexedBidiGraph newInstance(int maxArity) {
        return new ChunkedMutableIntAttributed32BitIndexedBidiGraph();
    }

    @Override
    protected boolean supportsMultigraph() {
        return false;
    }
}
