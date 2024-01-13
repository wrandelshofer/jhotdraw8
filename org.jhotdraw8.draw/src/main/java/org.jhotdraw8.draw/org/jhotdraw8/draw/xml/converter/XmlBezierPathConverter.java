/*
 * @(#)XmlBezierPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
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
public class XmlBezierPathConverter implements Converter<BezierPath> {

    private final boolean nullable;

    public XmlBezierPathConverter(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public @Nullable BezierPath fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException, IOException {
        String input = buf.toString();
        StreamCssTokenizer tt = new StreamCssTokenizer(new CharBufferReader(buf));

        BezierPath p = null;
        if (tt.next() == CssTokenType.TT_IDENT) {
            if (!nullable) {
                throw new ParseException("String expected. " + tt.current(), buf.position());
            }
            if (CssTokenType.IDENT_NONE.equals(tt.currentString())) {
                buf.position(buf.limit());
                return null;
            }
        }
        BezierPathBuilder builder = new BezierPathBuilder();
        SvgPaths.buildFromSvgString(builder, input);
        BezierPath path = builder.build();
        buf.position(buf.limit());
        return path;
    }

    @Override
    public <TT extends BezierPath> void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier,
                                                                @Nullable TT value) throws IOException {
        if (value == null) {
            if (!nullable) {
                throw new IllegalArgumentException("value");
            }
            out.append("none");
            return;
        }

        out.append(SvgPaths.doubleSvgStringFromAwt(value.getPathIterator(null)));// we lose smooth!

    }

    @Override
    public BezierPath getDefaultValue() {
        return nullable ? null : BezierPath.of();
    }
}
