/*
 * @(#)ChildCombinator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A "child combinator" matches an element if its first selector matches on the
 * parent of the element and if its second selector matches on the element
 * itself.
 *
 * @author Werner Randelshofer
 */
public class ChildCombinator extends Combinator {

    public ChildCombinator(@Nullable SourceLocator sourceLocator, SimpleSelector first, Selector second) {
        super(sourceLocator, first, second);
    }

    @Override
    public String toString() {
        return "(" + first + " > " + second + ")";
    }

    @Override
    public @Nullable <T> T match(SelectorModel<T> model, T element) {
        T result = second.match(model, element);
        if (result != null) {
            result = first.match(model, model.getParent(result));
        }
        return result;
    }

    @Override
    public int getSpecificity() {
        return first.getSpecificity() + second.getSpecificity();
    }

    @Override
    public void produceTokens(Consumer<CssToken> consumer) {
        first.produceTokens(consumer);
        consumer.accept(new CssToken(CssTokenType.TT_GREATER_THAN));
        second.produceTokens(consumer);
    }

    /**
     * This selector matches only on a specific type, if its second
     * selector matches only on a specific type.
     *
     * @return {@code second.matchesOnlyOnASpecificType()}
     */
    @Override
    public @Nullable TypeSelector matchesOnlyOnASpecificType() {
        return second.matchesOnlyOnASpecificType();
    }
}
