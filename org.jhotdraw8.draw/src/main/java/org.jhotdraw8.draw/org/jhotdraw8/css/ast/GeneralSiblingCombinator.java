/*
 * @(#)GeneralSiblingCombinator.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.CssToken;
import org.jhotdraw8.css.CssTokenType;
import org.jhotdraw8.css.SelectorModel;

import java.util.function.Consumer;

/**
 * An "general sibling combinator" matches an element if its first selector
 * matches on a previous sibling of the element and if its second selector
 * matches the element.
 *
 * @author Werner Randelshofer
 */
public class GeneralSiblingCombinator extends Combinator {

    public GeneralSiblingCombinator(SimpleSelector first, Selector second) {
        super(first, second);
    }

    @Override
    public @NonNull String toString() {
        return first + " ~ " + second;
    }

    @Override
    public @Nullable <T> T match(@NonNull SelectorModel<T> model, T element) {
        T result = second.match(model, element);
        T siblingElement = result;
        while (siblingElement != null) {
            siblingElement = model.getPreviousSibling(siblingElement);
            result = first.match(model, siblingElement);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Override
    public final int getSpecificity() {
        return first.getSpecificity() + second.getSpecificity();
    }

    @Override
    public void produceTokens(@NonNull Consumer<CssToken> consumer) {
        first.produceTokens(consumer);
        consumer.accept(new CssToken(CssTokenType.TT_TILDE));
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
