/*
 * @(#)Converter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts a data value of type {@code T} from or to a String representation.
 * <p>
 * This interface is designed so that it can be adapted to the various String
 * conversion APIs in the JDK.
 *
 * @param <T> the data type
 */
public interface Converter<T> {
    /**
     * Constructs a value from a string.
     * <p>
     * The converter should try to create the value greedily, by consuming as
     * many characters as possible for the value.
     * <p>
     * This method does not change the state of the converter.
     *
     * @param in         A char buffer which holds the string. The char buffer must be
     *                   treated as read only! The position of the char buffer denotes the
     *                   beginning of the string when this method is invoked. After completion of
     *                   this method, the position is set after the last consumed character.
     * @param idResolver The factory for looking up object ids. Nullable for some
     *                   converters.
     * @return The value. Nullable.
     * @throws ParseException if conversion failed. The error offset field is
     *                        set to the position where parsing failed. The position of the buffer is
     *                        undefined.
     */
    default @Nullable T fromString(CharSequence in, @Nullable IdResolver idResolver) throws ParseException {
        return fromString(CharBuffer.wrap(in), idResolver);
    }

    /**
     * Constructs a value from a string.
     * <p>
     * The converter should try to create the value greedily, by consuming as
     * many characters as possible for the value.
     * <p>
     * This method does not change the state of the converter.
     *
     * @param in         A char buffer which holds the string. The char buffer must be
     *                   treated as read only! The position of the char buffer denotes the
     *                   beginning of the string when this method is invoked. After completion of
     *                   this method, the position is set after the last consumed character.
     * @param idResolver The factory for looking up object ids. Nullable for non-resolving
     *                   converters.
     * @return The value. Nullable.
     * @throws ParseException if conversion failed. The error offset field is
     *                        set to the position where parsing failed. The position of the buffer is
     *                        undefined.
     */
    @Nullable
    T fromString(CharBuffer in, @Nullable IdResolver idResolver) throws ParseException;

    /**
     * Constructs a value from a string.
     * <p>
     * The converter should try to create the value greedily, by consuming as
     * many characters as possible for the value.
     * <p>
     * This method does not change the state of the converter.
     *
     * @param in A char buffer which holds the string. The char buffer must be
     *           treated as read only! The position of the char buffer denotes the
     *           beginning of the string when this method is invoked. After completion of
     *           this method, the position is set after the last consumed character.
     * @return The value. Nullable.
     * @throws ParseException if conversion failed. The error offset field is
     *                        set to the position where parsing failed. The position of the buffer is
     *                        undefined.
     */
    default @Nullable T fromString(CharBuffer in) throws ParseException {
        return fromString(in, null);
    }

    /**
     * Constructs a value from a CharSequence.
     * <p>
     * The conversion only succeeds if the entire CharSequence is consumed.
     * <p>
     * This method does not change the state of the converter.
     * <p>
     * Note: this is a convenience method. Implementing classes rarely need to
     * overwrite this method.
     *
     * @param in The String.
     * @return The value. Nullable.
     * @throws ParseException on conversion failure
     */
    default @Nullable T fromString(CharSequence in) throws ParseException {
        CharBuffer buf = CharBuffer.wrap(in);
        T value = fromString(buf);
        if (buf.remaining() != 0) {
            throw new ParseException(buf.remaining() + " remaining character(s) not consumed." + " remaining: \"" + buf + "\".", buf.position());
        }
        return value;
    }

    /**
     * Convenience method that throws a ParseException if the value is null.
     *
     * @param in         the String
     * @param idResolver the id resolver
     * @return the value
     * @throws ParseException if the value is null
     */
    default T fromStringNonNull(CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        T m = fromString(in, idResolver);
        if (m == null) {
            throw new ParseException("Value must not be null.", 0);
        }
        return m;
    }

    /**
     * Convenience method that throws a ParseException if the value is null.
     *
     * @param in the String
     * @return the value
     * @throws ParseException if the value is null
     */
    default T fromStringNonNull(CharBuffer in) throws ParseException {
        T m = fromString(in);
        if (m == null) {
            throw new ParseException("Value must not be null.", 0);
        }
        return m;
    }

    /**
     * Convenience method that throws a ParseException if the value is null.
     *
     * @param in         the String
     * @param idResolver the id resolver
     * @return the value
     * @throws ParseException if the value is null
     */
    default T fromStringNonNull(CharSequence in, @Nullable IdResolver idResolver) throws ParseException {
        T m = fromString(in, idResolver);
        if (m == null) {
            throw new ParseException("Value must not be null.", 0);
        }
        return m;
    }

