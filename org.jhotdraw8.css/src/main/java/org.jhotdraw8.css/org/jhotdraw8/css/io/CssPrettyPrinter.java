/*
 * @(#)CssPrettyPrinter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.io;

import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.StreamCssTokenizer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class CssPrettyPrinter implements Appendable {
    private enum Syntax {
        STYLESHEET, SELECTOR, DECLARATION_KEY, DECLARATION_VALUE, ROUND_BLOCK, SQUARE_BLOCK, CURLY_BLOCK
    }

    private final Deque<Syntax> stack = new ArrayDeque<>();
    int indentation = 0;
    private String indenter = "\t";
    private final Appendable w;
    private boolean mustIndent = false;

    public CssPrettyPrinter(Appendable w) {
        this.w = w;
        stack.push(Syntax.STYLESHEET);
    }

    @Override
    public CssPrettyPrinter append(CharSequence str) {
        try {
            print(str);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    @Override
    public CssPrettyPrinter append(CharSequence csq, int start, int end) {
        append(csq.subSequence(start, end));
        return this;
    }

    @Override
    public CssPrettyPrinter append(char c) {
        return append("" + c);
    }

    public String getIndenter() {
        return indenter;
    }

    public void setIndenter(String indenter) {
        this.indenter = indenter;
    }

    public void print(CharSequence str) throws IOException {
        StreamCssTokenizer tt = new StreamCssTokenizer(str, null);
        print(tt);
    }

    public void print(CssTokenizer tt) throws IOException {

        while (tt.nextNoSkip() != CssTokenType.TT_EOF) {
            CssToken token = tt.getToken();

            int oldsize = stack.size();
            parseToken(token);
            // increase has only an effect on the next token
            // decrease (or no change) affects the current token
            indentation = Math.min(stack.size(), oldsize);
            //
            switch (token.getType()) {
            case CssTokenType.TT_S:
                if (token.getStringValueNonNull().indexOf('\n') < 0) {
                    if (!mustIndent) {
                        w.append(" ");
                    }
                } else {
                    w.append('\n');
                    mustIndent = true;
                }
                continue;
            case CssTokenType.TT_FUNCTION:
            case CssTokenType.TT_LEFT_BRACKET:
            case CssTokenType.TT_LEFT_CURLY_BRACKET:
            case CssTokenType.TT_LEFT_SQUARE_BRACKET:
            case CssTokenType.TT_RIGHT_BRACKET:
            case CssTokenType.TT_RIGHT_CURLY_BRACKET:
            case CssTokenType.TT_RIGHT_SQUARE_BRACKET:
            case CssTokenType.TT_SEMICOLON:
            case CssTokenType.TT_COLON:
            case CssTokenType.TT_AT_KEYWORD:
            case CssTokenType.TT_BAD_COMMENT:
            case CssTokenType.TT_BAD_STRING:
            case CssTokenType.TT_BAD_URI:
            case CssTokenType.TT_CDC:
            case CssTokenType.TT_CDO:
            case CssTokenType.TT_COLUMN:
            case CssTokenType.TT_COMMA:
            case CssTokenType.TT_COMMENT:
            case CssTokenType.TT_DASH_MATCH:
            case CssTokenType.TT_DIMENSION:
            case CssTokenType.TT_EOF:
            case CssTokenType.TT_HASH:
            case CssTokenType.TT_IDENT:
            case CssTokenType.TT_INCLUDE_MATCH:
            case CssTokenType.TT_NUMBER:
            case CssTokenType.TT_PERCENT_DELIM:
            case CssTokenType.TT_PERCENTAGE:
            case CssTokenType.TT_PLUS:
            case CssTokenType.TT_PREFIX_MATCH:
            case CssTokenType.TT_SLASH:
            case CssTokenType.TT_STRING:
            case CssTokenType.TT_SUBSTRING_MATCH:
            case CssTokenType.TT_SUFFIX_MATCH:
            case CssTokenType.TT_UNICODE_RANGE:
            case CssTokenType.TT_URL:
            case CssTokenType.TT_VERTICAL_LINE:
                break;
            }

            if (mustIndent) {
                indent();
                mustIndent = false;
            }
            w.append(token.fromToken());
        }
    }

    private void indent() throws IOException {
        for (int i = 1; i < indentation; i++) {
            w.append(indenter);
        }
    }

    private void parseToken(CssToken token) {
        switch (token.getType()) {
            case CssTokenType.TT_S, CssTokenType.TT_AT_KEYWORD, CssTokenType.TT_BAD_COMMENT, CssTokenType.TT_BAD_STRING, CssTokenType.TT_BAD_URI, CssTokenType.TT_CDC, CssTokenType.TT_CDO, CssTokenType.TT_COLUMN, CssTokenType.TT_COMMA, CssTokenType.TT_COMMENT, CssTokenType.TT_DASH_MATCH, CssTokenType.TT_DIMENSION, CssTokenType.TT_EOF, CssTokenType.TT_HASH, CssTokenType.TT_INCLUDE_MATCH, CssTokenType.TT_NUMBER, CssTokenType.TT_PERCENT_DELIM, CssTokenType.TT_PERCENTAGE, CssTokenType.TT_PLUS, CssTokenType.TT_PREFIX_MATCH, CssTokenType.TT_SLASH, CssTokenType.TT_STRING, CssTokenType.TT_SUBSTRING_MATCH, CssTokenType.TT_SUFFIX_MATCH, CssTokenType.TT_UNICODE_RANGE, CssTokenType.TT_URL, CssTokenType.TT_VERTICAL_LINE:
            break;
        case CssTokenType.TT_FUNCTION:
        case CssTokenType.TT_LEFT_BRACKET:
            stack.push(Syntax.ROUND_BLOCK);
            break;
        case CssTokenType.TT_LEFT_CURLY_BRACKET:
            stack.push(Syntax.CURLY_BLOCK);
            break;
        case CssTokenType.TT_LEFT_SQUARE_BRACKET:
            stack.push(Syntax.SQUARE_BLOCK);
            break;
        case CssTokenType.TT_RIGHT_BRACKET:
            while (stack.size() > 1 && stack.pop() != Syntax.ROUND_BLOCK) {
            }
            break;
        case CssTokenType.TT_RIGHT_CURLY_BRACKET:
            while (stack.size() > 1 && stack.pop() != Syntax.CURLY_BLOCK) {
            }
            break;
        case CssTokenType.TT_RIGHT_SQUARE_BRACKET:
            while (stack.size() > 1 && stack.pop() == Syntax.SQUARE_BLOCK) {
            }
            break;
        case CssTokenType.TT_SEMICOLON:
            while (stack.size() > 1 && stack.pop() != Syntax.DECLARATION_VALUE) {
            }
            break;
        case CssTokenType.TT_COLON:
            if (stack.peek() == Syntax.DECLARATION_KEY) {
                stack.pop();
                stack.push(Syntax.DECLARATION_VALUE);
            }
            break;
        case CssTokenType.TT_IDENT:
            if (stack.peek() == Syntax.CURLY_BLOCK) {
                stack.push(Syntax.DECLARATION_KEY);
            }
            break;
        }
    }
}
