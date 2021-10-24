package org.jhotdraw8.graph;

class SimpleMutableDirectedGraphTest extends AbstractMutableDirectedGraphTest {
    @Override
    protected MutableDirectedGraph<Integer, Character> newInstance() {
        return new SimpleMutableDirectedGraph<>();
    }

    @Override
    protected MutableDirectedGraph<Integer, Character> newInstance(DirectedGraph<Integer, Character> g) {
        return new SimpleMutableDirectedGraph<>(g);
    }
}