/*
 * @(#)Rule.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.Nullable;

public abstract class Rule extends AbstractSyntaxTree {
    public Rule(@Nullable SourceLocator sourceLocator) {
        super(sourceLocator);
    }
}
