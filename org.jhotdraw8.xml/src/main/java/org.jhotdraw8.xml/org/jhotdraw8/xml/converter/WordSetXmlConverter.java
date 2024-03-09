/*
 * @(#)XmlWordSetConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.immutable.ImmutableSet;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

/**
 * WordSetConverter converts a set of Strings from/to a
 * String.
 * <p>
 * The word set is actually a "set of space separated tokens", as specified in
 * HTML 5.
 * <p>
 * (Do not use this converter for tokens specified in XML Schema Part 2. A token
 * in XML Schema Part 2 can have internal spaces, but no consecutive sequences
 * of two or more spaces.)
 * <p>
 * The word set converter coalesces duplicate entries if they have the same
 * Unicode NFC form. The tokens are sorted case-insensitively using their Unicode NFD form.
 * <p>
 * References:
 * <dl>
 * <dt>HTML 5, Common Microsyntaxes, Space-separated tokens</dt>
 * <dd><a href="https://dev.w3.org/html5/spec-preview/common-microsyntaxes.html#set-of-space-separated-tokens">w3.org</a></dd>
 *
 * <dt>XML Schema Part 2, Built-in datatypes, Derived datatypes, Token</dt>
 * <dd><a href="https://www.w3.org/TR/xmlschema-2/#token">w3.org</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class WordSetXmlConverter implements Converter<ImmutableSet<String>> {

    private final @Nullable Comparator<String> comparator;

    public WordSetXmlConverter() {
        this(Comparator.comparing(o -> Normalizer.normalize(o.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)));
    }

    public WordSetXmlConverter(@Nullable Comparator<String> comparator) {
        this.comparator = comparator;
    }

    @Override
    public <TT extends ImmutableSet<String>> void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (value == null) {
            return;
        }
        Iterable<String> words;
        if (comparator != null) {
            words = new ArrayList<>(value.asSet());
            ((ArrayList<String>) words).sort(comparator);
        } else {
            words = value;
        }
        boolean isFirst = true;
        for (String s : words) {
            if (isFirst) {
                isFirst = false;
            } else {
                out.append(" ");
            }
            out.append(s);
        }

    }

    @Override
    public ImmutableSet<String> fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) {
        String[] strings = buf.toString().split("\\s+");

        // If there is a comparator, we do not need to use a sequenced set,
        // because we are going to sort the set in method toString() anyway.
        ImmutableSet<String> words = comparator == null ? ChampVectorSet.of() : ChampSet.of();
        for (String str : strings) {
            words = words.add(Normalizer.normalize(str, Normalizer.Form.NFC));
        }
        buf.position(buf.length());// consume buffer
        return words;
    }

    @Override
    public @Nullable ImmutableSet<String> getDefaultValue() {
        return ChampVectorSet.of();
    }
}
