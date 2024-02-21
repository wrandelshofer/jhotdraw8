/*
 * @(#)XmlSetConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedSet;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * Parses a set with items separated by configurable delimiters.
 * <p>
 * References:
 * <dl>
 * <dt>HTML 5, Common Microsyntaxes, Space-separated tokens</dt>
 * <dd><a href="https://dev.w3.org/html5/spec-preview/common-microsyntaxes.html#set-of-space-separated-tokens">w3.org</a></dd>
 * </dl>
 *
 * @param <T> the element type
 */
public class SetXmlConverter<T> implements Converter<ImmutableSequencedSet<T>> {
    private final @NonNull Converter<T> elementConverter;
    private final @Nullable Pattern delimiterPattern;
    private final @Nullable String delimiter;
    private final @Nullable String prefix;
    private final @Nullable String suffix;

    /**
     * Creates a new instance with a space character " " as the delimiter.
     *
     * @param elementConverter
     */
    public SetXmlConverter(@NonNull Converter<T> elementConverter) {
        this(elementConverter, " ");
    }

    public SetXmlConverter(@NonNull Converter<T> elementConverter, @Nullable String delimiter) {
        this(elementConverter, delimiter, null, null, null);
    }

    public SetXmlConverter(@NonNull Converter<T> elementConverter, @Nullable String delimiter,
                           @Nullable String prefix, @Nullable String suffix,
                           @Nullable Comparator<T> comparatorForSorting
    ) {
        this.elementConverter = elementConverter;
        this.delimiter = delimiter == null ? " " : delimiter;
        this.delimiterPattern = Pattern.compile(delimiter == null || delimiter.isBlank() ? "\\s+"
                : delimiter.indexOf(' ') == -1 ? delimiter : "\\s*" + delimiter.trim() + "\\s*");
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public @Nullable ImmutableSequencedSet<T> fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        String str = in.toString();
        if (prefix != null) {
            if (!str.startsWith(prefix)) throw new ParseException("Must start with \"" + prefix + "\"", 0);
            str = str.substring(prefix.length());
        }
        if (suffix != null) {
            if (!str.endsWith(suffix)) throw new ParseException("Must end with \"" + suffix + "\"", 0);
            str = str.substring(0, str.length() - suffix.length());
        }

        var set = ChampVectorSet.<T>of();
        for (var elem : delimiterPattern.split(str)) {
            if (elem.isEmpty()) {
                continue;
            }
            set = set.add(elementConverter.fromString(elem, idResolver));
        }
        in.position(in.length());
        return set;
    }

    @Override
    public boolean needsIdResolver() {
        return elementConverter.needsIdResolver();
    }

    @Override
    public <TT extends ImmutableSequencedSet<T>> void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (prefix != null) out.append(prefix);
        if (value != null) {
            boolean first = true;
            for (var elem : value) {
                if (first) {
                    first = false;
                } else {
                    out.append(delimiter);
                }
                elementConverter.toString(out, idSupplier, elem);
            }
        }
        if (suffix != null) out.append(suffix);
    }

    @Override
    public @Nullable ImmutableSequencedSet<T> getDefaultValue() {
        return null;
    }
}
