/*
 * @(#)AbstractBackLink.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.Nullable;

/**
 * Abstract base class for back links.
 *
 * @param <T> the concrete back link type
 */
public class AbstractBackLink<T extends AbstractBackLink<T>> {
    /**
     * The number of ancestors that this back link has.
     */
    protected final int depth;
    /**
     * The parent back link.
     */
    protected final @Nullable T parent;


    /**
     * Creates a new instance.
     *
     * @param parent the parent back link
     */
    public AbstractBackLink(@Nullable T parent) {
        this.parent = parent;
        this.depth = parent == null ? 0 : parent.getDepth() + 1;
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

}
