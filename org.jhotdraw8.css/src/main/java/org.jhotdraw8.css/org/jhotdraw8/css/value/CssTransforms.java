/*
 * @(#)CssTransforms.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css.value;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jspecify.annotations.Nullable;

public class CssTransforms {
    /**
     * Don't let anyone instantiate this class.
     */
    private CssTransforms() {
    }

    public static CssRectangle2D transform(Transform transform, CssRectangle2D b) {
        Bounds tb = transform.transform(b.getConvertedBoundsValue());
        DefaultUnitConverter c = DefaultUnitConverter.getInstance();
        return new CssRectangle2D(
                c.convertSize(tb.getMinX(), UnitConverter.DEFAULT, b.getMinX().getUnits()),
                c.convertSize(tb.getMinY(), UnitConverter.DEFAULT, b.getMinY().getUnits()),
                c.convertSize(tb.getWidth(), UnitConverter.DEFAULT, b.getWidth().getUnits()),
                c.convertSize(tb.getHeight(), UnitConverter.DEFAULT, b.getHeight().getUnits())
        );
    }

    public static Transform createReshapeTransform(Rectangle2D src, double destX, double destY, double destW, double destH) {
        return createReshapeTransform(
                src.getMinX(), src.getMinY(), src.getWidth(), src.getHeight(),
                destX, destY, destW, destH
        );
    }
    public static Transform createReshapeTransform(CssRectangle2D csssrc, CssSize destX, CssSize destY, CssSize destW, CssSize destH) {
        return createReshapeTransform(csssrc.getConvertedValue(),
                destX.getConvertedValue(), destY.getConvertedValue(), destW.getConvertedValue(), destH.getConvertedValue()
        );
    }

    public static Transform createReshapeTransform(double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh) {
        double scaleX = dw / sw;
        double scaleY = dh / sh;

        Transform t = new Translate(dx - sx, dy - sy);
        if (!Double.isNaN(scaleX) && !Double.isNaN(scaleY)
            && !Double.isInfinite(scaleX) && !Double.isInfinite(scaleY)
            && (scaleX != 1d || scaleY != 1d)) {
            t = concat(t, new Scale(scaleX, scaleY, sx, sy));
        }
        return t;
    }

    public static Transform concat(@Nullable Transform... transforms) {
        Transform a = null;
        for (Transform b : transforms) {
            a = (a == null || a.isIdentity()) ? b : (b == null || b.isIdentity() ? a : a.createConcatenation(b));
        }
        return a == null ? new Translate() : a;
    }
}
