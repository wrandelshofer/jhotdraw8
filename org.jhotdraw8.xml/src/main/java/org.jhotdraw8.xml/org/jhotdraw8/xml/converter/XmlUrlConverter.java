/*
 * @(#)XmlUrlConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;

import java.io.IOException;
import java.net.URL;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * XmlUrlConverter.
 *
 * @author Werner Randelshofer
 */
public class XmlUrlConverter implements Converter<URL> {

    public XmlUrlConverter() {
    }

    @Override
    public void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, @Nullable URL value) throws IOException {
        if (value != null) {
            out.append(value.toString());
        }
    }

    @Override
    public @Nullable URL fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException, IOException {
        String str = in.toString();
        if (str.isEmpty()) {
            return null;
        }
        URL value = new URL(str);
        in.position(in.limit());
        return value;
    }

    @Override
    public @Nullable URL getDefaultValue() {
        return null;
    }
}
