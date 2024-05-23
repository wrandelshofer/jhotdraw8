/*
 * @(#)TypeSelector.java
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
 * A "type selector" matches an element if the element has a specific type.
 *
 * @author Werner Randelshofer
 */
public class TypeSelector extends SimpleSelector {
    /**
     * Special namespace value which means 'any namespace'.
     * <p>
     * Value: {@value #ANY_NAMESPACE}
     */
    public static final String ANY_NAMESPACE = "*";
    /**
     * Special namespace value which means 'without a namespace'.
     * <p>
     * See <a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>.
     * <p>
     * Value: {@code null}
     */
    public static final @Nullable String WITHOUT_NAMESPACE = null;
    private final @Nullable String namespacePattern;
    private final String type;

    /**
     * Creates a new instance
     * @param sourceLocator source locator for debugging
     * @param namespacePattern an optional namespace ("*" means any namespace,
     *                         null means no namespace)
     * @param type the type name
     */
    public TypeSelector(@Nullable SourceLocator sourceLocator, @Nullable String namespacePattern, String type) {
        super(sourceLocator);
        this.namespacePattern = namespacePattern;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Type:"
                + (namespacePattern == null ? "" : namespacePattern + "|")
                + type;
    }

    @Override
    public @Nullable <T> T match(SelectorModel<T> model, @Nullable T element) {
        return (element != null && model.hasType(element, namespacePattern, type)) //
                ? element : null;
    }

    @Override
    public int getSpecificity() {
        return 1;
    }

    @Override
    public void produceTokens(Consumer<CssToken> consumer) {
        if (!ANY_NAMESPACE.equals(namespacePattern)) {
            if (namespacePattern != null) {
                consumer.accept(new CssToken(CssTokenType.TT_IDENT, namespacePattern));
            }
            consumer.accept(new CssToken(CssTokenType.TT_VERTICAL_LINE));
        }
        consumer.accept(new CssToken(CssTokenType.TT_IDENT, type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeSelector that = (TypeSelector) o;
        return Objects.equals(namespacePattern, that.namespacePattern) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespacePattern, type);
    }

    public @Nullable String getNamespacePattern() {
        return namespacePattern;
    }

    public String getType() {
        return type;
    }

    @Override
    public @Nullable TypeSelector matchesOnlyOnASpecificType() {
        return this;
    }
}
