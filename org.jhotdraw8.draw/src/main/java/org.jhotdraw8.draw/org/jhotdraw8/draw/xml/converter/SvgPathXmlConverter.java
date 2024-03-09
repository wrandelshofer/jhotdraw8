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

/**
 * Converts an {@code SVGPath} from/to an XML attribute text.
 *
 * @author Werner Randelshofer
 */
public class SvgPathXmlConverter implements Converter<SVGPath> {

    public SvgPathXmlConverter() {
    }

    @Override
    public @Nullable SVGPath fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) {
        final String string = buf.toString();
        buf.position(buf.length());
        if (string.isEmpty()) {
            return null;
        }
        SVGPath p = new SVGPath();
        p.setContent(string);
        return p;
    }

    @Override
    public void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, @Nullable SVGPath value) throws IOException {
        final String content = value == null ? null : value.getContent();
        out.append(content == null ? "" : content);
    }

    @Override
    public @Nullable SVGPath getDefaultValue() {
        return new SVGPath();
    }
}
