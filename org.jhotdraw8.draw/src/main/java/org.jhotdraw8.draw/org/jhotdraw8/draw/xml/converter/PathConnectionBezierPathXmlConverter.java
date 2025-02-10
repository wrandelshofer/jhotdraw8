/*
 * @(#)XmlBezierPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Converts an BezierNodeList path to an XML attribute value.
 * <p>
 * The null value will be converted to an empty String.
 * <p>
 * If the path is a straight line, it will be converted to an empty String.
 *
 */
public class PathConnectionBezierPathXmlConverter extends BezierPathXmlConverter {

    public PathConnectionBezierPathXmlConverter() {
        super(true);
    }


    @Override
    public <TT extends BezierPath> void toString(Appendable out, @Nullable IdSupplier idSupplier,
                                                 @Nullable TT value) throws IOException {
        if (value == null
            || value.size() == 2
               && (value.getFirst().getMask() & (BezierNode.OUT_MASK)) == 0//MOVE_TO
               && (value.getLast().getMask() & (BezierNode.IN_MASK | BezierNode.CLOSE_MASK | BezierNode.MOVE_MASK)) == 0//LINE_TO
        ) {
            out.append("");
            return;
        }

        super.toString(out, idSupplier, value);

    }

}
