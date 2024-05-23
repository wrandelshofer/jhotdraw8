/*
 * @(#)StyleRule.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A style rule associates a selector list to a list of declarations.
 *
 * @author Werner Randelshofer
 */
public class StyleRule extends Rule {

    private final SelectorGroup selectorList;
    private final ImmutableList<Declaration> declarations;

    public StyleRule(@Nullable SourceLocator sourceLocator, SelectorGroup selectorGroup, List<Declaration> declarations) {
        super(sourceLocator);
        this.selectorList = selectorGroup;
        this.declarations = VectorList.copyOf(declarations);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("StyleRule: ");
        buf.append(selectorList);
        buf.append("{");
        for (Declaration r : declarations) {
            buf.append(r.toString());
            buf.append(';');
        }
        buf.append("}");
        return buf.toString();
    }

    public SelectorGroup getSelectorGroup() {
        return selectorList;
    }

    public ImmutableList<Declaration> getDeclarations() {
        return declarations;
    }
}
