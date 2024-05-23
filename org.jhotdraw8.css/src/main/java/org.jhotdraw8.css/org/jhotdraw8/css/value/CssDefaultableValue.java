/*
 * @(#)CssDefaultableValue.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css.value;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Wraps a value that supports CSS defaulting.
 *
 * @param <T> the value type
 * @see CssDefaulting
 */
public class CssDefaultableValue<T> {
    private final @Nullable CssDefaulting defaulting;
    private final @Nullable T value;

    public CssDefaultableValue(@Nullable CssDefaulting defaulting, @Nullable T value) {
        this.defaulting = defaulting;
        this.value = value;
    }

    public CssDefaultableValue(CssDefaulting defaulting) {
        this(defaulting, null);
    }

    public CssDefaultableValue(@Nullable T value) {
        this(null, value);
    }

    public @Nullable CssDefaulting getDefaulting() {
        return defaulting;
    }

    public @Nullable T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "CssDefaultableValue{" +
                /*"defaulting=" +*/ defaulting +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CssDefaultableValue<?> that = (CssDefaultableValue<?>) o;
        return defaulting == that.defaulting &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaulting, value);
    }
}
