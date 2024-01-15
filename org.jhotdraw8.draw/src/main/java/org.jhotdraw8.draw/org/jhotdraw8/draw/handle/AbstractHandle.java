/*
 * @(#)AbstractHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.figure.Figure;

/**
 * AbstractHandle.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractHandle implements Handle {

    // ---
    // Fields
    // ---
    protected final @NonNull Figure owner;

    // ---
    // Constructors
    // ---
    public AbstractHandle(@NonNull Figure owner) {
        this.owner = owner;
    }

    // ---
    // Behavior
    // ---
    @Override
    public final void dispose() {
    }

    @Override
    public @NonNull Figure getOwner() {
        return owner;
    }

    /**
     * Returns true if both handles have the same class.
     */
    @Override
    public boolean isCompatible(@NonNull Handle that) {
        return that.getClass() == this.getClass();
    }

}
