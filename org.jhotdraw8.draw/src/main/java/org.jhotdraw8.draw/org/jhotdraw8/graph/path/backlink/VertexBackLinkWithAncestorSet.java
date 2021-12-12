package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.AddOnlyPersistentTrieSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

/**
 * Represents a vertex back link with depth and a set of ancestors.
 *
 * @param <V> the vertex data type
 */
public class VertexBackLinkWithAncestorSet<V> extends AbstractBackLink<VertexBackLinkWithAncestorSet<V>> {
    private final @NonNull V vertex;

    /**
     * This set contains the vertex of this back link and the vertices of all parent backlinks.
     */
    private final @NonNull AddOnlyPersistentTrieSet<V> ancestors;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex data
     * @param parent the parent back link
     */
    public VertexBackLinkWithAncestorSet(
            @NonNull V vertex,
            @Nullable VertexBackLinkWithAncestorSet<V> parent,
            @NonNull AddOnlyPersistentTrieSet<V> ancestors) {
        super(parent);
        this.vertex = vertex;
        this.ancestors = ancestors;
    }

    /**
     * Converts an {@link VertexBackLinkWithAncestorSet} to {@link VertexBackLinkWithCost}.
     *
     * @param node         the {@link VertexBackLinkWithAncestorSet}
     * @param zero         the zero cost value
     * @param costFunction the cost function
     * @param sumFunction  the sum function for cost values
     * @param <VV>         the vertex data type
     * @param <CC>         the cost number type
     * @return the converted {@link VertexBackLinkWithCost
     */
    public static <VV, CC extends Number & Comparable<CC>> @Nullable VertexBackLinkWithCost<VV, CC> toVertexBackLinkWithCost(
            @Nullable VertexBackLinkWithAncestorSet<VV> node,
            @NonNull CC zero,
            @NonNull BiFunction<VV, VV, CC> costFunction,
            @NonNull BiFunction<CC, CC, CC> sumFunction) {
        if (node == null) {
            return null;
        }


        Deque<VertexBackLinkWithAncestorSet<VV>> deque = new ArrayDeque<>();
        for (VertexBackLinkWithAncestorSet<VV> n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        VertexBackLinkWithCost<VV, CC> newNode = null;
        for (VertexBackLinkWithAncestorSet<VV> n : deque) {
            newNode = new VertexBackLinkWithCost<>(n.getVertex(), newNode,
                    newNode == null
                            ? zero
                            : sumFunction.apply(newNode.getCost(),
                            costFunction.apply(newNode.getVertex(), n.getVertex())));
        }
        return newNode;
    }

    public AddOnlyPersistentTrieSet<V> getAncestors() {
        return ancestors;
    }

    public @NonNull V getVertex() {
        return vertex;
    }
}
