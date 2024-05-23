/*
 * @(#)SubstringMatchSelector.java
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
 * A "substring match selector" {@code *=} matches an element if the element has
 * an attribute with the specified name and its value contains the specified
 * substring.
 *
 * @author Werner Randelshofer
 */
public class SubstringMatchSelector extends AbstractAttributeSelector {
    private final @Nullable String namespacePattern;
    private final String attributeName;
    private final String substring;

    /**
     * Creates a new instance.
     *
     * @param sourceLocator source locator for debugging
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param attributeName the attribute name
     * @param substring the substring in the attribute value
     */
    public SubstringMatchSelector(@Nullable SourceLocator sourceLocator, @Nullable String namespacePattern, String attributeName, String substring) {
        super(sourceLocator);
        this.namespacePattern = namespacePattern;
        this.attributeName = attributeName;
        this.substring = substring;
    }

    @Override
    protected @Nullable <T> T match(SelectorModel<T> model, T element) {
        return (model.attributeValueContains(element, namespacePattern, attributeName, substring))//
                ? element : null;
    }

    @Override
    public String toString() {
        return "[" + attributeName + "*=" + substring + ']';
    }

    @Override
    public void produceTokens(Consumer<CssToken> consumer) {
        consumer.accept(new CssToken(CssTokenType.TT_LEFT_SQUARE_BRACKET));
        if (!TypeSelector.ANY_NAMESPACE.equals(namespacePattern)) {
            if (namespacePattern != null) {
                consumer.accept(new CssToken(CssTokenType.TT_IDENT, namespacePattern));
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
        return Objects.equals(namespacePattern, that.namespacePattern) && attributeName.equals(that.attributeName) && substring.equals(that.substring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespacePattern, attributeName, substring);
    }
}
