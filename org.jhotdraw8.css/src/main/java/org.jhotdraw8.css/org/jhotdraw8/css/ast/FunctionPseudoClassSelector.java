/*
 * @(#)FunctionPseudoClassSelector.java
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
 * A "class selector" matches an element based on the value of its "pseudo
 * class" attribute.
 *
 */
public class FunctionPseudoClassSelector extends PseudoClassSelector {

    private final String functionIdentifier;

    public FunctionPseudoClassSelector(@Nullable SourceLocator sourceLocator, String functionIdentifier) {
        super(sourceLocator);
        this.functionIdentifier = functionIdentifier;
    }

    @Override
    public String toString() {
        return "FunctionPseudoClass:" + functionIdentifier + "(" + ")";
    }

    @Override
    public @Nullable <T> T match(SelectorModel<T> model, @Nullable T element) {
        return (element != null && model.hasPseudoClass(element, functionIdentifier)) //
                ? element : null;
    }

    public String getFunctionIdentifier() {
        return functionIdentifier;
    }

    @Override
    public void produceTokens(Consumer<CssToken> consumer) {
        consumer.accept(new CssToken(CssTokenType.TT_COLON));
        consumer.accept(new CssToken(CssTokenType.TT_FUNCTION, functionIdentifier));
        consumer.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FunctionPseudoClassSelector that = (FunctionPseudoClassSelector) o;
        return functionIdentifier.equals(that.functionIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionIdentifier);
    }
}
