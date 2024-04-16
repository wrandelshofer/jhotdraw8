/*
 * @(#)AbstractCssFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;

import org.jhotdraw8.annotation.NonNull;

/**
 * Abstract base class for CSS functions.
 *
 * @param <T> the element type of the DOM
 */
public abstract class AbstractCssFunction<T> implements CssFunction<T> {
    private final @NonNull String name;

    public AbstractCssFunction(@NonNull String name) {
        this.name = name;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

}
