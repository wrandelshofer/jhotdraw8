/*
 * @(#)Combinator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Abstract superclass for "combinator"s.
 * <p>
 * A combinator combines the results of two selectors.
 *
 */
public abstract class Combinator extends Selector {

    protected final SimpleSelector first;
    protected final Selector second;

    public Combinator(@Nullable SourceLocator sourceLocator, SimpleSelector firstSelector, Selector secondSelector) {
        super(sourceLocator);
        this.first = firstSelector;
        this.second = secondSelector;

    }

    @Override
    public String toString() {
        return "Combinator{" + "simpleSelector=" + first + ", selector=" + second + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Combinator that = (Combinator) o;
        return first.equals(that.first) && second.equals(that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