    /**
     * Convenience method that throws a ParseException if the value is null.
     *
     * @param in the String
     * @return the value
     * @throws ParseException if the value is null
     */
    default T fromStringNonNull(CharSequence in) throws ParseException {
        T m = fromString(in);
        if (m == null) {
            throw new ParseException("Value must not be null.", 0);
        }
        return m;
    }

    /**
     * Provides a default value for APIs which always require a value even if
     * conversion from String failed.
     *
     * @return The default value to use when conversion from String failed.
     */
    @Nullable
    T getDefaultValue();

    /**
     * Returns a help text which describes the supported String format.
     * <p>
     * The format should be described in the following notation:
     * <ul>
     * <li>A production should consist of a single line starting with
     * {@code Format of ⟨x⟩:}. Where {@code x} is the name of the production.
     * <li>A terminal symbol should be given with its literal characters.
     * Whitespace characters should be implied. Mandatory whitespace characters
     * can be indicated with a bottom square bracket: {@code ⎵}.</li>
     * <li>Nonterminal symbols should be given in mathematical angle brackets:
     * {@code ⟨x⟩}.</li>
     * <li>Alternatives should be separated by full-width vertical bar:
     * {@code x｜y}.</li>
     * <li>Groups of symbols should be surrounded by full width parantheses:
     * {@code （x y）}.</li>
     * <li>Zero or one occurrences of a symbol should be surrounded by full
     * width square brackets: {@code ［x］}.</li>
     * <li>Zero or many occurrences of a symbol should be surrounded by full
     * width angle brackets: {@code ｛x｝}.</li>
     * </ul>
     * Example:
     * <pre>
     * Format of ⟨Color⟩: ⟨name⟩｜ #⟨hex⟩｜ rgb(⟨r⟩,⟨g⟩,⟨b⟩)｜ rgba(⟨r⟩,⟨g⟩,⟨b⟩,⟨a⟩)｜ hsb(⟨h⟩,⟨s⟩,⟨b⟩)｜ hsba(⟨h⟩,⟨s⟩,⟨b⟩,⟨a⟩)
     * </pre>
     *
     * @return help text. Returns null if no help text is available.
     */
    default @Nullable String getHelpText() {
        return null;
    }

    default boolean needsIdResolver() {
        return false;
    }

    // ----
    // convenience methods
    // ----

    /**
     * Converts a value to a string and appends it to the provided
     * {@code Appendable}.
     * <p>
     * This method does not change the state of the converter.
     *
     * @param <TT>       the value type
     * @param out        The appendable
     * @param idSupplier The factory for creating object ids. Nullable for non-resolving
     *                   converters.
     * @param value      The value. Nullable.
     * @throws IOException thrown by Appendable
     */
    <TT extends T> void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException;

    /**
     * Converts a value to a string and appends it to the provided
     * {@code Appendable}.
     * <p>
     * This method does not change the state of the converter.
     *
     * @param <TT>  the value type
     * @param out   The appendable
     * @param value The value. Nullable.
     * @throws IOException thrown by Appendable
     */
    default <TT extends T> void toString(Appendable out, @Nullable TT value) throws IOException {
        toString(out, null, value);
    }

    /**
     * Converts a value to a String.
     * <p>
     * This method does not change the state of the converter.
     * <p>
     * Note: this is a convenience method. Implementing classes rarely need to
     * overwrite this method.
     *
     * @param <TT>  the value type
     * @param value The value. Nullable.
     * @return The String.
     */
    default <TT extends T> String toString(@Nullable TT value) {
        StringBuilder out = new StringBuilder();
        try {
            toString(out, value);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return out.toString();
    }

    /**
     * Converts a value to a String.
     * <p>
     * This method does not change the state of the converter.
     * <p>
     * Note: this is a convenience method. Implementing classes rarely need to
     * overwrite this method.
     *
     * @param <TT>  the value type
     * @param value The value. Nullable.
     * @return The String.
     */
    default <TT extends T> String toString(@Nullable IdSupplier idSupplier, @Nullable TT value) {
        StringBuilder out = new StringBuilder();
        try {
            toString(out, idSupplier, value);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return out.toString();
    }
}
