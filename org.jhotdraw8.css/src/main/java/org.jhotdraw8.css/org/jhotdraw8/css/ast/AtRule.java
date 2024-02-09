/*
 * @(#)AtRule.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;

import java.util.List;

/**
 * An "at-rule" consists of an "at-keyword", a list of header tokens and a
 * list of body tokens.
 *
 * @author Werner Randelshofer
 */
public class AtRule extends Rule {
    private final @NonNull String atKeyword;
    private final @NonNull ImmutableList<CssToken> header;
    private final @NonNull ImmutableList<CssToken> body;

    /**
     * Creates a new instance.
     *
     * @param sourceLocator
     * @param atKeyword     the "at-keyword"
     * @param header        the list of header tokens
     * @param body          the list of body tokens
     */
    public AtRule(@Nullable SourceLocator sourceLocator, @NonNull String atKeyword,
                  @NonNull List<? extends CssToken> header, @NonNull List<? extends CssToken> body) {
        super(sourceLocator);
        this.atKeyword = atKeyword;
        this.header = VectorList.copyOf(header);
        this.body = VectorList.copyOf(body);
    }

    @Override
    public @NonNull String toString() {
        StringBuilder buf = new StringBuilder("AtRule: ");
        buf.append(atKeyword);
        if (!header.isEmpty()) {
            buf.append(" ");
            for (CssToken t : header) {
                buf.append(t.fromToken());
            }
        }
        if (!header.isEmpty() && !body.isEmpty()) {
            buf.append(" ");
        }
        if (!body.isEmpty()) {
            buf.append("{");
            for (CssToken t : body) {
                buf.append(t.fromToken());
            }
            buf.append("}");
        }
        return buf.toString();
    }

    /**
     * Gets the "at-keyword".
     *
     * @return the "at-keyword".
     */
    public @NonNull String getAtKeyword() {
        return atKeyword;
    }

    /**
     * Gets the list of header tokens.
     *
     * @return the header tokens
     */
    public @NonNull ReadOnlyList<CssToken> getHeader() {
        return header;
    }

    /**
     * Gets the list of body tokens.
     *
     * @return the body tokens
     */
    public @NonNull ReadOnlyList<CssToken> getBody() {
        return body;
    }

}
