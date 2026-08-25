/*
 * @(#)ArcBackLinkWithAncestorSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/// Represents an arc back link with depth and a set of ancestors.
///
/// @param <V> the vertex type
/// @param <A> the arrow type
public class ArcBackLinkWithAncestorSet<V, A> extends AbstractBackLink<ArcBackLinkWithAncestorSet<V, A>> {
    private final V vertex;
    private final @Nullable A arrow;

    /// This set contains the vertex of this back link and the vertices of all
    /// parent backlinks.
    ///
    /// This set is only needed for backlinks that are in the search frontier.
    /// Once they leave the search frontier, the set is removed.
    private @Nullable PersistentSet<V> ancestors;

    /// Creates a new instance.
    ///
    /// @param vertex the vertex data
    /// @param arrow  the arrow data
    /// @param parent the parent back link
    public ArcBackLinkWithAncestorSet(
            V vertex,
            @Nullable A arrow,
            @Nullable ArcBackLinkWithAncestorSet<V, A> parent,
            PersistentSet<V> ancestors) {
        super(parent);
        this.vertex = vertex;
        this.arrow = arrow;
        this.ancestors = ancestors;
    }

    /// Converts an [ArcBackLinkWithAncestorSet] to [ArcBackLinkWithCost].
    ///
    /// @param <VV>         the vertex data type
    /// @param <AA>         the arrow data type
    /// @param node         the [ArcBackLinkWithAncestorSet]
    /// @param costFunction the cost function
    /// @return the converted [ArcBackLinkWithCost]
    public static <VV, AA> @Nullable ArcBackLinkWithCost<VV, AA> toArcBackLinkWithCost(@Nullable ArcBackLinkWithAncestorSet<VV, AA> node,
                                                                                       ToIntFunction3<VV, VV, AA> costFunction) {
        if (node == null) {
            return null;
        }


        Deque<ArcBackLinkWithAncestorSet<VV, AA>> deque = new ArrayDeque<>();
        for (ArcBackLinkWithAncestorSet<VV, AA> n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        ArcBackLinkWithCost<VV, AA> newNode = null;
        for (ArcBackLinkWithAncestorSet<VV, AA> n : deque) {
            newNode = new ArcBackLinkWithCost<>(n.getVertex(), n.getArrow(), newNode,
                    newNode == null
                            ? 0
                            : (newNode.getCost() +
                            costFunction.apply(newNode.getVertex(), n.getVertex(), n.getArrow())));
        }
        return newNode;
    }

    public PersistentSet<V> removeAncestors() {
        if (ancestors == null) {
            throw new IllegalStateException("ancestors already removed");
        }
        PersistentSet<V> ancestors = this.ancestors;
        this.ancestors = null;
        return ancestors;
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

}
