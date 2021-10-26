package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

public class SimpleMutableDirectedGraph<V, A> extends AbstractMutableDirectedGraph<V, A>
        implements MutableDirectedGraph<V, A> {


    public SimpleMutableDirectedGraph() {
        this(0, 0, false);
    }

    public SimpleMutableDirectedGraph(int vertexCapacity, int arrowCapacity, boolean identityMap) {
        super(vertexCapacity, arrowCapacity, identityMap);
    }

    public SimpleMutableDirectedGraph(DirectedGraph<V, A> g) {
        super(g);

    }

    @Override
    public void addVertex(@NonNull V v) {
        buildAddVertex(v);
    }


    @Override
    public void removeVertex(@NonNull V v) {
        buildRemoveVertex(v);
    }

    @Override
    public void addArrow(@NonNull V v, @NonNull V u, A a) {
        buildAddArrow(v, u, a);
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u, A a) {
        buildRemoveArrow(v, u, a);
    }

    @Override
    public void removeArrow(@NonNull V v, @NonNull V u) {
        buildRemoveArrow(v, u);
    }

    @Override
    public void removeArrowAt(@NonNull V v, int k) {
        buildRemoveArrowAt(v, k);
    }

}
