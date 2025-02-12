/*
 * @(#)XmlWordListConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * WordListConverter converts an ImmutableObservableList of Strings into a
 * String.
 * <p>
 * The word list is actually a "set ofCollection space separated tokens", as specified in
 * HTML 5 and in XML Schema Part 2.
 * <p>
 * The word list converter coalesces duplicate entries if they have the same
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
 */
public class WordListXmlConverter implements Converter<PersistentList<String>> {

    public static final Comparator<String> NFD_COMPARATOR
            = Comparator.comparing(o -> Normalizer.normalize(o, Normalizer.Form.NFD));

    public WordListXmlConverter() {
    }

    @Override
    public <TT extends PersistentList<String>> void toString(Appendable out,
                                                             @Nullable IdSupplier idSupplier,
                                                             @Nullable TT value) throws IOException {
        if (value == null) {
            return;
        }
        final TreeSet<String> tree = new TreeSet<>(NFD_COMPARATOR);
        tree.addAll(value.asList());
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
    public PersistentList<String> fromString(CharBuffer buf, @Nullable IdResolver idResolver) {
        final TreeSet<String> tree = new TreeSet<>(NFD_COMPARATOR);
        tree.addAll(Arrays.asList(buf.toString().split("\\s+")));
        buf.position(buf.length());// consume buffer
        return VectorList.copyOf(tree);
    }

    @Override
    public @Nullable PersistentList<String> getDefaultValue() {
        return VectorList.of();
    }
}
