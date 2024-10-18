/*
 * @(#)Declaration.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A "declaration" associates a "propertyName" with a list of preserved tokens. If
 * the list of preserved tokens is empty, the declaration must be ignored.
 *
 * @author Werner Randelshofer
 */
public class Declaration extends AbstractSyntaxTree {
    private final @Nullable String namespace;
    private final String propertyName;
    private final PersistentList<CssToken> terms;
    private final int startPos;
    private final int endPos;
    private final int lineNumber;

    public Declaration(@Nullable SourceLocator sourceLocator, @Nullable String namespace, String propertyName, List<CssToken> terms, int startPos, int endPos, int lineNumber) {
        super(sourceLocator);
        this.namespace = namespace;
        this.propertyName = propertyName;
        this.terms = VectorList.copyOf(terms);
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

    public String getPropertyName() {
        return propertyName;
    }

    public PersistentList<CssToken> getTerms() {
        return terms;
    }

    public String getTermsAsString() {
        StringBuilder buf = new StringBuilder();

        for (CssToken t : terms) {
            buf.append(t.toString());
        }
        return buf.toString();
    }

    @Override
    public String toString() {

        return propertyName + ":" + getTermsAsString();
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

}
