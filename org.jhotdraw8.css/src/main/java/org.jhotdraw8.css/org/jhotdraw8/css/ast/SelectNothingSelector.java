/*
 * @(#)SelectNothingSelector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.model.SelectorModel;
import org.jspecify.annotations.Nullable;

/**
 * A "select nothing selector" matches nothing.
 * <p>
 * This selector is used, when the parser does not understand the
 * syntax of the selector.
 *
 */
public class SelectNothingSelector extends SimpleSelector {

    public SelectNothingSelector(@Nullable SourceLocator sourceLocator) {
        super(sourceLocator);
    }

    @Override
    public String toString() {
        return "SelectNothing";
    }

    @Override
    public @Nullable <T> T match(SelectorModel<T> model, T element) {
        return null;
    }

    @Override
    public int getSpecificity() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return SelectNothingSelector.class.hashCode();
    }

}
