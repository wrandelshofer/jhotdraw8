/*
 * @(#)IdSelector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * An "id selector" matches an element if the element has an id with the
 * specified value.
 *
 * @author Werner Randelshofer
 */
public class IdSelector extends SimpleSelector {

    private final String id;

    public IdSelector(@Nullable SourceLocator sourceLocator, String id) {
        super(sourceLocator);
        this.id = id;
    }

    @Override
    public String toString() {
        return "Id:" + id;
    }

    @Override
    public @Nullable <T> T match(SelectorModel<T> model, @Nullable T element) {
        return (element != null && model.hasId(element, id)) //
                ? element : null;
    }

    @Override
    public int getSpecificity() {
        return 100;
    }

    @Override
    public void produceTokens(Consumer<CssToken> consumer) {
        consumer.accept(new CssToken(CssTokenType.TT_HASH, id));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdSelector that = (IdSelector) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
