package org.jhotdraw8.graph;

public class MutableIntAttributed16BitIndexedBidiGraphTest extends AbstractMutableIndexedBidiGraphTest {
    @Override
    protected MutableIndexedBidiGraph newInstance(int maxArity) {
        return new MutableIntAttributed16BitIndexedBidiGraph(0, maxArity);
    }

    @Override
    protected boolean supportsMultigraph() {
        return true;
    }
}
