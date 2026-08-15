/*
 * @(#)AndCombinator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/// An "and combinator" matches an element if both its tree selector and its
/// offset selector match the element.
public class AndCombinator extends Combinator {

    /// Creates a new instance.
    ///
    /// @param sourceLocator
    /// @param first         the tree selector
    /// @param second        the offset selector
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

    /// This selector matches only on a specific type, if its tree or its offset
    /// selector matches only on a specific type.
    ///
    /// @return `tree.matchesOnlyOnASpecificType()!=null? tree.matchesOnlyOnASpecificType(): offset.matchesOnlyOnASpecificType()`
    @Override
    public @Nullable TypeSelector matchesOnlyOnASpecificType() {
        TypeSelector firstQN = first.matchesOnlyOnASpecificType();
        TypeSelector secondQN = second.matchesOnlyOnASpecificType();
        return firstQN != null ? firstQN : secondQN;
    }
}
