/*
 * @(#)Combinator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Objects;

/**
 * Abstract superclass for "combinator"s.
 * <p>
 * A combinator combines the results of two selectors.
 *
 * @author Werner Randelshofer
 */
public abstract class Combinator extends Selector {

    protected final @NonNull SimpleSelector first;
    protected final @NonNull Selector second;

    public Combinator(@Nullable SourceLocator sourceLocator, @NonNull SimpleSelector firstSelector, @NonNull Selector secondSelector) {
        super(sourceLocator);
        this.first = firstSelector;
        this.second = secondSelector;

    }

    @Override
    public @NonNull String toString() {
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
