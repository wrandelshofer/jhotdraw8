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
public class VertexBackLinkWithCost<V, C extends Number & Comparable<C>> extends AbstractBackLinkWithCost<VertexBackLinkWithCost<V, C>, C> {
    private final @NonNull V vertex;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex data
     * @param parent the parent back link
     * @param cost   the cumulated cost of this back link. Must be zero if parent is null.
     */
    public VertexBackLinkWithCost(@NonNull V vertex, @Nullable VertexBackLinkWithCost<V, C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = vertex;
    }

    public @NonNull V getVertex() {
        return vertex;
    }

    /**
     * Converts an {@link VertexBackLinkWithCost} into a vertex sequence.
     *
     * @param node            the {@link VertexBackLinkWithCost}
     * @param mappingFunction the mapping function
     * @param <VV>            the vertex data type
     * @param <CC>            the cost number type
     * @param <XX>            the vertex sequence element type
     * @return the vertex sequence
     */
    public static <VV, CC extends Number & Comparable<CC>, XX> @Nullable OrderedPair<ImmutableList<XX>, CC> toVertexSequence(
            @Nullable VertexBackLinkWithCost<VV, CC> node,
            @NonNull Function<VertexBackLinkWithCost<VV, CC>, XX> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<XX> deque = new ArrayDeque<>();
        for (VertexBackLinkWithCost<VV, CC> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }

}
