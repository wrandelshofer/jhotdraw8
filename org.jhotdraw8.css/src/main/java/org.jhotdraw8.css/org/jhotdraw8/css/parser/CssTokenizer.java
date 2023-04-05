/*
 * @(#)CssTokenizer.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.parser;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.ast.SourceLocator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.jhotdraw8.css.parser.CssTokenType.TT_AT_KEYWORD;
import static org.jhotdraw8.css.parser.CssTokenType.TT_BAD_COMMENT;
import static org.jhotdraw8.css.parser.CssTokenType.TT_BAD_STRING;
import static org.jhotdraw8.css.parser.CssTokenType.TT_BAD_URI;
import static org.jhotdraw8.css.parser.CssTokenType.TT_CDC;
import static org.jhotdraw8.css.parser.CssTokenType.TT_CDO;
import static org.jhotdraw8.css.parser.CssTokenType.TT_COLUMN;
import static org.jhotdraw8.css.parser.CssTokenType.TT_COMMENT;
import static org.jhotdraw8.css.parser.CssTokenType.TT_DASH_MATCH;
import static org.jhotdraw8.css.parser.CssTokenType.TT_DIMENSION;
import static org.jhotdraw8.css.parser.CssTokenType.TT_EOF;
import static org.jhotdraw8.css.parser.CssTokenType.TT_FUNCTION;
import static org.jhotdraw8.css.parser.CssTokenType.TT_HASH;
import static org.jhotdraw8.css.parser.CssTokenType.TT_IDENT;
import static org.jhotdraw8.css.parser.CssTokenType.TT_INCLUDE_MATCH;
import static org.jhotdraw8.css.parser.CssTokenType.TT_NUMBER;
import static org.jhotdraw8.css.parser.CssTokenType.TT_PERCENTAGE;
import static org.jhotdraw8.css.parser.CssTokenType.TT_PREFIX_MATCH;
import static org.jhotdraw8.css.parser.CssTokenType.TT_S;
import static org.jhotdraw8.css.parser.CssTokenType.TT_STRING;
import static org.jhotdraw8.css.parser.CssTokenType.TT_SUBSTRING_MATCH;
import static org.jhotdraw8.css.parser.CssTokenType.TT_SUFFIX_MATCH;
import static org.jhotdraw8.css.parser.CssTokenType.TT_UNICODE_RANGE;
import static org.jhotdraw8.css.parser.CssTokenType.TT_URL;

/**
 * Defines the API of a CSS Tokenizer.
 *
 * @author Werner Randelshofer
 */
public interface CssTokenizer {


    /**
     * Returns the current value converted to a string.
     * The returned value can be used for String comparisons of the value.
     *
     * @return the current value
     */
    default @Nullable String currentValue() {
        switch (current()) {
        case TT_AT_KEYWORD:
            return "@" + currentString();
        case TT_BAD_COMMENT:
            return "bad comment";
        case TT_BAD_STRING:
            return "bad string";
        case TT_BAD_URI:
            return "bad uri";
        case TT_CDC:
            return "<!--";
        case TT_CDO:
            return "-->";
        case TT_COLUMN:
            return "|";
        case TT_COMMENT:
            return currentString();
        case TT_DASH_MATCH:
            return "|=";
        case TT_DIMENSION:
            return currentNumber() + currentString();
        case TT_EOF:
            return "eof";
        case TT_FUNCTION:
            return currentString();
        case TT_HASH:
            return currentString();
        case TT_IDENT:
            return currentString();
        case TT_INCLUDE_MATCH:
            return "~=";
        case TT_NUMBER:
            return "" + currentNumber();
        case TT_PERCENTAGE:
            return currentNumber() + "%";
        case TT_PREFIX_MATCH:
            return "^=";
        case TT_S:
            return " ";
        case TT_STRING:
            return currentString();
        case TT_SUBSTRING_MATCH:
            return "*=";
        case TT_SUFFIX_MATCH:
            return "$=";
        case TT_UNICODE_RANGE:
            return currentString();
        case TT_URL:
            return currentString();
        default:
            return Character.toString((char) current());
        }
    }

    @Nullable
    Number currentNumber();

    default @NonNull Number currentNumberNonNull() {
        Number number = currentNumber();
        if (number == null) {
            throw new AssertionError("currentNumber");
        }
        return number;
    }

    /**
     * Returns the current string value.
     *
     * @return the current string value
     */
    @Nullable
    String currentString();

    default @NonNull String currentStringNonNull() {
        String str = currentString();
        if (str == null) {
            throw new AssertionError("currentString");
        }
        return str;
    }

    /**
     * Returns the current token type.
     *
     * @return the current token type
     */
    int current();

    int getLineNumber();

    @Nullable SourceLocator getSourceLocator();

    int getStartPosition();

    int getEndPosition();

    /**
     * Gets the current position.
     *
     * @return the start position of the token if a token has been pushed back,
     * the end position of the token otherwise
     */
    int getNextPosition();


    /**
     * Gets the next token skipping whitespaces and comments.
     *
     * @return the next non-whitespace token
     * @throws IOException on io exception
     */
    int next() throws IOException;

    /**
     * Gets the next token without skipping whitespaces and comments.
     *
     * @return the next token
     * @throws IOException on io exception
     */
    int nextNoSkip() throws IOException;

    /**
     * Skips the next token if it is of type {@code ttype}.
     *
     * @param ttype the token type t skip
     * @throws IOException
     */
    default void skipIfPresent(int ttype) throws IOException {
        if (next() != ttype) {
            pushBack();
        }
    }

    /**
     * Invokes {@link #next()} and checks if it is a "none" identifier.
     *
     * @return true if "none"
     * @throws IOException on io error
     */
    default boolean nextIsIdentNone() throws IOException {
        return next() == CssTokenType.TT_IDENT && currentStringNonNull().equals(CssTokenType.IDENT_NONE);
    }

    /**
     * Fetches the next token and throws a parse exception if it
     * is not of the required type.
     *
     * @param ttype   the required token type
     * @param message the error message
     * @throws ParseException if the token is not of the required type
     * @throws IOException    on IO exception
     */
    default void requireNextToken(int ttype, String message) throws ParseException, IOException {
        if (next() != ttype) {
            throw createParseException(message);
        }
    }

    /**
     * Creates a parse exception which contains the specified message,
     * the token that was found, and the current position of the tokenizier.
     *
     * @param message the message
     * @return a new parse exception
     */
    default ParseException createParseException(String message) {
        return new ParseException(message + " Found: '" + getToken() + "'.", getStartPosition());
    }

    /**
     * Fetches the next token and throws a parse exception if it
     * is not of the required type.
     *
     * @param ttype   the required token type
     * @param message the error message
     * @throws ParseException if the token is not of the required type
     * @throws IOException    on IO exception
     */
    default void requireNextNoSkip(int ttype, String message) throws ParseException, IOException {
        if (nextNoSkip() != ttype) {
            throw createParseException(message);
        }
    }

    /**
     * Pushes the current token back.
     */
    void pushBack();

    CssToken getToken();

    /**
     * Scans the remaining tokens and adds them to a list.
     *
     * @return a new list
     * @throws IOException on io exception
     */
    default @NonNull List<CssToken> toTokenList() throws IOException {
        List<CssToken> list = new ArrayList<>();
        while (nextNoSkip() != CssTokenType.TT_EOF) {
            list.add(getToken());
        }

        return list;
    }
}
