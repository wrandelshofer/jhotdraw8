/*
 * @(#)AbstractBackLinkWithCost.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jspecify.annotations.Nullable;

/// Abstract base class for back links.
///
/// @param <T> the concrete back link type
public class AbstractBackLinkWithCost<T extends AbstractBackLinkWithCost<T>>
        extends AbstractBackLink<T> {
    /// The cost for reaching this back link from the root ancestor.
    private final int cost;


    /// Creates a new instance.
    ///
    /// @param parent the parent back link
    /// @param cost   the cumulated cost of this back link. Must be zero if parent is null.
    public AbstractBackLinkWithCost(@Nullable T parent, int cost) {
        super(parent);
        this.cost = cost;
    }

    /// The cost
    ///
    /// @return cost
    public int getCost() {
        return cost;
    }
}
