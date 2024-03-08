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
    private final String emptyString = "";
    private final String falseString = "false";
    private final String oneString = "1";
    private final String zeroString = "0";
    private final boolean nullable;

    /**
     * Creates a new instance.
     */
    public BooleanXmlConverter(boolean nullable) {
        this.nullable = nullable;
    }
    /**
     * Creates a new instance.
     */
    public BooleanXmlConverter() {
        this(false);
    }

    @Override
    public void toString(@NonNull Appendable buf, @Nullable IdSupplier idSupplier, Boolean value) throws IOException {
        buf.append(value ? trueString : falseString);
    }

    @Override
    public @Nullable Boolean fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        String str = in.toString();
        in.position(in.length());
        switch (str) {
            case trueString, oneString -> {
                return true;
            }
            case falseString, zeroString -> {
                return false;
            }
            case emptyString -> {
                if (nullable) return null;
            }
        }
        throw new ParseException("\"" + trueString + "\", \"" + falseString + "\"" +
                "\"" + oneString + "\", \"" + zeroString + "\"" +
                                 " expected instead of \"" + str + "\".", 0);
    }

    @Override
    public @NonNull Boolean getDefaultValue() {
        return false;
    }

}
