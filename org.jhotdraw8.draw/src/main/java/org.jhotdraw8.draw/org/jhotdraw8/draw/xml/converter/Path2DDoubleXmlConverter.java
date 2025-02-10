/*
 * @(#)XmlPath2DDoubleConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.geom.SvgPaths;
import org.jspecify.annotations.Nullable;

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
 */
public class Path2DDoubleXmlConverter implements Converter<Path2D.Double> {

    public Path2DDoubleXmlConverter() {
    }

    @Override
    public Path2D.@Nullable Double fromString(CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
        final String string = buf.toString();
        buf.position(buf.length());
        if ("none".equals(string)) {
            return null;
        }
        return SvgPaths.svgStringToAwtShape(string);
    }

    @Override
    public void toString(Appendable out, @Nullable IdSupplier idSupplier, Path2D.@Nullable Double value) throws IOException {
        final String content = value == null ? null : SvgPaths.awtPathIteratorToDoubleSvgString(value.getPathIterator(null));
        out.append(content == null ? "none" : content);
    }

    @Override
    public Path2D.@Nullable Double getDefaultValue() {
        return new Path2D.Double();
    }
}
