/*
 * @(#)AbstractAttributeSelector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

/**
 * An abstract "attribute selector" matches an element based on its attributes.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractAttributeSelector extends SimpleSelector {

    public AbstractAttributeSelector() {
    }

    @Override
    public final int getSpecificity() {
        return 10;
    }

}
