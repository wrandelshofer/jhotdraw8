/*
 * @(#)ClassSelector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A "class selector" matches an element if the element has a style class with
 * the specified value.
 *
 * @author Werner Randelshofer
 */
public class ClassSelector extends SimpleSelector {

    private final @NonNull String clazz;

    public ClassSelector(@NonNull String clazz) {
        this.clazz = clazz;
    }

    @Override
    public @NonNull String toString() {
        return "Class:" + clazz;
    }

    @Override
    public @Nullable <T> T match(@NonNull SelectorModel<T> model, @Nullable T element) {
        return (element != null && model.hasStyleClass(element, clazz)) //
                ? element : null;
    }

    @Override
    public int getSpecificity() {
        return 10;
    }

    @Override
    public void produceTokens(@NonNull Consumer<CssToken> consumer) {
        consumer.accept(new CssToken(CssTokenType.TT_POINT));
        consumer.accept(new CssToken(CssTokenType.TT_IDENT, clazz));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassSelector that = (ClassSelector) o;
        return clazz.equals(that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz);
    }
}
