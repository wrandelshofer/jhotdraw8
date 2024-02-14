/*
 * @(#)CssListConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Parses a list with items separated by configurable optional delimiters.
 * <p>
 * If the delimiter consists of multiple character tokens, then the parser accepts each
 * of the character tokens and any combination of them.
 * <p>
 * The delimiters are optional when the list is parsed from a String.
 * When the list is converted into a String, then the specified delimiters are used to
 * separate the items.
 * <p>
 * Stops parsing at EOF, semicolon and closing bracket.
 * <p>
 * This parser is intentionally forgiving, so that lists can be output with nice
 * delimiters, but the user does not need to type the delimiter.
 * <p>
 * In CSS list elements are separated by a comma character.
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Syntax Module Level 3, 5.3.11. Parse a comma-separated list of component values</dt>
 *     <dd><a href="https://www.w3.org/TR/css-syntax-3/#parse-comma-separated-list-of-component-values">w3.org</a></dd>
 * </dl>
 *
 * @param <T> the element type
 */
public class ListCssConverter<T> implements CssConverter<ImmutableList<T>> {
    /**
     * When nonnull this comparator is used to sort the list.
     */
    private final @Nullable Comparator<T> comparatorForSorting;

    private final @NonNull CssConverter<T> elementConverter;
    private final @NonNull ImmutableList<CssToken> delimiter;
    private final @NonNull ImmutableList<CssToken> prefix;
    private final @NonNull ImmutableList<CssToken> suffix;
    private final @NonNull Set<Integer> delimiterChars;

    public ListCssConverter(@NonNull CssConverter<T> elementConverter) {
        this(elementConverter, ", ");
    }

    public ListCssConverter(@NonNull CssConverter<T> elementConverter, @Nullable String delimiter) {
        this(elementConverter, delimiter, null, null);
    }

    public ListCssConverter(@NonNull CssConverter<T> elementConverter, @Nullable String delimiter,
                            @Nullable String prefix, @Nullable String suffix) {
        this(elementConverter, parseDelim(delimiter == null ? " " : delimiter), parseDelim(prefix), parseDelim(suffix));
    }

    /**
     * @param elementConverter     the convert for a single element
     * @param delimiter            a String which must be parsable into character tokens, each token and any combination of them
     *                             is accepted as an optional delimiter
     * @param prefix               the prefix of the list, for example a left bracket
     * @param suffix               the suffix of the list, for example a right bracket
     * @param comparatorForSorting if this value is non-null, then it is used to sort the list
     */
    public ListCssConverter(@NonNull CssConverter<T> elementConverter, @Nullable String delimiter, @Nullable String prefix, @Nullable String suffix, @Nullable Comparator<T> comparatorForSorting) {
        this(elementConverter, parseDelim(delimiter), parseDelim(prefix), parseDelim(suffix), comparatorForSorting);
    }

    private static List<CssToken> parseDelim(@Nullable String delim) {
        try {
            return delim == null ? List.of() : new StreamCssTokenizer(delim, null).toTokenList();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * Creates a new instance that does not sort the elements.
     *
     * @param elementConverter converter for elements
     * @param delimiter        optional delimiter for parsing; the delimiter is used for pretty printing
     * @param prefix           white-space tokens for pretty printing that are used when producing tokens or a String
     * @param suffix           white-space tokens for pretty printing that are used when producing tokens or a String
     */
    public ListCssConverter(@NonNull CssConverter<T> elementConverter,
                            @NonNull Iterable<CssToken> delimiter,
                            @NonNull Iterable<CssToken> prefix,
                            @NonNull Iterable<CssToken> suffix
    ) {
        this(elementConverter, delimiter, prefix, suffix, null);
    }

    /**
     * Creates a new instance.
     *
     * @param elementConverter     converter for elements
     * @param delimiter            optional delimiter for parsing; the delimiter is used for pretty printing
     * @param prefix               white-space tokens for pretty printing that are used when producing tokens or a String
     * @param suffix               white-space tokens for pretty printing that are used when producing tokens or a String
     * @param comparatorForSorting optional comparator for sorting; null means no sorting
     */
    public ListCssConverter(@NonNull CssConverter<T> elementConverter,
                            @NonNull Iterable<CssToken> delimiter,
                            @NonNull Iterable<CssToken> prefix,
                            @NonNull Iterable<CssToken> suffix,
                            @Nullable Comparator<T> comparatorForSorting
    ) {
        this.elementConverter = elementConverter;
        this.delimiter = VectorList.copyOf(delimiter);
        this.prefix = VectorList.copyOf(prefix);
        this.suffix = VectorList.copyOf(suffix);
        delimiterChars = new HashSet<>();
        for (CssToken cssToken : delimiter) {
            if (cssToken.getType() >= 0) {
                delimiterChars.add(cssToken.getType());
            }
        }
        this.comparatorForSorting = comparatorForSorting;
    }


    @Override
    public ImmutableList<T> parse(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() == CssTokenType.TT_IDENT && CssTokenType.IDENT_NONE.equals(tt.currentString())) {
            return VectorList.of();
        } else {
            tt.pushBack();
        }

        ArrayList<T> list = new ArrayList<>();
        Loop:
        for (; ; ) {
            int ttype = tt.nextNoSkip();
            if (delimiterChars.contains(ttype)) {
                continue Loop;
            }
            switch (ttype) {
                case CssTokenType.TT_S:
                    continue Loop;
                case CssTokenType.TT_EOF:
                case CssTokenType.TT_SEMICOLON:
                case CssTokenType.TT_RIGHT_BRACKET:
                case CssTokenType.TT_RIGHT_CURLY_BRACKET:
                case CssTokenType.TT_RIGHT_SQUARE_BRACKET:
                    tt.pushBack();
                    break Loop;
                default:
                    tt.pushBack();
                    T elem = elementConverter.parse(tt, idResolver);
                    if (elem != null) {
                        list.add(elem);
                    }
                    break;
            }

        }
        tt.pushBack();
        if (comparatorForSorting != null) {
            list.sort(comparatorForSorting);
        }
        return VectorList.copyOf(list);
    }

    @Override
    public boolean needsIdResolver() {
        return elementConverter.needsIdResolver();
    }

    @Override
    public <TT extends ImmutableList<T>> void produceTokens(@Nullable TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) throws IOException {
        if (value == null) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        } else if (value.isEmpty()) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        } else {
            for (CssToken t : prefix) {
                out.accept(t);
            }
            boolean first = true;
            Iterable<T> ordered;
            if (comparatorForSorting != null) {
                ArrayList<T> ts = new ArrayList<>(value.toMutable());
                ts.sort(comparatorForSorting);
                ordered = ts;
            } else {
                ordered = value;
            }
            for (T elem : ordered) {
                if (elem == null) {
                    continue;
                }
                if (first) {
                    first = false;
                } else {
                    for (CssToken t : delimiter) {
                        out.accept(t);
                    }
                }
                elementConverter.produceTokens(elem, idSupplier, out);
            }
            for (CssToken t : suffix) {
                out.accept(t);
            }
        }
    }

    @Override
    public @Nullable ImmutableList<T> getDefaultValue() {
        return VectorList.of();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Format of ⟨List⟩: empty | ⟨Item⟩, ⟨Item⟩, ...\n"
               + "With ⟨Item⟩:\n  " + elementConverter.getHelpText();
    }
}
