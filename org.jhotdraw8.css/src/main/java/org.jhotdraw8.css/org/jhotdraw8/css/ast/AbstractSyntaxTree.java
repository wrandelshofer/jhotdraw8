/*
 * @(#)AbstractSyntaxTree.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.parser.CssToken;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Abstract syntax tree for cascading style sheets.
 *
 */
public abstract class AbstractSyntaxTree {
    private final @Nullable SourceLocator sourceLocator;

    public AbstractSyntaxTree(@Nullable SourceLocator sourceLocator) {
        this.sourceLocator = sourceLocator;
    }

    /**
     * Produces tokens for the subtree starting at this tree node.
     *
     * @param consumer a consumer for the tokens
     */
    public void produceTokens(Consumer<CssToken> consumer) {
    }

    public @Nullable SourceLocator getSourceLocator() {
        return sourceLocator;
    }
}
