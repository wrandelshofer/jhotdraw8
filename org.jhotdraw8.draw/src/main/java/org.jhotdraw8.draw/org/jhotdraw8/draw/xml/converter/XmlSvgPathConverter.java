/*
 * @(#)XmlSvgPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import javafx.scene.shape.SVGPath;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts an {@code SVGPath} from/to an XML attribute text.
 *
 * @author Werner Randelshofer
 */
public class XmlSvgPathConverter implements Converter<SVGPath> {

    public XmlSvgPathConverter() {
    }

    @Override
    public @Nullable SVGPath fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException, IOException {
        CharBuffer out = CharBuffer.allocate(buf.remaining());
        int count = buf.read(out);
        out.position(0);
        out.limit(count);
        final String string = out.toString();
        if ("none".equals(string)) {
            return null;
        }
        SVGPath p = new SVGPath();
        p.setContent(string);
        return p;
    }

    @Override
    public void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, @Nullable SVGPath value) throws IOException {
        final String content = value == null ? null : value.getContent();
        out.append(content == null ? "none" : content);
    }

    @Override
    public @NonNull SVGPath getDefaultValue() {
        SVGPath p = new SVGPath();
        return p;
    }
}
