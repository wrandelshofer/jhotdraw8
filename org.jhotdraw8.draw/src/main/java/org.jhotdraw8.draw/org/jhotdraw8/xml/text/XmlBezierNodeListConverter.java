/*
 * @(#)XmlBezierNodeListConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.text;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.base.io.CharSequenceReader;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.geom.BezierNode;
import org.jhotdraw8.geom.BezierNodePath;
import org.jhotdraw8.geom.BezierNodePathBuilder;
import org.jhotdraw8.geom.SvgPaths;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts an BezierNodeList path to a CSS String.
 * <p>
 * The null value will be converted to the CSS identifier "none".
 *
 * @author Werner Randelshofer
 */
public class XmlBezierNodeListConverter implements Converter<ImmutableList<BezierNode>> {

    private final boolean nullable;

    public XmlBezierNodeListConverter(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public @Nullable ImmutableList<BezierNode> fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException, IOException {
        String input = buf.toString();
        StreamCssTokenizer tt = new StreamCssTokenizer(new CharSequenceReader(buf));

        ImmutableList<BezierNode> p = null;
        if (tt.next() == CssTokenType.TT_IDENT) {
            if (!nullable) {
                throw new ParseException("String expected. " + tt.current(), buf.position());
            }
            if (CssTokenType.IDENT_NONE.equals(tt.currentString())) {
                buf.position(buf.limit());
                return null;
            }
        }
        BezierNodePathBuilder builder = new BezierNodePathBuilder();
        SvgPaths.buildFromSvgString(builder, input);
        p = builder.build();

        buf.position(buf.limit());

        return p;
    }

    @Override
    public <TT extends ImmutableList<BezierNode>> void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier,
                                                                @Nullable TT value) throws IOException {
        if (value == null) {
            if (!nullable) {
                throw new IllegalArgumentException("value");
            }
            out.append("none");
            return;
        }

        out.append(SvgPaths.doubleSvgStringFromAwt(new BezierNodePath(value).getPathIterator(null)));// we lose smooth!

    }

    @Override
    public ImmutableList<BezierNode> getDefaultValue() {
        return nullable ? null : ImmutableArrayList.of();
    }
}
