/*
 * @(#)StyleRule.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;

import java.util.List;

/**
 * A style rule associates a selector list to a list of declarations.
 *
 * @author Werner Randelshofer
 */
public class StyleRule extends Rule {

    private final @NonNull SelectorGroup selectorList;
    private final @NonNull ImmutableList<Declaration> declarations;

    public StyleRule(@Nullable SourceLocator sourceLocator, @NonNull SelectorGroup selectorGroup, @NonNull List<Declaration> declarations) {
        super(sourceLocator);
        this.selectorList = selectorGroup;
        this.declarations = VectorList.copyOf(declarations);
    }

    @Override
    public @NonNull String toString() {
        StringBuilder buf = new StringBuilder("StyleRule: ");
        buf.append(selectorList.toString());
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

    public @NonNull ImmutableList<Declaration> getDeclarations() {
        return declarations;
    }
}
