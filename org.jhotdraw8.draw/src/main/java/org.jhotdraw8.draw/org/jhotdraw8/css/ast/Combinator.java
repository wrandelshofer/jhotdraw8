/*
 * @(#)Combinator.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;

import java.util.Objects;

/**
 * Abstract superclass for "combinator"s.
 * <p>
 * A combinator combines the results of two selectors.
 *
 * @author Werner Randelshofer
 */
public abstract class Combinator extends Selector {

    protected final @NonNull SimpleSelector firstSelector;
    protected final @NonNull Selector secondSelector;

    public Combinator(@NonNull SimpleSelector firstSelector, @NonNull Selector secondSelector) {
        this.firstSelector = firstSelector;
        this.secondSelector = secondSelector;

    }

    @Override
    public @NonNull String toString() {
        return "Combinator{" + "simpleSelector=" + firstSelector + ", selector=" + secondSelector + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Combinator that = (Combinator) o;
        return firstSelector.equals(that.firstSelector) && secondSelector.equals(that.secondSelector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstSelector, secondSelector);
    }
}
