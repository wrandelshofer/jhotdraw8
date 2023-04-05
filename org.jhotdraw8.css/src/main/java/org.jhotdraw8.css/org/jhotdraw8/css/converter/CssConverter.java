/*
 * @(#)CssConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.StreamCssTokenizer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Converts a data value of type {@code T} from or to a CSS Tokenizer.
 *
 * @param <T> the type of the data that can be converted from/to CSS
 * @author Werner Randelshofer
 */
public interface CssConverter<T> extends Converter<T> {
    /**
     * Parses from the given tokenizer and moves the tokenizer
     * to the next token past the value.
     *
     * @param tt         tokenizer positioned on the token
     * @param idResolver the id factory
     * @return the parsed value
     * @throws ParseException on parse exception
     * @throws IOException    on io exception
     */
    @Nullable
    T parse(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException;

    /**
     * Parses from the given tokenizer and moves the tokenizer
     * to the next token past the value.
     *
     * @param tt         tokenizer positioned on the token
     * @param idResolver the id factory
     * @return the parsed value
     * @throws ParseException on parse exception
     * @throws IOException    on io exception
     */
    default @NonNull T parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        T value = parse(tt, idResolver);
        if (value == null) {
            throw new ParseException("Value expected.", tt.getStartPosition());
        }
        return value;
    }

    /**
     * Produces tokens for the specified value.
     *
     * @param <TT>       the value type
     * @param value      the value
     * @param idSupplier the id factory
     * @param out        the consumer for the tokens
     * @throws IOException on IO exception
     */
    <TT extends T> void produceTokens(@Nullable TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) throws IOException;

    default @NonNull <TT extends T> List<CssToken> toTokens(@Nullable TT value, @Nullable IdSupplier idSupplier) throws IOException {
        List<CssToken> list = new ArrayList<>();
        produceTokens(value, idSupplier, list::add);
        return list;
    }

    /**
     * Converts the value to String.
     *
     * @param value the value
     * @param <TT>  the value type
     * @return a String
     */
    @Override
    default @NonNull <TT extends T> String toString(@Nullable TT value) {
        return toString(value, null);
    }

    /**
     * Converts the value to String.
     *
     * @param value     the value
     * @param idFactory the id factory
     * @param <TT>      the value type
     * @return a String
     */
    default @NonNull <TT extends T> String toString(@Nullable TT value, @Nullable IdFactory idFactory) {
        StringBuilder buf = new StringBuilder();
        try {
            produceTokens(value, idFactory, buf::append);
        } catch (IOException e) {
            // toString cannot throw, if we get an exception, there is a programming error.
            throw new UncheckedIOException(e);
        }
        return buf.toString();
    }

    @Override
    default <TT extends T> void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, TT value) throws IOException {
        Consumer<CssToken> consumer = token -> {
            try {
                out.append(token.fromToken());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
        try {
            produceTokens(value, idSupplier, consumer);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    default T fromString(@NonNull CharSequence buf, @Nullable IdResolver idResolver) throws ParseException {
        try {
            StreamCssTokenizer tt = new StreamCssTokenizer(buf, null);
            return parse(tt, idResolver);
        } catch (IOException e) {
            throw new RuntimeException("unexpected io exception", e);
        }
    }

    @Override
    default T fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
        try {
            int startPos = buf.position();
            StreamCssTokenizer tt = new StreamCssTokenizer(buf, null);
            T value = parse(tt, idResolver);
            buf.position(startPos + tt.getNextPosition());
            return value;
        } catch (IOException e) {
            throw new RuntimeException("unexpected io exception", e);
        }
    }


    /**
     * Gets a help text.
     *
     * @return a help text.
     */
    @Override
    @Nullable String getHelpText();

    default @NonNull ImmutableList<String> getExamples() {
        return ImmutableArrayList.of();
    }

    boolean isNullable();
}
