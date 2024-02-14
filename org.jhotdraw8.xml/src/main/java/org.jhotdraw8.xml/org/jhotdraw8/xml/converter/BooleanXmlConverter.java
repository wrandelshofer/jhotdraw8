/*
 * @(#)XmlBooleanConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts a {@code Boolean} into the XML String representation.
 * <p>
 * Reference:
 * <a href="http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#boolean">W3C: XML
 * Schema Part 2: Datatypes Second Edition: 3.2.5 boolean</a>
 * </p>
 *
 * @author Werner Randelshofer
 */
public class BooleanXmlConverter implements Converter<Boolean> {

    private static final long serialVersionUID = 1L;

    private final String trueString = "true";
    private final String falseString = "false";
    private final String oneString = "1";
    private final String zeroString = "0";

    /**
     * Creates a new instance.
     */
    public BooleanXmlConverter() {
    }

    @Override
    public void toString(@NonNull Appendable buf, @Nullable IdSupplier idSupplier, Boolean value) throws IOException {
        buf.append(value ? trueString : falseString);
    }

    @Override
    public @NonNull Boolean fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        int pos = in.position();
        StringBuilder out = new StringBuilder();
        while (in.remaining() > 0 && !Character.isWhitespace(in.charAt(0))) {
            out.append(in.get());
        }
        String str = out.toString();
        switch (str) {
        case trueString:
        case oneString:
            return true;
        case falseString:
        case zeroString:
            return false;
        }
        in.position(pos);
        throw new ParseException("\"" + trueString + "\", \"" + falseString + "\"" +
                "\"" + oneString + "\", \"" + zeroString + "\"" +
                " expected instead of \"" + str + "\".", pos);
    }

    @Override
    public @NonNull Boolean getDefaultValue() {
        return false;
    }

    /**
     * XmlWordConverter.
     *
     * @author Werner Randelshofer
     */
    public static class WordXmlConverter implements Converter<String> {

        public WordXmlConverter() {
        }

        @Override
        public void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, @Nullable String value) throws IOException {
            if (value == null) {
                return;
            }
            for (char ch : value.toCharArray()) {
                if (Character.isWhitespace(ch)) {
                    break;
                }
                out.append(ch);
            }
        }

        @Override
        public @NonNull String fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException, IOException {
            int pos = in.position();
            StringBuilder out = new StringBuilder();
            while (in.remaining() > 0 && !Character.isWhitespace(in.charAt(0))) {
                out.append(in.get());
            }
            if (out.length() == 0) {
                in.position(pos);
                throw new ParseException("word expected", pos);
            }
            return out.toString();
        }

        @Override
        public @NonNull String getDefaultValue() {
            return "";
        }
    }
}
