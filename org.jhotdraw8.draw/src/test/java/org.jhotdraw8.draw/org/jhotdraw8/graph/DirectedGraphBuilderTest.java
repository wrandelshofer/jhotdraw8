package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

class DirectedGraphBuilderTest extends AbstractDirectedGraphBuilderTest {
    @NonNull
    protected DirectedGraphBuilder<String, Integer> createInstance() {
        DirectedGraphBuilder<String, Integer> instance = new DirectedGraphBuilder<>();
        return instance;
    }
}