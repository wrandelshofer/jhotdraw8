/*
 * @(#)AbstractAttributeSelector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jspecify.annotations.Nullable;

/**
 * An abstract "attribute selector" matches an element based on its attributes.
 *
 */
public abstract class AbstractAttributeSelector extends SimpleSelector {

    public AbstractAttributeSelector(@Nullable SourceLocator sourceLocator) {
        super(sourceLocator);
    }

    @Override
    public final int getSpecificity() {
        return 10;
    }

}
