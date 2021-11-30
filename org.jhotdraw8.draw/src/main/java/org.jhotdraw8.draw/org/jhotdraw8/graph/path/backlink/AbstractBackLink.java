package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Abstract base class for back links.
 *
 * @param <T> the concrete back link type
 */
public class AbstractBackLink<T extends AbstractBackLink<T, C>, C extends Number & Comparable<C>> {
    /**
     * The number of ancestors that this back link has.
     */
    protected final int depth;
    /**
     * The parent back link.
     */
    protected final @Nullable T parent;

    /**
     * The cost for reaching this back link from the root ancestor.
     */
    private final @NonNull C cost;


    /**
     * Creates a new instance.
     *
     * @param parent the parent back link
     * @param cost   the cumulated cost of this back link. Must be zero if parent is null.
     */
    public AbstractBackLink(@Nullable T parent, @NonNull C cost) {
        this.parent = parent;
        this.depth = parent == null ? 0 : parent.getDepth() + 1;
        this.cost = cost;
    }

    /**
     * The number of ancestors that this backlink has.
     *
     * @return the depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * The parent back link.
     *
     * @return the parent
     */
    public @Nullable T getParent() {
        return parent;
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
