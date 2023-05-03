/*
 * @(#)GeneralSiblingCombinator.java
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
 * An "general sibling combinator" matches an element if its first selector
 * matches the sibling of the element and if its second selector
 * matches the element.
 *
 * @author Werner Randelshofer
 */
public class GeneralSiblingCombinator extends Combinator {

    public GeneralSiblingCombinator(@Nullable SourceLocator sourceLocator, @NonNull SimpleSelector first, @NonNull Selector second) {
        super(sourceLocator, first, second);
    }

    @Override
    public @NonNull String toString() {
        return first + " ~ " + second;
    }

    @Override
    public @Nullable <T> T match(@NonNull SelectorModel<T> model, T element) {
        T secondMatch = second.match(model, element);
        if (secondMatch != null) {
            for (T siblingElement = model.getPreviousSibling(element);
                 siblingElement != null;
                 siblingElement = model.getPreviousSibling(siblingElement)) {
                T firstMatch = first.match(model, siblingElement);
                if (firstMatch != null) {
                    return secondMatch;
                }
            }
        }
        return null;
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
