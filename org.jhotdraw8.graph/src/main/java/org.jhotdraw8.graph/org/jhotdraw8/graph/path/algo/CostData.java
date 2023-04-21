/*
 * @(#)CostData.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.Nullable;

/**
 * Holds cost and visit count data.
 *
 * @param <C> the cost number type
 */
class CostData<C> {
    private final @Nullable C cost;
    private int visiCount;

    public CostData(@Nullable C cost, int visiCount) {
        this.cost = cost;
        this.visiCount = visiCount;
    }

    public @Nullable C getCost() {
        return cost;
    }

    public int getVisiCount() {
        return visiCount;
    }

    public void increaseVisitCount() {
        visiCount++;
    }
}
