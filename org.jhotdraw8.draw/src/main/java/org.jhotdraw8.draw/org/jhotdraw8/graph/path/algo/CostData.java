package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;

/**
 * Holds cost and visit count data.
 *
 * @param <C> the cost number type
 */
class CostData<C> {
    private final @NonNull C cost;
    private int visiCount;

    public CostData(@NonNull C cost, int visiCount) {
        this.cost = cost;
        this.visiCount = visiCount;
    }

    public @NonNull C getCost() {
        return cost;
    }

    public int getVisiCount() {
        return visiCount;
    }

    public void increaseVisitCount() {
        visiCount++;
    }
}
