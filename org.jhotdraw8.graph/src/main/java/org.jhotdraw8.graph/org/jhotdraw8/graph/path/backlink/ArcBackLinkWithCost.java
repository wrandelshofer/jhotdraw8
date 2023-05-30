/*
 * @(#)ArcBackLinkWithCost.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.SimpleOrderedPair;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents an arc back link with cost and depth.
 *
 * @param <V> the vertex type
 * @param <A> the arrow type
 * @param <C> the cost number type
 */
public class ArcBackLinkWithCost<V, A, C extends Number & Comparable<C>> extends AbstractBackLinkWithCost<ArcBackLinkWithCost<V, A, C>, C> {
    private final @NonNull V vertex;
    private final @Nullable A arrow;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex data
     * @param arrow  the arrow data
     * @param parent the parent back link
     * @param cost   the cumulated cost of this back link. Must be zero if parent is null.
     */
    public ArcBackLinkWithCost(@NonNull V vertex, @Nullable A arrow, @Nullable ArcBackLinkWithCost<V, A, C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = vertex;
        this.arrow = arrow;
    }


    /**
     * Converts an {@link ArcBackLinkWithCost} into a vertex sequence.
     *
     * @param node            the {@link ArcBackLinkWithCost}
     * @param mappingFunction the mapping function
     * @param <VV>            the vertex data type
     * @param <AA>            the arrow data type
     * @param <CC>            the cost number type
     * @param <XX>            the vertex sequence element type
     * @return the vertex sequence
     */
    public static <VV, AA, CC extends Number & Comparable<CC>, XX> @Nullable SimpleOrderedPair<ImmutableList<XX>, CC> toVertexSequence(@Nullable ArcBackLinkWithCost<VV, AA, CC> node,
                                                                                                                                       @NonNull Function<ArcBackLinkWithCost<VV, AA, CC>, XX> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        Deque<XX> deque = new ArrayDeque<>();
        for (ArcBackLinkWithCost<VV, AA, CC> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new SimpleOrderedPair<>(VectorList.copyOf(deque), node.getCost());
    }

    /**
     * Converts an {@link ArcBackLinkWithCost} into an arrow sequence.
     *
     * @param node            the {@link ArcBackLinkWithCost}
     * @param mappingFunction the mapping function
     * @param <VV>            the vertex data type
     * @param <AA>            the arrow data type
     * @param <CC>            the cost number type
     * @param <XX>            the arrow sequence element type
     * @return the arrow sequence
     */
    public static <VV, AA, CC extends Number & Comparable<CC>, XX> @Nullable SimpleOrderedPair<ImmutableList<XX>, CC> toArrowSequence(
            @Nullable ArcBackLinkWithCost<VV, AA, CC> node,
            @NonNull BiFunction<ArcBackLinkWithCost<VV, AA, CC>, ArcBackLinkWithCost<VV, AA, CC>, XX> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        Deque<XX> deque = new ArrayDeque<>();
        ArcBackLinkWithCost<VV, AA, CC> prev = node;
        for (ArcBackLinkWithCost<VV, AA, CC> parent = node.getParent(); parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent, prev));
            prev = parent;
        }
        return new SimpleOrderedPair<>(VectorList.copyOf(deque), node.getCost());
    }

    public @Nullable A getArrow() {
        return arrow;
    }

    public @NonNull V getVertex() {
        return vertex;
    }

}
