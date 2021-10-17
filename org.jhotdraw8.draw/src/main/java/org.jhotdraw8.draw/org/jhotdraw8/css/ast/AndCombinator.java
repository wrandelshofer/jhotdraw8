/*
 * @(#)AndCombinator.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.CssToken;
import org.jhotdraw8.css.SelectorModel;

import java.util.function.Consumer;

/**
 * An "and combinator" matches an element if both its first selector and its
 * second selector match the element.
 *
 * @author Werner Randelshofer
 */
public class AndCombinator extends Combinator {

    public AndCombinator(SimpleSelector first, Selector second) {
        super(first, second);
    }

    @Override
    public @NonNull String toString() {
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
