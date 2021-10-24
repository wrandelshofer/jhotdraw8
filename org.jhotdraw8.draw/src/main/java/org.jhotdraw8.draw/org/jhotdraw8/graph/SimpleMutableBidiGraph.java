package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

public class SimpleMutableBidiGraph<V, A> extends AbstractMutableBidiGraph<V, A>
        implements MutableBidiGraph<V, A> {

    public SimpleMutableBidiGraph() {
    }

    public SimpleMutableBidiGraph(DirectedGraph<V, A> g) {
        super(g);
    }


    @Override
    public void addArrow(@NonNull V v, @NonNull V u, A data) {
        buildAddArrow(v, u, data);
    }

    @Override
    public void addVertex(@NonNull V v) {
        buildAddVertex(v);
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u, A data) {
        buildRemoveArrow(v, u, data);
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u) {
        buildRemoveArrow(v, u);
    }

    @Override
    public void removeArrowAt(@NonNull V v, int k) {
        buildRemoveArrowAt(v, k);
    }

    @Override
    public void removeVertex(@NonNull V v) {
        buildRemoveVertex(v);
    }

}
