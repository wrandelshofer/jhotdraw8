/*
 * @(#)XmlStringConverter.java
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

/**
 * Converts a {@code String} into the XML String representation.
 * <p>
 * Reference:
 * <a href="https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#string">W3C: XML
 * Schema Part 2: Datatypes Second Edition: 3.2.5 string</a>
 * </p>
 *
 * @author Werner Randelshofer
 */
public class StringXmlConverter implements Converter<String> {

    private final boolean nullable;

    /**
     * Creates a new instance.
     */
    public StringXmlConverter() {
        this(false);
    }

    /**
     * Creates a new instance.
     */
    public StringXmlConverter(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public void toString(@NonNull Appendable buf, @Nullable IdSupplier idSupplier, String value) throws IOException {
        if (value != null) {
            buf.append(value);
        }
    }

    @Override
    public @Nullable String fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) {
        if (in != null) {
            if (in.isEmpty() && nullable) {
                return null;
            }
            String converted = in.toString();
            in.position(in.position() + in.remaining());
            return converted;
        }
        return null;
    }

    @Override
    public @Nullable String getDefaultValue() {
        return null;
    }
}
