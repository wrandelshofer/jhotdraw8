/*
 * @(#)XmlWordSetConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.collection.champ.ChampImmutableSet;
import org.jhotdraw8.collection.immutable.ImmutableSet;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * WordSetConverter converts an Set of Strings from/to a
 * String.
 * <p>
 * The word set is actually a "set ofCollection space separated tokens", as specified in
 * HTML 5 and in XML Schema Part 2.
 * <p>
 * The word set converter coalesces duplicate entries if they have the same
 * Unicode NFD form. The tokens are sorted using their Unicode NFD form.
 * <p>
 * References:
 * <dl>
 * <dt>HTML 5, Common Microsyntaxes, Space-separated tokens</dt>
 * <dd><a href="https://dev.w3.org/html5/spec-preview/common-microsyntaxes.html#set-of-space-separated-tokens">w3.org</a></dd>
 *
 * <dt>XML Schema Part 2, Built-in datatypes, Derived datatypes, CssToken</dt>
 * <dd><a href="https://www.w3.org/TR/xmlschema-2/#token">w3.org</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class XmlWordSetConverter implements Converter<ImmutableSet<String>> {

    public static final Comparator<String> NFD_COMPARATOR
            = Comparator.comparing(o -> Normalizer.normalize(o, Normalizer.Form.NFD));

    public XmlWordSetConverter() {
    }

    @Override
    public <TT extends ImmutableSet<String>> void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (value == null) {
            return;
        }
        final TreeSet<String> tree = new TreeSet<>(NFD_COMPARATOR);
        tree.addAll(value.asSet());
        boolean isFirst = true;
        for (String s : tree) {
            if (isFirst) {
                isFirst = false;
            } else {
                out.append(" ");
            }
            out.append(s);
        }

    }

    @Override
    public ImmutableSet<String> fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (buf == null) {
            return ChampImmutableSet.of();
        }
        final TreeSet<String> tree = new TreeSet<>(NFD_COMPARATOR);
        tree.addAll(Arrays.asList(buf.toString().split("\\s+")));
        buf.position(buf.length());// consume buffer
        return ChampImmutableSet.copyOf(tree);
    }

    @Override
    public ImmutableSet<String> getDefaultValue() {
        return ChampImmutableSet.of();
    }
}
