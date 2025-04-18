/*
 * @(#)XmlBezierPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.base.io.CharBufferReader;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.geom.SvgPaths;
import org.jhotdraw8.geom.shape.PathMetrics;
import org.jhotdraw8.geom.shape.PathMetricsBuilder;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts an BezierNodeList path to an XML attribute value.
 * <p>
 * The null value will be converted to the CSS identifier "none".
 *
 */
public class PathMetricsXmlConverter implements Converter<PathMetrics> {

    private final boolean nullable;

    public PathMetricsXmlConverter(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public @Nullable PathMetrics fromString(CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
        String input = buf.toString();
        buf.position(buf.limit());
        StreamCssTokenizer tt = new StreamCssTokenizer(new CharBufferReader(buf));

        PathMetrics p = null;
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
        PathMetricsBuilder builder = new PathMetricsBuilder();
        SvgPaths.buildSvgString(builder, input);
        p = builder.build();


        return p;
    }

    @Override
    public <TT extends PathMetrics> void toString(Appendable out, @Nullable IdSupplier idSupplier,
                                                  @Nullable TT value) throws IOException {
        if (value == null) {
            if (!nullable) {
                throw new IllegalArgumentException("value");
            }
            out.append("none");
            return;
        }

        out.append(SvgPaths.awtPathIteratorToDoubleSvgString(value.getPathIterator(null)));

    }

    @Override
    public @Nullable PathMetrics getDefaultValue() {
        return nullable ? null : new PathMetricsBuilder().build();
    }
}
