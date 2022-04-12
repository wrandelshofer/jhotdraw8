/*
 * @(#)TypeSelector.java
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
 * A "type selector" matches an element if the element has a specific type.
 *
 * @author Werner Randelshofer
 */
public class TypeSelector extends SimpleSelector {
    private final @Nullable String namespacePattern;
    private final @NonNull String type;

    /**
     * @param namespacePattern null means no namespace,
     *                         {@link SelectorModel#ANY_NAMESPACE} means any namespace
     * @param type
     */
    public TypeSelector(@Nullable String namespacePattern, @NonNull String type) {
        this.namespacePattern = namespacePattern;
        this.type = type;
    }

    @Override
    public @NonNull String toString() {
        return "Type:"
                + (namespacePattern == null ? "" : namespacePattern + "|")
                + type;
    }

    @Override
    public @Nullable <T> T match(@NonNull SelectorModel<T> model, @Nullable T element) {
        return (element != null && model.hasType(element, namespacePattern, type)) //
                ? element : null;
    }

    @Override
    public int getSpecificity() {
        return 1;
    }

    @Override
    public void produceTokens(@NonNull Consumer<CssToken> consumer) {
        if (!SelectorModel.ANY_NAMESPACE.equals(namespacePattern)) {
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

    public String getNamespacePattern() {
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
