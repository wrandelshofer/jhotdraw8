/*
 * @(#)FigurePropertyChangeEvent.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Event;
import org.jhotdraw8.fxcollection.typesafekey.Key;

/**
 * FigurePropertyChangeEvent.
 *
 * @author Werner Randelshofer
 */
public class FigurePropertyChangeEvent extends Event<Figure> {

    private static final long serialVersionUID = 1L;
    private final @NonNull Key<?> key;
    private final @Nullable Object oldValue;
    private final @Nullable Object newValue;
    private final boolean wasAdded;
    private final boolean wasRemoved;

    public <T> FigurePropertyChangeEvent(@NonNull Figure source, @NonNull Key<T> key, @Nullable T oldValue, @Nullable T newValue, boolean wasAdded, boolean wasRemoved) {
        super(source);
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.wasAdded = wasAdded;
        this.wasRemoved = wasRemoved;
    }

    /**
     * Returns the key of the property that has changed.
     *
     * @return the key or null
     */
    public @NonNull Key<?> getKey() {
        return key;
    }

    public @Nullable <T> T getOldValue() {
        @SuppressWarnings("unchecked") T oldValue = (T) this.oldValue;
        return oldValue;
    }

    public @Nullable <T> T getNewValue() {
        @SuppressWarnings("unchecked") T newValue = (T) this.newValue;
        return newValue;
    }

    /**
     * If the change is the result of an add operation.
     *
     * @return true if a new key-value entry was added to the map.
     */
    public boolean wasAdded() {
        return wasAdded;
    }

    /**
     * If the change is the result of a remove operation.
     *
     * @return true if an existing key-value entry was removed from the map.
     */
    public boolean wasRemoved() {
        return wasRemoved;
    }
}
