/*
 * @(#)ComponentValue.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jspecify.annotations.Nullable;

/**
 * A ComponentValue is the abstract base class of CssToken, CurlyBlock,
 * RoundBlock, SquareBlock and FunctionBlock.
 *
 * @author Werner Randelshofer
 */
public abstract class ComponentValue extends AbstractSyntaxTree {

    public ComponentValue(@Nullable SourceLocator sourceLocator) {
        super(sourceLocator);
    }
}
