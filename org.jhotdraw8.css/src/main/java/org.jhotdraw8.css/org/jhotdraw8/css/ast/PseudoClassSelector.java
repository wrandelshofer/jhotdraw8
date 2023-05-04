/*
 * @(#)PseudoClassSelector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.Nullable;

/**
 * A "pseudo class selector" matches an element based on criteria which are not
 * directly encoded in the element.
 *
 * @author Werner Randelshofer
 */
public abstract class PseudoClassSelector extends SimpleSelector {

    public PseudoClassSelector(@Nullable SourceLocator sourceLocator) {
        super(sourceLocator);
    }

    @Override
    public final int getSpecificity() {
        return 10;
    }

}
