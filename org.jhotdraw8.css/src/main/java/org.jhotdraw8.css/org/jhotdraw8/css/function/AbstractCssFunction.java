/*
 * @(#)AbstractCssFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;


/**
 * Abstract base class for CSS functions.
 *
 * @param <T> the element type of the DOM
 */
public abstract class AbstractCssFunction<T> implements CssFunction<T> {
    private final String name;

    public AbstractCssFunction(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
