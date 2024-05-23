/*
 * @(#)SimpleSelector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jspecify.annotations.Nullable;

/**
 * Abstract superclass for "simple selector"s.
 *
 * @author Werner Randelshofer
 */
public abstract class SimpleSelector extends Selector {

    public SimpleSelector(@Nullable SourceLocator sourceLocator) {
        super(sourceLocator);
    }
}
