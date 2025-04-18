/*
 * @(#)FigurePropertyChangeEvent.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import org.jhotdraw8.base.event.Event;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jspecify.annotations.Nullable;

import java.io.Serial;

/**
 * FigurePropertyChangeEvent.
 *
 */
public class FigurePropertyChangeEvent extends Event<Figure> {

    @Serial
    private static final long serialVersionUID = 1L;
    private final Key<?> key;
    private final @Nullable Object oldValue;
    private final @Nullable Object newValue;
    private final boolean wasAdded;
    private final boolean wasRemoved;

    public <T> FigurePropertyChangeEvent(Figure source, Key<T> key, @Nullable T oldValue, @Nullable T newValue, boolean wasAdded, boolean wasRemoved) {
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
    public Key<?> getKey() {
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
