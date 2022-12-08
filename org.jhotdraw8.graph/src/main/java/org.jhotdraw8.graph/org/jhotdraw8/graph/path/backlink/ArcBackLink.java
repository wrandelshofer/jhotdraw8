/*
 * @(#)ArcBackLink.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.function.TriFunction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

/**
 * Represents an arc back link with depth.
 *
 * @param <V> the vertex type
 * @param <A> the arrow type
 */
public class ArcBackLink<V, A> extends AbstractBackLink<ArcBackLink<V, A>> {
    private final @NonNull V vertex;
    private final @Nullable A arrow;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex data
     * @param arrow  the arrow data
     * @param parent the parent back link
     */
    public ArcBackLink(@NonNull V vertex, @Nullable A arrow, @Nullable ArcBackLink<V, A> parent) {
        super(parent);
        this.vertex = vertex;
        this.arrow = arrow;
    }


    public @Nullable A getArrow() {
        return arrow;
    }

    public @NonNull V getVertex() {
        return vertex;
    }

    @Override
    public @NonNull String toString() {
        return "ArcBackLink{" +
                "depth=" + depth +
                ", vertex=" + vertex +
                ", arrow=" + arrow +
                '}';
    }

    /**
     * Converts an {@link ArcBackLink} to {@link ArcBackLinkWithCost}.
     *
     * @param node         the {@link ArcBackLink}
     * @param zero         the zero cost value
     * @param costFunction the cost function
     * @param sumFunction  the sum function for cost values
     * @param <VV>         the vertex data type
     * @param <AA>         the arrow data type
     * @param <CC>         the cost number type
     * @return the converted {@link ArcBackLinkWithCost}
     */
    public static <VV, AA, CC extends Number & Comparable<CC>> @Nullable ArcBackLinkWithCost<VV, AA, CC> toArcBackLinkWithCost(@Nullable ArcBackLink<VV, AA> node,
                                                                                                                               @NonNull CC zero,
                                                                                                                               @NonNull TriFunction<VV, VV, AA, CC> costFunction,
                                                                                                                               @NonNull BiFunction<CC, CC, CC> sumFunction) {
        if (node == null) {
            return null;
        }


        Deque<ArcBackLink<VV, AA>> deque = new ArrayDeque<>();
        for (ArcBackLink<VV, AA> n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        ArcBackLinkWithCost<VV, AA, CC> newNode = null;
        for (ArcBackLink<VV, AA> n : deque) {
            newNode = new ArcBackLinkWithCost<>(n.getVertex(), n.getArrow(), newNode,
                    newNode == null
                            ? zero
                            : sumFunction.apply(newNode.getCost(),
                            costFunction.apply(newNode.getVertex(), n.getVertex(), n.getArrow())));
        }
        return newNode;
    }

}
