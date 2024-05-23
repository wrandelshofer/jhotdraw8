/*
 * @(#)CssTransforms.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.value;

import javafx.geometry.Bounds;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.geom.FXTransforms;

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

    public static Transform createReshapeTransform(CssRectangle2D csssrc, CssSize destX, CssSize destY, CssSize destW, CssSize destH) {
        return FXTransforms.createReshapeTransform(csssrc.getConvertedValue(),
                destX.getConvertedValue(), destY.getConvertedValue(), destW.getConvertedValue(), destH.getConvertedValue()
        );
    }

}
