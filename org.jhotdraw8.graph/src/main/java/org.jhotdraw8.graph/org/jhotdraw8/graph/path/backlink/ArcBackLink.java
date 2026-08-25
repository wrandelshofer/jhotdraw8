/*
 * @(#)ArcBackLink.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/// Represents an arc back link with depth.
///
/// @param <V> the vertex type
/// @param <A> the arrow type
public class ArcBackLink<V, A> extends AbstractBackLink<ArcBackLink<V, A>> {
    private final V vertex;
    private final @Nullable A arrow;

    /// Creates a new instance.
    ///
    /// @param vertex the vertex data
    /// @param arrow  the arrow data
    /// @param parent the parent back link
    public ArcBackLink(V vertex, @Nullable A arrow, @Nullable ArcBackLink<V, A> parent) {
        super(parent);
        this.vertex = vertex;
        this.arrow = arrow;
    }


    public @Nullable A getArrow() {
        return arrow;
    }

    public V getVertex() {
        return vertex;
    }

    @Override
    public String toString() {
        return "ArcBackLink{" +
                "depth=" + depth +
                ", vertex=" + vertex +
                ", arrow=" + arrow +
                '}';
    }

    /// Converts an [ArcBackLink] to [ArcBackLinkWithCost].
    ///
    /// @param <VV>         the vertex data type
    /// @param <AA>         the arrow data type
    /// @param node         the [ArcBackLink]
    /// @param costFunction the cost function
    /// @return the converted [ArcBackLinkWithCost]
    public static <VV, AA> @Nullable ArcBackLinkWithCost<VV, AA> toArcBackLinkWithCost(@Nullable ArcBackLink<VV, AA> node,
                                                                                       ToIntFunction3<VV, VV, AA> costFunction) {
        if (node == null) {
            return null;
        }


        Deque<ArcBackLink<VV, AA>> deque = new ArrayDeque<>();
        for (ArcBackLink<VV, AA> n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        ArcBackLinkWithCost<VV, AA> newNode = null;
        for (ArcBackLink<VV, AA> n : deque) {
            newNode = new ArcBackLinkWithCost<>(n.getVertex(), n.getArrow(), newNode,
                    newNode == null
                            ? 0
                            : newNode.getCost() +
                            costFunction.apply(newNode.getVertex(), n.getVertex(), n.getArrow()));
        }
        return newNode;
    }

}
