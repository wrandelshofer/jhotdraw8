/*
 * @(#)CssListConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.StreamCssTokenizer;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Parses a list with items separated by whitespace or a configurable delimiter.
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Syntax Module Level 3, 5.3.11. Parse a comma-separated list of component values</dt>
 *     <dd><a href="https://www.w3.org/TR/css-syntax-3/#parse-comma-separated-list-of-component-values">w3.org</a></dd>
 * </dl>
 *
 * @param <T> the element type
 */
public class CssListConverter<T> implements CssConverter<ImmutableList<T>> {

    private final CssConverter<T> elementConverter;
    private final @NonNull ImmutableList<CssToken> delimiter;
    private final @NonNull String delimiterString;

    public CssListConverter(@NonNull CssConverter<T> elementConverter) {
        this(elementConverter, ",");

    }

    public CssListConverter(@NonNull CssConverter<T> elementConverter, @Nullable String delimiterString) {
        this.elementConverter = elementConverter;
        this.delimiter = ImmutableArrayList.copyOf(parseDelim(delimiterString));
        this.delimiterString = delimiterString;
    }

    private static List<CssToken> parseDelim(@Nullable String delim) {
        try {
            return delim == null ? List.of() : new StreamCssTokenizer(delim).toTokenList();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public @Nullable ImmutableList<T> getDefaultValue() {
        return ImmutableArrayList.of();
    }

    @Override
    public String getHelpText() {
        return "Format of ⟨List⟩: ⟨Item⟩, ⟨Item⟩, ...\n"
                + "With ⟨Item⟩:\n  " + elementConverter.getHelpText();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public ImmutableList<T> parse(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() == CssTokenType.TT_IDENT && CssTokenType.IDENT_NONE.equals(tt.currentString())) {
            return ImmutableArrayList.of();
        } else {
            tt.pushBack();
        }

        ArrayList<T> list = new ArrayList<>();
        Loop:
        while (true) {
            int ttype = tt.next();
            switch (ttype) {
                case CssTokenType.TT_EOF:
                case CssTokenType.TT_SEMICOLON:
                case CssTokenType.TT_RIGHT_BRACKET:
                case CssTokenType.TT_RIGHT_CURLY_BRACKET:
                case CssTokenType.TT_RIGHT_SQUARE_BRACKET:
                    tt.pushBack();
                    break Loop;
                default:
                    tt.pushBack();

                    // Parse optional delimiter - either match nothing or match all delimiter characters
                    int matched = 0;
                    for (CssToken cssToken : delimiter) {
                        if (tt.next() != cssToken.getType()) {
                            tt.pushBack();
                            break;
                        }
                        matched++;
                    }
                    if (matched != 0 && matched != delimiter.size()) {
                        throw new ParseException("List item delimiter '" + delimiterString + "' expected.", tt.getStartPosition());
                    }
                    if (delimiter.size() > 0 && matched == delimiter.size()) {
                        continue;
                    }

                    // Parse element
                    T elem = elementConverter.parse(tt, idResolver);
                    if (elem != null) {
                        list.add(elem);
                    }

                    break;
            }

        }
        tt.pushBack();
        return new ImmutableArrayList<>(list);
    }

    @Override
    public <TT extends ImmutableList<T>> void produceTokens(@Nullable TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) throws IOException {
        if (value == null || value.isEmpty()) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        } else {
            boolean first = true;
            for (T elem : value) {
                if (elem == null) {
                    continue;
                }
                if (first) {
                    first = false;
                } else {
                    for (CssToken cssToken : delimiter) {
                        out.accept(cssToken);
                    }
                    out.accept(new CssToken(' '));
                }
                elementConverter.produceTokens(elem, idSupplier, out);
            }
        }
    }
}
