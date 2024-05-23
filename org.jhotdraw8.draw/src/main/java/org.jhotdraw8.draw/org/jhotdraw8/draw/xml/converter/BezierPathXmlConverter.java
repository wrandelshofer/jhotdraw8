/*
 * @(#)XmlBezierPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jspecify.annotations.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.base.io.CharBufferReader;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.geom.SvgPaths;
import org.jhotdraw8.geom.shape.BezierPath;
import org.jhotdraw8.geom.shape.BezierPathBuilder;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts an BezierNodeList path to an XML attribute value.
 * <p>
 * The null value will be converted to the CSS identifier "none".
 *
 * @author Werner Randelshofer
 */
public class BezierPathXmlConverter implements Converter<BezierPath> {

    private final boolean nullable;

    public BezierPathXmlConverter(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public @Nullable BezierPath fromString(CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
        String input = buf.toString();
        buf.position(buf.limit());
        StreamCssTokenizer tt = new StreamCssTokenizer(new CharBufferReader(buf));

        BezierPath p = null;
        try {
            if (tt.next() == CssTokenType.TT_IDENT) {
                if (!nullable) {
                    throw new ParseException("String expected. " + tt.current(), buf.position());
                }
                if (CssTokenType.IDENT_NONE.equals(tt.currentString())) {
                    buf.position(buf.limit());
                    return null;
                }
            }
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), 0);
        }
        BezierPathBuilder builder = new BezierPathBuilder();
        SvgPaths.buildSvgString(builder, input);
        BezierPath path = builder.build();
        return path;
    }

    @Override
    public <TT extends BezierPath> void toString(Appendable out, @Nullable IdSupplier idSupplier,
                                                                @Nullable TT value) throws IOException {
        if (value == null) {
            if (!nullable) {
                throw new IllegalArgumentException("value");
            }
            out.append("none");
            return;
        }

        out.append(SvgPaths.awtPathIteratorToDoubleSvgString(value.getPathIterator(null)));// we lose smooth!

    }

    @Override
    public @Nullable BezierPath getDefaultValue() {
        return nullable ? null : BezierPath.of();
    }
}
