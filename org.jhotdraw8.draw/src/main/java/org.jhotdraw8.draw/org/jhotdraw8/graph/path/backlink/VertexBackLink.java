package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;

import java.util.ArrayDeque;
import java.util.function.Function;

/**
 * Represents a vertex back link with cost and depth.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class VertexBackLink<V, C extends Number & Comparable<C>> extends AbstractBackLink<VertexBackLink<V, C>, C> {
    private final @NonNull V vertex;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex data
     * @param parent the parent back link
     * @param cost   the cumulated cost of this back link. Must be zero if parent is null.
     */
    public VertexBackLink(@NonNull V vertex, @Nullable VertexBackLink<V, C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = vertex;
    }

    public static <V, C extends Number & Comparable<C>, X> @Nullable OrderedPair<ImmutableList<X>, C> toVertexSequence(@Nullable VertexBackLink<V, C> node,
                                                                                                                       @NonNull Function<VertexBackLink<V, C>, X> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<X> deque = new ArrayDeque<>();
        for (VertexBackLink<V, C> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }

    public @NonNull V getVertex() {
        return vertex;
    }

}
