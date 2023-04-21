/*
 * @(#)IncludeMatchSelector.java
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
 * An "include match selector" {@code ~=} matches an element if the element has
 * an attribute with the specified name and the attribute value contains a word
 * list with the specified word.
 *
 * @author Werner Randelshofer
 */
public class IncludeMatchSelector extends AbstractAttributeSelector {
    private final @Nullable String namespace;
    private final @NonNull String attributeName;
    private final @NonNull String word;

    public IncludeMatchSelector(@Nullable String namespace, @NonNull String attributeName, @NonNull String word) {
        this.namespace = namespace;
        this.attributeName = attributeName;
        this.word = word;
    }

    @Override
    protected @Nullable <T> T match(@NonNull SelectorModel<T> model, @NonNull T element) {
        return model.attributeValueContainsWord(element, namespace, attributeName, word) ? element : null;
    }

    @Override
    public @NonNull String toString() {
        return "[" + attributeName + "~=" + word + ']';
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
        consumer.accept(new CssToken(CssTokenType.TT_INCLUDE_MATCH));
        consumer.accept(new CssToken(CssTokenType.TT_STRING, word));
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
        IncludeMatchSelector that = (IncludeMatchSelector) o;
        return Objects.equals(namespace, that.namespace) && attributeName.equals(that.attributeName) && word.equals(that.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, attributeName, word);
    }
}
