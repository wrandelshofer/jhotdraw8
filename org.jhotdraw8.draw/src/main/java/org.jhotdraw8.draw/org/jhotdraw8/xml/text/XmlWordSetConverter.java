/*
 * @(#)XmlWordSetConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.text;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.PersistentSet;
import org.jhotdraw8.collection.PersistentTrieSet;
import org.jhotdraw8.io.IdResolver;
import org.jhotdraw8.io.IdSupplier;
import org.jhotdraw8.text.Converter;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * WordListConverter converts an PersistentSet of Strings into a
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
 * @author Werner Randelshofer
 */
public class XmlWordSetConverter implements Converter<PersistentSet<String>> {

    public static final Comparator<String> NFD_COMPARATOR
            = Comparator.comparing(o -> Normalizer.normalize(o, Normalizer.Form.NFD));

    public XmlWordSetConverter() {
    }

    @Override
    public <TT extends PersistentSet<String>> void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
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
    public PersistentSet<String> fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (buf == null) {
            return PersistentTrieSet.of();
        }
        final TreeSet<String> tree = new TreeSet<>(NFD_COMPARATOR);
        tree.addAll(Arrays.asList(buf.toString().split("\\s+")));
        buf.position(buf.length());// consume buffer
        return PersistentTrieSet.copyOf(tree);
    }

    @Override
    public PersistentSet<String> getDefaultValue() {
        return PersistentTrieSet.of();
    }
}
