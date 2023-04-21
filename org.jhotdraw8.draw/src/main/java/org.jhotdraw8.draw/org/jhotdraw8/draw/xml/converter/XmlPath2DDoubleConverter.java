/*
 * @(#)XmlPath2DDoubleConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.geom.SvgPaths;

import java.awt.geom.Path2D;
import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts an {@code Path2D.Double} from/to an XML attribute text.
 * <pre>
 * unicode       = '\' , ( 6 * hexd
 *                       | hexd , 5 * [hexd] , w
 *                       );
 * escape        = ( unicode
 *                 | '\' , -( newline | hexd)
 *                 ) ;
 * string        = string1 | string2 ;
 * string1       = '"' , { -( '"' ) | '\\' , newline |  escape } , '"' ;
 * string2       = "'" , { -( "'" ) | '\\' , newline |  escape } , "'" ;
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class XmlPath2DDoubleConverter implements Converter<Path2D.Double> {

    public XmlPath2DDoubleConverter() {
    }

    @Override
    public @Nullable Path2D.Double fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException, IOException {
        CharBuffer out = CharBuffer.allocate(buf.remaining());
        int count = buf.read(out);
        out.position(0);
        out.limit(count);
        final String string = out.toString();
        if ("none".equals(string)) {
            return null;
        }
        return SvgPaths.awtShapeFromSvgString(string);
    }

    @Override
    public void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, @Nullable Path2D.Double value) throws IOException {
        final String content = value == null ? null : SvgPaths.doubleSvgStringFromAwt(value.getPathIterator(null));
        out.append(content == null ? "none" : content);
    }

    @Override
    public @NonNull Path2D.Double getDefaultValue() {
        Path2D.Double p = new Path2D.Double();
        return p;
    }
}
