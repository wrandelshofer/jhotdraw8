/*
 * @(#)AbstractBackLinkWithCost.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for back links.
 *
 * @param <T> the concrete back link type
 * @param <C> the cost number type
 */
public class AbstractBackLinkWithCost<T extends AbstractBackLinkWithCost<T, C>, C extends Number & Comparable<C>>
        extends AbstractBackLink<T> {
    /**
     * The cost for reaching this back link from the root ancestor.
     */
    private final C cost;


    /**
     * Creates a new instance.
     *
     * @param parent the parent back link
     * @param cost   the cumulated cost of this back link. Must be zero if parent is null.
     */
    public AbstractBackLinkWithCost(@Nullable T parent, C cost) {
        super(parent);
        this.cost = cost;
    }

    /**
     * The cost
     *
     * @return cost
     */
    public C getCost() {
        return cost;
    }
}
