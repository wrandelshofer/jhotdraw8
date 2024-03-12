/*
 * @(#)ExistsMatchSelector.java
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
 * An "exists match" matches an element if the element has an attribute with the
 * specified name.
 *
 * @author Werner Randelshofer
 */
public class ExistsMatchSelector extends AbstractAttributeSelector {
    private final @Nullable String namespacePattern;
    private final @NonNull String attributeName;

    /**
     * Creates a new instance.
     *
     * @param sourceLocator source locator for debugging
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param attributeName the attribute name
     */
    public ExistsMatchSelector(@Nullable SourceLocator sourceLocator, @Nullable String namespacePattern, @NonNull String attributeName) {
        super(sourceLocator);
        this.namespacePattern = namespacePattern;
        this.attributeName = attributeName;
    }

    @Override
    protected @Nullable <T> T match(@NonNull SelectorModel<T> model, @NonNull T element) {
        return model.hasAttribute(element, namespacePattern, attributeName) ? element : null;
    }

    @Override
    public @NonNull String toString() {
        return "[" + namespacePattern + ":" + attributeName + ']';
    }

    @Override
    public void produceTokens(@NonNull Consumer<CssToken> consumer) {
        consumer.accept(new CssToken(CssTokenType.TT_LEFT_SQUARE_BRACKET));
        if (!TypeSelector.ANY_NAMESPACE.equals(namespacePattern)) {
            if (namespacePattern != null) {
                consumer.accept(new CssToken(CssTokenType.TT_IDENT, namespacePattern));
            }
            consumer.accept(new CssToken(CssTokenType.TT_VERTICAL_LINE));
        }
        consumer.accept(new CssToken(CssTokenType.TT_IDENT, attributeName));
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
        ExistsMatchSelector that = (ExistsMatchSelector) o;
        return Objects.equals(namespacePattern, that.namespacePattern) && attributeName.equals(that.attributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespacePattern, attributeName);
    }
}
