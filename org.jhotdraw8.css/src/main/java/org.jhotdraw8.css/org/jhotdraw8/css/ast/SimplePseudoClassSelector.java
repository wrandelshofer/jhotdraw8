/*
 * @(#)SimplePseudoClassSelector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A "simple class selector" matches an element based on the value of its
 * "pseudo class" attribute.
 *
 */
public class SimplePseudoClassSelector extends PseudoClassSelector {

    private final String pseudoClass;

    public SimplePseudoClassSelector(@Nullable SourceLocator sourceLocator, String pseudoClass) {
        super(sourceLocator);
        this.pseudoClass = pseudoClass;
    }

    @Override
    public String toString() {
        return "PseudoClass:" + pseudoClass;
    }

    @Override
    public @Nullable <T> T match(SelectorModel<T> model, @Nullable T element) {
        return (element != null && model.hasPseudoClass(element, pseudoClass)) //
                ? element : null;
    }

    @Override
    public void produceTokens(Consumer<CssToken> consumer) {
        consumer.accept(new CssToken(CssTokenType.TT_COLON));
        consumer.accept(new CssToken(CssTokenType.TT_IDENT, pseudoClass));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimplePseudoClassSelector that = (SimplePseudoClassSelector) o;
        return pseudoClass.equals(that.pseudoClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pseudoClass);
    }
}
