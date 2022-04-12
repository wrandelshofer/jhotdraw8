/*
 * @(#)EqualsMatchSelector.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.CssToken;
import org.jhotdraw8.css.CssTokenType;
import org.jhotdraw8.css.SelectorModel;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * An "attribute value selector" matches an element if the element has an
 * attribute with the specified name and value.
 *
 * @author Werner Randelshofer
 */
public class EqualsMatchSelector extends AbstractAttributeSelector {
    private final @Nullable String namespace;
    private final @NonNull String attributeName;
    private final @NonNull String attributeValue;

    public EqualsMatchSelector(@Nullable String namespace, @NonNull String attributeName, @NonNull String attributeValue) {
        this.namespace = namespace;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    @Override
    protected @Nullable <T> T match(@NonNull SelectorModel<T> model, @NonNull T element) {
        return model.attributeValueEquals(element, namespace, attributeName, attributeValue) ? element : null;
    }

    @Override
    public @NonNull String toString() {
        return "[" + attributeName + "=\"" + attributeValue + "\"]";
    }

    @Override
    public void produceTokens(@NonNull Consumer<CssToken> consumer) {
        consumer.accept(new CssToken(CssTokenType.TT_LEFT_SQUARE_BRACKET));
        if (!SelectorModel.ANY_NAMESPACE.equals(namespace)) {
            if (namespace != null) {
                consumer.accept(new CssToken(CssTokenType.TT_IDENT, namespace));
            }
            consumer.accept(new CssToken(CssTokenType.TT_VERTICAL_LINE));
        }
        consumer.accept(new CssToken(CssTokenType.TT_IDENT, attributeName));
        consumer.accept(new CssToken(CssTokenType.TT_EQUALS));
        consumer.accept(new CssToken(CssTokenType.TT_STRING, attributeValue));
        consumer.accept(new CssToken(CssTokenType.TT_RIGHT_SQUARE_BRACKET));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EqualsMatchSelector that = (EqualsMatchSelector) o;
        return Objects.equals(namespace, that.namespace) && attributeName.equals(that.attributeName) && attributeValue.equals(that.attributeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, attributeName, attributeValue);
    }
}
