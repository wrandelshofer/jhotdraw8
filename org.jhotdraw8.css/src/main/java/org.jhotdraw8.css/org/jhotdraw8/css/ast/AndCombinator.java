/*
 * @(#)AndCombinator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * An "and combinator" matches an element if both its first selector and its
 * second selector match the element.
 *
 */
public class AndCombinator extends Combinator {

    /**
     * Creates a new instance.
     *
     * @param sourceLocator
     * @param first         the first selector
     * @param second        the second selector
     */
    public AndCombinator(@Nullable SourceLocator sourceLocator, SimpleSelector first, Selector second) {
        super(sourceLocator, first, second);
    }

    @Override
    public String toString() {
        return "(" + first + " && " + second + ")";
    }

    @Override
    public @Nullable <T> T match(SelectorModel<T> model, T element) {
        T firstResult = first.match(model, element);
        return (firstResult != null && second.match(model, element) != null) ? firstResult : null;
    }

    @Override
    public int getSpecificity() {
        return first.getSpecificity() + second.getSpecificity();
    }

    @Override
    public void produceTokens(Consumer<CssToken> consumer) {
        first.produceTokens(consumer);
        second.produceTokens(consumer);
    }

    /**
     * This selector matches only on a specific type, if its first or its second
     * selector matches only on a specific type.
     *
     * @return {@code first.matchesOnlyOnASpecificType()!=null
     * ? first.matchesOnlyOnASpecificType()
     * : second.matchesOnlyOnASpecificType()}
     */
    @Override
    public @Nullable TypeSelector matchesOnlyOnASpecificType() {
        TypeSelector firstQN = first.matchesOnlyOnASpecificType();
        TypeSelector secondQN = second.matchesOnlyOnASpecificType();
        return firstQN != null ? firstQN : secondQN;
    }
}
