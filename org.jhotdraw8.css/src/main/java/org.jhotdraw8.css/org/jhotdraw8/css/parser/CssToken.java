/*
 * @(#)CssToken.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.parser;

import org.jhotdraw8.base.converter.NumberConverter;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * CssToken.
 *
 * @author Werner Randelshofer
 */
public class CssToken {

    /**
     * The token type.
     */
    private final int ttype;
    /**
     * The string value.
     */
    private final @Nullable String stringValue;
    /**
     * The numeric value.
     */
    private final @Nullable Number numericValue;

    private final int startPos;
    private final int endPos;
    private final int lineNumber;

    private final @Nullable Character preferredQuoteChar;

    private static final NumberConverter NUMBER_CONVERTER = new NumberConverter();

    public CssToken(int ttype, String stringValue) {
        this(ttype, stringValue, null, 0, 0, stringValue.length());

    }

    public CssToken(int ttype, String stringValue, @Nullable Character preferredQuoteChar) {
        this(ttype, stringValue, null, preferredQuoteChar, 0, 0, stringValue.length());

    }

    public CssToken(int ttype) {
        this(ttype, Character.toString((char) ttype), null, null, 0, 0, 1);

    }

    public CssToken(int ttype, Number numericValue, String stringValue) {
        this(ttype, stringValue, numericValue, null, 0, 0, 1);
    }

    public CssToken(int ttype, Number numericValue) {
        this(ttype, "", numericValue, null, 0, 0, 1);
    }

    public CssToken(int ttype, String stringValue, Number numericValue, int lineNumber, int startPos, int endPos) {
        this(ttype, stringValue, numericValue, null, lineNumber, startPos, endPos);
    }

    public CssToken(int ttype, @Nullable String stringValue, @Nullable Number numericValue, @Nullable Character preferredQuoteChar, int lineNumber, int startPos, int endPos) {
        switch (ttype) {
        case CssTokenType.TT_DIMENSION:
            Objects.requireNonNull(numericValue, "numericValue must not be null for ttype=TT_DIMENSION");
            Objects.requireNonNull(stringValue, "stringValue must not be null for ttype=TT_DIMENSION");
            break;
        case CssTokenType.TT_NUMBER:
        case CssTokenType.TT_PERCENTAGE:
            Objects.requireNonNull(numericValue, "numeric value must not be null for ttype=TT_NUMBER or ttype=TT_PERCENTAGE");
            break;
        case CssTokenType.TT_IDENT:
        case CssTokenType.TT_AT_KEYWORD:
            if (stringValue == null || stringValue.isEmpty()) {
                throw new IllegalArgumentException("stringValue must not be null or empty for ttype=TT_IDENT or ttype=TT_AT_KEYWORD");
            }
            break;
        default:
            if (ttype < 0 && ttype != CssTokenType.TT_EOF && stringValue == null) {
                throw new IllegalArgumentException("string value must not be null for ttype=" + ttype);
            }
            break;
        }
        this.ttype = ttype;
        this.stringValue = stringValue;
        this.numericValue = numericValue;
        this.lineNumber = lineNumber;
        this.startPos = startPos;
        this.endPos = endPos;
        this.preferredQuoteChar = preferredQuoteChar;
    }

    public String getStringValueNonNull() {
        return Objects.requireNonNull(stringValue, "stringValue");
    }

    public Number getNumericValueNonNull() {
        return Objects.requireNonNull(numericValue, "numericValue");
    }

    @Override
    public @Nullable String toString() {
        return fromToken();
    }

