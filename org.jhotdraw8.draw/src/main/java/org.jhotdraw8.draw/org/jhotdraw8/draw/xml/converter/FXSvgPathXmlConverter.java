/*
 * @(#)XmlFXSvgPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import javafx.scene.shape.PathElement;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.geom.FXSvgPaths;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts an {@code SVGPath} from/to an XML attribute text.
 *
 * @author Werner Randelshofer
 */
public class FXSvgPathXmlConverter implements Converter<ImmutableList<PathElement>> {

    public FXSvgPathXmlConverter() {
    }

    @Override
    public @Nullable ImmutableList<PathElement> fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
        CharBuffer out = CharBuffer.allocate(buf.remaining());
        int count = 0;
        try {
            count = buf.read(out);
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), 0);
        }
        out.position(0);
        out.limit(count);
        final String string = out.toString();
        if ("none".equals(string)) {
            return null;
        }
        return VectorList.copyOf(FXSvgPaths.pathElementsFromSvgString(string));
    }

    @Override
    public <TT extends ImmutableList<PathElement>> void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        final String content = value == null ? null : FXSvgPaths.doubleSvgStringFromPathElements(value.asList());
        out.append(content == null ? "none" : content);
    }

    @Override
    public @Nullable ImmutableList<PathElement> getDefaultValue() {
        return VectorList.of();
    }
}
