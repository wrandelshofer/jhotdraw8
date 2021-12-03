package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;

import java.util.ArrayDeque;
import java.util.function.Function;

/**
 * Represents an indexed vertex back link with cost and depth.
 *
 * @param <C> the cost number type
 */
public class IndexedVertexBackLinkWithCost<C extends Number & Comparable<C>> extends AbstractBackLinkWithCost<IndexedVertexBackLinkWithCost<C>, C> {

    final int vertex;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex index
     * @param parent the parent back link
     * @param cost   the cumulated cost of this back link. Must be zero if parent is null.
     */
    public IndexedVertexBackLinkWithCost(int vertex, @Nullable IndexedVertexBackLinkWithCost<C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = vertex;
    }


    public int getVertex() {
        return vertex;
    }

    public static <XX, CC extends Number & Comparable<CC>> @Nullable OrderedPair<ImmutableList<XX>, CC> toVertexSequence(@Nullable IndexedVertexBackLinkWithCost<CC> node,
                                                                                                                         @NonNull Function<IndexedVertexBackLinkWithCost<CC>, XX> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<XX> deque = new ArrayDeque<>();
        for (IndexedVertexBackLinkWithCost<CC> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }

}
