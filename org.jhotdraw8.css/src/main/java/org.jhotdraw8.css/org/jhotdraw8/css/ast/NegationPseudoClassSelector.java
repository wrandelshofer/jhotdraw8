/*
 * @(#)NegationPseudoClassSelector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.model.SelectorModel;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Implements the negation pseudo-class selector.
 * <p>
 * The negation pseudo-class, {@code :not(X)}, is a functional notation taking a
 * simple selector (excluding the negation pseudo-class itself) as an argument.
 * It represents an element that is not represented by its argument.
 * <p>
 * Negations may not be nested; {@code :not(:not(...))} is invalid.
 * Note also that since pseudo-elements are not simple selectors,
 * they are not a valid argument to {@code :not()}.
 * <p>
 * See <a href="https://www.w3.org/TR/2018/REC-selectors-3-20181106/#negation">negation pseudo-class</a>.
 */
public class NegationPseudoClassSelector extends FunctionPseudoClassSelector {

    private final SimpleSelector selector;

    public NegationPseudoClassSelector(@Nullable SourceLocator sourceLocator, String functionIdentifier, SimpleSelector selector) {
        super(sourceLocator, functionIdentifier);
        this.selector = selector;
    }

    @Override
    public String toString() {
        return "FunctionPseudoClass:" + getFunctionIdentifier() + "(" + ")";
    }

    @Override
    public @Nullable <T> T match(SelectorModel<T> model, @Nullable T element) {
        final T match = selector.match(model, element);
        return match == null ? element : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        NegationPseudoClassSelector that = (NegationPseudoClassSelector) o;
        return selector.equals(that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), selector);
    }

    @Override
    public int getSpecificity() {
        return selector.getSpecificity();
    }
}
