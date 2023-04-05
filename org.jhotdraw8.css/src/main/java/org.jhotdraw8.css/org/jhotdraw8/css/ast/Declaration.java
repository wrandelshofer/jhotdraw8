/*
 * @(#)Declaration.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.css.parser.CssToken;

import java.util.List;

/**
 * A "declaration" associates a "propertyName" with a list of preserved tokens. If
 * the list of preserved tokens is empty, the declaration must be ignored.
 *
 * @author Werner Randelshofer
 */
public class Declaration extends AbstractSyntaxTree {
    private final @Nullable String namespace;
    private final @NonNull String propertyName;
    private final @NonNull ImmutableList<CssToken> terms;
    private final int startPos;
    private final int endPos;
    private final int lineNumber;

    public Declaration(@Nullable String namespace, @NonNull String propertyName, @NonNull List<CssToken> terms, int startPos, int endPos, int lineNumber) {
        super(null);
        this.namespace = namespace;
        this.propertyName = propertyName;
        this.terms = ImmutableArrayList.copyOf(terms);
        this.startPos = startPos;
        this.endPos = endPos;
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public @Nullable String getNamespace() {
        return namespace;
    }

    public @NonNull String getPropertyName() {
        return propertyName;
    }

    public @NonNull ImmutableList<CssToken> getTerms() {
        return terms;
    }

    public @NonNull String getTermsAsString() {
        StringBuilder buf = new StringBuilder();

        for (CssToken t : terms) {
            buf.append(t.toString());
        }
        return buf.toString();
    }

    @Override
    public @NonNull String toString() {

        return propertyName + ":" + getTermsAsString();
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

}
