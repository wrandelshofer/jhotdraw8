/*
 * @(#)PrefixMatchSelector.java
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
 * A "prefix match selector" {@code ^=} matches an element if the element has an
 * attribute with the specified name and its value starts with the specified
 * substring.
 *
 */
public class PrefixMatchSelector extends AbstractAttributeSelector {
    private final @Nullable String namespacePattern;
    private final String attributeName;
    private final String prefix;

    /**
     * Creates a new instance.
     *
     * @param sourceLocator    source locator for debugging
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param attributeName    the attribute name
     * @param prefix           the attribute value prefix
     */
    public PrefixMatchSelector(@Nullable SourceLocator sourceLocator, @Nullable String namespacePattern, String attributeName, String prefix) {
        super(sourceLocator);
        this.namespacePattern = namespacePattern;
        this.attributeName = attributeName;
        this.prefix = prefix;
    }

    @Override
    protected @Nullable <T> T match(SelectorModel<T> model, T element) {
        return (model.attributeValueStartsWith(element, namespacePattern, attributeName, prefix))//
                ? element : null;
    }

    @Override
    public String toString() {
        return "[" + attributeName + "^=" + prefix + ']';
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
        consumer.accept(new CssToken(CssTokenType.TT_PREFIX_MATCH));
        consumer.accept(new CssToken(CssTokenType.TT_STRING, prefix));
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
        PrefixMatchSelector that = (PrefixMatchSelector) o;
        return Objects.equals(namespacePattern, that.namespacePattern) && attributeName.equals(that.attributeName) && prefix.equals(that.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespacePattern, attributeName, prefix);
    }
}
