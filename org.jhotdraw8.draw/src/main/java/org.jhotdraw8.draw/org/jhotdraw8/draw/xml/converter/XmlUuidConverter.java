/*
 * @(#)XmlPoint2DConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.UUID;

/**
 * Converts a {@code javafx.geometry.Point2D} into a {@code String} and vice
 * versa.
 *
 * @author Werner Randelshofer
 */
public class XmlUuidConverter implements Converter<UUID> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     */
    public XmlUuidConverter() {
    }

    @Override
    public void toString(@NonNull Appendable buf, @Nullable IdSupplier idSupplier, UUID value) throws IOException {
        if (value != null) {
            buf.append(value.toString());
        }
    }

    @Override
    public @Nullable UUID fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        if (in != null) {
            String converted = in.toString();
            in.position(in.position() + in.remaining());
            try {
                return UUID.fromString(converted);
            } catch (IllegalArgumentException e) {
                throw new ParseException("\"" + converted + "\" is not a legal UUID.", 0);
            }

        }
        return null;
    }

    @Override
    public @Nullable UUID getDefaultValue() {
        return null;
    }
}