    public @Nullable String fromToken() {
        if (ttype >= 0) {
            return stringValue;
        }
        return switch (ttype) {
            case CssTokenType.TT_IDENT -> fromIDENT();
            case CssTokenType.TT_AT_KEYWORD -> fromHASHorAT('@', stringValue);
            case CssTokenType.TT_STRING -> fromSTRING();
            case CssTokenType.TT_BAD_STRING -> fromBAD_STRING(stringValue);
            case CssTokenType.TT_BAD_URI -> fromBAD_URI(stringValue);
            case CssTokenType.TT_HASH -> fromHASHorAT('#', stringValue);
            case CssTokenType.TT_NUMBER -> fromNUMBER();
            case CssTokenType.TT_PERCENTAGE -> fromPERCENTAGE();
            case CssTokenType.TT_DIMENSION -> fromDIMENSION();
            case CssTokenType.TT_URL -> fromURL();
            case CssTokenType.TT_UNICODE_RANGE -> fromUNICODE_RANGE();
            case CssTokenType.TT_CDO -> fromCDO();
            case CssTokenType.TT_CDC -> fromCDC();
            case CssTokenType.TT_S -> fromS();
            case CssTokenType.TT_COMMENT -> fromCOMMENT();
            case CssTokenType.TT_FUNCTION -> fromIDENT() + "(";
            case CssTokenType.TT_INCLUDE_MATCH -> fromINCLUDE_MATCH();
            case CssTokenType.TT_DASH_MATCH -> fromDASH_MATCH();
            case CssTokenType.TT_PREFIX_MATCH -> fromPREFIX_MATCH();
            case CssTokenType.TT_SUFFIX_MATCH -> fromSUFFIX_MATCH();
            case CssTokenType.TT_SUBSTRING_MATCH -> fromSUBSTRING_MATCH();
            case CssTokenType.TT_COLUMN -> fromCOLUMN();
            case CssTokenType.TT_EOF -> "<EOF>";
            default -> throw new InternalError("Unsupported TTYPE:" + ttype);
        };
    }

    private String fromCDC() {
        return "<!--";
    }

    private String fromCDO() {
        return "-->";
    }

    private String fromIDENT() {
        return fromIDENT(stringValue);
    }

