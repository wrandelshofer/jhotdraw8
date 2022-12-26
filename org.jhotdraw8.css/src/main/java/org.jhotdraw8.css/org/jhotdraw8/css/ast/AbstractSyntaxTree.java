/*
 * @(#)AbstractSyntaxTree.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.parser.CssToken;

import java.util.function.Consumer;

/**
 * Abstract syntax tree for cascading style sheets.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractSyntaxTree {
    public AbstractSyntaxTree() {
    }

    /**
     * Produces tokens for the subtree starting at this tree node.
     *
     * @param consumer a consumer for the tokens
     */
    public void produceTokens(Consumer<CssToken> consumer) {
    }
}
