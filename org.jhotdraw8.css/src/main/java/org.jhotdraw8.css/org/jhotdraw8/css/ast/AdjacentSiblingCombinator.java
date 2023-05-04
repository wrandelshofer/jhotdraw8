/*
 * @(#)AdjacentSiblingCombinator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;

import java.util.function.Consumer;

/**
 * An "adjacent sibling combinator" matches an element if its first selector
 * matches on the adjacent sibling of the element and if its second selector
 * matches the element.
 *
 * @author Werner Randelshofer
 */
public class AdjacentSiblingCombinator extends Combinator {

    /**
     * Creates a new instance.
     *
     * @param sourceLocator
     * @param first         the first selector
     * @param second        the second selector
     */
    public AdjacentSiblingCombinator(@Nullable SourceLocator sourceLocator, SimpleSelector first, Selector second) {
        super(sourceLocator, first, second);
    }

    @Override
    public @NonNull String toString() {
        return first + " + " + second;
    }

    @Override
    public @Nullable <T> T match(@NonNull SelectorModel<T> model, T element) {
        T result = second.match(model, element);
        if (result != null) {
            result = first.match(model, model.getPreviousSibling(result));
        }
        return result;
    }

    @Override
    public int getSpecificity() {
        return first.getSpecificity() + second.getSpecificity();
    }

    @Override
    public void produceTokens(@NonNull Consumer<CssToken> consumer) {
        first.produceTokens(consumer);
        consumer.accept(new CssToken(CssTokenType.TT_PLUS));
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
