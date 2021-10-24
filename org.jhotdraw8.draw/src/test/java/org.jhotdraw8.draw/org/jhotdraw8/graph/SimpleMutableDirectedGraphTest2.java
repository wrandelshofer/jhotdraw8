package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

class SimpleMutableDirectedGraphTest2 extends AbstractMutableDirectedGraphTest0 {
    @NonNull
    protected DirectedGraphBuilder<String, Integer> createInstance() {
        DirectedGraphBuilder<String, Integer> instance = new DirectedGraphBuilder<>();
        return instance;
    }
}