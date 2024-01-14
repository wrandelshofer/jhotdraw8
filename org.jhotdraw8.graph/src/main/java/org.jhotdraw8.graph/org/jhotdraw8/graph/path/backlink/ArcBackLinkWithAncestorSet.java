/*
 * @(#)ArcBackLinkWithAncestorSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.icollection.SimpleImmutableAddOnlySet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

/**
 * Represents an arc back link with depth and a set of ancestors.
 *
 * @param <V> the vertex type
 * @param <A> the arrow type
 */
public class ArcBackLinkWithAncestorSet<V, A> extends AbstractBackLink<ArcBackLinkWithAncestorSet<V, A>> {
    private final @NonNull V vertex;
    private final @Nullable A arrow;

    /**
     * This set contains the vertex of this back link and the vertices of all
     * parent backlinks.
     * <p>
     * This set is only needed for backlinks that are in the search frontier.
     * Once they leave the search frontier, the set is removed.
     */
    private @Nullable SimpleImmutableAddOnlySet<V> ancestors;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex data
     * @param arrow  the arrow data
     * @param parent the parent back link
     */
    public ArcBackLinkWithAncestorSet(
            @NonNull V vertex,
            @Nullable A arrow,
            @Nullable ArcBackLinkWithAncestorSet<V, A> parent,
            @NonNull SimpleImmutableAddOnlySet<V> ancestors) {
        super(parent);
        this.vertex = vertex;
        this.arrow = arrow;
        this.ancestors = ancestors;
    }

    /**
     * Converts an {@link ArcBackLinkWithAncestorSet} to {@link ArcBackLinkWithCost}.
     *
     * @param node         the {@link ArcBackLinkWithAncestorSet}
     * @param zero         the zero cost value
     * @param costFunction the cost function
     * @param sumFunction  the sum function for cost values
     * @param <VV>         the vertex data type
     * @param <AA>         the arrow data type
     * @param <CC>         the cost number type
     * @return the converted {@link ArcBackLinkWithCost}
     */
    public static <VV, AA, CC extends Number & Comparable<CC>> @Nullable ArcBackLinkWithCost<VV, AA, CC> toArcBackLinkWithCost(@Nullable ArcBackLinkWithAncestorSet<VV, AA> node,
                                                                                                                               @NonNull CC zero,
                                                                                                                               @NonNull Function3<VV, VV, AA, CC> costFunction,
                                                                                                                               @NonNull BiFunction<CC, CC, CC> sumFunction) {
        if (node == null) {
            return null;
        }


        Deque<ArcBackLinkWithAncestorSet<VV, AA>> deque = new ArrayDeque<>();
        for (ArcBackLinkWithAncestorSet<VV, AA> n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        ArcBackLinkWithCost<VV, AA, CC> newNode = null;
        for (ArcBackLinkWithAncestorSet<VV, AA> n : deque) {
            newNode = new ArcBackLinkWithCost<>(n.getVertex(), n.getArrow(), newNode,
                    newNode == null
                            ? zero
                            : sumFunction.apply(newNode.getCost(),
                            costFunction.apply(newNode.getVertex(), n.getVertex(), n.getArrow())));
        }
        return newNode;
    }

    public @NonNull SimpleImmutableAddOnlySet<V> removeAncestors() {
        if (ancestors == null) {
            throw new IllegalStateException("ancestors already removed");
        }
        SimpleImmutableAddOnlySet<V> ancestors = this.ancestors;
        this.ancestors = null;
        return ancestors;
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

}
