/*
 * @(#)ListCssTokenizer.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.parser;

import org.jhotdraw8.css.ast.SourceLocator;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static org.jhotdraw8.css.parser.CssTokenType.TT_BAD_COMMENT;
import static org.jhotdraw8.css.parser.CssTokenType.TT_CDC;
import static org.jhotdraw8.css.parser.CssTokenType.TT_CDO;
import static org.jhotdraw8.css.parser.CssTokenType.TT_COMMENT;
import static org.jhotdraw8.css.parser.CssTokenType.TT_S;

public class ListCssTokenizer implements CssTokenizer {
    private final PersistentList<CssToken> in;
    private int index = 0;
    private boolean pushBack = true;
    private @Nullable CssToken current;
    private static final CssToken EOF = new CssToken(CssTokenType.TT_EOF);

    public ListCssTokenizer(List<CssToken> in) {
        this(VectorList.copyOf(in));
    }

    public ListCssTokenizer(ReadableList<CssToken> in) {
        this.in = VectorList.copyOf(in);
        current = in.isEmpty() ? EOF : in.get(0);
    }

    @Override
    public @Nullable Number currentNumber() {
        return current.getNumericValue();
    }

    @Override
    public @Nullable String currentString() {
        return current.getStringValue();
    }

    @Override
    public int current() {
        return current.getType();
    }

    @Override
    public int getLineNumber() {
        return current.getLineNumber();
    }

    @Override
    public @Nullable SourceLocator getSourceLocator() {
        return new SourceLocator(getStartPosition(), getLineNumber(), null);
    }

    @Override
    public int getStartPosition() {
        return current.getStartPos();
    }

    @Override
    public int getEndPosition() {
        return current.getEndPos();
    }

    @Override
    public int getNextPosition() {
        if (pushBack) {
            return current.getStartPos();
        } else {
            return current.getEndPos();
        }
    }

    @Override
    public int next() {
        skipWhitespace();
        while (skipComment()) {
            skipWhitespace();
        }
        return nextNoSkip();
    }

    @Override
    public int nextNoSkip() {
        if (pushBack) {
            pushBack = false;
        } else {
            index++;
            if (index < in.size()) {
                current = in.get(index);
            } else {
                current = EOF;
            }
        }
        return current.getType();
    }


    private void skipWhitespace() {
        while (nextNoSkip() == TT_S//
                || current.getType() == TT_CDC//
                || current.getType() == TT_CDO) {
        }
        pushBack();
    }

    private boolean skipComment() {
        boolean didSkip = false;
        while (nextNoSkip() == TT_COMMENT//
                || current.getType() == TT_BAD_COMMENT) {
            didSkip = true;
        }
        pushBack();
        return didSkip;
    }

    @Override
    public void pushBack() {
        pushBack = true;
    }


    @Override
    public CssToken getToken() {
        return current;
    }
}
