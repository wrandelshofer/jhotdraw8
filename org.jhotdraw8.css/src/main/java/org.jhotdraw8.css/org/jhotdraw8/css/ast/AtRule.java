/*
 * @(#)AtRule.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An "at-rule" consists of an "at-keyword", a list of header tokens and a
 * list of body tokens.
 *
 */
public class AtRule extends Rule {
    private final String atKeyword;
    private final PersistentList<CssToken> header;
    private final PersistentList<CssToken> body;

    /**
     * Creates a new instance.
     *
     * @param sourceLocator
     * @param atKeyword     the "at-keyword"
     * @param header        the list of header tokens
     * @param body          the list of body tokens
     */
    public AtRule(@Nullable SourceLocator sourceLocator, String atKeyword,
                  List<? extends CssToken> header, List<? extends CssToken> body) {
        super(sourceLocator);
        this.atKeyword = atKeyword;
        this.header = VectorList.copyOf(header);
        this.body = VectorList.copyOf(body);
    }

    @Override
    public String toString() {
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
    public String getAtKeyword() {
        return atKeyword;
    }

    /**
     * Gets the list of header tokens.
     *
     * @return the header tokens
     */
    public ReadableList<CssToken> getHeader() {
        return header;
    }

    /**
     * Gets the list of body tokens.
     *
     * @return the body tokens
     */
    public ReadableList<CssToken> getBody() {
        return body;
    }

}
