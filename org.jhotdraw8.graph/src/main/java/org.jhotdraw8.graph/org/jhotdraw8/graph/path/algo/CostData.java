/*
 * @(#)CostData.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

/// Holds cost and visit count data.
///
/// @param <C> the cost number type
class CostData {
    private final int cost;
    private int visiCount;

    public CostData(int cost, int visiCount) {
        this.cost = cost;
        this.visiCount = visiCount;
    }

    public int getCost() {
        return cost;
    }

    public int getVisiCount() {
        return visiCount;
    }

    public void increaseVisitCount() {
        visiCount++;
    }
}
