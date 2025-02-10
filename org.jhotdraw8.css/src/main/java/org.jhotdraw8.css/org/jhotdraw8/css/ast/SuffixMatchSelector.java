/*
 * @(#)SuffixMatchSelector.java
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
 * A "suffix match selector" {@code $=} matches an element if the element has an
 * attribute with the specified name and its value ends with the specified
 * substring.
 *
 */
public class SuffixMatchSelector extends AbstractAttributeSelector {
    private final @Nullable String namespacePattern;
    private final String attributeName;
    private final String suffix;

    /**
     * Creates a new instance.
     *
     * @param sourceLocator source locator for debugging
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param attributeName the attribute name
     * @param suffix the suffix of the attribute value
     */
    public SuffixMatchSelector(@Nullable SourceLocator sourceLocator, @Nullable String namespacePattern, String attributeName, String suffix) {
        super(sourceLocator);
        this.namespacePattern = namespacePattern;
        this.attributeName = attributeName;
        this.suffix = suffix;
    }

    @Override
    protected @Nullable <T> T match(SelectorModel<T> model, T element) {
        return (model.attributeValueEndsWith(element, namespacePattern, attributeName, suffix))//
                ? element : null;
    }

    @Override
    public String toString() {
        return "[" + attributeName + "&=" + suffix + ']';
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
        consumer.accept(new CssToken(CssTokenType.TT_SUFFIX_MATCH));
        consumer.accept(new CssToken(CssTokenType.TT_STRING, suffix));
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
        SuffixMatchSelector that = (SuffixMatchSelector) o;
        return Objects.equals(namespacePattern, that.namespacePattern) && attributeName.equals(that.attributeName) && suffix.equals(that.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespacePattern, attributeName, suffix);
    }
}