    private String fromIDENT(String value) {
        StringBuilder out = new StringBuilder();
        Reader r = new StringReader(value);
        try {
            int ch = r.read();

            // identifier may start with zero or more '-'
            while (ch == '-') {
                out.append((char) ch);
                ch = r.read();
            }

            if (ch == -1) {
                throw new IllegalArgumentException("nmstart missing! value=\"" + value + "\".");
            }

            // escape nmstart if necessary
            if (ch == '_' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || 0xA0 <= ch) {
                out.append((char) ch);
            } else {
                switch (ch) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '\n':
                    String hex = Integer.toHexString(ch);
                    out.append('\\');
                    out.append(hex);
                    out.append(' ');
                    break;
                default:
                    out.append('\\');
                    out.append((char) ch);
                    break;
                }
            }

            while (-1 != (ch = r.read())) {
                // escape nmchar if necessary
                if (ch == '_' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || '0' <= ch && ch <= '9' || ch == '-' || 0xA0 <= ch) {
                    out.append((char) ch);
                } else {
                    out.append('\\');
                    out.append((char) ch);
                }
            }
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IOException", e);
        }
    }

    private String fromHASHorAT(char hashOrAt, String value) {
        StringBuilder out = new StringBuilder();
        out.append(hashOrAt);
        Reader r = new StringReader(value);
        try {
            for (int ch = r.read(); ch != -1; ch = r.read()) {
                // escape nmchar if necessary
                if (ch == '_' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || '0' <= ch && ch <= '9' || ch == '-' || 0xA0 <= ch) {
                    out.append((char) ch);
                } else {
                    out.append('\\');
                    out.append((char) ch);
                }
            }
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IOException", e);
        }
    }

    private String fromSTRING() {
        return fromSTRING(stringValue);
    }

    private String fromSTRING(String value) {
        char quoteChar =
                preferredQuoteChar != null
                        ? preferredQuoteChar
                        : value.indexOf('"') < 0 || value.indexOf('\'') < 0 ? '"' : '\'';
        return fromSTRING(value, quoteChar, quoteChar);
    }

    private String fromBAD_URI(String value) {
        return fromURL(value);
    }

    private String fromBAD_STRING(String value) {
        char quoteChar =
                preferredQuoteChar != null
                        ? preferredQuoteChar
                        : value.indexOf('"') < 0 || value.indexOf('\'') < 0 ? '"' : '\'';
        return fromSTRING(value, quoteChar, '\n');
    }

    private String fromSTRING(String value, final char firstQuoteChar, final char lastQuoteChar) {
        StringBuilder out = new StringBuilder();
        out.append(firstQuoteChar);
        for (char ch : value.toCharArray()) {
            switch (ch) {
            case ' ':
                out.append(ch);
                break;
            case '\\':
                out.append('\\');
                out.append('\\');
                break;
            case '\n':
                out.append('\\');
                out.append('\n');
                break;
            default:
                if (ch == firstQuoteChar) {
                    out.append('\\');
                    out.append(firstQuoteChar);
                } else {

                    if (Character.isISOControl(ch) || Character.isWhitespace(ch)) {
                        out.append('\\');
                        String hex = Integer.toHexString(ch);
                        for (int i = 0, n = 6 - hex.length(); i < n; i++) {
                            out.append('0');
                        }
                        out.append(hex);
                    } else {
                        out.append(ch);
                    }

                }
                break;
            }
        }
        out.append(lastQuoteChar);
        return out.toString();
    }

    private String fromNUMBER() {
        return NUMBER_CONVERTER.toString(numericValue.doubleValue());
    }

    private String fromPERCENTAGE() {
        return Double.isFinite(numericValue.doubleValue()) ? fromNUMBER() + "%" : fromNUMBER();
    }

    private String fromDIMENSION() {
        return !stringValue.isEmpty() && Double.isFinite(numericValue.doubleValue()) ? fromNUMBER() + fromIDENT() : fromNUMBER();
    }

    private String fromURL() {
        return fromURL(stringValue);
    }

    private String fromURL(String stringValue) {
        StringBuilder out = new StringBuilder();
        out.append("url(");
        Reader r = new StringReader(stringValue);
        try {
            for (int ch = r.read(); ch != -1; ch = r.read()) {
                final boolean escape = switch (ch) {
                    case '"', '\'', '(', ')', '\\' -> true;
                    default -> Character.isWhitespace(ch) || Character.isISOControl(ch);
                };
                if (escape) {
                    String hex = Integer.toHexString(ch);
                    out.append('\\');
                    out.append(hex);
                    out.append(' ');
                } else {
                    out.append((char) ch);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IOException", e);
        }


        out.append(')');
        return out.toString();
    }

    private @Nullable String fromUNICODE_RANGE() {
        return stringValue;
    }

    private @Nullable String fromS() {
        return stringValue;
    }

    private String fromCOMMENT() {
        return "/" + "*" + stringValue.replace("*" + '/', "* /") + '*' + '/';
    }

    private @Nullable String fromINCLUDE_MATCH() {
        return "~=";
    }

    private @Nullable String fromDASH_MATCH() {
        return "|=";
    }

    private @Nullable String fromPREFIX_MATCH() {
        return "^=";
    }

    private @Nullable String fromSUFFIX_MATCH() {
        return "$=";
    }

    private @Nullable String fromSUBSTRING_MATCH() {
        return "*=";
    }

    private @Nullable String fromCOLUMN() {
        return stringValue;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public @Nullable String getStringValue() {
        return stringValue;
    }

    public @Nullable Number getNumericValue() {
        return numericValue;
    }

    public int getType() {
        return ttype;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CssToken cssToken = (CssToken) o;
        return ttype == cssToken.ttype && Objects.equals(stringValue, cssToken.stringValue) && Objects.equals(numericValue, cssToken.numericValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ttype, stringValue, numericValue);
    }
}
