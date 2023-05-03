/*
 * @(#)SubstringMatchSelector.java
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
 * A "substring match selector" {@code *=} matches an element if the element has
 * an attribute with the specified name and its value contains the specified
 * substring.
 *
 * @author Werner Randelshofer
 */
public class SubstringMatchSelector extends AbstractAttributeSelector {
    private final @Nullable String namespace;
    private final @NonNull String attributeName;
    private final @NonNull String substring;

    public SubstringMatchSelector(@Nullable SourceLocator sourceLocator, @Nullable String namespace, @NonNull String attributeName, @NonNull String substring) {
        super(sourceLocator);
        this.namespace = namespace;
        this.attributeName = attributeName;
        this.substring = substring;
    }

    @Override
    protected @Nullable <T> T match(@NonNull SelectorModel<T> model, @NonNull T element) {
        return (model.attributeValueContains(element, namespace, attributeName, substring))//
                ? element : null;
    }

    @Override
    public @NonNull String toString() {
        return "[" + attributeName + "*=" + substring + ']';
    }

    @Override
    public void produceTokens(@NonNull Consumer<CssToken> consumer) {
        consumer.accept(new CssToken(CssTokenType.TT_LEFT_SQUARE_BRACKET));
        if (!TypeSelector.ANY_NAMESPACE.equals(namespace)) {
            if (namespace != null) {
                consumer.accept(new CssToken(CssTokenType.TT_IDENT, namespace));
            }
            consumer.accept(new CssToken(CssTokenType.TT_VERTICAL_LINE));
        }
            consumer.accept(new CssToken(CssTokenType.TT_IDENT, attributeName));
        consumer.accept(new CssToken(CssTokenType.TT_SUBSTRING_MATCH));
        consumer.accept(new CssToken(CssTokenType.TT_STRING, substring));
        consumer.accept(new CssToken(CssTokenType.TT_RIGHT_SQUARE_BRACKET));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubstringMatchSelector that = (SubstringMatchSelector) o;
        return Objects.equals(namespace, that.namespace) && attributeName.equals(that.attributeName) && substring.equals(that.substring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, attributeName, substring);
    }
}
